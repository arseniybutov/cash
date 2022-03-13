package ru.crystals.pos.customerdisplay.ncr5976;

import jpos.JposException;
import jpos.LineDisplay;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.customerdisplay.CustomerDisplayImpl;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.ncr5976.core.ResBundleCustomerDisplayNCR;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class CustomerDisplayPluginImplNCR5976 extends TextCustomerDisplayPluginAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(CustomerDisplayImpl.class);

    private static final int MAX_CHAR_PER_LINE = 20;
    private static boolean requireNCRRetailStart = true;
    private static boolean addHookShutdownNCRRetail = true;
    private String service;
    private static final String START = "start";
    private static final String STOP = "stop";
    private long runServiceTimeout = 60000;

    private LineDisplay display;

    public CustomerDisplayPluginImplNCR5976() {
        display = new LineDisplay();
    }

    private void controlService(final String command) throws CustomerDisplayPluginException {
        LOG.info("NCRRetail - {}", command);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Integer> task = executor.submit(() -> {
                if (SystemUtils.IS_OS_WINDOWS) {
                    return 0;
                }
                String[] commandForRun = {service, command};
                return Runtime.getRuntime().exec(commandForRun).waitFor();
            });
            if (runServiceTimeout > 0) {
                task.get(runServiceTimeout, TimeUnit.MILLISECONDS);
            } else {
                task.get();
            }
            LOG.trace("Executable is finished");
        } catch (TimeoutException e) {
            LOG.error("Error processing executable", e);
            throw new CustomerDisplayPluginException(ResBundleCustomerDisplayNCR.getString("SERVICE_TIMEOUT"));
        } catch (Exception e) {
            LOG.error("Error processing executable", e);
            throw new CustomerDisplayPluginException(ResBundleCustomerDisplayNCR.getString("SERVICE_ERROR"));
        }
    }

    @Override
    public synchronized void open() throws CustomerDisplayPluginException {
        service = getServicePath();
        try {
            if (requireNCRRetailStart) {
                controlService(START);
                //Останов при перезагрузке кассы
                if (addHookShutdownNCRRetail) {
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            controlService(STOP);
                        } catch (CustomerDisplayPluginException e) {
                            LOG.error(e.getMessage());
                        }
                    }));
                    addHookShutdownNCRRetail = false;
                }
                requireNCRRetailStart = false;
            }
            display.open("LineDisplay.1");
            display.claim(5000);
            display.setDeviceEnabled(true);
        } catch (JposException e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    @Override
    public void close() throws CustomerDisplayPluginException {
        try {
            display.close();
        } catch (JposException e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    @Override
    protected void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        try {
            byte[] bytes = text.getBytes("CP866");
            char[] chars = new char[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                chars[i] = (char) bytes[i];
            }
            display.displayTextAt(row, column, new String(chars), 0);
        } catch (JposException | UnsupportedEncodingException e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        try {
            display.clearText();
        } catch (JposException e) {
            throw new CustomerDisplayPluginException(e.getMessage());
        }
    }
}
