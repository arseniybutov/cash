package ru.crystals.pos.barcodeprocessing.markdown.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import ru.crystals.pos.barcodeprocessing.processors.config.BarcodeMask;
import ru.crystals.pos.barcodeprocessing.processors.config.BasicBarcodeProcessorConfig;
import ru.crystals.pos.barcodeprocessing.processors.mask.PreparedBarcodeMask;

/**
 * Класс описывающий JSON-конфиг плагина
 * <p>
 * Пожалуйста, не запихивайте в него и в его составные части логику (см. {@link PreparedBarcodeMask}
 */
public class MarkdownBarcodeConfig extends BasicBarcodeProcessorConfig {

    @JsonCreator
    public MarkdownBarcodeConfig(@JsonProperty("masks") List<BarcodeMask> masks) {
        super(masks);
    }

}
