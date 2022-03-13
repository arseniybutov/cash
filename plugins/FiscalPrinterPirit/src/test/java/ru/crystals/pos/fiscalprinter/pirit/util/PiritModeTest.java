package ru.crystals.pos.fiscalprinter.pirit.util;

import org.junit.Test;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AgentType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PiritModeTest {

    @Test
    public void isAgentAvailable() {
        final PiritMode piritMode = new PiritMode(0b0011_0001_0000_0000);
        assertFalse(piritMode.isAgentAvailable(AgentType.AGENT));
        assertTrue(piritMode.isAgentAvailable(AgentType.COMMISSIONAIRE));
        assertTrue(piritMode.isAgentAvailable(AgentType.BANK_PAY_AGENT));
    }
}