package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProtectedCatalogImpl implements ProtectedCatalog {

    private static final Logger log = LoggerFactory.getLogger(ProtectedCatalogImpl.class);

    private static final byte[] HEADER = new byte[]{
            0x7f, 0x45, 0x4c, 0x46, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x03, 0x00, 0x03, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x57, 0x00, 0x00, 0x34, 0x00, 0x00, 0x00,
            0x28, 0x15, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x34, 0x00, 0x20, 0x00, 0x04, 0x00, 0x28, 0x00,
            0x0f, 0x00, 0x0e, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x60, 0x0a, 0x01, 0x00, 0x60, 0x0a, 0x01, 0x00, 0x05, 0x00, 0x00, 0x00,
            0x00, 0x10, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x10, 0x01, 0x00, 0x00, 0x10, 0x01, 0x00,
            0x00, 0x10, 0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x00, 0x06, 0x00, 0x00, 0x00,
            0x00, 0x10, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x10, 0x11, 0x01, 0x00, 0x10, 0x11, 0x01, 0x00
    };

    private final List<ProtectedFile> protectedFiles;

    private final Path auditPath;

    public ProtectedCatalogImpl(Path auditPath) {
        this.auditPath = auditPath.toAbsolutePath();
        protectedFiles = Stream.of(
                new ProtectedFile("core.so", 121_356, "e39c4193953ff6ea941a156be33b84c8"),
                new ProtectedFile("reports.so", 53_982, "a8680adf0868a9cc08344561a646ddb7"),
                new ProtectedFile("sale.so", 94_216, "e5701fa62273aaabaa3453e30dbfec8b")
        ).sorted()
                .collect(Collectors.toList());
    }

    @Override
    public void init() throws IOException {
        if (!Files.exists(this.auditPath)) {
            log.debug("Protected catalog will be initialized ({})", auditPath);
            if (this.auditPath.getParent() != null) {
                Files.createDirectories(this.auditPath.getParent());
            }
            Files.createDirectory(this.auditPath);
            protectedFiles.forEach(this::create);
        }
        if (!validate()) {
            throw new IOException("Protected files corrupted");
        }
    }

    private void create(ProtectedFile protectedFile) {
        try {
            final ByteArrayOutputStream rand = new ByteArrayOutputStream(protectedFile.getSize());
            rand.write(HEADER);
            long seed = protectedFile.getName().length() + protectedFile.getSize();
            final Random random = new Random(seed);
            final byte[] bytes = new byte[protectedFile.getSize() - HEADER.length];
            random.nextBytes(bytes);
            rand.write(bytes);
            rand.writeTo(Files.newOutputStream(auditPath.resolve(protectedFile.getName())));
        } catch (Exception e) {
            log.error("Unable to create file {}", protectedFile.getName());
        }
    }

    @Override
    public boolean validate() {
        log.debug("Validation started");
        if (!Files.exists(auditPath)) {
            log.error("Protected catalog doesn't exists ({})", auditPath);
            return false;
        }
        if (!Files.isDirectory(auditPath)) {
            log.error("Invalid protected catalog: file instead of directory ({})", auditPath);
            return false;
        }
        final List<Path> auditPathCurrentFiles;
        try (Stream<Path> files = Files.list(auditPath)) {
            auditPathCurrentFiles = files
                    .map(Path::toAbsolutePath)
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Unable to verify check sum of protected files", e);
            return false;
        }
        if (auditPathCurrentFiles.isEmpty() || auditPathCurrentFiles.size() != protectedFiles.size()) {
            log.error("Invalid count of protected files. Remove protected catalog {} to restore files", auditPath);
            return false;
        }

        for (int i = 0; i < protectedFiles.size(); i++) {
            if (!validate(auditPathCurrentFiles.get(i), protectedFiles.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean validate(Path currentFile, ProtectedFile protectedFile) {
        if (!isValidPath(currentFile, protectedFile)) {
            return false;
        }
        if (!isValidSize(currentFile, protectedFile)) {
            return false;
        }
        if (!isValidMD5(currentFile, protectedFile)) {
            return false;
        }
        return true;
    }

    private boolean isValidMD5(Path currentFile, ProtectedFile protectedFile) {
        try (final InputStream data = Files.newInputStream(currentFile)) {
            final String md5 = DigestUtils.md5Hex(data);
            if (!md5.equalsIgnoreCase(protectedFile.getMd5())) {
                log.error("First invalid file (md5 {}): {}", md5, currentFile);
                return false;
            }
        } catch (IOException e) {
            log.error("Unable to calculate md5 for {}", currentFile, e);
            return false;
        }
        return true;
    }

    private boolean isValidSize(Path currentFile, ProtectedFile protectedFile) {
        try {
            final long size = Files.size(currentFile);
            if (size != protectedFile.getSize()) {
                log.error("First invalid file (size {}): {}", size, currentFile);
                return false;
            }
        } catch (IOException e) {
            log.error("Unable to calculate size for {}", currentFile, e);
            return false;
        }
        return true;
    }

    private boolean isValidPath(Path currentFile, ProtectedFile protectedFile) {
        if (Files.isDirectory(currentFile)) {
            log.error("First invalid file (directory): {}", currentFile);
            return false;
        }
        if (!auditPath.resolve(protectedFile.getName()).toAbsolutePath().equals(currentFile)) {
            log.error("First invalid file: {}", currentFile);
            return false;
        }
        return true;
    }

}
