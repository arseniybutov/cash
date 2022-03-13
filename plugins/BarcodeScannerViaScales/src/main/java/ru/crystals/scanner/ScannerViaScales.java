package ru.crystals.scanner;

import ru.crystals.pos.scale.Scale;

public interface ScannerViaScales {

    void fireBarcodeScannerEvent(String barcode);

    void setScale(Scale scale);

    Scale getScale();
}
