package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.highlevel.types.ValueDecoder;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public class Value extends Result {
    // 55h
    public final byte[] data;

    private final int valueOffset;

    public Value(Response response) {
        super(response);

        data = response.getData();
        valueOffset = response.getDataOffset() + 2;
    }

    public <T, D extends ValueDecoder<T>> T get(D decoder) {
        return get(decoder, 0, data.length - valueOffset);
    }

    /**
     * Получить значение с помощью выбранного декодера
     *
     * @param decoder требуемый декодер
     * @param index индекс внутри составного значения (например, регистр содержит несколько разных значений)
     * @param length длина в байтах
     * @param <T> тип декодируемого значения
     * @param <D> тип соответствующего декодера
     * @return значение требуемого типа
     */
    public <T, D extends ValueDecoder<T>> T get(D decoder, int index, int length) {
        return decoder.decode(data, valueOffset + index, length);
    }
}
