package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;

public class PiritErrorMsg {
    private static boolean fnDevice = false;

    public static void setFnDevice(boolean fnDevice){
        PiritErrorMsg.fnDevice = fnDevice;
    }

    public static String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case 0x00:
                return ResBundleFiscalPrinterPirit.getString("SUCCESSFULLY_EXECUTED");

            // Ошибки выполнения команд
            case 0x01:
                return ResBundleFiscalPrinterPirit.getString("ERROR_STATE_FP");
            case 0x02:
                return ResBundleFiscalPrinterPirit.getString("ERROR_NUMBER_OF_FUNCTION");
            case 0x03:
                return ResBundleFiscalPrinterPirit.getString("ERROR_FORMAT");

            // Ошибки протокола передачи данных
            case 0x04:
                return ResBundleFiscalPrinterPirit.getString("ERROR_BUFFER_OVERFLOW");
            case 0x05:
                return ResBundleFiscalPrinterPirit.getString("ERROR_TIME_OUT_SEND_DATA");
            case 0x06:
                return ResBundleFiscalPrinterPirit.getString("ERROR_PASSWORD");
            case 0x07:
                return ResBundleFiscalPrinterPirit.getString("ERROR_CRC");

            // Ошибки печатающего устройства
            case 0x08:
                return ResBundleFiscalPrinterPirit.getString("WARN_END_OF_PAPER");
            case 0x09:
                return ResBundleFiscalPrinterPirit.getString("WARN_PRINTER_NOT_READY");

            // Ошибки даты/времени
            case 0x0A:
                return ResBundleFiscalPrinterPirit.getString("WARN_CURRENT_SHIFT_MORE_24_H");
            case 0x0B:
                return ResBundleFiscalPrinterPirit.getString("WARN_DIFFERENCE_TIME_MORE_8_MIN");
            case 0x0C:
                return ResBundleFiscalPrinterPirit.getString("WARN_ERROR_NEW_DATA");

            // Прочие ошибки
            case 0x0D:
                return ResBundleFiscalPrinterPirit.getString("ERROR_PASSWORD_FOR_ACCESS_TO_FISCAL_MEMORY");
            case 0x0E:
                return ResBundleFiscalPrinterPirit.getString("ERROR_NEGATIVE_RESULT");
            case 0x0F:
                return ResBundleFiscalPrinterPirit.getString("WARN_YOU_MAST_CLOSE_SHIFT");
            case 0x10:
                return ResBundleFiscalPrinterPirit.getString("NO_DATA_IN_LOG");
            case 0x11:
                return ResBundleFiscalPrinterPirit.getString("ERROR_USING_SD_CARD");
            case 0x12:
                return ResBundleFiscalPrinterPirit.getString("ERROR_SENDING_DATA_TO_OFD");

            // Фатальные ошибки
            case 0x20:
                return ResBundleFiscalPrinterPirit.getString("FATAL_ERROR");
            case 0x21:
                return ResBundleFiscalPrinterPirit.getString("ERROR_FREE_FISCAL_MEMORY");

            // Ошибки ЭКЛЗ / ФН
            case 0x41:
                return fnDevice ? ResBundleFiscalPrinterPirit.getString("ERROR_FORMAT_FN_COMMAND") : ResBundleFiscalPrinterPirit.getString("ERROR_FORMAT_EKLZ_COMMAND");
            case 0x42:
                return fnDevice ? ResBundleFiscalPrinterPirit.getString("ERROR_STATE_FN") : ResBundleFiscalPrinterPirit.getString("ERROR_STATE_EKLZ");
            case 0x43:
                return fnDevice ? ResBundleFiscalPrinterPirit.getString("FAILURE_FN") : ResBundleFiscalPrinterPirit.getString("FAILURE_EKLZ");
            case 0x44:
                return fnDevice ? ResBundleFiscalPrinterPirit.getString("FAILURE_CPU_FN") : ResBundleFiscalPrinterPirit.getString("FAILURE_CPU_EKLZ");
            case 0x45:
                return fnDevice ? ResBundleFiscalPrinterPirit.getString("ERROR_TIME_LIFE_FN") : ResBundleFiscalPrinterPirit.getString("ERROR_TIME_LIFE_EKLZ");
            case 0x46:
                return fnDevice ? ResBundleFiscalPrinterPirit.getString("ERROR_FN_IS_FULL") : ResBundleFiscalPrinterPirit.getString("ERROR_EKLZ_IS_FULL");
            case 0x47:
                return ResBundleFiscalPrinterPirit.getString("ERROR_DATE_TIME");
            case 0x48:
                return ResBundleFiscalPrinterPirit.getString("WARN_NO_REQUESTED_DATA");
            case 0x49:
                return ResBundleFiscalPrinterPirit.getString("INCORRECT_COMMAND_PARAMETERS");
            case 0x4A:
                return ResBundleFiscalPrinterPirit.getString("ERROR_NO_EKLZ_RESPONSE");
            case 0x4B:
                return ResBundleFiscalPrinterPirit.getString("ERROR_EKLZ_COMMUNICATION");

            //Ошибки Белорусии
            case 0x80:
                return ResBundleFiscalPrinterPirit.getString("SKNO_COMMON_ERROR");
            case 0x91:
                return ResBundleFiscalPrinterPirit.getString("SKNO_CONNECT_ERROR");
            case 0x92:
                return ResBundleFiscalPrinterPirit.getString("SKZI_CONNECT_ERROR");
            case 0x93:
                return ResBundleFiscalPrinterPirit.getString("SKNO_BROKEN");
            case 0x94:
                return ResBundleFiscalPrinterPirit.getString("IDENTIFICATION_FAIL");
            case 0x95:
                return ResBundleFiscalPrinterPirit.getString("PROHIBITION_OF_SERVICE_SKZI");
            case 0x96:
                return ResBundleFiscalPrinterPirit.getString("PROHIBITION_OF_OF_SERVICE_SKZI");
            case 0x97:
                return ResBundleFiscalPrinterPirit.getString("PROHIBITION_OF_SERVICE_NOT_SENT_Z");
            case 0x98:
                return ResBundleFiscalPrinterPirit.getString("PROHIBITION_OF_SERVICE_SKNO_MEMORY_OVERFLOW");

            case 0x50:
                return ResBundleFiscalPrinterPirit.getString("OVERFLOW_DATA_TLV");
            case 0x51:
                return ResBundleFiscalPrinterPirit.getString("NO_TRANSPORT_CONNECT");
            case 0x52:
                return ResBundleFiscalPrinterPirit.getString("RESURS_KS");
            case 0x54:
                return ResBundleFiscalPrinterPirit.getString("FULL_MEMORY_DOC_OFD");
            case 0x55:
                return ResBundleFiscalPrinterPirit.getString("OLDEST_MESSAGE_MORE_THEN_30_DAYS");
            case 0x56:
                return ResBundleFiscalPrinterPirit.getString("FN_SHIFT_MORE_24_H");
            case 0x57:
                return ResBundleFiscalPrinterPirit.getString("DIFFERENCE_MORE_THAN_5_MINUTES");
            case 0x60:
                return ResBundleFiscalPrinterPirit.getString("ERROR_MESSAGE_OFD");
            case 0x61:
                return ResBundleFiscalPrinterPirit.getString("NO_CONNECT_FN");
            case 0x62:
                return ResBundleFiscalPrinterPirit.getString("FN_COMMUNICATION_ERROR");
            case 0x63:
                return ResBundleFiscalPrinterPirit.getString("LONG_COMMAND_FOR_FN");

            // Ошибки, связанные с маркировкой ФФД 1.2
            case 0x72:
                return ResBundleFiscalPrinterPirit.getString("MARK_CODE_WORK_FORBIDDEN");
            case 0x73:
                return ResBundleFiscalPrinterPirit.getString("COMMANDS_ORDER_FOR_CM_IS_BROKEN");
            case 0x74:
                return ResBundleFiscalPrinterPirit.getString("MARK_CODE_WORK_BLOCKED");
            case 0x75:
                return ResBundleFiscalPrinterPirit.getString("MARK_CODE_TABLE_FULL");
            case 0x7C:
                return ResBundleFiscalPrinterPirit.getString("TLV_BLOCK_REQUISITES_ERROR");
            case 0x7E:
                return ResBundleFiscalPrinterPirit.getString("MARK_CODE_WAS_NOT_CHECKED");

            default:
                return ResBundleFiscalPrinterPirit.getString("UNKNOWN_ERROR");
        }
    }

    public static CashErrorType getErrorType(int errorCode) {
        switch (errorCode) {
        // Ошибки выполнения команд
            case 0x01:
                return CashErrorType.NOT_CRITICAL_ERROR;
            case 0x02:
                return CashErrorType.NOT_CRITICAL_ERROR;
            case 0x03:
                return CashErrorType.NOT_CRITICAL_ERROR;
                // Ошибки протокола передачи данных
            case 0x04:
                return CashErrorType.NEED_RESTART;
            case 0x05:
                return CashErrorType.NEED_RESTART;
            case 0x06:
                return CashErrorType.FATAL_ERROR;
            case 0x07:
                return CashErrorType.NEED_RESTART;
                // Ошибки печатающего устройства
            case 0x08:
                return CashErrorType.NOT_CRITICAL_ERROR;
            case 0x09:
                return CashErrorType.NOT_CRITICAL_ERROR;
                // Ошибки даты/времени
            case 0x0A:
                return CashErrorType.NOT_CRITICAL_ERROR;
            case 0x0B:
                return CashErrorType.NOT_CRITICAL_ERROR;
            case 0x0C:
                return CashErrorType.FATAL_ERROR;
                // Прочие ошибки
            case 0x0D:
                return CashErrorType.FATAL_ERROR;
            case 0x0E:
                return CashErrorType.NOT_CRITICAL_ERROR;
            case 0x0F:
                return CashErrorType.NOT_CRITICAL_ERROR;
                // Фатальные ошибки
            case 0x20:
                return CashErrorType.FATAL_ERROR;
            case 0x21:
                return CashErrorType.FATAL_ERROR;
                // Ошибки ЭКЛЗ / ФН
            case 0x41:
                return CashErrorType.FATAL_ERROR;
            case 0x42:
                return CashErrorType.FATAL_ERROR;
            case 0x43:
                return fnDevice ? CashErrorType.FISCAL_ERROR_REBOOT : CashErrorType.FATAL_ERROR;
            case 0x44:
                return fnDevice ? CashErrorType.FISCAL_ERROR_REBOOT : CashErrorType.FATAL_ERROR;
            case 0x45:
                return CashErrorType.FATAL_ERROR;
            case 0x46:
                return CashErrorType.FATAL_ERROR;
            case 0x47:
                return CashErrorType.FATAL_ERROR;
            case 0x48:
                return CashErrorType.NOT_CRITICAL_ERROR;
            case 0x49:
                return CashErrorType.FATAL_ERROR;
            case 0x4A:
                return CashErrorType.FATAL_ERROR;
            case 0x4B:
                return CashErrorType.FATAL_ERROR;
            case 0x50:
                return CashErrorType.FATAL_ERROR;
            case 0x51:
                return CashErrorType.FATAL_ERROR;
            case 0x52:
                return CashErrorType.FATAL_ERROR;
            case 0x54:
                return CashErrorType.FATAL_ERROR;
            case 0x60:
                return CashErrorType.FATAL_ERROR;
            case 0x61:
                return CashErrorType.FATAL_ERROR;
            case 0x62:
                return CashErrorType.FATAL_ERROR;
            case 0x63:
                return CashErrorType.FATAL_ERROR;
            case 0x80:
                return CashErrorType.FISCAL_ERROR_REBOOT;
            default:
                return CashErrorType.NOT_CRITICAL_ERROR;
        }
    }

    public static CashErrorType getErrorType() {
        return CashErrorType.FISCAL_ERROR;
    }
}
