package ru.crystals.pos.fiscalprinter.atol3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Соответствие кодов оплат в ФР с кодами ФФД (Формат фискальных данных)
 * @author Tatarinov Eduard
 */
public enum CodePayment {
    CASH(0L, 1L, false),
    ELECTRONIC(1L, 2L, true),
    ADVANCE(13L, 3L, true),
    CREDIT(14L, 4L, true),
    CONSIDERATION(15L, 5L, true);

    private static Map<Long, CodePayment> codes = new HashMap<>();
    private static List<CodePayment> cashlessCodes = new ArrayList<>();

    /**
     * Код по ФФД
     */
    private final Long ffdCode;
    
    /**
     * Код в фискальном регистраторе
     */
    private final Long fiscalCode;
    
    /**
     * Безналичный тип оплаты
     */
    private final boolean cashless;
    
    static {
        Stream.of(CodePayment.values()).forEach(cd -> {
            codes.put(cd.getFfdCode(), cd);
            if (cd.isCashless()) {
                cashlessCodes.add(cd);
            }
        });
    }
    private CodePayment(Long ffdCode, Long fiscalCode, boolean cashless) {
        this.ffdCode = ffdCode;
        this.fiscalCode = fiscalCode;
        this.cashless = cashless;
    }
    
    public Long getFfdCode() {
        return ffdCode;
    }

    public Long getFiscalCode() {
        return fiscalCode;
    }

    public boolean isCashless() {
        return cashless;
    }

    /**
     * По1лучить список типов оплат по безналу
     * @return 
     */
    public static List<CodePayment> getCashlessCodes() {
        return cashlessCodes;
    }

    /**
     * Получить код оплаты в ФР по коду ФФД
     * @param ffdCode код оплаты по ФФД
     * @return код оплаты в ФР
     */
    public static Long getFiscalCodeByFfdCode(Long ffdCode) {
        CodePayment code = codes.get(ffdCode);
        if (code != null) {
            return code.getFiscalCode();
        }
        return CodePayment.ELECTRONIC.getFiscalCode();  // //Если ФФД код не определен, то считаем что это электронные
    }
    
}
