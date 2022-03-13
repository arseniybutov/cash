package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.TableStructure;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Запрос структуры таблицы".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link TableStructure структура таблицы}.
 * 
 * @author aperevozchikov
 */
public class GetTableStructureCommand extends BaseCommand<TableStructure> {

    /**
     * номер таблицы, структуру которой хотим получить
     */
    private byte tableNo;

    /**
     * Единственно правильный конструктор.
     * 
     * @param tableNo
     *            номер таблицы, структуру которой хотим получить
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetTableStructureCommand(byte tableNo, int password) {
        super(password);
        this.tableNo = tableNo;
    }

    @Override
    public String toString() {
        return String.format("get-table-struct-cmd [tableNo: %s]", tableNo);
    }
    
    @Override
    public byte getCommandCode() {
        return 0x2D;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 5 байт
        byte[] result = new byte[5];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);
        
        // Номер таблицы
        result[4] = tableNo;

        return result;
    }

    @Override
    public TableStructure decodeResponse(byte[] response) {
        TableStructure result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new TableStructure();
        
        // Название таблицы: байты с 5го по 44й:
        result.setTableName(getString(Arrays.copyOfRange(response, 4, 44)));
        
        // количество рядов: байты: 45й (мл) и 46й (ст):
        result.setRowsCount(ShtrihUtils.getInt(response[44], response[45]));
        
        // количество полей - байт 47й:
        result.setFieldsCount(ShtrihUtils.getInt(response[46], (byte) 0));
        
        return result;
    }
    
    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 48 байт (вместе со служебными символами)
        if (response.length != 48) {
            return false;
        }
        
        return true;
    }
}
