package ru.crystals.pos.scale.massak.protocol100;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.scale.massak.protocol100.request.GetScalePar;
import ru.crystals.pos.scale.massak.protocol100.response.AckScalePar;

public class GetScaleParamTest extends Protocol100Test {

    private final byte[] CMD_GET_SCALE_PAR = new byte[]{-8, 85, -50, 1, 0, 117, 117, 0};
    private final byte[] CMD_ACK_SCALE_PAR = new byte[]{-8, 85, -50, 82, 0, 118, 77, 97, 120, 32, 54, 47, 49, 53, 32, 107, 103, 13, 10, 77, 105, 110, 32, 48, 46, 48, 52, 32, 107, 103, 13, 10, 101, 61, 50, 47, 53, 32, 103, 13, 10, 84, 61, 45, 54, 32, 107, 103, 13, 10, 70, 105, 120, 61, 48, 13, 10, 67, 111, 100, 101, 61, 53, 50, 54, 52, 56, 52, 13, 10, 85, 32, 51, 56, 46, 49, 54, 13, 10, 49, 55, 70, 51, 55, 57, 13, 10, 121, 32};

    @Test
    public void constructRequest() throws Exception {
        // given
        byte[] expected = CMD_GET_SCALE_PAR;
        // when
        GetScalePar getScalePar = new GetScalePar();
        // then
        assertEqualsWithoutCrc(expected, getScalePar.constructBytes());
    }

    @Test
    public void constructResponse() throws Exception {
        // given
        byte[] response = CMD_ACK_SCALE_PAR;
        // when
        AckScalePar ackScalePar = new AckScalePar(response);
        // then no exception is expected
    }

    @Test
    public void parse_PMax() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getPMax();
        // then
        Assert.assertEquals(param, "Max 6/15 kg");
    }

    @Test
    public void parse_PMin() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getPMin();
        // then
        Assert.assertEquals(param, "Min 0.04 kg");
    }

    @Test
    public void parse_Pe() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getPe();
        // then
        Assert.assertEquals(param, "e=2/5 g");
    }

    @Test
    public void parse_PT() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getPT();
        // then
        Assert.assertEquals(param, "T=-6 kg");
    }

    @Test
    public void parse_Fix() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getFix();
        // then
        Assert.assertEquals(param, "Fix=0");
    }

    @Test
    public void parse_CalCode() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getCalCode();
        // then
        Assert.assertEquals(param, "Code=526484");
    }

    @Test
    public void parse_POVer() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getPOVer();
        // then
        Assert.assertEquals(param, "U 38.16");
    }

    @Test
    public void parse_POSum() throws Exception {
        // given
        AckScalePar ackScalePar = new AckScalePar(CMD_ACK_SCALE_PAR);
        // when
        String param = ackScalePar.getPOSum();
        // then
        Assert.assertEquals(param, "17F379");
    }

    @Test(expected = Protocol100Exception.class)
    public void corruptedAnswer() throws Exception {
        // given
        byte[] corrupted = EMPTY_ANSWER;
        // when
        AckScalePar ackScalePar = new AckScalePar(corrupted);
        // then exception is thrown
    }

}
