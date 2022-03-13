package ru.crystals.pos.emsr.wintec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.crystals.pos.keyboard.plugin.TrackProcessorImpl;

import java.util.List;
import java.util.stream.Collectors;

public class WintecMsrServiceImplTest {

    private WintecMsrServiceImpl service;

    @Before
    public void init() {
        service = new WintecMsrServiceImpl(new TrackProcessorImpl());
    }

    @Test
    public void getTracksTest() {
        // три дорожки
        List<Integer> scanCodeList = ("%5B55476198236933344^6MC  STANDARD//TEST           ^191120115432000002088?\n" +
                ";5547619823693334=191120115432000002088?\n" +
                ";24490000000299?\n").chars().boxed().collect(Collectors.toList());
        String[] result = service.getTracks(scanCodeList);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertEquals("5B55476198236933344^6MC  STANDARD//TEST           ^19112011543200000208", result[0]);
        Assert.assertEquals("5547619823693334=19112011543200000208", result[1]);
        Assert.assertEquals("2449000000029", result[2]);
        Assert.assertNull(result[3]);

        // 1 дорожка
        scanCodeList = (";7800140861599?\n")
                .chars().boxed().collect(Collectors.toList());

        result = service.getTracks(scanCodeList);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertNull(result[0]);
        Assert.assertEquals("780014086159", result[1]);
        Assert.assertNull(result[2]);
        Assert.assertNull(result[3]);

        // проверим на null
        result = service.getTracks(null);
        Assert.assertTrue(result != null && result.length == 4);
        Assert.assertNull(result[0]);
        Assert.assertNull(result[1]);
        Assert.assertNull(result[2]);
        Assert.assertNull(result[3]);
    }

}
