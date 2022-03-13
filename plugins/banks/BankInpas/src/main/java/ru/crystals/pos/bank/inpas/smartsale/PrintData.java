package ru.crystals.pos.bank.inpas.smartsale;

public class PrintData {
    private PrintDataTags tag = PrintDataTags.UNSUPPORTED;
    private String name = null;
    private String value = null;

    public PrintDataTags getTag() {
        return tag;
    }

    public void setTag(PrintDataTags tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public enum PrintDataTags {
        CARD_TYPE_TAG("0xDE"),
        AID_TAG("0x4F"),
        SECOND_SLIP_TAG("0xDA"),
        FIRST_SLIP_TAG("0xDF"),
        UNSUPPORTED("0x00"),
        ;
        private String tag;

        private PrintDataTags(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }

        public static PrintDataTags getPrintDataTagsByTag(String strTag) {
            for (PrintDataTags tag : PrintDataTags.values()) {
                if (tag.getTag().equals(strTag)) {
                    return tag;
                }
            }
            return UNSUPPORTED;
        }

    }

}
