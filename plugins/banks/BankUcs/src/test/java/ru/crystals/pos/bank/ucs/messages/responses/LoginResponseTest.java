package ru.crystals.pos.bank.ucs.messages.responses;

import org.junit.Test;
import ru.crystals.pos.bank.ucs.messages.responses.LoginResponse.TerminalStatus;

import static org.fest.assertions.Assertions.assertThat;

public class LoginResponseTest {

    @Test
    public void shouldParseLoginResponse() {
        Object[][] data = {
                { "3112345678900210", TerminalStatus.READY },
                { "3112345678900211", TerminalStatus.NEED_FINALIZE_DAY },
                { "3112345678900212", TerminalStatus.NO_PAPER },
                { "3112345678900213", TerminalStatus.UNKNOWN }
        };

        for (Object[] dataCase : data) {
            Response response = ResponseFactory.parse((String) dataCase[0]);
            assertThat(response).isInstanceOf(LoginResponse.class);
            assertThat(((LoginResponse) response).getInfoCode()).isEqualTo("1");
            assertThat(((LoginResponse) response).getTerminalStatus()).isEqualTo(dataCase[1]);
            assertThat(((LoginResponse) response).getTerminalId()).isEqualTo("1234567890");
        }
    }

}
