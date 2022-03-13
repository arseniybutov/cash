package ru.crystals.pos.bank.zvt.protocol;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Возможные поля в ответах
 */
public enum TransactionField {
    TIMEOUT("01", 1),
    MAX_NUMBER_OF_STATUS_INFORMATIONS("02", 1),
    SERVICE_BYTE("03", 1),
    TRACK2_DATA("23", FieldDataType.LLVAR),
    TRACK3_DATA("24", FieldDataType.LLLVAR),
    TRACK1_DATA("2D", FieldDataType.LLLVAR),
    SYN_CHIP_DATA("2E", FieldDataType.LLLVAR),
    F_3A("3A", 2),
    F_A7("A7", FieldDataType.LLVAR),
    F_AF("AF", FieldDataType.LLLVAR),
    F_AA("AA", 3),
    F_D0("D0", 1),
    F_D2("D2", 1),
    F_D3("D3", 1),
    F_E0("E0", 1),
    F_E1("E1", FieldDataType.LLVAR),
    F_E2("E2", FieldDataType.LLVAR),
    F_E3("E3", FieldDataType.LLVAR),
    F_E4("E4", FieldDataType.LLVAR),
    F_E5("E5", FieldDataType.LLVAR),
    F_E6("E6", FieldDataType.LLVAR),
    F_E7("E7", FieldDataType.LLVAR),
    F_E8("E8", FieldDataType.LLVAR),
    F_E9("E9", 1),
    F_EA("EA", 1),
    F_EB("EB", 8),
    F_F0("F0", 1),
    F_F1("F1", FieldDataType.LLVAR),
    F_F2("F2", FieldDataType.LLVAR),
    F_F3("F3", FieldDataType.LLVAR),
    F_F4("F4", FieldDataType.LLVAR),
    F_F5("F5", FieldDataType.LLVAR),
    F_F6("F6", FieldDataType.LLVAR),
    F_F7("F7", FieldDataType.LLVAR),
    F_F8("F8", FieldDataType.LLVAR),
    F_F9("F9", 1),
    F_FA("FA", 1),
    F_FB("FB", 1),
    F_FC("FC", 1),
    F_FD("FD", 1),

    /**
     * Result-code as defined in chapter Error-Messages
     */
    RESULT_CODE("27", 1),
    /**
     * 6 byte BCD packed (payment-amount or total of the End-of-Day)
     */
    AMOUNT("04", 6),
    /**
     * trace-number, 3 byte BCD, for long trace numbers with more than 6 dig-its, the bitmap is set to 000000 and TLV tag 1F2B is used instead
     */
    TRACE("0B", 3),
    /**
     *
     */
    ORIGINAL_TRACE("37", 3),
    /**
     * 3 byte BCD HHMMSS
     */


    TIME("0C", 3),
    /**
     * 2 byte BCD MMDD
     */
    DATE("0D", 2),
    /**
     * expiry-date, 2 byte BCD in Format YYMM
     */
    EXPIRATION_DATE("0E", 2),
    /**
     * card sequence-number, 2 byte BCD packed
     */
    SEQ_NO("17", 2),
    /**
     * payment-type: 40 = offline 50 = card in terminal checked positively, but no Authorisation carried out 60 = online 70 = PIN-payment (also possible for
     * EMV-processing, i.e. credit cards, ecTrack2, ecEMV online/offline).
     * If the TLV-container is active, this information can be specified in tag 2F (see chapter TLV-container).
     */
    CC_PAYMENT_TYPE("19", 1, FieldDataType.BMP),

    /**
     * PAN for magnet-stripe or EF_ID for ec chip,
     * LLVAR (2 byte counter [FxFy], data BCD packed, D = separator),
     * e.g. F0 F3 01 23 45 (F0 F3 means 3 bytes follow)
     * receipt-data of the EF_ID: - card-number: byte 5-9 from EF_ID
     * - expiry-date: byte 11-12 from EF_ID
     * The transfer of the PAN for girocard transactions (ecTrack2, ecEMV
     */
    PAN("22", FieldDataType.LLVAR),
    /**
     * terminal-ID, 4 byte BCD packed
     */
    TERMINAL_ID("29", 4),
    /**
     * authorisation-attribute. The length of the bitmaps is always 8 byte.
     * contents:
     * 1) Maestro-cards (BMP8A = 46):
     * used-data max. 6 byte ASCII. The bitmap is filled with trailing zeros.
     * 2) Girocard-cards (ectrack2, ecEMV online/offline):
     * 8 byte ASCII padded with trailing zeros.
     * 3) other cards:
     * used-data max. 8 byte
     */
    AID("3B", 8),
    /**
     * 2 byte BCD packed. Value: 09 78 = EUR
     */
    CURRENCY_CODE("49", 2),
    /**
     * List of blocked goods-groups
     * LLVAR (2 byte counter [FxFy], data BCD packed),
     */
    BLOCKED_GOODS_GROUPS("4C", FieldDataType.LLVAR),
    /**
     * receipt-number, 2 byte BCD packed. Valid only for non-Geldkarte trans-actions.
     */
    RECEIPT_NO("87", 2),
    /**
     * card-type (= ZVT card-type ID), 1 byte binary; see chapter ZVT-card-type-ID. Via BMP 8A can only cards within the first 255 card-type-IDs be transferred. For
     * cards ID 256 upwards tag 41 must be used.
     * If the ZVT card-type ID is larger than decimal 255 then BMP 8A should contain ‘FF’ and tag 41 should be used (see chapter TLV-container), providing the ZVT
     * Card-Type ID is to be sent to the ECR. Alternatively BMP 8A can be omitted.
     */
    CARD_TYPE("8A", 1, FieldDataType.BINARY),
    /**
     * card-type-ID of the network operator; 1 byte binary.
     * If the network operator card-type ID is larger than decimal 255 then BMP 8C should contain ‘FF’ and tag 49 should be used (see chapter TLV-container), providing
     * the network operator card-type ID is to be sent to the ECR. Alternatively BMP 8C can be omitted.
     */
    CARD_TYPE_ID("8C", 1, FieldDataType.BINARY),
    /**
     * LLLVAR payment-record from Geldkarte with certificate according to “Schnittstellenspezifikation für die ZKA-Chipkarte - GeldKarte Version 5.2”. 100 bytes binary
     * (103 byte incl. LLLVAR); (only for Geldkarte).
     * This BMP is not available before the delivery of goods was confirmed.
     */
    PAYMENT_RECORDS("9A", FieldDataType.LLLVAR),
    /**
     * AID-parameter, 5 byte binary
     * Only Maestrocard.
     */
    AID_PAR("BA", 5, FieldDataType.BINARY),
    /**
     * contract-number for credit-cards, 15 byte, ASCII, not null-terminated.
     */
    VU_NUMBER("2A", 15, FieldDataType.ASCII),
    /**
     * additional text for credit-cards, LLLVAR, ASCII, not null-terminated.
     */
    ADDITIONAL_TEXT("3C", FieldDataType.LLLVAR),
    /**
     * the result-code, the AS is set if the host sends a result-code which can't be encoded in BCD . 1 byte, binary.
     */
    RESULT_CODE_AS("A0", 1, FieldDataType.BINARY),
    /**
     * analogous to receipt-number, <turnover-no> is however valid for all transactions. 3 byte BCD-packed. Not supported by all terminals.
     */
    TURNOVER_NO("88", 3),
    /**
     * name of the card-type, LLVAR, ASCII, null-terminated.
     * For EMV-applications the product name is provided here. This must be printed on the receipt.
     */
    CARD_NAME("8B", FieldDataType.LLVAR),
    /**
     * TLV-container; see chapter Defined Data-Objects
     * e.g. lists the forbidden goods-groups
     */
    ADDITIONAL_DATA("06", FieldDataType.TLV),

    // daily

    SINGLE_AMOUNTS("60", FieldDataType.LLLVAR),

    ;

    private static Map<String, TransactionField> mappedByCode = Arrays.stream(values())
            .collect(Collectors.toMap(TransactionField::getCode, Function.identity()));
    private final String code;
    private final int length;
    private final FieldDataType dataType;

    TransactionField(String code, int length, FieldDataType dataType) {
        this.code = code;
        this.length = length;
        this.dataType = dataType;
    }

    TransactionField(String code, FieldDataType dataType) {
        this(code, 0, dataType);
    }

    TransactionField(String code, int length) {
        this(code, length, FieldDataType.BCD);
    }

    public static TransactionField getByCode(String fieldCode) {
        return mappedByCode.get(fieldCode);
    }

    public String getCode() {
        return code;
    }

    public int getLength() {
        return length;
    }

    public FieldDataType getDataType() {
        return dataType;
    }
}
