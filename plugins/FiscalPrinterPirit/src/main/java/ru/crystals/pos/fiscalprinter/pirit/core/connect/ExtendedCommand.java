package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import org.apache.commons.lang.math.NumberUtils;
import ru.crystals.comportemulator.pirit.PiritCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Команда Пирита с подзапросом
 */
public enum ExtendedCommand {

    /**
     * Вернуть номер текущей смены
     * Целое число
     */
    GET_COUNTERS_SHIFT_NUMBER(PiritCommand.GET_COUNTERS, 1),
    /**
     * Вернуть суммы продаж по типам платежа
     * Дробное число * 16
     * Возвращается 16 значений – по максимально возможному количеству типов платежей
     */
    GET_COUNTERS_SALE_SUMS_BY_PAYMENT(PiritCommand.GET_COUNTERS, 3),
    /**
     * Вернуть количество оплат по продажам
     * Целое число * 16
     */
    GET_COUNTERS_SALE_COUNTS_BY_PAYMENT(PiritCommand.GET_COUNTERS, 4),
    /**
     * Вернуть суммы возвратов по типам платежа
     * Целое число * 16
     */
    GET_COUNTERS_REFUND_SUMS_BY_PAYMENT(PiritCommand.GET_COUNTERS, 5),
    /**
     * Вернуть количество оплат по возвратам
     * Целое число * 16
     */
    GET_COUNTERS_REFUND_COUNTS_BY_PAYMENT(PiritCommand.GET_COUNTERS, 6),
    /**
     * Вернуть данные по последнему X отчету или отчету о закрытии смены
     * Строка, Целое число, Дробное число, Целое число, Дробное число, Целое число, Дробное число, Целое число, Дробное число, Целое число, Дробное число, Целое число,
     * Дробное число
     * текущий операционный счетчик, номер документа, сумма в кассе, кол-во продаж (кол-во приходов), сумма продаж (сумма приходов), кол-во возвратов (возвратов
     * прихода), сумма возвратов (возвратов прихода), количество аннулированных, сумма аннулированных, количество внесений, сумма внесений, количество изъятий, сумма
     * изъятий
     */
    GET_COUNTERS_LAST_REPORT(PiritCommand.GET_COUNTERS, 12),
    /**
     * Вернуть количество оформленных чеков по типам операций
     * Целое число *6
     * Возвращается 6 значений – количество чеков продажи, возврата, аннулированных, отложенных, чеков внесения и изъятия
     */
    GET_COUNTERS_DOC_COUNTS_BY_TYPE(PiritCommand.GET_COUNTERS, 7),
    /**
     * Вернуть суммы по оформленным чекам
     * Дробное число * 4
     * Возвращается 4 значения – суммы по аннулированным и отложенным чекам, чекам внесения и изъятия
     */
    GET_COUNTERS_DOC_SUMS_BY_TYPE(PiritCommand.GET_COUNTERS, 8),
    /**
     * Вернуть суммы покупок по типам платежа
     * Дробное число * 16
     * Возвращается 16 значений – по максимально возможному количеству типов платежей.
     */
    GET_COUNTERS_EXPENSE_SUM_BY_PAYMENTS(PiritCommand.GET_COUNTERS, 16),
    /**
     * Вернуть суммы возврата покупок по типам платежа
     * Дробное число * 16
     * Возвращается 16 значений – по максимально возможному количеству типов платежей.
     */
    GET_COUNTERS_RETURN_EXPENSE_SUM_BY_PAYMENTS(PiritCommand.GET_COUNTERS, 17),
    /**
     * Вернуть количество оформленных чеков по типам операций
     * Целое число, Целое число
     * кол-во чеков покупок (расходов), кол-во чеков возвратов покупок (возвратов расхода)
     */
    GET_COUNTERS_EXPENSE_COUNT_BY_TYPE(PiritCommand.GET_COUNTERS, 15),
    /**
     * Вернуть данные по коррекциям
     * Целое число, Дробное число * 2
     * Количество коррекций, Суммы коррекций наличными и безналичными
     */
    GET_COUNTERS_CORRECTION_DATA(PiritCommand.GET_COUNTERS, 18),


    /**
     * Заводской номер ККТ (Строка)
     */
    GET_INFO_FACTORY_NUM(PiritCommand.GET_INFO, 1),
    /**
     * Идентификатор прошивки (Целое число)
     */
    GET_INFO_FW_ID(PiritCommand.GET_INFO, 2),
    /**
     * ИНН (Строка)
     */
    GET_INFO_INN(PiritCommand.GET_INFO, 3),
    /**
     * регистрационный номер ККТ
     */
    GET_INFO_REG_NUM(PiritCommand.GET_INFO, 4),
    /**
     * Вернуть дату и время последней фискальной операции
     * Дата, Время
     */
    GET_INFO_LAST_FISCAL_DATE(PiritCommand.GET_INFO, 5),
    /**
     * Вернуть сумму наличных в денежном ящике
     * Дробное число
     */
    GET_INFO_CASH_AMOUNT(PiritCommand.GET_INFO, 7),
    /**
     * Вернуть текущий операционный счетчик
     * Строка
     */
    GET_INFO_OPERATION_COUNTER(PiritCommand.GET_INFO, 11),
    /**
     * Вернуть нарастающий итог
     * Дробное число, Дробное число, Дробное число, Дробное число
     * Продажа (приход), Возврат (возврат прихода), Покупка (расход), Возврат покупки (возврат расхода)
     */
    GET_INFO_INC_TOTALS(PiritCommand.GET_INFO, 12),
    /**
     * Вернуть тип прошивки
     * Целое число
     * 0 - стандартная прошивка, 1 - отладочный комплект
     */
    GET_INFO_FW_TYPE(PiritCommand.GET_INFO, 15),
    /**
     * Вернуть размер бумаги текущего дизайна
     * Целое число
     * 0 - 80мм, 1 - 57мм
     */
    GET_INFO_PAPER_SIZE(PiritCommand.GET_INFO, 16),
    /**
     * Получить сколько символов вмещается в строку заданным номером шрифта
     *
     * Работает только для 2Ф (с дизайнами с номером меньше 16) и википринтов.
     * Для 2Ф с дизайном 16 и выше, а также для 1Ф возвращает неправильные значения.
     * Для РБ не поддерживается
     */
    GET_MAX_CHAR_FOR_FONT(PiritCommand.GET_INFO, 18),
    /**
     * Вернуть модель устройства
     * Целое число
     */
    GET_INFO_MODEL_ID(PiritCommand.GET_INFO, 21),
    /**
     * Вернуть систему налогообложения и режим работы и ФН
     * Целое число, Целое число, Целое число
     * Система налогообложения, Режим работы, Признак установки принтера в
     * автомате
     */
    GET_INFO_TAX_WORK_MODE(PiritCommand.GET_INFO, 23),


    /**
     * Вернуть счетчики текущего документа
     * Дробное число * 3
     * Возвращается 3 значения - сумма чека, сумма скидки по чеку, 0
     */
    GET_RECEIPT_DATA_CURRENT(PiritCommand.GET_RECEIPT_DATA, 1),
    /**
     * Вернуть данные по последнему закрытому чеку
     * Целое число, Строка, Целое число, Целое число, Дробное число, Дробное число, Дробное число, Строка, Целое число
     * тип чека (для аннулиров. = 0), текущий операц. счетчик, номер чека, номер документа, сумма чека, сумма скидки по чеку, 0, строка ФП (фиск. признак), Номер ФД
     */
    GET_RECEIPT_DATA_LAST(PiritCommand.GET_RECEIPT_DATA, 2),

    /**
     * Вернуть напряжение питания (мВ) Целое число
     * Возвращается значение в милливольтах
     */
    GET_SERVICE_INFO_VOLTAGE(PiritCommand.GET_SERVICE_INFO, 1),
    /**
     * Вернуть температуру термоголовки
     * Целое число
     * Возвращается значение в градусах
     */
    GET_SERVICE_INFO_TEMP(PiritCommand.GET_SERVICE_INFO, 2),
    /**
     * Вернуть количество отрезов резчика
     * Целое число
     */
    GET_SERVICE_INFO_CUT_COUNTS(PiritCommand.GET_SERVICE_INFO, 3),
    /**
     * Вернуть ресурс термоголовки
     * Целое число
     * Возвращается значение в мм
     */
    GET_SERVICE_INFO_HEAD_RES(PiritCommand.GET_SERVICE_INFO, 4),
    /**
     * Вернуть напряжение на батарейке (мВ)
     * Целое число
     * Возвращается значение в милливольтах
     */
    GET_SERVICE_INFO_BATTERY_VOLTAGE(PiritCommand.GET_SERVICE_INFO, 7),
    /**
     * Вернуть количество отрезов резчика (необнуляемое)
     * Целое число
     */
    GET_SERVICE_INFO_CUT_COUNTS_TOTAL(PiritCommand.GET_SERVICE_INFO, 8),
    /**
     * Вернуть ресурс термоголовки (необнуляемый)
     * Целое число
     * Возвращается значение в мм
     */
    GET_SERVICE_INFO_HEAD_RES_TOTAL(PiritCommand.GET_SERVICE_INFO, 9),
    /**
     * Вернуть регистрационный номер ФН
     * Строка
     */
    GET_FN_INFO_NUMBER(PiritCommand.GET_FN_INFO, 1),
    /**
     * Вернуть статус ФН
     * Целое число, Целое число, Целое число
     * Состояние ФН (см. таблицу 1), Состояние текущего документа (см. таблицу 3), Флаги предупреждения (см
     * . таблицу 4)
     */
    GET_FN_INFO_STATUS(PiritCommand.GET_FN_INFO, 2),
    /**
     * Вернуть номер последнего фискального документа
     * Строка
     */
    GET_FN_INFO_LAST_FD(PiritCommand.GET_FN_INFO, 3),
    /**
     * Вернуть дату и время регистрации
     * Дата, Время
     */
    GET_FN_INFO_REG_DATE(PiritCommand.GET_FN_INFO, 4),
    /**
     * Вернуть состояние обмена с ОФД
     * Целое число, Целое число, Целое число, Дата, Время
     * Статус обмена (см. таблицу 5), Количество документов для передачи в ОФД,
     * Номер первого документа для передачи в ОФД, Дата/время первого док-та для передачи в ОФД
     */
    GET_FN_INFO_OFD_STATUS(PiritCommand.GET_FN_INFO, 7),
    /**
     * Запрос версии ФН и версии ФФД
     * Строка, Число, Число, Строка, Число, Число, Число
     * Версия прошивки ФН, Версия ФН (0 - отладочный ФН, 1 - серийный ФН) Версия
     * ФФД (1209), Версия ККТ (1188), Версия ФФД ККТ (1189), Зарегистрированная версия ФФД ФН, Максимальная версия ФФД ФН (1190)
     */
    GET_EKLZ_INFO_FW(PiritCommand.GET_FN_INFO, 14),
    /**
     * Передача КМ в ФН для проверки достоверности КМ
     */
    VALIDATE_MARK_CODE(PiritCommand.MARK_CODE, 1),
    /**
     * Подтверждение включения КМ в чек
     */
    CONFIRM_MARK_CODE_ADD_TO_DOC(PiritCommand.MARK_CODE, 2),
    /**
     * Удаление сохраненных КМ в ФН
     */
    CLEAR_FN_MARK_BUFFER(PiritCommand.MARK_CODE, 3),
    /**
     * Передача КМ для включения в кассовый чек
     */
    REGISTER_MARK_CODE_WITH_POSITION(PiritCommand.MARK_CODE, 15),

    ;

    private static final Map<Integer, Map<Integer, ExtendedCommand>> INDEXED = Arrays.stream(values())
            .collect(Collectors.groupingBy(extCmd -> extCmd.getCmd().getCode(),
                    Collectors.toMap(ExtendedCommand::getSubCmd, Function.identity(), (a, b) -> a)));

    /**
     * Основная команда
     */
    private final PiritCommand cmd;

    /**
     * Номер подзапроса
     */
    private final int subCmd;

    ExtendedCommand(PiritCommand cmd, int subCmd) {
        this.cmd = cmd;
        this.subCmd = subCmd;
    }

    public static ExtendedCommand getByCommandAndSubCommand(PiritCommand commandID, String param) {
        if (!NumberUtils.isNumber(param)) {
            return null;
        }
        try {
            final int subCmd = Integer.parseInt(param);
            return INDEXED.getOrDefault(commandID.getCode(), Collections.emptyMap()).get(subCmd);
        } catch (Exception ignore) {
            return null;
        }
    }

    public PiritCommand getCmd() {
        return cmd;
    }

    public int getSubCmd() {
        return subCmd;
    }

    @Override
    public String toString() {
        return String.format("%s(%02X/%d)", name(), cmd.getCode(), subCmd);
    }
}
