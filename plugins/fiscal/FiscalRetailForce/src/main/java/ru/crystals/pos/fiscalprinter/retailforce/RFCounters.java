package ru.crystals.pos.fiscalprinter.retailforce;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Эмулируемые счетчики
 */
public class RFCounters {

    private static final String DOC_NUMBER = "doc.number";
    private static final String SHIFT_NUMBER = "shift.number";
    private static final String SHIFT_OPENED = "shift.opened";

    private final Path configPath;

    private Properties properties;

    public RFCounters(@NonNull String clientId) {
        this.configPath = Paths.get(String.format("modules/fiscalPrinter/rf/%s.properties", clientId));
    }

    public long getDocNumber() {
        init();
        return getDocNumberInner();
    }

    public long incDocNumber() {
        init();
        final long docNumber = getDocNumberInner();
        setDocNumber(docNumber + 1);
        writeProperties();
        return getDocNumberInner();
    }

    public boolean isShiftOpened() {
        init();
        return Boolean.parseBoolean(properties.getProperty(SHIFT_OPENED, "false"));
    }

    public long getShiftNumber() {
        init();
        return getShiftNumberInner();
    }

    public void openShift() {
        init();
        setShiftOpened(true);
        writeProperties();
    }

    public void closeShift() {
        init();
        final long shiftNumber = getShiftNumberInner();
        setShiftNumber(shiftNumber + 1);
        setShiftOpened(false);
        writeProperties();
    }

    private long getDocNumberInner() {
        return Long.parseLong(properties.getProperty(DOC_NUMBER, "0"));
    }

    private long getShiftNumberInner() {
        return Long.parseLong(properties.getProperty(SHIFT_NUMBER, "1"));
    }

    private void setShiftNumber(long shiftNumber) {
        properties.setProperty(SHIFT_NUMBER, String.valueOf(shiftNumber));
    }

    private void setShiftOpened(boolean opened) {
        properties.setProperty(SHIFT_OPENED, String.valueOf(opened));
    }

    private void setDocNumber(long docNumber) {
        properties.setProperty(DOC_NUMBER, String.valueOf(docNumber));
    }

    private void init() {
        if (properties != null) {
            return;
        }
        loadProperties();
    }

    private void loadProperties() {
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());
                Files.createFile(configPath);
            }
            try (InputStream in = Files.newInputStream(configPath)) {
                Properties loaded = new Properties();
                loaded.load(in);
                this.properties = loaded;
            }
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    private void writeProperties() {
        try (OutputStream out = Files.newOutputStream(configPath)) {
            properties.store(out, null);
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }
}
