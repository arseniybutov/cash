package ru.crystals.pos.bank.ucs;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class TerminalDelayTest {

    private long testTimeout = 300;
    private TerminalDelay delay = new TerminalDelay();

    @Test
    public void shouldWaitForTerminalAvailableAfterSuccessfulLogin() throws Exception {
        delay.setTerminalDelayAfterSuccessfulLogin(testTimeout);

        delay.successfulLogin();

        verifyTimeouts();
    }

    @Test
    public void shouldWaitForTerminalAvailableAfterSuccessful() throws Exception {
        delay.setTerminalDelayAfterSuccessfulResponse(testTimeout);

        delay.successful();

        verifyTimeouts();
    }

    @Test
    public void shouldWaitForTerminalAvailableAfterUnsuccessfulResponse() throws Exception {
        delay.setTerminalDelayAfterUnsuccessfulOperation(testTimeout);

        delay.unsuccessful();

        verifyTimeouts();
    }

    private void verifyTimeouts() {
        assertThat(delay.isAvailable()).isFalse();
        long start = System.currentTimeMillis();
        delay.waitForTerminal();
        long stop = System.currentTimeMillis() - start;
        assertThat(stop).isLessThan(testTimeout + 150);
        assertThat(delay.isAvailable()).isTrue();
    }

}
