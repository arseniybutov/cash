package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

/**
 * Содержимое ШК. В документации не описано, реализуем по аналогии с {@link QrCodeDocumentEntry}
 */
public class BarcodeDocumentEntry extends CommonDocumentEntry {
    public static final String TYPE_NAME = DTO_PREFIX + "BarcodeDocumentEntry";
}
