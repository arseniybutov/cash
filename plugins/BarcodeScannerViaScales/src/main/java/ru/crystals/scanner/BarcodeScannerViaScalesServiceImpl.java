package ru.crystals.scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.HardwareModule;
import ru.crystals.pos.barcodescanner.BarcodeScanner;
import ru.crystals.pos.barcodescanner.BarcodeScannerBeeper;
import ru.crystals.pos.barcodescanner.BarcodeScannerEvent;
import ru.crystals.pos.barcodescanner.ExtendedScaleScannerFunctions;
import ru.crystals.pos.scale.Scale;

import java.io.IOException;

public class BarcodeScannerViaScalesServiceImpl implements HardwareModule, ScannerViaScales, ExtendedScaleScannerFunctions {
    private static final Logger LOG = LoggerFactory.getLogger(BarcodeScanner.class);
    private BarcodeScannerEvent barcodeScannerListener;
    private Scale scale;

    @Override
    public void start() {
        LOG.debug("start()");
    }

    @Override
    public void stop() {
        LOG.debug("stop()");
    }

    @Override
    public void fireBarcodeScannerEvent(String barcode) {
        LOG.debug("fireBarcodeScannerEvent(" + barcode + "); barcodeScannerListener " + (barcodeScannerListener != null ? "!=" : "=") + " null");
        if (barcodeScannerListener != null) {
            LOG.debug("barcodeScannerListener.eventBarcodeScanner(" + barcode + ")");
            barcodeScannerListener.eventBarcodeScanner(barcode);
        }
    }

    public void setBarcodeScannerListener(BarcodeScannerEvent barcodeScannerListener) {
        this.barcodeScannerListener = barcodeScannerListener;
    }

    public BarcodeScannerEvent getBarcodeScannerListener() {
        return barcodeScannerListener;
    }

    @Override
    public void goodScanBeep() {
        if (scale instanceof BarcodeScannerBeeper) {
            ((BarcodeScannerBeeper) scale).goodScanBeep();
        }
    }

    @Override
    public void errorBeep() {
        if (scale instanceof BarcodeScannerBeeper) {
            ((BarcodeScannerBeeper) scale).errorBeep();
        }
    }

    @Override
    public void scannerTurnOn() throws IOException {
        if (scale instanceof ExtendedScaleScannerFunctions) {
            ((ExtendedScaleScannerFunctions) scale).scannerTurnOn();
        }
    }

    @Override
    public void tare() throws IOException {
        if (scale instanceof ExtendedScaleScannerFunctions) {
            ((ExtendedScaleScannerFunctions) scale).tare();
        }
    }

    @Override
    public void scaleSoftReset() throws IOException {
        if (scale instanceof ExtendedScaleScannerFunctions) {
            ((ExtendedScaleScannerFunctions) scale).scaleSoftReset();
        }
    }

    @Override
    public void setScale(Scale scale) {
        this.scale = scale;
    }

    @Override
    public Scale getScale() {
        return scale;
    }

}
