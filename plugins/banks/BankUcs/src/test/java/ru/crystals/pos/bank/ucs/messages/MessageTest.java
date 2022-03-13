package ru.crystals.pos.bank.ucs.messages;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.ucs.messages.responses.AuthorizationResponse;
import ru.crystals.pos.bank.ucs.messages.responses.ConsoleMessageResponse;
import ru.crystals.pos.bank.ucs.messages.responses.InitialErrorResponse;
import ru.crystals.pos.bank.ucs.messages.responses.PrintLineResponse;
import ru.crystals.pos.bank.ucs.messages.responses.Response;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseFactory;
import ru.crystals.pos.bank.ucs.messages.responses.ResponseType;
import ru.crystals.pos.bank.ucs.messages.responses.SimpleWithoutDataResponse;
import ru.crystals.pos.bank.ucs.messages.responses.UnknownResponse;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MessageTest {

    @Test
    public void shouldCreateMessageFromString() throws Exception {

        Response consoleMessageResponse = ResponseFactory.parse("5M123456789003ABC");
        assertThat(consoleMessageResponse).isInstanceOf(ConsoleMessageResponse.class);

        assertThat(((ConsoleMessageResponse) consoleMessageResponse).getMessage()).isEqualTo("ABC");
        assertThat(consoleMessageResponse.getTerminalId()).isEqualTo("1234567890");
        assertThat(consoleMessageResponse.getType()).isEqualTo(ResponseType.CONSOLE_MESSAGE);
    }

    @Test
    public void shouldCreateUnknownMessageFromString() throws Exception {

        Response unknownMessageResponse = ResponseFactory.parse("XX123456789003ABC");
        assertThat(unknownMessageResponse).isInstanceOf(UnknownResponse.class);
        assertThat(unknownMessageResponse.getTerminalId()).isEqualTo("1234567890");
        assertThat(unknownMessageResponse.getType()).isEqualTo(ResponseType.UNKNOWN);
    }

    @Test
    public void shouldCreateCorrectMessageTypeForSimpleTypesFromString() throws Exception {
        String terminalAndLength = "123456789000";
        Object[][] types = {

                { "32", ResponseType.PRINT_LINE, PrintLineResponse.class },

                { "50", ResponseType.INITIAL_OK_RESPONSE, SimpleWithoutDataResponse.class },
                { "51", ResponseType.INITIAL_REQUIRES_LOGIN_FIRST_RESPONSE, SimpleWithoutDataResponse.class },
                { "52", ResponseType.PIN_ENTRY_REQUIRED, SimpleWithoutDataResponse.class },
                { "53", ResponseType.ONLINE_AUTHORISATION_REQUIRED, SimpleWithoutDataResponse.class },
                { "54", ResponseType.INITIAL_NO_PREVIOUS_TRANSACTION_WITH_SUCH_REF, SimpleWithoutDataResponse.class },
                { "55", ResponseType.HOLD, SimpleWithoutDataResponse.class },
                { "5M", ResponseType.CONSOLE_MESSAGE, ConsoleMessageResponse.class },
                { "5X", ResponseType.INITIAL_ERROR_RESPONSE, InitialErrorResponse.class },

                { "60", ResponseType.AUTHORIZATION_RESPONSE, AuthorizationResponse.class },

                { "00", ResponseType.UNKNOWN, UnknownResponse.class }
        };

        for (Object[] type : types) {
            Response response = ResponseFactory.parse(type[0] + terminalAndLength);
            assertThat(response).isInstanceOf((Class) type[2]);
            assertThat(response.getType()).isEqualTo(type[1]);
        }
    }
}
