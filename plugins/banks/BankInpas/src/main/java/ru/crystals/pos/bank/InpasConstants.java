package ru.crystals.pos.bank;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public final class InpasConstants {

    public static final long SALE = 1;
    public static final long REFUND = 29;
    public static final long REVERSAL = 4;
    public static final long DAILY_LOG = 59;
    public static final long CHECK_STATE = 26;
    public static final long EXECUTE_USER_COMMAND = 63L;
    /**
     * Неописанный в документации код, приходит в процессе оплаты когда терминал меняет свое состояние
     */
    public static final Long USER_INPUT_CODE = 52L;

    /**
     * Коды ответа, для которых есть соответствующие текстовые расшифровки в
     * файле локализации
     */
    public static final Map<Integer, String> MESSAGES_FOR_RESULT_CODES =
            new ImmutableMap.Builder<Integer, String>()
                    .put(0, "ECD_0")
                    .put(1, "ECD_1")
                    .put(16, "ECD_16")
                    .put(17, "ECD_17")
                    .put(34, "ECD_34")
                    .put(54, "ECD_54")
                    .build();

    private InpasConstants() {
    }
}
