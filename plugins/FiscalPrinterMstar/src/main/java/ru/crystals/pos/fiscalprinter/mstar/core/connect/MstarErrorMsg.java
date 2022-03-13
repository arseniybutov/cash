package ru.crystals.pos.fiscalprinter.mstar.core.connect;

import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.mstar.core.ResBundleFiscalPrinterMstar;

public class MstarErrorMsg {

    public static String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case 0x00:
                return ResBundleFiscalPrinterMstar.getString("SUCCESSFULLY_EXECUTED");

            // Ошибки выполнения команд
            case 0x01:
                return ResBundleFiscalPrinterMstar.getString("ERROR_STATE_FP");
            case 0x02:
                return ResBundleFiscalPrinterMstar.getString("ERROR_NUMBER_OF_FUNCTION");
            case 0x03:
                return ResBundleFiscalPrinterMstar.getString("ERROR_FORMAT");

            // Ошибки протокола передачи данных
            case 0x04:
                return ResBundleFiscalPrinterMstar.getString("ERROR_BUFFER_OVERFLOW");
            case 0x05:
                return ResBundleFiscalPrinterMstar.getString("ERROR_TIME_OUT_SEND_DATA");
            case 0x06:
                return ResBundleFiscalPrinterMstar.getString("ERROR_PASSWORD");
            case 0x07:
                return ResBundleFiscalPrinterMstar.getString("ERROR_CRC");

            // Ошибки печатающего устройства
            case 0x08:
                return ResBundleFiscalPrinterMstar.getString("WARN_END_OF_PAPER");
            case 0x09:
                return ResBundleFiscalPrinterMstar.getString("WARN_PRINTER_NOT_READY");

            // Ошибки даты/времени
            case 0x0A:
                return ResBundleFiscalPrinterMstar.getString("WARN_CURRENT_SHIFT_MORE_24_H");
            case 0x0B:
                return ResBundleFiscalPrinterMstar.getString("WARN_DIFFERENCE_TIME_MORE_8_MIN");
            case 0x0C:
                return ResBundleFiscalPrinterMstar.getString("WARN_ERROR_NEW_DATA");

            // Прочие ошибки
            case 0x0D:
                return ResBundleFiscalPrinterMstar.getString("ERROR_PASSWORD_FOR_ACCESS_TO_FISCAL_MEMORY");
            case 0x0E:
                return ResBundleFiscalPrinterMstar.getString("ERROR_NEGATIVE_RESULT");
            case 0x0F:
                return ResBundleFiscalPrinterMstar.getString("WARN_YOU_MAST_CLOSE_SHIFT");

            // Фатальные ошибки
            case 0x20:
                return ResBundleFiscalPrinterMstar.getString("FATAL_ERROR");
            case 0x21:
                return ResBundleFiscalPrinterMstar.getString("ERROR_FREE_FISCAL_MEMORY");

            default:
                return ResBundleFiscalPrinterMstar.getString("UNKNOWN_ERROR");
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
            default:
                return CashErrorType.NOT_CRITICAL_ERROR;
        }
    }

    public static CashErrorType getErrorType() {
        return CashErrorType.FISCAL_ERROR;
    }
}
