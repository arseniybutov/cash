package ru.crystals.comportemulator.pirit;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Tatarinov Eduard
 */
public class PiritCommandTest {

    @Test
    public void testSetCommandsReadTimeout() {
        final PiritCommand command = PiritCommand.CLOSE_DOCUMENT;
        final int commandCode = 0x31;
        Assert.assertEquals(commandCode, command.getCode());
        final long initialReadTO = command.getReadTimeOut();
        Assert.assertEquals(SECONDS.toMillis(20), initialReadTO);

        PiritCommand.setCommandsReadTimeout(Collections.singletonMap(commandCode, SECONDS.toMillis(120)));

        Assert.assertEquals(SECONDS.toMillis(120), command.getReadTimeOut());
        Assert.assertNotEquals(initialReadTO, command.getReadTimeOut());
    }

    @Test
    public void testSetCommandsReadTimeoutOneCodeForTwoCommand() {
        final int commandCode = 0x00;
        final PiritCommand mainCommand = PiritCommand.GET_STATUS;
        final PiritCommand secondaryCommand = PiritCommand.GET_STATUS_FOR_NORMALIZE;
        final long initialMCReadTO = mainCommand.getReadTimeOut();
        final long initialSCReadTO = secondaryCommand.getReadTimeOut();
        Assert.assertEquals(commandCode, mainCommand.getCode());
        Assert.assertEquals(commandCode, secondaryCommand.getCode());
        Assert.assertEquals(1500L, initialMCReadTO);
        Assert.assertEquals(SECONDS.toMillis(20), initialSCReadTO);

        PiritCommand.setCommandsReadTimeout(Collections.singletonMap(commandCode, SECONDS.toMillis(120)));

        Assert.assertEquals(SECONDS.toMillis(120), mainCommand.getReadTimeOut());
        Assert.assertNotEquals(initialMCReadTO, mainCommand.getReadTimeOut());
        Assert.assertEquals(initialSCReadTO, secondaryCommand.getReadTimeOut());
    }

}
