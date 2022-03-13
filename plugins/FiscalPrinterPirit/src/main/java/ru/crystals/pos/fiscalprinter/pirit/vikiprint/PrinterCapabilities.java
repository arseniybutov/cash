package ru.crystals.pos.fiscalprinter.pirit.vikiprint;

public class PrinterCapabilities {
    private boolean fiscalDevice;
    private boolean ofdDevice = false;
    private int maxQRCodeLength = 255;

    private PrinterCapabilities() {
    }

    public static PrinterCapabilities fiscal() {
        PrinterCapabilities result = new PrinterCapabilities();
        result.fiscalDevice = true;
        return result;
    }

    public static PrinterCapabilities nonFiscal() {
        PrinterCapabilities result = new PrinterCapabilities();
        result.fiscalDevice = false;
        return result;
    }

    public PrinterCapabilities ofd() {
        this.ofdDevice = true;
        return this;
    }

    public PrinterCapabilities withMaxQRCode(int maxQRCodeLength) {
        this.maxQRCodeLength = maxQRCodeLength;
        return this;
    }

    public boolean isOfdDevice() { return ofdDevice; }
    public boolean isFiscalDevice() {
        return fiscalDevice;
    }

    public int getMaxQRCodeLength() {
        return maxQRCodeLength;
    }
}
