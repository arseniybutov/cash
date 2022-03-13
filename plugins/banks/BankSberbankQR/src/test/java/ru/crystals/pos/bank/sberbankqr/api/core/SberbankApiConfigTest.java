package ru.crystals.pos.bank.sberbankqr.api.core;

import org.junit.Test;

import static org.junit.Assert.*;

public class SberbankApiConfigTest {

    @Test
    public void authTokenTest() {
        final String expectedToken =
                "NmE5OGU1YTctNTA3My00MDNhLWFiZjUtNWU0MGRhYjBlOGVkOlU0Y0w1YlAxcUY3dEoxcVUza1A3a004ck44YU81akYzeU43b0o0Yk42cEo4clU2d1Y3";
        final SberbankApiConfig config = new SberbankApiConfig();
        config.setClientSecret("U4cL5bP1qF7tJ1qU3kP7kM8rN8aO5jF3yN7oJ4bN6pJ8rU6wV7");
        config.setClientId("6a98e5a7-5073-403a-abf5-5e40dab0e8ed");

        final String actual = config.getAuthToken();

        assertEquals(expectedToken, actual);
    }
}