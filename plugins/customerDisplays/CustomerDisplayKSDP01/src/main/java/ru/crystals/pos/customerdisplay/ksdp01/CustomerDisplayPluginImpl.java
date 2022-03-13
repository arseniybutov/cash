package ru.crystals.pos.customerdisplay.ksdp01;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.commons.JAXBContextFactory;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;
import ru.crystals.pos.customerdisplay.JuristicPerson;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayException;
import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.ksdp01.templates.parser.Screen;
import ru.crystals.pos.customerdisplay.ksdp01.templates.parser.Templates;
import ru.crystals.pos.customerdisplay.plugin.CustomerDisplayPluginAbstract;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.utils.SerialPortAdapter;
import ru.crystals.pos.utils.SerialPortAdapterObservable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class CustomerDisplayPluginImpl extends CustomerDisplayPluginAbstract implements Observer {
    private final Logger log = LoggerFactory.getLogger(CustomerDisplayPluginImpl.class);
    public static final String SEPARATOR = File.separator;
    public static final String FILE_PATH_PREFIX = "modules" + SEPARATOR + "customerDisplay" + SEPARATOR;
    private SerialPortAdapter serialPortAdapter = new SerialPortAdapter();
    private SerialPortAdapterObservable serialPortAdapterObservable;
    private boolean isDeviceAvailable = true;
    private HashMap<String, Screen> templatesMap = new HashMap<>();
    private long lastModified = 0L;
    private String templatesFilePath = FILE_PATH_PREFIX + "templates" + SEPARATOR + "viki-templates.xml";
    private boolean emulator = false;

    @Override
    public void open() throws CustomerDisplayPluginException {
        if (!emulator) {
            try {
                serialPortAdapterObservable.openPort();
            } catch (Exception e) {
                // Блок никогда не отработает. Все исключения обрабатываются в адаптере.
                log.error("", e);
            }
        }
    }

    @Override
    public void close() throws CustomerDisplayPluginException {
        if (!emulator) {
            serialPortAdapterObservable.close();
        }
    }

    @Override
    public void executeCommand(byte[] command, byte[] param) throws CustomerDisplayPluginException {
        if (!emulator) {
            if (isDeviceAvailable) {
                byte[] result = new byte[command.length + (param != null ? param.length : 0)];
                System.arraycopy(command, 0, result, 0, command.length);
                if (param != null) {
                    System.arraycopy(param, 0, result, command.length, param.length);
                }
                try {
                    serialPortAdapterObservable.write(result);
                } catch (Exception e) {
                    log.error("", e);
                    isDeviceAvailable = false;
                }
            } else {
                log.error("Device is not available");
            }
        }
    }

    @Override
    public void setPort(String port) {
        if (!emulator) {
            serialPortAdapterObservable = new SerialPortAdapterObservable(serialPortAdapter);
            serialPortAdapterObservable.addObserver(this);
            serialPortAdapterObservable.setSettingsPortID(port);
            serialPortAdapterObservable.setLogger(log);
            super.setPort(port);
        }
    }

    @Override
    public void setBaudRate(int baudRate) {
        serialPortAdapter.setBaudRate(baudRate);
        super.setBaudRate(baudRate);
    }

    @Override
    public void setDataBits(int dataBits) {
        serialPortAdapter.setDataBits(dataBits);
        super.setDataBits(dataBits);
    }

    @Override
    public void setStopBits(int stopBits) {
        serialPortAdapter.setStopBits(stopBits);
        super.setStopBits(stopBits);
    }

    @Override
    public void setParity(int parity) {
        serialPortAdapter.setParity(parity);
        super.setParity(parity);
    }

    @Override
    public void configureDisplay() {
        reloadTemplates();
    }

    private synchronized void setImage(byte[] imgBytes) throws CustomerDisplayPluginException {
        byte[] hex = {0x1C, 0x31};
        log.debug("Setting {} bytes image", hex);
        executeCommand(hex, imgBytes);
    }

    @Override
    public void addPositionStart(PositionEntity position, PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPositionStart".toLowerCase()), map));
    }

    @Override
    public void addPositionEnd(PositionEntity position, PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPositionEnd".toLowerCase()), map));
    }

    @Override
    public void addPositionReturnStart(PositionEntity position, Integer baseProductId, String baseCheck, PurchaseEntity purchase,
                                       HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPositionReturnStart".toLowerCase()), map));
    }

    @Override
    public void addPositionReturnEnd(PositionEntity position, Integer baseProductId, String baseCheck, PurchaseEntity purchase,
                                     HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPositionReturnEnd".toLowerCase()), map));
    }

    @Override
    public void addPositionReturnByPos(PositionEntity position, Integer baseProductId, String baseCheck, PurchaseEntity purchase,
                                       HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPositionReturnByPos".toLowerCase()), map));
    }

    @Override
    public void deletePosition(PositionEntity position, PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("deletePosition".toLowerCase()), map));
    }

    @Override
    public void deletePositionReturn(PositionEntity position, Integer baseProductId, String baseCheck, PurchaseEntity purchase,
                                     HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("deletePositionReturn".toLowerCase()), map));
    }

    @Override
    public void addPaymentStart(PaymentEntity payment, PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPaymentStart".toLowerCase()), map));
    }

    @Override
    public void addPaymentEnd(PaymentEntity payment, PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPaymentEnd".toLowerCase()), map));
    }

    @Override
    public void addPaymentReturnStart(PaymentEntity payment, Integer basePaymentId, String baseCheck, PurchaseEntity purchase,
                                      HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPaymentReturnStart".toLowerCase()), map));
    }

    @Override
    public void addPaymentReturnEnd(PaymentEntity payment, PurchaseEntity purchase, Integer basePaymentId, PurchaseEntity basePurchase,
                                    HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPaymentReturnEnd".toLowerCase()), map));
    }

    @Override
    public void productInformation(ProductEntity product, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("productInformation".toLowerCase()), map));
    }

    @Override
    public void addProductNotification(ProductEntity product, Integer type, String message, HashMap<String, Object> map)
            throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addProductNotification".toLowerCase()), map));
    }

    @Override
    public void addPaymentNotification(PaymentEntity payment, Boolean isInterrupted, String message, HashMap<String, Object> map)
            throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("addPaymentNotification".toLowerCase()), map));
    }

    @Override
    public void discountNotification(DiscountPurchaseEntity discount, PurchaseEntity purchase, HashMap<String, Object> map)
            throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("discountNotification".toLowerCase()), map));
    }

    @Override
    public void checkTotalNotification(Long changeSum, PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("checkTotalNotification".toLowerCase()), map));
    }

    @Override
    public void checkTotalReturnNotification(PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("checkTotalReturnNotification".toLowerCase()), map));
    }

    @Override
    public void checkCanceledNotification(PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("checkCanceledNotification".toLowerCase()), map));
    }

    @Override
    public void checkDeferredNotification(PurchaseEntity purchase, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("checkDeferredNotification".toLowerCase()), map));
    }

    @Override
    public void welcomeNotification(JuristicPerson juristicPerson, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("welcomeNotification".toLowerCase()), map));
    }

    @Override
    public void serviceNotification(JuristicPerson juristicPerson, HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("serviceNotification".toLowerCase()), map));
    }

    @Override
    public void textNotification(HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("textNotification".toLowerCase()), map));
    }

    @Override
    public void phoneNotification(HashMap<String, Object> map) throws CustomerDisplayException {
        reloadTemplates();
        setImage(TemplateProcessing.processTemplate(templatesMap.get("phoneNotification".toLowerCase()), map));
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {0x1B, 0x5B, 0x32, 0x4A};
        executeCommand(hex, null);
    }

    @Override
    public void verifyDevice() throws CustomerDisplayPluginException {
        // Nothing to see here
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    private void reloadTemplates() {
        try {
            File file = new File(templatesFilePath);
            if (file.lastModified() > getLastModified()) {
                try (FileInputStream is = new FileInputStream(file)) {
                    ByteArrayInputStream bais = null;
                    if (is.available() > 0) {
                        byte[] bytes = new byte[is.available()];
                        is.read(bytes);
                        bais = new ByteArrayInputStream(bytes);
                    }
                    Templates templates = parseTemplates(bais);
                    setLastModified(file.lastModified());
                    if (templates != null) {
                        for (Screen scr : templates.getScreen()) {
                            templatesMap.put(scr.getName().toLowerCase(), scr);
                        }
                    }
                }
            }
        } catch (IOException | JAXBException e) {
            log.error("", e);
        }
    }

    private Templates parseTemplates(ByteArrayInputStream xmlTemplate) throws JAXBException, IOException {
        Unmarshaller u;
        JAXBContext context;
        Templates result;

        if (xmlTemplate == null || xmlTemplate.available() == 0) {
            return null;
        }
        try {
            context = JAXBContextFactory.getContext("ru.crystals.pos.customerdisplay.ksdp01.templates.parser");
            u = context.createUnmarshaller();
            result = (Templates) u.unmarshal(xmlTemplate);
        } finally {
            xmlTemplate.close();
        }
        return result;
    }

    @Override
    public void update(Observable o, Object arg) {
        isDeviceAvailable = (Boolean) arg;
        if (isDeviceAvailable) {
            configureDisplay();
        } else {
            log.error("Device is not available");
        }
    }

    public boolean isEmulator() {
        return emulator;
    }

    public void setEmulator(boolean emulator) {
        this.emulator = emulator;
        if (emulator) {
            new CDEmulatorView();
        }
    }
}
