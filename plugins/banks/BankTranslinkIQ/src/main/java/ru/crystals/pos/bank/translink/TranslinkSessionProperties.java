package ru.crystals.pos.bank.translink;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;

public class TranslinkSessionProperties {

    private static final String DOC_NUMBER = "doc.number";
    private static final String ACCESS_TOKEN = "auth.token";
    private static final Path CONFIG_PATH = Paths.get("modules/bank/translink/translink.properties");

    private final DateTimeFormatter df = DateTimeFormatter.ofPattern("ddHHmmssSSS");
    private Properties properties;

    public String nextDocNumber() {
        initConfig();
        final Optional<String> currentDocNumber = getDocNumber();
        if (currentDocNumber.isPresent()) {
            throw new IllegalStateException("Previous document should be closed first");
        }
        final String docNumber = LocalDateTime.now().format(df);
        properties.setProperty(DOC_NUMBER, docNumber);
        writeProperties();
        return docNumber;
    }

    public Optional<String> getDocNumber() {
        initConfig();
        return Optional.ofNullable(StringUtils.trimToNull(properties.getProperty(DOC_NUMBER)));
    }

    public Optional<String> getAccessToken() {
        initConfig();
        return Optional.ofNullable(StringUtils.trimToNull(properties.getProperty(ACCESS_TOKEN)));
    }

    public void setAccessToken(String accessToken) {
        initConfig();
        properties.setProperty(ACCESS_TOKEN, accessToken);
    }

    public void closeDocNumber() {
        initConfig();
        properties.remove(DOC_NUMBER);
        writeProperties();
    }

    public void clearAccessToken() {
        initConfig();
        properties.remove(ACCESS_TOKEN);
        writeProperties();
    }

    private void initConfig() {
        if (properties != null) {
            return;
        }
        loadProperties();
    }

    private void loadProperties() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                Files.createFile(CONFIG_PATH);
            }
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                Properties loaded = new Properties();
                loaded.load(in);
                this.properties = loaded;
            }
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }

    private void writeProperties() {
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            properties.store(out, null);
        } catch (IOException io) {
            throw new UncheckedIOException(io);
        }
    }
}
