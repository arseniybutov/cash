package ru.crystals.pos.scale.massak.protocol100;

import org.junit.Test;
import ru.crystals.pos.scale.massak.protocol100.request.SetZero;
import ru.crystals.pos.scale.massak.protocol100.response.AckSet;

public class SetZeroTest extends Protocol100Test {

    private final byte[] CMD_SET_ZERO = new byte[]{-8, 85, -50, 1, 0, 114, -110, 73};
    private final byte[] CMD_ACK_SET = new byte[]{-8, 85, -50, 1, 0, 39, 39, 0};

    @Test
    public void constructRequest() throws Exception {
        // given
        byte[] expected = CMD_SET_ZERO;
        // when
        SetZero setZero = new SetZero();
        // then
        assertEqualsWithoutCrc(expected, setZero.constructBytes());
    }

    @Test
    public void constructResponse() throws Exception {
        // given
        byte[] response = CMD_ACK_SET;
        // when
        AckSet ackSet = new AckSet(response);
        // then no exception is expected
    }

    @Test(expected = Protocol100Exception.class)
    public void corruptedAnswer() throws Exception {
        // given
        byte[] corrupted = EMPTY_ANSWER;
        // when
        AckSet ackSet = new AckSet(corrupted);
        // then exception is thrown
    }

}
