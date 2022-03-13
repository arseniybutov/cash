package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * Дополняет StringComboBoxModel сохранением истории баркодов в файл, чтобы не терять информацию при перезагрузке.
 */
public class FileStringComboBoxModel extends StringComboBoxModel {
    private static final Logger LOG = LoggerFactory.getLogger(FileStringComboBoxModel.class);
    private File dataFile;

    public FileStringComboBoxModel(String fileName) {
        dataFile = new File(fileName);
        if (dataFile.exists()) {
            try(LineNumberReader reader = new LineNumberReader(new FileReader(dataFile))) {

                String line = reader.readLine();
                while (line != null) {
                    super.addElement(line);
                    line = reader.readLine();
                }
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        } else {
        	addElement("SRXCASH-KEY-1");
        	addElement("X-1");
        }
    }

    @Override
    public void addElement(Object element) {
        if (needAdd(element)) {
            super.addElement(element);
            saveModelToFile();
        }
    }

    private void saveModelToFile() {
        try(FileWriter writer = new FileWriter(dataFile)) {
            for (int i = 0; i < getSize(); i++) {
                writer.write(getElementAt(i).toString());
                writer.write("\n");
            }
            writer.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
