package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.dto.documententry;

import static ru.crystals.pos.fiscalprinter.nfd.transport.deserializers.ResponseDeserializerUtils.DTO_PREFIX;

/**
 * Содержимое QR-кода.
 */
public class QrCodeDocumentEntry extends CommonDocumentEntry {
    public static final String TYPE_NAME = DTO_PREFIX + "QrCodeDocumentEntry";
}
