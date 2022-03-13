package ru.crystals.pos.bank.ucs.messages.responses;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import ru.crystals.pos.bank.ucs.TestUtils;

import static org.fest.assertions.Assertions.assertThat;

public class ConsoleMessageResponseTest {

    @Test
    public void shouldParseInitialErrorResponse() {
        String headerWithoutLength = "5M1234567890";
        String[][] testData = {
                { "Я" },
                { "Текст о ходе выполнении оплаты" },
                { StringUtils.leftPad("Максимально возможная строка", 255, "S") },
                { "" }
        };

        for (String[] dataCase : testData) {
            ConsoleMessageResponse response =
                    (ConsoleMessageResponse) ResponseFactory.parse(TestUtils.prepareMessage(headerWithoutLength, dataCase[0]));
            assertThat(response).isInstanceOf(ConsoleMessageResponse.class);
            assertThat(response.getMessage()).isEqualTo(StringUtils.trimToNull(dataCase[0]));
        }
    }

}
