package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihItemCode;

/**
 * Команда: "Передать КТН". Отправляет тег 1162 (Код товарной номенклатуры), привязанный к операции. Метод должен
 * вызываться только после команды RegOperationCommand.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 */
public class FNSendItemCodeCommand extends BaseCommand<Object> {

    /**
     * Тег ФФД для реквизита "Код товарной номенклатуры" - 1162
     */
    private static final byte[] ITEM_CODE_TAG = new byte[]{(byte) 0x8A, (byte) 0x04};

    /**
     * Код товарной номенклатуры, состоит из маркировки, GTIN и серийного номера
     */
    private ShtrihItemCode itemCode;

    /**
     * Единственно правильный конструктор.
     *
     * @param itemCode код товарной номенклатуры
     * @param password пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public FNSendItemCodeCommand(ShtrihItemCode itemCode, int password) {
        super(password);

        if (itemCode == null) {
            throw new NullPointerException("FNSendItemCodeCommand(ShtrihItemCode): The argument is NULL!");
        }

        this.itemCode = itemCode;
    }

    @Override
    public String toString() {
        return String.format("send-item-code-cmd");
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x4D;
    }

    @Override
    public byte[] getArguments() {
        byte[] serialData = itemCode.getSerialData();
        byte[] marking = itemCode.getMarking();
        byte[] gtin = itemCode.getGtin();

        // 4 байта - пароль, 4 байта -заголовок TLV(тег+длинна), данные
        byte[] result = new byte[4 + 4 + marking.length + gtin.length + serialData.length];

        // пароль оператора (4 байта)
        int resByteCount = 4;
        System.arraycopy(password, 0, result, 0, resByteCount);
        // добавляем тег 1162
        for (byte code : ITEM_CODE_TAG) {
            result[resByteCount++] = code;
        }
        // добавляем длину КТН
        result[resByteCount++] = (byte) (marking.length + gtin.length + serialData.length);
        result[resByteCount++] = (byte) 0x00;
        // добовляем данные КТН - маркировку, GTIN, серийый номер
        System.arraycopy(marking, 0, result, resByteCount, marking.length);
        resByteCount += marking.length;
        System.arraycopy(gtin, 0, result, resByteCount, gtin.length);
        resByteCount += gtin.length;
        System.arraycopy(serialData, 0, result, resByteCount, serialData.length);

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }
}