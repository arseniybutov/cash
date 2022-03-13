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
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

/**
 * Дефолтный плагин управления рекламным контентом.
 * Отдает дефолтный плейлист, обновляемый при добавлении файлов в папочку с контентом
 * Значительная часть кода "честно" скопирована из плагина верного.
 * Отчеты и недефолтные плейлисты исключены
 *
 * @author Irodion1
 * @see <a href="https://crystals.atlassian.net/browse/SRTB-4745">SRTB-4745</a>
 * @since 10.2.94.0
 */
public class AdvertisingPluginDefault implements AdvertisingPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AdvertisingPluginDefault.class);

    /**
     * Название провайдера
     */
    private static final String PROVIDER_NAME = "default";

    /**
     * Имплементируется в AdvertisingService, через него оповещаем об изменениях в плейлисте
     */
    private ChangeContentListener contentListener;

    /**
     * Наблюдалка за папкой с контентом
     */
    private WatchService watcher;

    /**
     * Путь до контента
     * Берется из конфига
     */
    private String contentPath;

    /**
     * Интервал проверки событий обновления файлов в папочке контента
     * Берется из конфига
     */
    private long periodCheckContent;

    /**
     * Время проигрывания одного файла.
     * Берется из конфига
     */
    private int playTime;

    private InternalCashPoolExecutor executor;

    /**
     * Собственно, наш дефолтный плейлист
     */
    private PlayList playList;

    @Override
    public void start(ChangeContentListener listener, ChangeContentListener defaultListener, InternalCashPoolExecutor executor, Properties properties) {
        try {
            this.executor = executor;
            this.watcher = FileSystems.getDefault().newWatchService();
            this.contentListener = defaultListener;
            Paths.get(getDefaultContentPath()).register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            startCheckChange();
        } catch (IOException ex) {
            LOG.error("--- Error:", ex);
        }
    }

    /**
     * Начнем периодически проверять папку с контентом
     */
    private void startCheckChange() {
        executor.scheduleAtFixedRate(this::checkChangeFiles, periodCheckContent, periodCheckContent, TimeUnit.SECONDS);
    }

    /**
     * Обрабатывает события наблюдалки за папкой с контентом
     */
    private void checkChangeFiles() {
        WatchKey wk = watcher.poll();
        if (wk != null) {
            wk.pollEvents().forEach(event -> LOG.info("--- Operation: {}, FileName: {}",
                    event.kind().name(),
                    event.context().toString()));
            playList = createPlayList();
            if (contentListener != null) {
                contentListener.change();
            }
            wk.reset();
        }
    }

    /**
     * Читаем содержимое плейлиста.
     * Запускается при старте плагина и при событиях об обновлении файлов в папке контента
     *
     * @return плейлист
     */
    private PlayList createPlayList() {
        List<Content> contents = new LinkedList<>();
        try (DirectoryStream<Path> dirs = Files.newDirectoryStream(Paths.get(getDefaultContentPath()))) {
            for (Object name : dirs) {
                String path = name.toString();
                ContentType type = getContentType(path);
                if (ContentType.UNKNOWN.equals(type)) {
                    continue;
                }
                contents.add(new Content(path, type, playTime));
            }
        } catch (Exception ex) {
            LOG.error("--- Error get default playlist:", ex);
        }
        return new PlayList(contents, LocalDateTime.now());
    }

    @Override
    public PlayList getPlaylist(LocalDateTime start) {
        return null;
    }

    @Override
    public PlayList getCurrentPlaylist() {
        return null;
    }

    @Override
    public PlayList getDefaultContent() {
        if (playList == null) {
            playList = createPlayList();
        }
        return playList;
    }

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public void sendReport(PlayList playList) {
        // ничего не надо
    }

    public String getDefaultContentPath() {
        return contentPath + "default/";
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath.endsWith("/") ? contentPath : contentPath + "/";
    }

    public void setPeriodCheckContent(long periodCheckContent) {
        this.periodCheckContent = periodCheckContent;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
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

}
