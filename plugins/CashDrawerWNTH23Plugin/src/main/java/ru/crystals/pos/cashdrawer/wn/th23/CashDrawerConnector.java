package ru.crystals.pos.cashdrawer.wn.th23;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CashDrawerConnector {

    private static final Logger LOG = LoggerFactory.getLogger(CashDrawerConnector.class);

    private static final String I2C_LIB_NAME = "/home/tc/storage/crystal-cash/lib/io/wn-portio";
    private static final String I2C_MODULE_NAME = "i2c-KERNEL";
    private static final String OPEN_FLAG_FIRST_DRAWNER = "0x40";
    private static final String OPEN_FLAG_SECOND_DRAWNER = "0x80";

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
        } catch (InterruptedException e){
            LOG.error("Error executeCommand: {}", e);
            Thread.currentThread().interrupt();
        }
        return result;
    }

    private int openDrawerByNativeLib(int number) {
        String sudo = "sudo";
        String drawnerNumFlag = "-o" + number;
        String drawnerInvertFlag = "-i";
        String pulseTimeFlag = "-pn";
        String pulseTime = "150";
        return decodeDrawnerStatus(number, executeCommand(sudo, I2C_LIB_NAME, drawnerInvertFlag, pulseTimeFlag, pulseTime, drawnerNumFlag));
    }

    private int getDrawerOpenedByNativeLib(int number) {
        String sudo = "sudo";
        String drawnerStatusFlag = "-s";
        return decodeDrawnerStatus(number, executeCommand(sudo, I2C_LIB_NAME, drawnerStatusFlag));
    }

    private int decodeDrawnerStatus(int number, List<String> cmdResult) {
        if (!cmdResult.isEmpty()) {
            if (number == 1 && cmdResult.get(0).equals(OPEN_FLAG_FIRST_DRAWNER)) {
                return 1;
            }
            if (number == 2 && cmdResult.get(0).equals(OPEN_FLAG_SECOND_DRAWNER)) {
                return 1;
            }
            return 0;
        }
        return 0;
    }

    public int getDrawerOpened(int number) {
        return getDrawerOpenedByNativeLib(number);
    }

    public int openDrawer(int number) {
        return openDrawerByNativeLib(number);
    }

}
