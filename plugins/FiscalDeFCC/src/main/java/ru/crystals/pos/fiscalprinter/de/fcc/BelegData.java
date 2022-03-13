package ru.crystals.pos.fiscalprinter.de.fcc;

/**
 *
 * @author dalex
 */
public class BelegData {

    private byte[] src;
    private byte[] certificate;
    private int signaturecounter;
    private long datatimestoptransaction;
    private String publickey;
    private String hashalgorithm;
    private String signature;
    private String processData;

    public BelegData() {
    }

    public byte[] getSrc() {
        return src;
    }

    public void setSrc(byte[] src) {
        this.src = src;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    public void setCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public int getSignaturecounter() {
        return signaturecounter;
    }

    public void setSignaturecounter(int signaturecounter) {
        this.signaturecounter = signaturecounter;
    }

    public long getDatatimestoptransaction() {
        return datatimestoptransaction;
    }

    public void setDatatimestoptransaction(long datatimestoptransaction) {
        this.datatimestoptransaction = datatimestoptransaction;
    }

    public String getPublickey() {
        return publickey;
    }

    public void setPublickey(String publickey) {
        this.publickey = publickey;
    }

    public String getHashalgorithm() {
        return hashalgorithm;
    }

    public void setHashalgorithm(String hashalgorithm) {
        this.hashalgorithm = hashalgorithm;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getProcessData() {
        return processData;
    }

    public void setProcessData(String processData) {
        this.processData = processData;
    }

    @Override
    public String toString() {
        return "BelegData{" + "src=" + src + ", certificate=" + certificate + ", signaturecounter=" + signaturecounter
                + ", datatimestoptransaction=" + datatimestoptransaction + ", publickey=" + publickey
                + ", hashalgorithm=" + hashalgorithm + ", signature=" + signature + '}';
    }
}
