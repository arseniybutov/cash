package ru.crystals.pos.fiscalprinter.de.fcc.ber;

import java.util.Arrays;

/**
 *
 * @author dalex
 *
 * Beleg log ASN.1 object ids.
 * defined in "Technical Guideline BSI TR-03151"
 * Used document version Version 1.0.1 20. December 2018
 * page 79
 */
public enum BerID {

    transaction_log("transaction-log", /*            */ new int[]{4, 0, 127, 0, 7, 3, 7, 1, 1}),
    /**/
    ecdsa_plain_SHA224("ecdsa-plain-SHA224", /*      */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 2}),
    ecdsa_plain_SHA256("ecdsa-plain-SHA256", /*      */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 3}),
    ecdsa_plain_SHA384("ecdsa-plain-SHA384", /*      */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 4}),
    ecdsa_plain_SHA512("ecdsa-plain-SHA512", /*      */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 5}),
    ecdsa_plain_SHA3_224("ecdsa-plain-SHA3-224", /*  */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 8}),
    ecdsa_plain_SHA3_256("ecdsa-plain-SHA3-256", /*  */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 9}),
    ecdsa_plain_SHA3_384("ecdsa-plain-SHA3-384", /*  */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 10}),
    ecdsa_plain_SHA3_512("ecdsa-plain-SHA3-512", /*  */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 1, 11}),
    ecsdsa_plain_SHA224("ecsdsa-plain-SHA224", /*    */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 1}),
    ecsdsa_plain_SHA256("ecsdsa-plain-SHA256", /*    */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 2}),
    ecsdsa_plain_SHA384("ecsdsa-plain-SHA384", /*    */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 3}),
    ecsdsa_plain_SHA512("ecsdsa-plain-SHA512", /*    */ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 4}),
    ecsdsa_plain_SHA3_224("ecsdsa-plain-SHA3-224", /**/ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 5}),
    ecsdsa_plain_SHA3_256("ecsdsa-plain-SHA3-256", /**/ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 6}),
    ecsdsa_plain_SHA3_384("ecsdsa-plain-SHA3-384", /**/ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 7}),
    ecsdsa_plain_SHA3_512("ecsdsa-plain-SHA3-512", /**/ new int[]{4, 0, 127, 0, 7, 1, 1, 4, 4, 8}),;

    private String name;
    private int[] id;

    private BerID(String name, int[] id) {
        this.name = name;
        this.id = id;
    }

    public static BerID get(int[] data) {
        if (data == null) {
            return null;
        }
        for (BerID berId : BerID.values()) {
            if (berId.isEquals(data)) {
                return berId;
            }
        }
        return null;
    }

    public boolean isEquals(int[] id) {
        if (!Arrays.equals(this.id, id)) {
            return false;
        }
        return true;
    }

}
