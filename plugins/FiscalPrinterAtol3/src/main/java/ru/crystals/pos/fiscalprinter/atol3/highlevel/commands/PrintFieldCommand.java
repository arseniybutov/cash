package ru.crystals.pos.fiscalprinter.atol3.highlevel.commands;

import java.io.IOException;
import java.io.OutputStream;

public class PrintFieldCommand extends CommandWithConfirmation {
    private static final int CODE = 0x87;

    // <Флаги (1)>
    private final int flags;

    // <Принтер (1)>
    private final int printer;

    // <Шрифты (1)>
    private final int font;

    // <Множители (1)>
    private final int multiplier;

    // <Межстрочие (1)>
    private final int interval;

    // <Яркость (1)>
    private final int brightness;

    // <РежимЧЛ (1)>
    private final int modeCheck;

    // <РежимКЛ (1)>
    private final int modeControl;

    // <Форматирование (1)>
    // Поле не используется, но для совместимости должно содержать ноль
    private final int format = 0;

    // <Резерв (2)>
    // Поле не используется, но для совместимости должно содержать ноль
    private final int reserved = 0;

    private final String text;

    public PrintFieldCommand(int flags, int printer, int font, int multiplier, int interval, int brightness, int modeCheck, int modeControl, String text) {
        super(CODE);
        this.flags = flags;
        this.printer = printer;
        this.font = font;
        this.multiplier = multiplier;
        this.interval = interval;
        this.brightness = brightness;
        this.modeCheck = modeCheck;
        this.modeControl = modeControl;
        this.text = text;
    }

    @Override
    void writeData(OutputStream stream) throws IOException {
        stream.write(flags);
        stream.write(printer);
        stream.write(font);
        stream.write(multiplier);
        stream.write(interval);
        stream.write(brightness);
        stream.write(modeCheck);
        stream.write(modeControl);
        stream.write(format);
        stream.write(reserved >> 8);
        stream.write(reserved & 0xFF);
        stream.write(encode(text));
    }
}
