package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: "Получить данные последнего документа ФН"
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link FiscalDocumentData данные последнего документа ФН}.
 *
 * @author m.magomedov
 */
public class GetLastDocInfoCommand extends BaseCommand<FiscalDocumentData> {

    private byte[] docNumber;

    /**
     * Единственно правильный конструктор.
     *
     * @param password пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetLastDocInfoCommand(long docNumber, int password) {
        super(password);
        this.docNumber = toByteArray(docNumber);
    }

    public byte[] toByteArray(long docNumber) {
        byte[] newNumber = new byte[4];
        newNumber[0] = (byte) docNumber;
        newNumber[1] = (byte) (docNumber >>> 8 & 0xFF);
        newNumber[2] = (byte) (docNumber >>> 16 & 0xFF);
        newNumber[3] = (byte) (docNumber >>> 24 & 0xFF);
        return newNumber;
    }

    @Override
    public byte getCommandPrefix() {
        return (byte) 0xFF;
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0x0A;
    }

    @Override
    public byte[] getArguments() {
        byte[] result = new byte[8];
        System.arraycopy(password, 0, result, 0, 4);
        System.arraycopy(docNumber, 0, result, 4, 4);
        return result;
    }

    @Override
    public FiscalDocumentData decodeResponse(byte[] response) {
        if(!validateResponse(response)) {
            return null;
        }
        FiscalDocumentData fiscalDocumentData = new FiscalDocumentData();
        byte[] summaOfDocument = getBytes(response, 5, 25);
        byte typeDocument = response[20];
        byte[] numberFD = getBytes(response, 4, 15);
        fiscalDocumentData.setSum(ShtrihUtils.getLong(summaOfDocument));
        fiscalDocumentData.setNumFD(ShtrihUtils.getLong(numberFD));
        fiscalDocumentData.setType(typeDocument == 1 ? FiscalDocumentType.SALE : FiscalDocumentType.REFUND);
        return fiscalDocumentData;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        // длина ответа должна быть 27 байт (вместе со служебными символами)
        boolean checkTypeDocument = response[5] == 3 && (response[20] == 1 || response[20] == 2);
        return super.validateResponse(response) && response.length == 27 && checkTypeDocument;
    }

    private byte[] getBytes(byte[] response, int length, int from) {
        byte[] result = new byte[length];
        for(int i = 0; i < result.length; i++)
            result[i] = response[from - i];
        return result;
    }
}
