package ru.crystals.pos.advertising;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.advertising.ds.Content;
import ru.crystals.pos.advertising.ds.ContentType;
import ru.crystals.pos.advertising.ds.PlayList;
import ru.crystals.pos.advertising.ds.Signature;
import ru.crystals.pos.property.Properties;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Плагин управления рекламным контентом "Верный"
 *
 * @author Tatarinov Eduard
 */
public class AdvertisingPluginVerniy implements AdvertisingPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AdvertisingPluginVerniy.class);

    private static final String PLAYLIST_PATH = "modules/advertising/templates/playlist/";
    private static final String CONTENT_PATH = "modules/advertising/templates/content/";

    private static final String PROVIDER_NAME = "verniy";
    private static final String PLAYLIST_FILE_PREFIX = "pl_";
    private static final String PLAYLIST_FILE_SUFFIX = ".res";
    private static final String PLAYLIST_FILE_PATTERN = "pl_[0-9][0-9]_[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]";  // пример имени файла плейлиста pl_09_20180101
    private static final Pattern PLAYLIST_FILE_SPLITTER = Pattern.compile("_");

    private static final String CONTENT_FILE_PREFIX = "R_";
    private static final int C_MD5_SIZE = 32;
    private static final int C_FILENUMBER_SIZE = 4;
    private static final int C_BLOCK_SIZE = 2;
    private static final int C_PLANNED_SIZE = 2;
    private static final int C_ACTUAL_SIZE = 2;
    private static final int C_PLAYTIME_SIZE = 1;

    private static long cashNumber;

    private String playlistPath = PLAYLIST_PATH;
    private String contentPath = CONTENT_PATH;
    private String reportPath = PLAYLIST_PATH + "report/";
    private int periodCheckPlaylist = 60;   // период проверки изменения каталога с плейлистами
    private int defaultPlaytime = 10;
    private String defaultContentPath = contentPath + "default/";
    private WatchService watcher;
    private ChangeContentListener contentListener;
    Map<LocalDateTime, Path> playlists = new LinkedHashMap<>();

    private InternalCashPoolExecutor executor;

    /**
     * Провайдер времени для возможности подмены {@link LocalDateTime#now()} в тестах.
     * <p>
     * После решения SRTB-3343 можно будет избавиться в пользу общего {@link ru.crystals.utils.time.Now}
     */
    private Clock clock;

    @Override
    public void start(ChangeContentListener listener, ChangeContentListener defaultListener, InternalCashPoolExecutor executor, Properties properties) {
        try {
            this.executor = executor;
            cashNumber = properties.getCashNumber();
            this.contentListener = listener;
            this.watcher = FileSystems.getDefault().newWatchService();
            Paths.get(playlistPath).register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            fillPlaylists();
            startCheckChange();
        } catch (IOException ex) {
            LOG.error("--- Error:", ex);
        }
    }

    private void startCheckChange() {
        executor.scheduleAtFixedRate(this::checkChangeFiles, periodCheckPlaylist, periodCheckPlaylist, TimeUnit.SECONDS);
    }

    @Override
    public PlayList getPlaylist(LocalDateTime prevPlaylistStart) {
        LocalDateTime startDt = prevPlaylistStart;
        PlayList result = null;
        if (prevPlaylistStart == null) {
            LocalDateTime currentDt = now();
            startDt = currentDt.toLocalDate().atTime(currentDt.getHour(), 0);
        } else {
            startDt = startDt.plusHours(1);
        }
        // ищем ближайший доступный плейлист
        final LocalDateTime dt = LocalDateTime.of(startDt.toLocalDate(), startDt.toLocalTime());
        startDt = playlists.keySet().stream()
                .filter((plDt) -> plDt.isEqual(dt) || plDt.isAfter(dt))
                .sorted()
                .findFirst()
                .orElse(null);
        if (startDt != null) {
            result = getPlayList(startDt);
        }
        return result;
    }

    @Override
    public PlayList getCurrentPlaylist() {
        LocalDateTime currentDt = now();
        LocalDateTime startDt = currentDt.toLocalDate().atTime(currentDt.getHour(), 0);

        return getPlayList(startDt);
    }

    @Override
    public PlayList getDefaultContent() {
        List<Content> contents = new LinkedList<>();
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(Paths.get(defaultContentPath))) {
            for (Object name : dirs) {
                String path = name.toString();
                ContentType type = getContentType(path);
                if (ContentType.UNKNOWN.equals(type)) {
                    continue;
                }
                contents.add(new Content(path, type, defaultPlaytime));
            }
        } catch (Exception ex) {
            LOG.error("--- Error get default playlist:", ex);
        }
        return new PlayList(contents, now());
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public void sendReport(PlayList playList) {
        byte[] report = createReport(playList);
        try (SeekableByteChannel file = Files.newByteChannel(Paths.get(reportPath + getReportFileNameByDateTime(playList.getStartDateTime())),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            file.write(ByteBuffer.wrap(report));
        } catch (Exception ex) {
            LOG.error("--- Error save report: ", ex);
        }
    }

    private byte[] createReport(PlayList playList) {
        int blockSize = C_BLOCK_SIZE + C_FILENUMBER_SIZE + C_MD5_SIZE + C_PLAYTIME_SIZE + C_PLANNED_SIZE + C_ACTUAL_SIZE;
        Map<Content, Integer> plannedContent = getPlayList(playList.getStartDateTime()).calcCountEqualContent();
        Map<Content, Integer> actualContent = playList.calcCountEqualContent();
        byte[] result = new byte[blockSize * plannedContent.size()];
        int resultIndex = 0;
        for (Map.Entry<Content, Integer> contentEntry : plannedContent.entrySet()) {
            byte[] block = new byte[blockSize];
            Content content = contentEntry.getKey();
            // размер блока
            System.arraycopy(integerAsByteArray(blockSize - C_BLOCK_SIZE, C_BLOCK_SIZE, true), 0, block, 0, C_BLOCK_SIZE);

            // номер файла
            String[] dtList = PLAYLIST_FILE_SPLITTER.split(content.getContentPath());
            System.arraycopy(integerAsByteArray(Integer.valueOf(dtList[dtList.length - 1]), C_FILENUMBER_SIZE, true), 0, block, C_BLOCK_SIZE, C_FILENUMBER_SIZE);

            // контрольная сумма
            System.arraycopy(content.getCrc().getBytes(), 0, block, C_BLOCK_SIZE + C_FILENUMBER_SIZE, C_MD5_SIZE);

            // время проигрывания
            block[C_BLOCK_SIZE + C_FILENUMBER_SIZE + C_MD5_SIZE] = (byte) content.getPlayTime();

            // Планируемое кол-во показов
            System.arraycopy(integerAsByteArray(contentEntry.getValue(), C_PLANNED_SIZE, true), 0, block,
                    (C_BLOCK_SIZE + C_FILENUMBER_SIZE + C_MD5_SIZE + C_PLAYTIME_SIZE), C_PLANNED_SIZE);

            // Актуальное кол-во показов
            int actualCount = actualContent.getOrDefault(content, 0);
            System.arraycopy(integerAsByteArray(actualCount, C_ACTUAL_SIZE, true), 0, block,
                    (C_BLOCK_SIZE + C_FILENUMBER_SIZE + C_MD5_SIZE + C_PLAYTIME_SIZE + C_PLANNED_SIZE), C_ACTUAL_SIZE);

            // Копируем блок в результирующий массив и сдвигаем индекс
            System.arraycopy(block, 0, result, resultIndex, blockSize);
            resultIndex = resultIndex + blockSize;
        }
        return result;
    }

    public void setPlaylistPath(String playlistPath) {
        this.playlistPath = addSlash(playlistPath);
    }

    public void setContentPath(String contentPath) {
        this.contentPath = addSlash(contentPath);
    }

    public void setReportPath(String reportPath) {
        this.reportPath = addSlash(reportPath);
    }

    public void setDefaultContentPath(String defaultContentPath) {
        this.defaultContentPath = addSlash(defaultContentPath);
    }

    public void setPeriodCheckChange(int periodCheckChange) {
        this.periodCheckPlaylist = periodCheckChange;
    }

    public void setDefaultPlaytime(int defaultPlaytime) {
        this.defaultPlaytime = defaultPlaytime;
    }

    private String addSlash(String path) {
        if (path.endsWith("/")) {
            return path;
        }
        return path + "/";
    }

    private PlayList getPlayList(LocalDateTime dt) {
        try {
            Path playListFile = playlists.get(dt);
            return playListFile != null ? parsePlayListFile(playListFile, dt) : null;
        } catch (IOException ex) {
            LOG.error("", ex);
            return null;
        }
    }

    private void checkChangeFiles() {
        LOG.info("--- Check change playlist files ---");
        WatchKey wk = watcher.poll();
        if (wk != null) {
            wk.pollEvents().forEach(event -> LOG.info("--- Operation: {}, FileName: {}",
                    event.kind().name(),
                    event.context().toString())
            );
            fillPlaylists();
            if (contentListener != null) {
                contentListener.change();
            }
            wk.reset();
        }

    }

    private void fillPlaylists() {
        LocalDateTime currenDateTime = now().truncatedTo(ChronoUnit.HOURS);
        playlists.clear();
        try (DirectoryStream dirs = Files.newDirectoryStream(Paths.get(playlistPath), PLAYLIST_FILE_PATTERN)) {
            for (Object name : dirs) {
                Path path = (Path) name;
                try {
                    LocalDateTime plDateTime = getPlaylistStartDateTimeFromFilename(path.getFileName().toString());
                    if (currenDateTime.isBefore(plDateTime) || currenDateTime.isEqual(plDateTime)) {
                        LOG.info("--- Add playlist: {}", path);
                        playlists.put(plDateTime, path);
                    }
                } catch (DateTimeException e) {
                    LOG.error("--- Parse filename - {}, error - {}", path, e.getMessage());
                    // Считаем что дата в файле кривая и идем дальше
                }
            }
        } catch (Exception ex) {
            LOG.error("", ex);
        }

    }

    private LocalDateTime getPlaylistStartDateTimeFromFilename(String fileName) {
        List<String> dtList = Arrays.asList(PLAYLIST_FILE_SPLITTER.split(fileName));
        LocalDate pldate = LocalDate.parse(dtList.get(2), DateTimeFormatter.ofPattern("yyyyMMdd"));
        return pldate.atTime(Integer.valueOf(dtList.get(1)), 0);
    }

    private PlayList parsePlayListFile(Path file, LocalDateTime dtStart) throws IOException {
        byte[] data = Files.readAllBytes(file);

        List<Content> contents = new LinkedList<>();
        int startBlockPosition = 0;
        int startContentPosition = C_BLOCK_SIZE;
        while (startBlockPosition < data.length) {
            int blockSize = byteArrayAsInteger(Arrays.copyOfRange(data, startBlockPosition, startContentPosition), true);
            Content content = parseContent(Arrays.copyOfRange(data, startContentPosition, startContentPosition + blockSize));
            if (content != null) {
                contents.add(content);
            }
            startBlockPosition = startContentPosition + blockSize;
            startContentPosition = startBlockPosition + C_BLOCK_SIZE;
        }
        return new PlayList(contents, dtStart);
    }

    private Content parseContent(byte[] data) {
        Content result = null;
        int fileNum = byteArrayAsInteger(Arrays.copyOfRange(data, 0, C_FILENUMBER_SIZE), true);
        String crc = new String(Arrays.copyOfRange(data, C_FILENUMBER_SIZE, C_FILENUMBER_SIZE + C_MD5_SIZE));
        int playTime = data[data.length - 1];

        String filePath = String.format(contentPath + CONTENT_FILE_PREFIX + "%05d", fileNum);
        ContentType type = getContentType(filePath);
        if (ContentType.UNKNOWN.equals(type)) {
            return null;
        }
        result = new Content(filePath, type, playTime, crc, true);
        return result;
    }

    private static ContentType getContentType(String filePath) {
        try (InputStream br = Files.newInputStream(Paths.get(filePath))) {

            byte[] data = new byte[Signature.MIN_LENGTH];
            br.read(data, 0, data.length);
            return Signature.getContentType(data);
        } catch (IOException ex) {
            LOG.error("", ex);
            return ContentType.UNKNOWN;
        }
    }

    private static int byteArrayAsInteger(byte[] data, boolean needInverse) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        if (needInverse) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        if (data.length == 2) {
            return buffer.getShort();
        }
        return buffer.getInt();
    }

    private static byte[] integerAsByteArray(int data, int length, boolean needInverse) {
        ByteBuffer buffer = ByteBuffer.allocate(length);
        if (needInverse) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        if (length == 2) {
            buffer.putShort((short) data);
        } else if (length == 4) {
            buffer.putInt(data);
        }
        return buffer.array();
    }

    protected static String getReportFileNameByDateTime(LocalDateTime dt) {
        return String.format("%s%02d_%02d_%d%02d%02d%s", PLAYLIST_FILE_PREFIX, cashNumber, dt.getHour(), dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth(),
                PLAYLIST_FILE_SUFFIX);
    }

    private LocalDateTime now() {
        if (clock != null) {
            return LocalDateTime.now(clock);
        }
        return LocalDateTime.now();
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

}
