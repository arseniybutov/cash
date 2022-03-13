package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Properties;

public abstract class PrinterProperties {

    private static final String LOGO_FILE_LAST_MODIFIED = "logo.file.last.modified";

    protected Path configPath;
    protected Properties properties;

    public PrinterProperties() {
        setConfigPath();
        loadProperties();
    }

    protected abstract void setConfigPath();

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

    protected void writeProperties() {
        try (OutputStream out = Files.newOutputStream(configPath)) {
            properties.store(out, null);
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    public LocalDateTime getLogoFileLastModified() {
        String lastModified = properties.getProperty(LOGO_FILE_LAST_MODIFIED);
        if (lastModified == null) {
            return LocalDateTime.MIN;
        }
        return LocalDateTime.parse(lastModified);
    }

    public void updateLogoFileLastModified(LocalDateTime lastModified) {
        properties.setProperty(LOGO_FILE_LAST_MODIFIED, lastModified.toString());
        writeProperties();
    }
}
