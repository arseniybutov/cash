package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProtectedCatalogImplTest {

    private Path auditPath;
    private ProtectedCatalogImpl catalog;
    private Path core;

    @Before
    public void setUp() throws Exception {
        auditPath = Files.createTempDirectory("initTest");
        Files.delete(auditPath);
        catalog = new ProtectedCatalogImpl(auditPath);
        core = auditPath.resolve("core.so");
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(auditPath.toFile());
    }

    @Test
    public void baseInitTest() throws IOException {
        initAndCheckValid();

        verifyExpectedFiles(auditPath);
    }

    private void initAndCheckValid() throws IOException {
        catalog.init();
        assertTrue("Should be valid after successful initialization", catalog.validate());
    }

    @Test
    public void invalidOnUnexpectedFile() throws IOException {
        initAndCheckValid();

        final Path unexpectedFile = Files.createFile(auditPath.resolve("unexpectedFile"));
        assertFalse("Should be invalid when unexpected file created", catalog.validate());
        checkErrorOnInit("when unexpected file created");

        Files.delete(unexpectedFile);
        assertTrue("Should be valid when unexpected file removed", catalog.validate());
    }

    @Test
    public void invalidOnDeletedFile() throws IOException {
        initAndCheckValid();

        Files.delete(core);
        assertFalse("Should be invalid when protected file deleted", catalog.validate());
        checkErrorOnInit("when protected file deleted");
    }

    @Test
    public void invalidOnFileIsDirectrory() throws IOException {
        initAndCheckValid();

        Files.delete(core);
        Files.createDirectory(core);
        assertTrue("Expected protected file is directory (invalid test)", Files.isDirectory(core));

        assertFalse("Should be invalid when protected file is directory", catalog.validate());
        checkErrorOnInit("when protected file is directory");
    }

    @Test
    public void invalidOnChangedSize() throws IOException {
        initAndCheckValid();

        corruptFileWithSize(core);
        assertFalse("Should be invalid when size is changed", catalog.validate());

        checkErrorOnInit("when size is changed");
    }

    @Test
    public void invalidOnChangedContent() throws IOException {
        initAndCheckValid();

        final long originalSize = corruptFileWithMD5(core);
        assertEquals("Size should not be changed (invalid test)", originalSize, Files.size(core));
        assertFalse("Should be invalid when md5 is changed", catalog.validate());

        checkErrorOnInit("when md5 is changed");
    }

    @Test
    public void invalidOnDeletedAuditCatalog() throws IOException {
        initAndCheckValid();

        FileUtils.deleteDirectory(auditPath.toFile());
        assertFalse("Should be invalid when audit catalog deleted", catalog.validate());

        catalog.init();
        assertTrue("Should be valid due to re-init of audit catalog", catalog.validate());
    }

    private void checkErrorOnInit(String message) {
        try {
            catalog.init();
            fail("No expected error on init " + message);
        } catch (IOException e) {
            assertEquals("Invalid IOE message", "Protected files corrupted", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Unexpected error " + message + ": " + e);
        }
    }

    protected void verifyExpectedFiles(Path auditPath) throws IOException {
        try (Stream<Path> files = Files.list(auditPath)) {
            final List<String> names = files.map(p -> p.getName(p.getNameCount() - 1)
                    .toString())
                    .sorted()
                    .collect(Collectors.toList());
            assertEquals(Arrays.asList("core.so",
                    "reports.so",
                    "sale.so"), names);
        }
        verifyFile(auditPath, "core.so", 121_356, "e39c4193953ff6ea941a156be33b84c8");
        verifyFile(auditPath, "reports.so", 53_982, "a8680adf0868a9cc08344561a646ddb7");
        verifyFile(auditPath, "sale.so", 94_216, "e5701fa62273aaabaa3453e30dbfec8b");
    }

    private void verifyFile(Path auditPath, String name, long size, String md5) throws IOException {
        final Path file = auditPath.resolve(name);
        assertTrue("File not exist: " + name, Files.exists(file));
        assertEquals("File size mismatch", size, Files.size(file));
        assertEquals("File md5 mismatch", md5, DigestUtils.md5Hex(Files.newInputStream(file)));
    }

    private void corruptFileWithSize(Path core) throws IOException {
        Files.write(core, new byte[]{0x02}, StandardOpenOption.APPEND);
    }

    private long corruptFileWithMD5(Path core) throws IOException {
        final long originalSize = Files.size(core);
        final byte[] original = Files.readAllBytes(core);
        final ByteArrayOutputStream corrupted = new ByteArrayOutputStream(original.length);
        corrupted.write(original, 100, original.length - 100);
        corrupted.write(original, 0, 100);
        corrupted.writeTo(Files.newOutputStream(core));
        return originalSize;
    }

    @Test
    public void checkFilesInResources() throws URISyntaxException, IOException {
        final String rawPath = "/fiscal";
        final URL resource = ProtectedCatalogImpl.class.getResource(rawPath);
        final URI uri = resource.toURI();
        if (resource.getProtocol().startsWith("file")) {
            verifyExpectedFiles(Paths.get(uri));
            return;
        }
        try {
            FileSystem fs = FileSystems.getFileSystem(uri);
            verifyExpectedFiles(fs.getPath(rawPath));
        } catch (FileSystemNotFoundException fsnfe) {
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                verifyExpectedFiles(fs.getPath(rawPath));
            }
        }
    }
}