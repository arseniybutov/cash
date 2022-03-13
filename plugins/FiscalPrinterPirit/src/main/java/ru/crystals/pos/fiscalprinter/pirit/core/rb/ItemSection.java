package ru.crystals.pos.fiscalprinter.pirit.core.rb;

public class ItemSection extends ReportDataSection {

    private static final long DIVIDER_QUANT = 10L;

    private int correctionFlag;    // 1   Признак коррекции
    private long price;            // 8   Цена
    private long quantity;         // 8   Количество
    private long sum;              // 8   Сумма
    private String dimension;      // 5   Мерность
    private String positionNumber; // 5   Номер позиции
    private String article;        // 19  Артикул
    private String item;           // 57  Наименование
    private int sectionNumber;     // 2   Номер секции
    private int taxNumber;         // 2   Номер налога в таблице
    protected static final int SIZE = 117;


    public ItemSection(byte[] data) {
        super(data);
        this.correctionFlag = getByte(data[1]);
        this.price = getLong8Bytes(data, 2);
        this.quantity = getLong8Bytes(data, 10, DIVIDER_QUANT);
        this.sum = getLong8Bytes(data, 18);
        this.dimension = getStringBytes(data, 26, 5);
        this.positionNumber = getStringBytes(data, 31, 5);
        this.article = getStringBytes(data, 36, 19);
        this.item = getStringBytes(data, 55, 57);
        this.sectionNumber = getByte(data[113]) * 256 + getByte(data[112]);
        this.taxNumber = getByte(data[115]) * 256 + getByte(data[114]);
    }

    @Override
    public int getSize() {
        return SIZE;
    }

    public int getCorrectionFlag() {
        return correctionFlag;
    }

    public long getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public long getSum() {
        return sum;
    }

    public String getPositionNumber() {
        return positionNumber;
    }

    public String getArticle() {
        return article;
    }

    public String getItem() {
        return item;
    }

    public int getSectionNumber() {
        return sectionNumber;
    }

    public int getTaxNumber() {
        return taxNumber;
    }

    public String getDimension() {
        return dimension;
    }
}
