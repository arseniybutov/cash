package ru.crystals.pos.bank.ucs;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.ucs.utils.Timer;

public class TerminalDelay {
    private static Logger log = LoggerFactory.getLogger(TerminalDelay.class);
    private Timer terminalSilenceTimeout = new Timer();
    private long terminalDelayAfterSuccessfulLogin = secondsToMillis(3);
    private long terminalDelayAfterSuccessfulResponse = secondsToMillis(10);
    private long terminalDelayAfterUnsuccessfulOperation = secondsToMillis(17);
    private long terminalDelayAfterSuccessfulDailyLog = secondsToMillis(17);

    public void successfulLogin() {
        terminalSilenceTimeout.restart(terminalDelayAfterSuccessfulLogin);
    }

    public void successful() {
        terminalSilenceTimeout.restart(terminalDelayAfterSuccessfulResponse);
    }

    public void successfulDailyLog() {
        terminalSilenceTimeout.restart(terminalDelayAfterSuccessfulDailyLog);
    }

    public void unsuccessful() {
        terminalSilenceTimeout.restart(terminalDelayAfterUnsuccessfulOperation);
    }

    public boolean isAvailable() {
        return terminalSilenceTimeout.isExpired();
    }

    public long getRemainTimeToAvailable() {
        return terminalSilenceTimeout.getRemainTime();
    }

    public void waitForTerminal() {
        if (terminalSilenceTimeout.isExpired()) {
            return;
        }
        log.debug("Request will be send in {} ms due to terminal limitaion", terminalSilenceTimeout.getRemainTime());
        while (!terminalSilenceTimeout.isExpired() && !Thread.interrupted()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignore) {

            }
        }
    }

    public void setTerminalDelayAfterSuccessfulLogin(long terminalDelayAfterSuccessfulLogin) {
        this.terminalDelayAfterSuccessfulLogin = terminalDelayAfterSuccessfulLogin;
    }

    public void setTerminalDelayAfterSuccessfulResponse(long terminalDelayAfterSuccessfulResponse) {
        this.terminalDelayAfterSuccessfulResponse = terminalDelayAfterSuccessfulResponse;
    }

    public void setTerminalDelayAfterUnsuccessfulOperation(long terminalDelayAfterUnsuccessfulOperation) {
        this.terminalDelayAfterUnsuccessfulOperation = terminalDelayAfterUnsuccessfulOperation;
    }

    public void setTerminalDelayAfterSuccessfulDailyLog(long terminalDelayAfterSuccessfulDailyLog) {
        this.terminalDelayAfterSuccessfulDailyLog = secondsToMillis(terminalDelayAfterSuccessfulDailyLog);
    }

    private long secondsToMillis(long terminalDelayAfterSuccessfulDailyLog) {
        return terminalDelayAfterSuccessfulDailyLog * DateUtils.MILLIS_PER_SECOND;
    }
}
