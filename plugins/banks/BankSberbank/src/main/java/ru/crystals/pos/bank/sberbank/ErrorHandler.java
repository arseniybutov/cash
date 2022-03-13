package ru.crystals.pos.bank.sberbank;

import org.apache.commons.lang.StringUtils;

import java.util.Objects;
import java.util.Optional;

public class ErrorHandler {

    static String getErrorMessageByCode(String message, String code) {
        if (Objects.equals(code, SberbankResponseData.SUCCESSFUL_RESPONSE_CODE)) {
            return message;
        }
        final String resultMessage =  Optional.ofNullable(StringUtils.trimToNull(message)).orElseGet(() -> ResBundleBankSberbank.getString("ERROR_COMMUNICATION"));
            return String.format("%s (%s)", resultMessage, code);
    }

    public static String getErrorMessageByCode(int result) {
        String message;
        if ((result >= 5000) && (result <= 5056)) {
            message = ResBundleBankSberbank.getString("INCORRECT_TERMINAL_SETTINGS_OR_CHIP_CORRUPTED");
        } else if ((result >= 5100) && (result <= 5108)) {
            message = ResBundleBankSberbank.getString("CURRUPTED_CHIP");
        } else {
            switch (result) {
                case 12:
                case 4128:
                case 4496:
                case 4498:
                    message = ResBundleBankSberbank.getString("INCORRECT_THERMINAL_SETTINGS");
                    break;
                case 99:
                    message = ResBundleBankSberbank.getString("PINPAD_COMMUNICATION_ERROR");
                    break;
                case 361:
                case 362:
                case 363:
                case 364:
                case 4103:
                case 4104:
                    message = ResBundleBankSberbank.getString("COMMUNICATION_ERROR_WITH_CARD_CHIP");
                    break;
                case 405:
                case 708:
                case 709:
                    message = ResBundleBankSberbank.getString("PIN_IS_BLOCKED");
                    break;
                case 447:
                    message = ResBundleBankSberbank.getString("THREE_INCORRECT_PIN_INPUT_ATTEMPTS");
                    break;
                case 444:
                case 507:
                case 518:
                    message = ResBundleBankSberbank.getString("INCORRECT_TERMINAL_DATE");
                    break;
                case 572:
                case 584:
                case 585:
                case 4454:
                case 5109:
                    message = ResBundleBankSberbank.getString("CARD_EXPIRED");
                    break;
                case 3001:
                    message = ResBundleBankSberbank.getString("INSUFFICIENT_FUNDS_TO_LOAD_ON_CARD");
                    break;
                case 3002:
                    message = ResBundleBankSberbank.getString("BREAKED_FUNDS_LOAD_OPERATION_ON_CARD");
                    break;
                case 4101:
                case 4102:
                case 4111:
                case 4112:
                    message = ResBundleBankSberbank.getString("ENCASHMENT_REQUIRED");
                    break;
                case 4116:
                    message = ResBundleBankSberbank.getString("INCORRECT_LAST_DIGITS");
                    break;
                case 4134:
                    message = ResBundleBankSberbank.getString("DAILY_LOG_REQUIRED");
                    break;
                case 4300:
                case 4301:
                case 4302:
                case 4303:
                case 4305:
                case 4306:
                case 4308:
                    message = ResBundleBankSberbank.getString("INCORRECT_CASH_SETTINGS");
                    break;
                case 4401:
                    message = ResBundleBankSberbank.getString("BANK_CALL_REQUIRED");
                    break;
                case 4404:
                case 4407:
                    message = ResBundleBankSberbank.getString("REMOVE_CARD_REQUIRED");
                    break;
                case 4419:
                case 4497:
                case 3019:
                case 3020:
                case 3021:
                    message = ResBundleBankSberbank.getString("REGLAMENT_WORKS_ON_SERVER");
                    break;
                case 4100:
                case 4119:
                    message = ResBundleBankSberbank.getString("BANK_IS_NOT_RESPOND");
                    break;
                case 4115:
                    message = ResBundleBankSberbank.getString("MANUAL_INPUT_FOR_THIS_CARD_IS_DENIED");
                    break;
                case 4113:
                case 4114:
                    message = ResBundleBankSberbank.getString("TOO_LONG_WITHOUT_BANK_CONNECTION");
                    break;
                case 4117:
                    message = ResBundleBankSberbank.getString("CLIENT_REJECTED_PIN_INPUT");
                    break;
                case 521:
                case 4451:
                    message = ResBundleBankSberbank.getString("INSUFFICIENT_FUNDS");
                    break;
                case 5110:
                    message = ResBundleBankSberbank.getString("INACTIVATED_CARD");
                    break;
                case 4443:
                    message = ResBundleBankSberbank.getString("TAKE_AWAY_CARD");
                    break;
                case 4455:
                case 403:
                    message = ResBundleBankSberbank.getString("INCORRECT_PIN");
                    break;
                case 4125:
                    message = ResBundleBankSberbank.getString("CARD_HAS_CHIP");
                    break;
                case 574:
                case 579:
                case 705:
                case 706:
                case 707:
                case 2004:
                case 2005:
                case 2006:
                case 2007:
                case 2405:
                case 2406:
                case 2407:
                    message = ResBundleBankSberbank.getString("CARD_IS_BLOCKED");
                    break;
                case 2000:
                    message = ResBundleBankSberbank.getString("OPERATION_CANCELED_BY_USER");
                    break;
                case 2002:
                    message = ResBundleBankSberbank.getString("TOO_LONG_PIN_INPUT");
                    break;
                case 4457:
                    message = ResBundleBankSberbank.getString("OPERATION_DENIED_BY_CARD_SETTINGS");
                    break;
                case 4494:
                    message = ResBundleBankSberbank.getString("REPEATED_TRANSACTION");
                    break;
                case 4458:
                    message = ResBundleBankSberbank.getString("OPERATION_DENIED_BY_TERMINAL_SETTINGS");
                    break;
                case 4130:
                    message = ResBundleBankSberbank.getString("TERMINAL_MEMORY_IS_FULL");
                    break;
                case 5111:
                    message = ResBundleBankSberbank.getString("OPERATION_DENIED_FOR_THIS_CARD");
                    break;
                case 5116:
                case 5120:
                    message = ResBundleBankSberbank.getString("CLIENT_REFUSED_PIN_INPUT");
                    break;
                case 4132:
                case 5133:
                    message = ResBundleBankSberbank.getString("OPERATION_REJECTED_BY_CARD");
                    break;
                case 4118:
                    message = ResBundleBankSberbank.getString("OPERATIONS_NOT_FOUND");
                    break;
                case 4353:
                    message = ResBundleBankSberbank.getString("OPERATION_SUSPENSION");
                    break;
                default:
                    message = ResBundleBankSberbank.getString("ERROR_COMMUNICATION");
            }
        }
        return message;
    }

    public static boolean isCriticalError(String result) {
        return isCriticalError(Integer.parseInt(result));
    }

    public static boolean isCriticalError(int result) {
        switch (result) {
            case 4104:
            case 2004:
            case 99:
                return true;
            default:
                return false;
        }
    }
}
