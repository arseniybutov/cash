package ru.crystals.pos.scale.massak.protocol100;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.scale.massak.protocol100.request.GetMassa;
import ru.crystals.pos.scale.massak.protocol100.response.AckMassa;
import ru.crystals.pos.scale.massak.protocol100.response.Division;

public class GetMassaTest extends Protocol100Test {

    private final byte[] CMD_GET_MASSA = new byte[]{-8, 85, -50, 1, 0, 35, -40, -99};
    private final byte[] CMD_ACK_MASSA = new byte[]{-8, 85, -50, 13, 0, 36, -12, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 0, -48, 26};

    @Test
    public void constructRequest() throws Exception {
        // given
        byte[] expected = CMD_GET_MASSA;
        // when
        GetMassa getMassa = new GetMassa();
        // then
        assertEqualsWithoutCrc(expected, getMassa.constructBytes());
    }

    @Test
    public void constructResponse() throws Exception {
        // given
        byte[] response = CMD_ACK_MASSA;
        // when
        AckMassa ackMassa = new AckMassa(response);
        // then no exception is expected
    }

    @Test
    public void parse_Weight() throws Exception {
        // given
        AckMassa ackMassa = new AckMassa(CMD_ACK_MASSA);
        // when
        int param = ackMassa.getWeight();
        // then
        Assert.assertEquals(param, 500);
    }

    @Test
    public void parse_Division() throws Exception {
        // given
        AckMassa ackMassa = new AckMassa(CMD_ACK_MASSA);
        // when
        Division param = ackMassa.getDivision();
        // then
        Assert.assertEquals(param, Division.GRAM_1);
    }

    @Test
    public void parse_Stable() throws Exception {
        // given
        AckMassa ackMassa = new AckMassa(CMD_ACK_MASSA);
        // when
        boolean param = ackMassa.isStable();
        // then
        Assert.assertTrue(param);
    }

    @Test
    public void parse_Net() throws Exception {
        // given
        AckMassa ackMassa = new AckMassa(CMD_ACK_MASSA);
        // when
        boolean param = ackMassa.isNet();
        // then
        Assert.assertFalse(param);
    }

    @Test
    public void parse_Zero() throws Exception {
        // given
        AckMassa ackMassa = new AckMassa(CMD_ACK_MASSA);
        // when
        boolean param = ackMassa.isZero();
        // then
        Assert.assertFalse(param);
    }

    @Test
    public void parse_Tare() throws Exception {
        // given
        AckMassa ackMassa = new AckMassa(CMD_ACK_MASSA);
        // when
        int param = ackMassa.getTare();
        // then
        Assert.assertEquals(param, 0);
    }

    @Test(expected = Protocol100Exception.class)
    public void corruptedAnswer() throws Exception {
        // given
        byte[] corrupted = EMPTY_ANSWER;
        // when
        AckMassa ackMassa = new AckMassa(corrupted);
        // then exception is thrown
    }

}
