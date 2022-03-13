package ru.crystals.pos.barcodeprocessing.searchinvalidators.config;

import ru.crystals.pos.barcodeprocessing.processors.config.BarcodeMask;
import ru.crystals.pos.barcodeprocessing.processors.config.BasicBarcodeProcessorConfig;

import java.util.List;

public class SearchInValidatorsProcessorConfig extends BasicBarcodeProcessorConfig {
    public SearchInValidatorsProcessorConfig(List<BarcodeMask> masks) {
        super(masks);
    }
}
