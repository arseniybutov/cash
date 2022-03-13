package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Перечисление возможных режимов/состояний, в которых могут находиться ФР семейства "Штрих".
 * 
 * @author aperevozchikov
 */
public enum ShtrihModeEnum {

    /**
     * Принтер в рабочем режиме
     */
    IN_WORK((byte) 0),

    /**
     * Выдача данных
     */
    RESPONDING((byte) 1),

    /**
     * Открытая смена, 24 часа не кончились
     */
    SHIFT_IS_OPEN_FOR_LESS_THAN_24H((byte) 2),

    /**
     * Открытая смена, 24 часа кончились
     */
    SHIFT_IS_OPEN_FOR_MORE_THAN_24H((byte) 3),

    /**
     * Закрытая смена
     */
    SHIFT_IS_CLOSED((byte) 4),

    /**
     * Блокировка по неправильному паролю налогового инспектора
     */
    BLOCKED_CAUSE_OF_WRON_TAX_PASSWORD((byte) 5),

    /**
     * Ожидание подтверждения ввода даты
     */
    WAITING_FOR_DATE_CONFIRMATION((byte) 6),

    /**
     * Разрешение изменения положения десятичной точки
     */
    DECIMAL_SEPARATOR_CHANGE_ALLOWED((byte) 7),

    /**
     * Открытый документ
     */
    DOCUMENT_IS_OPEN((byte) 8),

    /**
     * Режим разрешения технологического обнуления. В этот режим ККМ переходит по включению питания, если некорректна информация в энергонезависимом
     * ОЗУ ККМ.
     */
    CLEAN_UP_ALLOWED((byte) 9),

    /**
     * Тестовый прогон
     */
    TEST((byte) 10),

    /**
     * Печать полного фис. отчета
     */
    PRINTING_FULL_REPORT((byte) 11),

    /**
     * Печать отчёта ЭКЛЗ
     */
    PRINTING_EKLZ_REPORT((byte) 12),

    /**
     * Работа с фискальным подкладным документом
     */
    PROCESSING_FISCAL_DOCUMENT((byte) 13),

    /**
     * Печать подкладного документа
     */
    PRINTING_FISCAL_DOCUMENT((byte) 14),

    /**
     * Фискальный подкладной документ сформирован
     */
    FISCAL_DOCUMENT_IS_READY((byte) 15);

    ShtrihModeEnum(byte code) {
        this.code = code;
    }

    /**
     * Код состояния/режима
     */
    private byte code;

    public byte getCode() {
        return code;
    }
    
    /**
     * Вернет enum по его коду
     * 
     * @param code {@link #getCode() код} enum'а, что надо вернуть
     * @return <code>null</code>, если нету enum'а с таким кодом
     */
    public static ShtrihModeEnum getByCode(byte code) {
        for (ShtrihModeEnum s : ShtrihModeEnum.values()) {
            if (code == s.getCode()) {
                return s;
            }
        }
        return null;
    }

    /**
     * Вернет <code>true</code>, если указанный режим сигнализирует о том, что идет печать какого-то документа.
     * 
     * @param mode
     *            режим, что надо проверить
     * @return <code>false</code>, если аргумент <code>null</code>
     */
    public static boolean isPrinting(ShtrihModeEnum mode) {
        if (mode == null) {
            return false;
        }
        
        // либо идет печать отчета ЭКЛЗ, либо фискально отчета, либо подкладного документа:
        if (PRINTING_FULL_REPORT.equals(mode)) {
            return true;
        }
        if (PRINTING_EKLZ_REPORT.equals(mode)) {
            return true;
        }
        if (PRINTING_FISCAL_DOCUMENT.equals(mode)) {
            return true;
        }
        
        // по умолчанию ничего не печататется
        return false;
    }
    
    /**
     * Вернет <code>true</code>, если указанный режим позволяет закрыть смену.
     * 
     * @param mode
     *            режим, что надо проверить
     * @return <code>false</code>, если аргумент <code>null</code>
     */
    public static boolean canCloseShift(ShtrihModeEnum mode) {
        if (mode == null) {
            return false;
        }
        
        // если смена открыта, то ее можно закрыть
        if (SHIFT_IS_OPEN_FOR_LESS_THAN_24H.equals(mode) || SHIFT_IS_OPEN_FOR_MORE_THAN_24H.equals(mode)) {
            return true;
        }
        
        return false;
    }
}