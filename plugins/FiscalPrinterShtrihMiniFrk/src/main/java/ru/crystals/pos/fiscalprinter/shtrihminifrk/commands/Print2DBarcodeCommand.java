package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда "Печать многомерного штрих-кода".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответ} от данной команды не имеет объектного представления (всегда будет <code>null</code>).
 * 
 * @author aperevozchikov
 */
public class Print2DBarcodeCommand extends BaseCommand<Object> {

    /**
     * Тип ШК, что собираемся печатать
     */
    private Shtrih2DBarcodeType barcodeType;

    /**
     * длина данных ШК
     */
    private int barcodeDataLength;

    /**
     * Номер начального блока данных (предварительно загруженных командами {@link LoadDataCommand}) для этого ШК.
     */
    private int firstDataChunkNo;

    /**
     * Параметры команды (строго 5 байт)
     */
    private byte[] parameters;

    /**
     * Тип выравнивания ШК, что собираемся печатать
     */
    private Shtrih2DBarcodeAlignment alignment;

    /**
     * Единственно правильный конструктор.
     * 
     * @param barcodeType
     *            Тип ШК, что собираемся печатать
     * @param barcodeDataLength
     *            длина данных ШК
     * @param firstDataChunkNo
     *            Номер начального блока данных (предварительно загруженных командами {@link LoadDataCommand}) для этого ШК
     * @param parameters
     *            Параметры команды (строго 5 байт)
     * @param alignment
     *            Тип выравнивания ШК, что собираемся печатать; <code>null</code> распознается как {@link Shtrih2DBarcodeAlignment#CENTER}
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     * @throws IllegalArgumentException
     *             если <code>barcodeType</code> == <code>null</code>, либо размер <code>parameters</code> != <code>5</code>
     */
    public Print2DBarcodeCommand(Shtrih2DBarcodeType barcodeType, int barcodeDataLength, int firstDataChunkNo, byte[] parameters,
                                 Shtrih2DBarcodeAlignment alignment, int password) {
        super(password);

        if (barcodeType == null) {
            throw new IllegalArgumentException("Print2DBarcodeCommand: the \"barcodeType\" argument is NULL!");
        }
        if (parameters == null || parameters.length != 5) {
            throw new IllegalArgumentException(String.format("Print2DBarcodeCommand: the \"parameters\" argument is INVALID (length: %s)!",
                parameters == null ? "(NULL)" : parameters.length));
        }

        this.barcodeType = barcodeType;
        this.barcodeDataLength = barcodeDataLength;
        this.firstDataChunkNo = firstDataChunkNo;
        this.parameters = parameters;
        this.alignment = alignment == null ? Shtrih2DBarcodeAlignment.CENTER : alignment;
    }

    @Override
    public String toString() {
        return String.format("print-2d-barcode-cmd [type: %s; data-length: %s; first-chunk-no: %s; params: %s; alignment: %s]", barcodeType,
            barcodeDataLength, firstDataChunkNo, PortAdapterUtils.arrayToString(parameters), alignment);
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xDE;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 14 байт
        byte[] result = new byte[14];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Тип штрих-кода (1 байт)
        result[4] = barcodeType.getCode();

        // Длина данных штрих-кода (2 байта)
        result[5] = (byte) barcodeDataLength; // мл
        result[6] = (byte) (barcodeDataLength >>> 8); // ст

        // Номер начального блока данных (1байт)
        result[7] = (byte) firstDataChunkNo;

        // 5 параметров по 1 байту
        System.arraycopy(parameters, 0, result, 8, parameters.length);

        // выравнивание
        result[result.length - 1] = alignment.getCode();

        return result;
    }

    @Override
    public Object decodeResponse(byte[] response) {
        return null;
    }

}
