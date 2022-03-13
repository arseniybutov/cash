package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

/**
 * Created by Tatarinov Eduard on 11.01.17.
 */

import java.util.Arrays;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.auxiliary.ShtrihFieldType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.FieldStructure;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Запрос структуры поля ".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link FieldStructure структура поля}.
 *
 * @author aperevozchikov
 */
public class GetFieldStructureCommand extends BaseCommand<FieldStructure> {

    /**
     * Номер таблицы, структуру поля из которой хотим узнать
     */
    private byte tableNo;

    /**
     * номер поля в таблице {@link #tableNo} структуру которого хотим узнать
     */
    private byte fieldNo;

    /**
     * Единственно правильный конструктор.
     *
     * @param tableNo
     *            номер таблицы, структуру поля из которой хотим узнать
     * @param fieldNo
     *            номер поля в таблице <code>tableNo</code> структуру которого хотим узнать
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetFieldStructureCommand(byte tableNo, byte fieldNo, int password) {
        super(password);
        this.tableNo = tableNo;
        this.fieldNo = fieldNo;
    }

    @Override
    public String toString() {
        return String.format("get-field-struct-cmd [tableNo: %s; fieldNo: %s]", tableNo, fieldNo);
    }

    @Override
    public byte getCommandCode() {
        return 0x2E;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 6 байт
        byte[] result = new byte[6];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        // Номер таблицы
        result[4] = tableNo;

        // Номер поля
        result[5] = fieldNo;

        return result;
    }

    @Override
    public FieldStructure decodeResponse(byte[] response) {
        FieldStructure result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new FieldStructure();

        // Название поля: байты с 5го по 44й:
        result.setFieldName(getString(Arrays.copyOfRange(response, 4, 44)));

        // тип поля: 45й байт:
        ShtrihFieldType fieldType = ShtrihFieldType.getTypeByCode(response[44]);
        result.setFieldType(fieldType);

        // ширина поля: 46й байт:
        int fieldWidth = response[45];
        result.setFieldWidth(response[45]);

        // мин значение: байты с 47го по 47 + fieldWidth - 1:
        result.setMinValue(ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 46, 46 + fieldWidth))));

        // макс значение: байты с 47 + fieldWidth по 47 + fieldWidth + fieldWidth - 1:
        result.setMaxValue(ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 46 + fieldWidth, 46 + fieldWidth + fieldWidth))));

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть минимум 46 байт (вместе со служебными символами)
        if (response.length < 46) {
            return false;
        }

        int fieldWidth = response[45];
        //  Длина ответа должна быть 44 + filedWidth + filedWidth байт (+ 3 байта служебных символов):
        if (response.length != 3 + 44 + fieldWidth + fieldWidth) {
            return false;
        }

        // тип поля: 45й байт: должен быть либо 0, либо 1:
        ShtrihFieldType fieldType = ShtrihFieldType.getTypeByCode(response[44]);
        if (fieldType == null) {
            return false;
        }

        return true;
    }

}