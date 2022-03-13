package ru.crystals.pos.advertising;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.InternalCashPoolExecutorImpl;
import ru.crystals.pos.advertising.ds.PlayList;
import ru.crystals.pos.property.Properties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.HOURS;

/**
 * @author Tatarinov Eduard
 */
@RunWith(MockitoJUnitRunner.class)
public class AdvertisingPluginVerniyTest {

    private static final String PLAYLIST_PATH = "src/test/resources/templates/playlist/";
    private static final String CONTENT_PATH = "src/test/resources/templates/content/";

    private static final String PLAYLIST_FILE_PREFIX = "pl_";
    private static final String PLAYLIST_FILE_SUFFIX = ".res";
    private static final long CASH_NUMBER = 5;

    private InternalCashPoolExecutor executor = new InternalCashPoolExecutorImpl();
    private Properties props = new Properties();

    private AdvertisingPluginVerniy provider = new AdvertisingPluginVerniy();

    private static List<String> filename = new ArrayList<>();
    private static LocalDateTime currentDt = LocalDateTime.of(2019, Month.JULY, 9, 20, 25);

    @BeforeClass
    public static void initClass() {
        filename.add(getFileNameByDateTime(currentDt.minusHours(1)));
        filename.add(getFileNameByDateTime(currentDt));
        filename.add(getFileNameByDateTime(currentDt.plusHours(1)));
        filename.add(getFileNameByDateTime(currentDt.plusDays(1).with(LocalTime.MIDNIGHT)));
        filename.add(getFileNameByDateTime(currentDt.plusDays(1)));
        filename.forEach((name) -> {
            try {
                Files.copy(Paths.get(PLAYLIST_PATH, "playlist"), Paths.get(PLAYLIST_PATH, name));
            } catch (IOException ex) {
                //
            }
        });
    }

    @AfterClass
    public static void destroyClass() {
        filename.forEach((name) -> {
            try {
                Files.deleteIfExists(Paths.get(PLAYLIST_PATH, name));
            } catch (IOException ex) {
                //
            }
        });
    }

    @Before
    public void init() {
        props.setCashNumber(CASH_NUMBER);
        provider.setPlaylistPath(PLAYLIST_PATH);
        provider.setContentPath(CONTENT_PATH);
        provider.setClock(Clock.fixed(currentDt.atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault()));
        provider.start(() -> {}, () -> {}, executor, props);
    }

    @Test
    public void testGetContent() {
        PlayList pl = provider.getPlaylist(currentDt.truncatedTo(HOURS));
        LocalDateTime expectedStartDateTime = currentDt.plusHours(1).truncatedTo(HOURS);
        Assert.assertEquals(expectedStartDateTime, pl.getStartDateTime());
        Assert.assertEquals(10, pl.getContents().size());
    }

    @Test
    public void testNextGetContent() {
        PlayList pl = provider.getPlaylist(currentDt.minusHours(1).truncatedTo(HOURS));
        pl = provider.getPlaylist(pl.getStartDateTime());
        LocalDateTime expectedStartDateTime = currentDt.plusHours(1).truncatedTo(HOURS);
        Assert.assertEquals(expectedStartDateTime, pl.getStartDateTime());
        Assert.assertEquals(10, pl.getContents().size());
    }

    @Test
    public void testGetContentDateNull() {
        PlayList pl = provider.getPlaylist(null);
        Assert.assertEquals(currentDt.truncatedTo(HOURS), pl.getStartDateTime());
        Assert.assertEquals(10, pl.getContents().size());
    }

    @Test
    public void testGetContentNoPlaylist() {
        LocalDateTime dt = currentDt.plusDays(1);
        PlayList pl = provider.getPlaylist(dt);
        Assert.assertNull(pl);
    }

    @Test
    public void testGetReportFileNameByDateTime() {
        String expected = String.format("%s%02d_%02d_%d%02d%02d%s", PLAYLIST_FILE_PREFIX, CASH_NUMBER,
                currentDt.getHour(), currentDt.getYear(), currentDt.getMonthValue(), currentDt.getDayOfMonth(), PLAYLIST_FILE_SUFFIX);

        String reportFileName = AdvertisingPluginVerniy.getReportFileNameByDateTime(currentDt);

        Assert.assertEquals(expected, reportFileName);
    }

    public static String getFileNameByDateTime(LocalDateTime dt) {
        return String.format("pl_%02d_%d%02d%02d", dt.getHour(), dt.getYear(), dt.getMonthValue(), dt.getDayOfMonth());
    }
}
