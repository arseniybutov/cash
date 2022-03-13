package ru.crystals.pos.scale.emulator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Эмулятор прикассовых весов. При запуске плагин автоматически создаёт файл scales.emulator.properties в папке modules/scale/, если он не существует. Значение
 * свойства weight эмулирует вес груза в граммах
 */
public class ScaleEmulatorServiceImpl extends AbstractScalePluginImpl {
    private static final Logger logger = LoggerFactory.getLogger("fd");

    private static final String SCALES_EMULATOR_FILE = "scales.emulator.properties";

    private static final String SCALES_FILES = Constants.PATH_MODULES + Constants.SCALES + File.separator + SCALES_EMULATOR_FILE;

    private static final String PROPERTY_KEY_WEIGHT = "weight";

    private static final String ERROR_MESSAGE = "errorMessage";

    private int weight;

    private long lastModified;

    @Override
    public void start() {
        createScalesFile();
    }

    private void createScalesFile() {
        try {
            File file = new File(SCALES_FILES);
            if (!file.exists()) {
                file.createNewFile();

                Properties properties = new Properties();
                properties.setProperty(PROPERTY_KEY_WEIGHT, Integer.valueOf(weight).toString());

                FileOutputStream out = new FileOutputStream(file);
                properties.store(out, "Scales state");
            }
            lastModified = 0;
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    @Override
    public void stop() {
        //
    }

    @Override
    public int getWeight() throws ScaleException {
        loadWeight();
        return weight;
    }

    @Override
    public Boolean moduleCheckState() {
        return true;
    }

    private void loadWeight() throws ScaleException {
        File file = new File(SCALES_FILES);
        if (file.lastModified() > lastModified) {
            try (InputStream is = new FileInputStream(file)) {
                Properties properties = new Properties();
                properties.load(is);
                weight = Integer.valueOf(properties.getProperty(PROPERTY_KEY_WEIGHT));
                String errorMessage = properties.getProperty(ERROR_MESSAGE);
                if (!StringUtils.isEmpty(errorMessage)) {
                    throw new ScaleException(errorMessage);
                }
            } catch (IOException e) {
                logger.error("", e);
            }
            lastModified = file.lastModified();
        }
    }

}
