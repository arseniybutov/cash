package ru.crystals.pos.fiscalprinter.de.fcc;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.de.fcc.ber.BerLogType;
import ru.crystals.pos.fiscalprinter.de.fcc.ber.BerTag;
import ru.crystals.pos.fiscalprinter.de.fcc.ber.BerTagData;
import ru.crystals.pos.fiscalprinter.de.fcc.ber.BerType;
import ru.crystals.pos.fiscalprinter.de.fcc.logmessage.FCCLogMessage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dalex
 *
 * Beleg transaction data reader
 */
public class BelegTools {

    private static final Logger LOG = LoggerFactory.getLogger(BelegTools.class);

    public static final Map<Integer, BerTag> TAG_MAP;

    static {
        Map<Integer, BerTag> tMap = new HashMap<>();
        for (BerLogType t : BerLogType.values()) {
            tMap.put(t.getTag(), t);
        }

        for (BerType t : BerType.values()) {
            tMap.put(t.getTag(), t);
        }
        TAG_MAP = Collections.unmodifiableMap(tMap);
    }

    private BelegTools(){
    }

    /**
     * Byte array to string converter
     * @param data
     * @return
     */
    public static String toHex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte b : data) {
            result.append(String.format(" %02X", b));
        }
        return result.toString().trim();
    }

    /**
     * Byte array to long converter
     * @param data
     * @return
     */
    public static long bytesToLong(byte[] data) {
        long result = 0;
        int shift = 0;
        byte b;
        for (int i = data.length - 1; i > -1; i--) {
            b = data[i];
            result |= ((long) (b & 0xff) << shift);
            shift += 8;
        }
        return result;
    }

    /**
     * Beleg log data reader
     * @param belegFccBody - source data
     * @return deserialized data
     */
    public static BelegData getBelegInfo(byte[] belegFccBody) {
        BelegData result = new BelegData();
        result.setSrc(belegFccBody);
        try {
            ByteArrayOutputStream logOut = new ByteArrayOutputStream();
            ByteArrayOutputStream certOut = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(belegFccBody);
            String belegLogName = null;
            try (TarArchiveInputStream tarStream = (TarArchiveInputStream) new ArchiveStreamFactory().createArchiveInputStream("tar", in)) {
                TarArchiveEntry entry;
                int lastLogIndex = -1;
                int logIndex;
                while ((entry = (TarArchiveEntry) tarStream.getNextEntry()) != null) {
                    if (entry.getName().toLowerCase().endsWith(".log")) {
                        logIndex = getLogIndex(entry.getName());
                        if (logIndex > lastLogIndex) {
                            lastLogIndex = logIndex;
                            logOut.reset();
                            belegLogName = entry.getName();
                            IOUtils.copy(tarStream, logOut);
                        }
                    } else if (entry.getName().toLowerCase().endsWith(".cer")) {
                        IOUtils.copy(tarStream, certOut);
                    }
                }
            }

            if (belegLogName != null) {
                try {
                    String time = getValue(belegLogName, "_", "_");
                    if (time != null) {
                        result.setDatatimestoptransaction(Long.valueOf(time) * 1000);
                    }
                    String sig = getValue(belegLogName, "_Sig-", "_");
                    if (sig != null) {
                        result.setSignaturecounter(Integer.valueOf(sig));
                    }
                } catch (Exception ex) {
                    LOG.error("", ex);
                }
            }

            if (certOut.size() > 0) {
                X509Certificate cert = readCertificate(certOut.toByteArray());
                result.setHashalgorithm(cert.getSigAlgName());
                result.setPublickey(Base64.encodeBase64String(cert.getPublicKey().getEncoded()));
            }

            if (logOut.size() > 0) {
                FCCLogMessage logMessage = readLog(belegLogName, logOut.toByteArray());
                result.setSignature(logMessage.getSignature());
                result.setDatatimestoptransaction(logMessage.getUnixTime() == null ? 0 : Long.valueOf(logMessage.getUnixTime()) * 1000);
                result.setProcessData(logMessage.getProcessData());
            }
        } catch (Exception ex) {
            LOG.error("", ex);
        }
        return result;
    }

    private static int getLogIndex(String fileName) {
        String index = getValue(fileName, "_Fc-", ".log");
        try {
            return Integer.valueOf(index);
        } catch (Exception e) {
            LOG.error("Canot parse file index - " + fileName + " -> " + index, e);
        }
        return -1;
    }

    private static String getValue(String src, String prefix, String suffix) {
        int indexS = src.indexOf(prefix);
        int indexF;

        if (indexS != -1) {
            indexF = src.indexOf(suffix, indexS + prefix.length());
            if (indexF != -1) {
                return src.substring(indexS + prefix.length(), indexF);
            }
        }
        return null;
    }

    public static X509Certificate readCertificate(byte[] certificateData) throws CertificateException, IOException {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        try (ByteArrayInputStream fis = new ByteArrayInputStream(certificateData)) {
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(fis);
            return certificate;
        }
    }

    private static int read(byte[] data, int index) {
        return 0xff & data[index];
    }

    private static int readDataLength(BerTagData tagData, byte[] data, int index) throws IOException {
        int val = read(data, index);
        int lengthLength;
        if ((val & 0x80) == 0) {
            lengthLength = 1;
        } else {
            lengthLength = val & 0x7f;
            if (lengthLength > 4) {
                throw new IOException("Length is out of bound!");
            }
            val = 0;

            index++;
            for (int i = 0; i < lengthLength; i++) {
                int nextByte = read(data, index);
                if (nextByte == -1) {
                    throw new IOException("Unexpected end of input stream.");
                }
                val |= nextByte << (8 * (lengthLength - i - 1));
                index++;
            }
            lengthLength = lengthLength + 1;
        }
        tagData.setData(new byte[val]);
        return lengthLength;
    }

    private static FCCLogMessage readLog(String name, byte[] logData) throws IOException {
        LOG.info("Read log: {}", name);
        if (logData == null || logData.length == 0 || logData[0] != 0x30) {
            throw new IOException("Read log fail: " + name);
        }
        int index = 0;

        int tag;
        BerTagData tagData;
        List<BerTagData> tags = new LinkedList<>();
        while (index < logData.length) {
            tag = 0xff & logData[index];
            index++;

            tagData = new BerTagData(tag);
            index += readDataLength(tagData, logData, index);
            System.arraycopy(logData, index, tagData.getData(), 0, tagData.getData().length);

            if (tag != BerType.SEQUENCE.getTag()) {
                index += tagData.getData().length;
            }
            tags.add(tagData);
        }

        FCCLogMessage result = new FCCLogMessage();
        result.setData(tags);
        return result;
    }
}
