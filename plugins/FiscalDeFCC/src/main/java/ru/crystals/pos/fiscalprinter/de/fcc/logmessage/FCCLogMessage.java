package ru.crystals.pos.fiscalprinter.de.fcc.logmessage;

import ru.crystals.pos.fiscalprinter.de.fcc.BelegTools;
import ru.crystals.pos.fiscalprinter.de.fcc.ber.BerID;
import ru.crystals.pos.fiscalprinter.de.fcc.ber.BerTag;
import ru.crystals.pos.fiscalprinter.de.fcc.ber.BerTagData;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author dalex
 *
 * "Beleg" log deserialized data
 */
public class FCCLogMessage {

    /**
     * Main object head
     */
    private Object mainSeq;

    /**
     * Version of main object
     */
    private Integer version;

    /**
     * Main object id - it has same purpose like as Class type in java
     */
    private byte[] mainObjId;

    /**
     * Operation type - see @FCCOperationType
     */
    private String operationType;

    /**
     * Client id
     */
    private String clientId;

    /**
     * Printable version of operation data
     */
    private String processData;

    /**
     *
     */
    private String processType;

    /**
     * Transaction number
     */
    private Integer transactionNum;

    /**
     * Sub signature - purpose unknown -> read "Technical Guideline BSI
     * TR-03151"
     */
    private String subSig;

    /**
     * Signature object head
     */
    private Object subSeq;

    /**
     * Signature object type - it has same purpose like as Class type in java
     * define signature encryption type
     */
    private byte[] signatureObjIdData;

    /**
     * Readable signature object type
     */
    private BerID signatureObjId;

    /**
     * Signature counter
     */
    private Integer signatureNum;

    /**
     * Operation time
     */
    private Integer unixTime;

    /**
     * Signature value
     */
    private String signature;

    public FCCLogMessage() {
    }

    public void setData(List<BerTagData> tags) throws IOException {
        mainSeq = read(Integer.class, 0, tags);
        version = read(Integer.class, 1, tags);
        mainObjId = read(byte[].class, 2, tags);
        checkObjectType(mainObjId, BerID.transaction_log);

        operationType = read(String.class, 3, tags);
        clientId = read(String.class, 4, tags);
        processData = read(String.class, 5, tags);
        processType = read(String.class, 6, tags);
        transactionNum = read(Integer.class, 7, tags);
        subSig = read(String.class, 8, tags);
        subSeq = read(Integer.class, 9, tags);
        signatureObjIdData = read(byte[].class, 10, tags);
        signatureNum = read(Integer.class, 11, tags);
        unixTime = read(Integer.class, 12, tags);
        signature = read(String.class, 13, tags);

        if (signatureObjIdData != null) {
            signatureObjId = BerID.get(convertByteArray(signatureObjIdData));
        }
    }

    private void checkObjectType(byte[] data, BerID berID) throws IOException {
        if (!berID.equals(BerID.get(convertByteArray(data)))) {
            throw new IOException("Wrong object type detected: " + berID + " isn't match " + BelegTools.toHex(data));
        }
    }

    private int[] convertByteArray(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        int[] d = new int[byteArray.length];
        int i = 0;
        for (byte b : byteArray) {
            d[i] = b & 0xFF;
            i++;
        }
        return d;
    }

    private <T> T read(Class<T> type, int index, List<BerTagData> tags) throws IOException {
        if (tags.size() < index) {
            throw new IOException("Data object is too small");
        }
        BerTagData tagData = tags.get(index);
        BerTag berTag = BelegTools.TAG_MAP.get(tagData.getTag());
        Object obj = berTag.read(tagData);
        if (obj != null && !obj.getClass().isAssignableFrom(type)) {
            return null;
        }
        return (T) obj;
    }

    public int getVersion() {
        return version;
    }

    public String getOperationType() {
        return operationType;
    }

    public String getClientId() {
        return clientId;
    }

    public String getProcessData() {
        return processData;
    }

    public String getProcessType() {
        return processType;
    }

    public int getTransactionNum() {
        return transactionNum;
    }

    public int getSignatureNum() {
        return signatureNum;
    }

    public Integer getUnixTime() {
        return unixTime;
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return "FCCLogMessage{\n   " + "version=" + version + "\n   operationType="
                + operationType + "\n   clientId=" + clientId + "\n   processData="
                + processData + "\n   processType=" + processType + "\n   transactionNum="
                + transactionNum + "\n   subSig=" + subSig + "\n   signatureNum=" + signatureNum
                + "\n   time=" + unixTime + "\n   signatureObjId=" + signatureObjId + "\n   signature=" + signature + '}';
    }
}
