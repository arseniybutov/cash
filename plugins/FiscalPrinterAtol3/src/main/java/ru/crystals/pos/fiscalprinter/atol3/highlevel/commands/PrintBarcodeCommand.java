package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class PrintBarcodeCommand extends Command {
    private static final int CODE = 0xC1;

    //Тип штрихкода (1)>
    private final int type;

    //<Выравнивание (1)>
    private final int alignment;

    //<Ширина (1)>
    private final int width;

    //<Версия (2)>
    private final int version;

    //<Опции (2)>
    private final int option;

    //<Уровень коррекции (1)>
    private final int correction;

    //<Количество строк (1)>
    // Для QR-кода поле не используется и должно содержать 0
    private final int rowsNumber = 0;

    //<Количество столбцов (1)>
    // Для QR-кода поле не используется и должно содержать 0
    private final int colsNumber = 0;

    //<Пропорции штрихкода (2)>
    // Для QR-кода поле не используется и должно содержать 0
    private final int barcodeProportion = 0;

    //<Пропорции пикселя (2)>
    // Для QR-кода поле не используется и должно содержать 0
    private final int pixelProportion = 0;

    //<Строка данных 1 (100 для QR-кода)(13 для EAN-13)>
    private final String text;

    public PrintBarcodeCommand(int type, int alignment, int width, int version, int option, int correction, String text) {
        super(CODE);
        this.type = type;
        this.alignment = alignment;
        this.width = width;
        this.version = version;
        this.option = option;
        this.correction = correction;
        this.text = text;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(type);
        stream.write(alignment);
        stream.write(width);
        stream.write(encode(version, 2));
        stream.write(encode(option, 2));
        stream.write(correction);
        stream.write(rowsNumber);
        stream.write(colsNumber);
        stream.write(encode(barcodeProportion, 2));
        stream.write(encode(pixelProportion, 2));
        stream.write(encode(text));
    }
}
