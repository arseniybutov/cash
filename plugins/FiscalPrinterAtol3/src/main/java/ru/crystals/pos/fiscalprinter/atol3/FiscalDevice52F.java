package ru.crystals.pos.fiscalprinter.atol3;

public class FiscalDevice52F extends FiscalDevice {

    public FiscalDevice52F() {
        name = "АТОЛ 52Ф";
    }


    public int getMaxLengthField(MaxLengthField maxLen) {
        int maxTextLength = 36;
        int maxSettingLength = 48;
        int maxPaymentLength = 64;
        if (maxLen == MaxLengthField.DEFAULTTEXT) {
            return maxTextLength;
        } else if (maxLen == MaxLengthField.PAYMENTNAME) {
            return maxPaymentLength;
        } else {
            return maxSettingLength;
        }
    }
}