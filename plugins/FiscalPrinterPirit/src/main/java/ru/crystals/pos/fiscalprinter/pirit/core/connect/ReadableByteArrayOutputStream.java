package ru.crystals.pos.fiscalprinter.pirit.core.connect;

import java.io.ByteArrayOutputStream;

/**
 * Класс добавляет к ByteArrayOutputStream возможность читать байт по индексу из текущего массива
 * 
 * @author dalex
 */
public class ReadableByteArrayOutputStream extends ByteArrayOutputStream {

    /**
     * Прочитать байт по индексу
     * @param index
     * @return 
     */
    public int valueAt(int index) {
        return buf[index];
    }
}
