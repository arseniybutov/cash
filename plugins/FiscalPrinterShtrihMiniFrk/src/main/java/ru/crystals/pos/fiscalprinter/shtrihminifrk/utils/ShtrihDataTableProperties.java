package ru.crystals.pos.fiscalprinter.shtrihminifrk.utils;

import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ShtrihDataTableProperties {
    private static final String HEADER_PARAMETER = "HEADER_LINE_";
    private static final int HEADER_SIZE = 14;
    private static final String FILE_NAME = "shtrih_data_storage.properties";
    private static final String filePath = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + FILE_NAME;
    private static Properties properties;

    public ShtrihDataTableProperties() {
        properties = new Properties();
    }

    public List<FontLine> getHeader(int startLine, int endLine) throws ShtrihException {
        if (startLine > endLine || endLine > HEADER_SIZE) {
            throw new ShtrihException("Invalid params: startLine " + startLine + " endLine " + endLine);
        }
        List<FontLine> resultHeader = new ArrayList();
        for (int i = 1; i < HEADER_SIZE; i++) {
            String headerLineName = HEADER_PARAMETER + i;
            String line = getStringProperty(headerLineName);
            if(line!=null && line.length()>0) {
                resultHeader.add(new FontLine(line));
            }
        }
        return resultHeader;
    }

    public void setHeader(List<FontLine> header) throws ShtrihException {
        cleanHeader();
        saveHeader(header, 0, header.size());
    }

    public void setHeader(List<FontLine> header, int startLine, int endLine) throws ShtrihException {
        cleanHeader();
        saveHeader(header, startLine, endLine);
    }

    public void loadState() throws ShtrihException {
        File file = new File(filePath);
        if (file.exists()) {
            try {
                InputStream is = new FileInputStream(file);
                properties.load(is);
                is.close();
            } catch (Exception e) {
                throw new ShtrihException("Unable to load counters");
            }
        } else {
            updateState();
        }
    }

    private void updateState() throws ShtrihException {
        try {
            OutputStream out = new FileOutputStream(filePath);
            properties.store(out, "shtrih_data_storage");
            out.close();
        } catch (IOException e) {
            throw new ShtrihException(e.getMessage());
        }
    }


    private String getStringProperty(String name) {
        String line = properties.getProperty(name);
        return line == null ? "": line;
    }

    private void setStringProperty(String name, String value) {
        properties.setProperty(name, value);
    }

    private void cleanHeader() throws ShtrihException {
        for (int i = 1; i < HEADER_SIZE; i++) {
            String headerLineName = HEADER_PARAMETER + i;
            setStringProperty(headerLineName, "");
        }
        try {
            updateState();
        } catch (ShtrihException e) {
            throw new ShtrihException("Error update shtrih data storage ", e);
        }
    }

    private void saveHeader(List<FontLine> header, int startLine, int endLine) throws ShtrihException {
        if (header.size() > HEADER_SIZE) {
            throw new ShtrihException("Invalid header size " + header);
        }
        int j = 0;
        for (int i = startLine; i < endLine; i++) {
            String headerLineName = HEADER_PARAMETER + i;
            setStringProperty(headerLineName, header.get(j).getContent());
            j++;
        }
        try {
            updateState();
        } catch (ShtrihException e) {
            throw new ShtrihException("Error update shtrih data storage ", e);
        }
    }

}
