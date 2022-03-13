package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class BeginPositionCommand extends Command {

    /**
     * EAh - Начать формирование позиции
     */
    private static final int CODE = 0xEA;

    /**
     * Флаги. Проверяется только младший бит: 0 – выполнить операцию, 1 – режим проверки операции
     */
    private final int flag = 0;
    /**
     * Параметр. Всегда должен быть равен 1.
     */
    private final int parameter = 1;
    /**
     * Резерв. Поле не используется и должно содержать 0
     */
    private final int reserved = 0;

    public BeginPositionCommand() {
        super(CODE);
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flag);
        stream.write(parameter);
        stream.write(reserved);
    }
}
