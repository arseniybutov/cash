package ru.crystals.pos.fiscalprinter.pirit.core.rb;

import java.util.Arrays;

public class OpenSection extends ReportDataSection {
    protected static final int SIZE = 48;
    private int typeDoc;        // 1  Тип документа
    private int operatorNumber; // 2  Номер оператора
    private int departNumber;   // 2  Номер отдела
    private int checkNumber;    // 4  Номер чека
    private int shiftNumber;    // 2  Номер смены
    private int docNumber;      // 4  Номер документа
    private DateSection date;        // 7  Дата Структура  типа “Data”
    private String operatorName;// 24 ФИО оператора

    public OpenSection(byte[] data) {
        super(data);
        this.typeDoc = getByte(data[1]);
        this.operatorNumber = getByte(data[3]) * 256 + getByte(data[2]);
        this.departNumber = getByte(data[5]) * 256 + getByte(data[4]);
        this.checkNumber = ((getByte(data[9]) * 256 + getByte(data[8])) * 256 + getByte(data[7])) * 256 + getByte(data[6]);
        this.shiftNumber = getByte(data[11]) * 256 + getByte(data[10]);
        this.docNumber = ((getByte(data[15]) * 256 + getByte(data[14])) * 256 + getByte(data[13])) * 256 + getByte(data[12]);
        this.date = new DateSection(Arrays.copyOfRange(data, 16, 16 + DateSection.SIZE));
        this.operatorName = getStringBytes(data, 23, 24);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public int getTypeDoc() {
        return typeDoc;
    }

    public void setTypeDoc(int typeDoc) {
        this.typeDoc = typeDoc;
    }

    public int getOperatorNumber() {
        return operatorNumber;
    }

    public void setOperatorNumber(int operatorNumber) {
        this.operatorNumber = operatorNumber;
    }

    public int getDepartNumber() {
        return departNumber;
    }

    public void setDepartNumber(int departNumber) {
        this.departNumber = departNumber;
    }

    public int getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(int checkNumber) {
        this.checkNumber = checkNumber;
    }

    public int getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public int getDocNumber() {
        return docNumber;
    }

    public void setDocNumber(int docNumber) {
        this.docNumber = docNumber;
    }

    public DateSection getDate() {
        return date;
    }

    public void setDate(DateSection date) {
        this.date = date;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }
}
