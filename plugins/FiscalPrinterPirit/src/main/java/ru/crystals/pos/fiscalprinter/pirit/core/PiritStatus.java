package ru.crystals.pos.fiscalprinter.pirit.core;

import ru.crystals.pos.fiscalprinter.transport.DataPacket;

import static ru.crystals.pos.utils.ByteUtils.hasBit;

public class PiritStatus {
    /**
     * Статус фатального состояния ККТ
     * Номер бита - Пояснение
     * 0 - Неверная контрольная сумма NVR
     * 1 - Неверная контрольная сумма в конфигурации
     * 2 - Нет связи с ФН
     * 3 - Зарезервировано
     * 4 - Зарезервировано
     * 5 - ККТ не авторизовано
     * 6 - Фатальная ошибка ФН
     * 7 - Зарезервировано
     */
    private final int fatalStateFlags;
    /**
     * Статус текущих флагов ККТ
     * Номер бита - Пояснение
     * 0 - Не выполнена команда “Начало работы”
     * 1 - Нефискальный режим
     * 2 - Смена открыта
     * 3 - Смена больше 24 часов
     * 4 - Архив ФН закрыт
     * 5 - ФН не зарегистрирован
     * 6 - Зарезервировано
     * 7 - Зарезервировано
     * 8 - Не было завершено закрытие смены, необходимо повторить операцию
     */
    private final int stateFlags;
    /**
     * Статус документа
     * Номер бита - Пояснение
     * 0..3 - Тип текущего открытого документа (см. ниже)
     * 4..7 - Состояние документа (см. ниже)
     * <p>
     * Тип текущего открытого документа
     * 0 - документ закрыт
     * 1 - сервисный документ
     * 2 - чек на продажу (приход)
     * 3 - чек на возврат (возврат прихода)
     * 4 - внесение в кассу
     * 5 - инкассация
     * 6 - чек на покупку (расход)
     * 7 - чек на возврат покупки (возврат расхода)
     * 9 - чек корр. приход
     * 10 - чек корр. расход
     * 11 - чек корр. возврат прихода
     * 12 - чек корр. возврат расхода
     * <p>
     * Состояние документа
     * 0 - документ закрыт
     * 1 - устанавливается после команды "открыть документ" (Для типов документа 2 и 3 можно добавлять товарные позиции)
     * 2 - Устанавливается после первой команды "Подытог"
     * 3 - Устанавливается после второй команды "Подытог" или после начала команды "Оплата" (Можно только производить оплату различными типами платежных средств)
     * 4 - Расчет завершен, требуется закрыть документ
     * 8 - Команда закрытия документа была дана в ФН, но документ не был завершен. Аннулирование документа невозможно
     */
    private final int documentStateFlags;

    public PiritStatus(DataPacket dp) {
        this(dp.getIntegerSafe(0).orElse(0),
                dp.getIntegerSafe(1).orElse(0),
                dp.getIntegerSafe(2).orElse(0));
    }

    public PiritStatus(int fatalStateFlags, int stateFlags, int documentStateFlags) {
        this.fatalStateFlags = fatalStateFlags;
        this.stateFlags = stateFlags;
        this.documentStateFlags = documentStateFlags;
    }

    public boolean needToStartWork() {
        return hasBit(stateFlags, 0);
    }

    public boolean isAuthorized() {
        return !hasBit(fatalStateFlags, 5);
    }

    public boolean isFiscalMode() {
        return !hasBit(stateFlags, 1);
    }

    public boolean isShiftOpened() {
        return hasBit(stateFlags, 2);
    }

    public boolean isFnClosed() {
        return hasBit(stateFlags, 4);
    }

    /**
     * Команда закрытия документа была дана в ФН, но документ не был завершен. Аннулирование документа невозможно
     *
     * Нужно послать команду "Закрыть документ повторно"
     */
    public boolean docNeedToBeClosed() {
        return hasBit(documentStateFlags, 7);
    }

    public boolean isDocOpened() {
        return (documentStateFlags & 0x1F) != 0;
    }

}
