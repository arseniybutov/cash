package ru.crystals.pos.emsr.partner;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.pos.keyboard.plugin.TrackProcessorImpl;

import java.util.List;
import java.util.stream.Collectors;

public class PartnerMsrServiceImplTest {

    private PartnerMsrServiceImpl service;

    @Before
    public void init() {
        service = new PartnerMsrServiceImpl(new TrackProcessorImpl());
    }

    @Test
    public void testGet3Tracks() {
        List<Integer> scanCodeList = stringToIntegerList("%B5547619823693334^MC STANDARD/TEST          ^19112011543200000208?\n" +
                ";5547619823693334=19112011543200000208?\n" +
                ";2449000000029?\n");
        String[] result = service.getTracks(scanCodeList);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertEquals("B5547619823693334^MC STANDARD/TEST          ^19112011543200000208", result[0]);
        Assert.assertEquals("5547619823693334=19112011543200000208", result[1]);
        Assert.assertEquals("2449000000029", result[2]);
        Assert.assertNull(result[3]);
    }

    @Test
    public void testGet1Track() {
        List<Integer> scanCodeList = stringToIntegerList(";780014086159?\n");
        String[] result = service.getTracks(scanCodeList);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertNull(result[0]);
        Assert.assertEquals("780014086159", result[1]);
        Assert.assertNull(result[2]);
        Assert.assertNull(result[3]);
    }

    @Test
    public void testWhenNull() {
        String[] result = service.getTracks(null);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertNull(result[0]);
        Assert.assertNull(result[1]);
        Assert.assertNull(result[2]);
        Assert.assertNull(result[3]);
    }

    @Test
    public void testGet1TrackWithoutEnter() {
        List<Integer> scanCodeList = stringToIntegerList(";2449000000029?");
        String[] result = service.getTracks(scanCodeList);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertNull(result[0]);
        Assert.assertEquals("2449000000029", result[1]);
        Assert.assertNull(result[2]);
        Assert.assertNull(result[3]);
    }

    @Test
    public void testGet3TracksWithoutEnter() {
        List<Integer> scanCodeList = stringToIntegerList("%B5547619823693334^MC STANDARD/TEST          ^19112011543200000208?" +
                ";5547619823693334=19112011543200000208?" +
                ";2449000000029?");
        String[] result = service.getTracks(scanCodeList);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertEquals("B5547619823693334^MC STANDARD/TEST          ^19112011543200000208", result[0]);
        Assert.assertEquals("5547619823693334=19112011543200000208", result[1]);
        Assert.assertEquals("2449000000029", result[2]);
        Assert.assertNull(result[3]);
    }

    private List<Integer> stringToIntegerList(String str) {
        return str.chars().boxed().collect(Collectors.toList());
    }
}
