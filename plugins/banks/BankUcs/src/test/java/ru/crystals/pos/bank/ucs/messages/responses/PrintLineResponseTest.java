package ru.crystals.pos.bank.ucs.messages.responses;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import ru.crystals.pos.bank.ucs.TestUtils;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

public class PrintLineResponseTest {

    private String commandAndTerminal = "321234567890";

    private static final String LAST_LINE = "1";
    private static final String NOT_LAST_LINE = "0";

    private static final String LINE_SEPARATOR = String.valueOf((char) 0x0A);
    private static final String SLIP_DELIMITER = String.valueOf((char) 0x0D);

    @Test
    public void shouldParsePrintLineResponse() {

        Object[][] data = {
                { NOT_LAST_LINE, "" },
                { NOT_LAST_LINE, "A" },
                { NOT_LAST_LINE, "Текст" },
                { LAST_LINE, StringUtils.rightPad("Текст длиной 255 символов", 255 - NOT_LAST_LINE.length(), "X") },
                { LAST_LINE, StringUtils.join(Arrays.asList("Первая строка", "Вторая строка", "Третья строка"), LINE_SEPARATOR) }
        };

        for (Object[] dataCase : data) {
            String expectedLastLineFlag = (String) dataCase[0];
            String expectedTextLine = (String) dataCase[1];
            List<String> expectedTextLines = Arrays.asList(StringUtils.split(expectedTextLine, LINE_SEPARATOR));

            PrintLineResponse printLine = (PrintLineResponse) ResponseFactory.parse(
                    TestUtils.prepareMessage(commandAndTerminal, expectedLastLineFlag + expectedTextLine));
            assertThat(printLine.getTextLine()).isEqualTo(expectedTextLine);
            assertThat(printLine.getTextLines()).isEqualTo(expectedTextLines);
            assertThat(printLine.isLastLine()).isEqualTo(expectedLastLineFlag.equals(LAST_LINE));
            assertThat(printLine.isSlipDelimiter()).isFalse();
        }
    }

    @Test
    public void shouldParseSlipDelimiter() {
        PrintLineResponse response = (PrintLineResponse) ResponseFactory.parse(TestUtils.prepareMessage(commandAndTerminal, NOT_LAST_LINE
                + SLIP_DELIMITER));
        assertThat(response.getType()).isEqualTo(ResponseType.PRINT_LINE);
        assertThat(response.isSlipDelimiter()).isTrue();
        assertThat(response.getTextLine()).isEqualTo(SLIP_DELIMITER);
        assertThat(response.getTextLines()).isEmpty();
    }
}
