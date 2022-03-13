package ru.crystals.pos.bank.ucs.messages.responses;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import ru.crystals.pos.bank.ucs.ResBundleBankUcs;
import ru.crystals.pos.bank.ucs.TestUtils;

import static org.fest.assertions.Assertions.assertThat;

public class InitialErrorResponseTest {
    @Test
    public void shouldParseInitialErrorResponse2() {
        String headerWithoutLength = "5X1234567890";
        String[][] testData =
            { { "07", "Ошибка при выполнении оплаты" }, { "10", "Ошибка при выполнении возврата" }, { "Z3", "Неизвестная ошибка с кодом больше FF" },
                { "G4", StringUtils.leftPad("Максимально возможная строка", 255 - 2, "S") }, { "N6", "" }, { "", "" } };

        for (String[] dataCase : testData) {
            String data = dataCase[0] + dataCase[1];
            String expectedCode = StringUtils.trimToNull(dataCase[0]);
            String expectedMessage = StringUtils.trimToNull(dataCase[1]);
            String expectedCombinedMessage = ResBundleBankUcs.getString("TERMINAL_ANSWER") + ": " + expectedMessage + " (" + expectedCode + ")";

            InitialErrorResponse response = (InitialErrorResponse) ResponseFactory.parse(TestUtils.prepareMessage(headerWithoutLength, data));
            assertThat(response.getErrorCode()).isEqualTo(expectedCode);
            assertThat(response.getErrorMessage()).isEqualTo(expectedMessage);
            assertThat(response.getCombinedMessage()).isEqualTo(expectedCombinedMessage);
        }
    }
}
