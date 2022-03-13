package ru.crystals.pos.customerdisplay.csibarcodescanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class BarcodeScannerConnector {

    public static final Logger LOG = LoggerFactory.getLogger(BarcodeScannerConnector.class);

    private InputStream in = null;

    public void open(InputStream inputStream) throws Exception {
        log("---{ open }---");
        if (inputStream == null) {
            throw new Exception("BarcodeScannerConnector inputStream is null");
        }
        in = inputStream;
    }

    public void close() {
        log("---{ close }---");
        try {
            in.close();
        } catch (Exception e) {
            LOG.error("", e);
        }
    }

    public int available() throws IOException {
        return in.available();
    }

    public byte[] readAll() {
        try {
            byte[] buff = new byte[in.available()];
            in.read(buff);
            return buff;
        } catch (IOException e) {
            LOG.error("", e);
        }
        return null;
    }

    private void log(String text) {
        LOG.info(text);
    }

}
