package ru.crystals.pos.cashdrawer.rx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CashDrawerConnector {

    private static final Logger LOG = LoggerFactory.getLogger(CashDrawerConnector.class);

    private static final String I2C_LIB_NAME = "/home/tc/storage/crystal-cash/lib/io/china-cashdrawer";
    private static final String I2C_MODULE_NAME = "i2c-KERNEL";
    private static final String DRAWER_OPEN_FLAG = "1";

    public void init() {
        loadLibs();
        addRights();
    }

    private void loadLibs() {
        String tceLoad = "tce-load";
        String flag = "-i";
        executeCommand(tceLoad, flag, I2C_MODULE_NAME);
    }

    private void addRights() {
        String chmod = "chmod";
        String flag = "+x";
        executeCommand(chmod, flag, I2C_LIB_NAME);
    }

    private List<String> executeCommand(String... commands) {
        ProcessBuilder builder = new ProcessBuilder(commands);
        LOG.debug("executeCommand: {}" + builder.command());
        Process p;
        List<String> result = new ArrayList<>();
        try {
            p = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    LOG.debug(line);
                    result.add(line);
                }
                p.waitFor();
            }
        } catch (IOException e) {
            LOG.error("Error executeCommand: {}", e);
        } catch (InterruptedException e) {
            LOG.error("Error executeCommand: {}", e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private int openDrawerByNativeLib() {
        String sudo = "sudo";
        String drawnerNumFlag = "-o";
        executeCommand(sudo, I2C_LIB_NAME, drawnerNumFlag);
        return getDrawerOpenedByNativeLib();
    }

    private int getDrawerOpenedByNativeLib() {
        String sudo = "sudo";
        String drawnerStatusFlag = "-i";
        return decodeDrawerStatus(executeCommand(sudo, I2C_LIB_NAME, drawnerStatusFlag));
    }

    /**
     * Утилита china-cashdrawer возвращает ответ в виде 3х строк
     * "
     * Product Name: CSI RX1
     * Drawer status:
     * 0
     * "
     */
    private int decodeDrawerStatus(List<String> cmdResult) {
        if (!cmdResult.isEmpty() && cmdResult.size() > 1) {
            if (cmdResult.get(2).equals(DRAWER_OPEN_FLAG)) {
                return 1;
            }
            return 0;
        }
        return 0;
    }

    public int getDrawerOpened() {
        return getDrawerOpenedByNativeLib();
    }

    public int openDrawer() {
        return openDrawerByNativeLib();
    }

}
