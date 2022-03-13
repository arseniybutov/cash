package ru.crystals.pos.bank.bpc;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(Parameterized.class)
public class BankBPCTerminalStatesTest {
    private boolean expected;

    public BankBPCTerminalStatesTest(boolean expected) {
        this.expected = expected;
    }

    private BankBPCServiceImpl service = spy(new BankBPCServiceImpl());

    @Parameterized.Parameters
    public static List<Object[]> dataForTest() {
        return Arrays.asList(new Object[][]{ { true }, { false } });
    }

    @Test
    public void testRequestTerminalStateIfOffline() {
        //given
        doReturn(expected).when(service).isHostOnline();
        //when
        boolean result = service.requestTerminalStateIfOffline();
        //then
        assertThat(result).isEqualTo(expected);
        verify(service).isHostOnline();
    }

    @Test
    public void testRequestTerminalStateIfOnline() {
        //given
        doReturn(expected).when(service).isHostOnline();
        //when
        boolean result = service.requestTerminalStateIfOnline();
        //then
        assertThat(result).isEqualTo(expected);
        verify(service).isHostOnline();
    }
}
