package ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashErrorType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.ResBundleFiscalPrinterShtrihMiniFrk;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.ShtrihErrorMsg;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFlags;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihMode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihModeEnum;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihSubState;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihResourceBundleWorker;
import ru.crystals.pos.utils.PortAdapterException;
import ru.crystals.pos.utils.PortAdapterIllegalStateException;
import ru.crystals.pos.utils.PortAdapterNoConnectionException;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * Набор инструментов для перевода {@link ShtrihException ошибок Штриха} в понятные кассовому техпроцессу {@link FiscalPrinterException}.
 *
 * @author aperevozchikov
 */
public abstract class ShtrihExceptionConvertor {
    private static final Logger log = LoggerFactory.getLogger(ShtrihExceptionConvertor.class);

    /**
     * Сконвертит указанную ошибку, возникшую при инфо-обмене (или попытке инфо-обмена) с фискальником, в {@link FiscalPrinterException ошибку,
     * понятную кассовому техпроцессу}.
     *
     * @param exception ошибка, что надо сконвертнуть
     * @return <code>null</code> - если не удалось сконвертнуть эту ошибку в ошибку фискальника (т.е., возможно, аргумент и не является ошибкой
     * фискальника)
     */
    public static FiscalPrinterException convert(Exception exception) {
        if (exception == null) {
            return null;
        }
        log.debug("entering convert(Exception). The argument is: {}, {}", exception.getClass(), exception.getMessage());
        FiscalPrinterException result = null;
        if (exception instanceof ShtrihException) {
            result = convert((ShtrihException) exception);
        } else if (exception instanceof PortAdapterException) {
            result = convert((PortAdapterException) exception);
        } else if (exception instanceof IOException) {
            // ошибка ввода-вывода. скажем, что связи нет
            result = new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("ERROR_DEVICE_NOT_CONNECTED"), CashErrorType.FISCAL_ERROR);
        }
        if (result != null) {
            log.debug("leaving convert(Exception). The result is: {}, {}", result.getExceptionType(), result.getMessage());
        }
        return result;
    }

    /**
     * Сконвертит указанную {@link PortAdapterException ошибку инфо-обмена} в {@link FiscalPrinterException ошибку, понятную кассовому техпроцессу}.
     *
     * @param exception "Общая" (generic) ошибка при информационном обмене через порт, что надо сконвертнуть
     * @return <code>null</code>, если не удалось произвести конвертацию - по любой причине
     */
    private static FiscalPrinterException convert(PortAdapterException exception) {
        FiscalPrinterException result = null;
        if (exception == null) {
            return null;
        }

        if (exception instanceof PortAdapterNoConnectionException) {
            // с устройством просто нету связи
            result = new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("ERROR_DEVICE_NOT_CONNECTED"), CashErrorType.FISCAL_ERROR);
        } else if (exception instanceof PortAdapterIllegalStateException) {
            // устройство в некорректном состоянии - все равно, что связи с ним нет
            result = new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("ERROR_DEVICE_NOT_CONNECTED"), CashErrorType.FISCAL_ERROR);
        }
        return result;
    }

    /**
     * Сконвертит указанную {@link ShtrihException Штриховую} ошибку в {@link FiscalPrinterException ошибку, понятную кассовому техпроцессу}.
     *
     * @param exception Штриховая ошибка, что надо сконвертнуть
     * @return <code>null</code>, если не удалось произвести конвертацию - по любой причине
     */
    private static FiscalPrinterException convert(ShtrihException exception) {
        if (exception == null) {
            return null;
        }
        if (exception instanceof ShtrihResponseException) {
            return convert((ShtrihResponseException) exception);
        }
        // какая-то внутренняя ошибка - связанная, видимо, с нарушением протокола со стороны Штриха (маловероятно)
        //  либо с подготовкой данных для Штриха (например, формирование картинки для печати)
        //  просто передадим сообщение из ошибки:
        return new FiscalPrinterException(exception.getMessage());
    }

    /**
     * Вернет <code>true</code>, если указанный код ошибки указывает на то, что
     * "Некоторые из устройств, которые согласно местному финансовому законодательству должны быть подключены, отсутствуют".
     *
     * @param errorCode код ошибки
     * @return <code>false</code>, если данная ошибка не говорит об отсутствии необходимых [по законодательству] устройств
     */
    private static boolean isSomeLegalDeviceAbsent(byte errorCode) {
        // либо нету ФП, либо ЭКЛЗ
        return ShtrihResponseException.FIRST_FISCAL_BOARD_IS_ABSENT == errorCode ||
                ShtrihResponseException.SECOND_FISCAL_BOARD_IS_ABSENT == errorCode ||
                ShtrihResponseException.FISCAL_BOARD_IS_ABSENT == errorCode ||
                ShtrihResponseException.EKLZ_IS_ABSENT == errorCode;
    }

    /**
     * Вернет <code>true</code>, если указанный код ошибки указывает на то, что
     * "Фискальная память ФР исчерпана".
     *
     * @param errorCode код ошибки
     * @return <code>false</code>, если данная ошибка не говорит об исчерпании памяти ФР
     */
    private static boolean isFiscalMemoryOverflowError(byte errorCode) {
        // либо уже не осталось свободных записей в ФП (фискальной памяти) (0..2100)
        //  либо лимит пере-регистраций ФР исчерпан (0..16)
        return ShtrihResponseException.FISCALIZE_COUNT_OVERFLOW == errorCode ||
                ShtrihResponseException.FISCALIZE_COUNT_OVERFLOW_NOT_SUPPORTED == errorCode ||
                ShtrihResponseException.SHIFT_REPORTS_MEMORY_OVERFLOW == errorCode ||
                ShtrihResponseException.ACTIVATION_SEG_OVERFLOW == errorCode;
    }

    /**
     * Вернет <code>true</code>, если указанный код ошибки указывает на то, что
     * "Нет связи с фискальной памятью ФР".
     *
     * @param errorCode код ошибки
     * @return <code>false</code>, если данная ошибка не говорит об отсутствии связи с ФП.
     */
    private static boolean isFiscalMemoryCommunicationLoss(byte errorCode) {
        // 5 кодов ошибок зарезервировано в протоколе
        return ShtrihResponseException.FISCAL_BOARD_COMMUNICATION_ERROR == errorCode ||
                ShtrihResponseException.FISCAL_BOARD_COMMUNICATION_ERROR_80 == errorCode ||
                ShtrihResponseException.FISCAL_BOARD_COMMUNICATION_ERROR_81 == errorCode ||
                ShtrihResponseException.FISCAL_BOARD_COMMUNICATION_ERROR_82 == errorCode ||
                ShtrihResponseException.FISCAL_BOARD_COMMUNICATION_ERROR_83 == errorCode;
    }

    /**
     * Вернет "общее" (техническое - не обязательно понятное кассиру) описание указанной ошибки.
     *
     * @param exception ошибка. описание которой надо вернуть
     * @return <code>null</code>, если не удалось определить общее описание ошибки - по любой причине
     */
    private static String getGeneralExceptionMessage(ShtrihResponseException exception) {
        if (exception == null) {
            log.error("getGeneralExceptionMessage(ShtrihResponseException): the argument is NULL");
            return null;
        }

        String pattern = ShtrihResourceBundleWorker.getLocalValue("shtrih.error");
        if (pattern == null) {
            log.error("getGeneralExceptionMessage(ShtrihResponseException): \"shtrih.error\" key was not found in the resource file(s)");
            return null;
        }

        String errorCode = String.format("0x%02X", exception.getErrorCode());
        String commandCode = String.format("0x%02X", exception.getCommand());

        String errorKey = String.format("shtrih.error.%X", exception.getErrorCode());
        String commandKey = String.format("shtrih.command.%02Xh", exception.getCommand());

        String errorDesc = ShtrihResourceBundleWorker.getLocalValue(errorKey);
        String commandDesc = ShtrihResourceBundleWorker.getLocalValue(commandKey);

        return MessageFormat.format(pattern, errorCode, errorDesc, commandCode, commandDesc);

    }

    private static FiscalPrinterException convert(ShtrihResponseException exception) {
        if (exception == null) {
            return null;
        }

        // выясним, что за ошибка:
        if (ShtrihResponseException.WRONG_STATE == exception.getErrorCode() || ShtrihResponseException.WRONG_SUBSTATE == exception.getErrorCode()) {
            // команда не поддерживается в данном режиме - выясним что за режим и сформируем ответ
            if (exception.getDeviceState() != null) {
                ShtrihStateDescription state = exception.getDeviceState();
                ShtrihMode shtrihMode = state.getMode();
                ShtrihModeEnum stateEnum = shtrihMode.getStateNumber();
                ShtrihFlags flags = state.getFlags();
                ShtrihSubState subState = state.getSubState();

                if (flags != null && flags.isCaseCover()) {
                    // нельзя выполнить операцию, т.к. поднята крышка ФР
                    return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("JPOS_EFPTR_COVER_OPEN"),
                            ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
                } else if (ShtrihSubState.PAPER_ABSENT_ACTIVELY.equals(subState) || ShtrihSubState.PAPER_ABSENT_PASSIVELY.equals(subState)) {
                    // ошибка возникла из-за того, что закончилась бумага
                    //  - НЕ ВАЖНО ГДЕ: в чековой ленте, в кассовой ленте, на подкладном документе
                    return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("PAPER_ABSENT_IN_FISCAL_REGISTER"),
                            ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
                } else if (ShtrihModeEnum.PRINTING_FISCAL_DOCUMENT.equals(stateEnum) && ShtrihMode.AWAITING_FOR_SLP_TO_BE_EXTRACTED == shtrihMode.getStateOfState()) {
                    // фискальный подкладной документ следует извлечь из принтера
                    return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("JPOS_EFPTR_SLP_FORM"),
                            ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
                }
            }
            // команда не поддерживается в данном режиме т у нас нет информации. что это за режим.
            //  полноценного exception'а вернуть не сможем
            //  вернем ошибку общего вида - см. ниже

        } else if (isSomeLegalDeviceAbsent(exception.getErrorCode())) {
            // нету некоторых обязательных [по законодательству] устройств:
            return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("JPOS_EFPTR_MISSING_DEVICES"),
                    ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
        } else if (isFiscalMemoryOverflowError(exception.getErrorCode())) {
            // фискальная память исчерпана
            return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("JPOS_EFPTR_FISCAL_MEMORY_FULL"),
                    ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
        } else if (ShtrihResponseException.INTERNAL_CLOCK_WAS_RESET == exception.getErrorCode()) {
            // часы в фискальнике "сбросились"
            return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("JPOS_EFPTR_CLOCK_ERROR"),
                    ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
        } else if (isFiscalMemoryCommunicationLoss(exception.getErrorCode())) {
            // нет связи с ФП
            return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("JPOS_EFPTR_FISCAL_MEMORY_DISCONNECTED"),
                    ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
        } else if (ShtrihResponseException.RECEIPT_RIBBON_ABSENT_ERROR == exception.getErrorCode()) {
            // нет чековой ленты - ФР умудряется выбросить этот Exception, если открыть крышку ФР в момент печати
            return new FiscalPrinterException(ResBundleFiscalPrinterShtrihMiniFrk.getString("PAPER_ABSENT_IN_FISCAL_REGISTER"),
                    ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
        }
        // так и не определили что за ошибка в понятном кассиру виде - вернем хотя бы в виде, понятном тех. специалисту:
        //  вернем ошибку "общего" вида:
        String desc = getGeneralExceptionMessage(exception);
        return new FiscalPrinterException(desc == null ? "" : desc, ShtrihErrorMsg.getErrorType(), (long) exception.getErrorCode());
    }
}
