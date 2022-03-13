package ru.crystals.pos.scale.massak.protocol100;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.scale.massak.protocol100.request.SetTare;
import ru.crystals.pos.scale.massak.protocol100.response.AckSetTare;

public class SetTareTest extends Protocol100Test {

    private final byte[] CMD_SET_TARE = new byte[]{-8, 85, -50, 5, 0, -93, 0, 0, 0, 0, 53, 120};
    private final byte[] CMD_ACK_SET_TARE = new byte[]{-8, 85, -50, 1, 0, 18, 18, 0};
    private final byte[] CMD_NACK_TARE = new byte[]{-8, 85, -50, 1, 0, 21, 21, 0};

    @Test
    public void constructRequest() throws Exception {
        // given
        byte[] expected = CMD_SET_TARE;
        // when
        SetTare setTare = new SetTare();
        // then
        assertEqualsWithoutCrc(expected, setTare.constructBytes());
    }

    @Test
    public void successfulResponse() throws Exception {
        // given
        byte[] response = CMD_ACK_SET_TARE;
        // when
        AckSetTare ackSetTare = new AckSetTare(response);
        // then no exception is expected
    }

    @Test
    public void unableToSetTareResponse() throws Exception {
        // given
        byte[] response = CMD_NACK_TARE;
        // when
        Protocol100Exception e = Assert.assertThrows(Protocol100Exception.class, () -> new AckSetTare(response));
        // then
        Assert.assertEquals(e.getMessage(), "Unable to set Tare");
    }

}
