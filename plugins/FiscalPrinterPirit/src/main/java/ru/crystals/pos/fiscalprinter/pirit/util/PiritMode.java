package ru.crystals.pos.fiscalprinter.pirit.util;

import ru.crystals.pos.fiscalprinter.datastruct.documents.AgentType;

/**
 * Класс который хранит маску режима работ прочитанную из Пирита,
 * и константы которые хранят необходимый флаг режима работы.
 */
public class PiritMode {
    /**
     * Маска прочитанная из пирита
     * <p>
     * Биты:
     * 0 - Шифрование
     * 1 - Автономный режим
     * 2 - Автоматический режим
     * 3 - применение в сфере услуг
     * 4 - печать БСО вместо чека
     * 5 - Применение в интернете
     * 6 - Продажа подакцизного товара
     * 7 - Проведение азартных игр
     * 8 - Применение банковскими платежными агентами
     * 9 - Применение банковскими платежными субагентами
     * 10 - Применение платежными агентами
     * 11 - Применение платежными субагентами
     * 12 - Применение поверенными
     * 13 - Применение коммисионерами
     * 14  - Применение агентами
     * 15 - Проведение лотереи
     */
    private final long modeMask;

    public PiritMode(long modeMask) {
        this.modeMask = modeMask;
    }

    /**
     * Проверяет включен ли режим по маске
     *
     * @param modeType режим работы который необходимо проверить
     * @return true если включен, false если выключен
     */
    public boolean isModeAvailable(int modeType) {
        return (modeMask & modeType) != 0;
    }

    /**
     * Проверяем доступна ли работа с переданным агентом
     *
     * @param agentType тип агента
     * @return true если доступна, false если не доступна.
     */
    public boolean isAgentAvailable(AgentType agentType) {
        return isModeAvailable(agentType.getWorkModeBitMask());
    }
}
