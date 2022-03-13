package ru.crystals.pos.plugins.purchases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.events.PurchaseStateListener;
import ru.crystals.pos.plugins.converters.ConvertersFactory;
import ru.crystals.pos.plugins.purchases.v1.converter.PurchaseConverter;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.ws.handlers.SOAPWSHandler;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Класс "поднимает" клиент для веб-сервиса быстрой отправки данных непосредственно с кассы Реализовано в рамках фичи SRTB-816
 */
@Component
public class PurchaseExporterClient implements PurchaseStateListener {
    private static final Logger LOG = LoggerFactory.getLogger(PurchaseExporterClient.class);
    protected static final String REPORTS_SERVICE_CONFIG_PATH = "modules/wsExporter/purchaseService.properties";
    protected static final String REPORTS_SERVICE_WSDL_PATH = "modules/wsExporter/wsdl/Set10PurchasesService.wsdl";
    protected static final String DEFAULT_NAMESPACE = "http://purchases.erpi.crystals.ru";
    protected static final String AUTH_USERNAME = "auth.username";
    protected static final String AUTH_PASSWORD = "auth.password";
    protected static final String SERVICE_NAMESPACE = "service.namespace";
    protected static final String SERVICE_WSDL_LOCATION = "service.location";
    protected static final String DEFAULT_SERVICE = "Set10PurchasesService";
    protected static final String DEFAULT_PORT = "Set10PurchasesPort";
    protected static final String VERSION_1 = "1.0";
    private Set10Purchases port;

    /**
     * Настройки кассы
     */
    @Autowired
    Properties cashProperties;

    /**
     * События техпроцесса
     */
    @Autowired
    TechProcessEvents techProcessEvents;

    private PurchaseConverter purchaseConverter;

    public void sendPurchase(PurchaseEntity purchase) {
        sendPurchases(Arrays.asList(purchase));
    }

    public void sendPurchases(List<PurchaseEntity> purchaseEntities) {
        try {
            byte[] data = purchaseConverter.convertData(purchaseEntities);
            port.processPurchases(data, purchaseConverter.getVersion());
            LOG.info("Purchase list {} successfully sent", purchaseEntities);
        } catch (Exception e) {
            LOG.error("Failed to send purchase list: {}", purchaseEntities);
            LOG.error("Error: ", e);
        }
    }

    /**
     * При старте кассы инициализируем модуль, соединяясь с веб-сервисом и подписываяь на события техпроцесса в случае ошибки - залоггируем и
     * продолжим работы. И подписки на техпроцесс не будет.
     */
    @PostConstruct
    public void init() {
        QName serviceName;
        QName portName;
        String location;


        /**
         * У нас пока нет других реализаций, и, возможно, не будет. поэтому не нужно пока делать настройку в БД или
         * еще где и соответственно реализовывать выбор версии. Так что тут хардкод
         */
        purchaseConverter = (PurchaseConverter) ConvertersFactory.getPurchasesConverters().get(VERSION_1);


        /**
         * Если включена соответствующая настройка, попытаемся законнектиться к внешнему веб-сервису
         */
        if (cashProperties.getExportPurchaseToERP()) {

            try {
                File propsFile = new File(REPORTS_SERVICE_CONFIG_PATH);

                java.util.Properties props = new java.util.Properties();
                props.load(new FileInputStream(propsFile));

                String clientNamespace = props.getProperty(SERVICE_NAMESPACE);
                location = props.getProperty(SERVICE_WSDL_LOCATION);
                serviceName = new QName(DEFAULT_NAMESPACE, DEFAULT_SERVICE);

                //создаем клиент
                Service service = Service.create(new File(REPORTS_SERVICE_WSDL_PATH).toURI().toURL(), serviceName);
                portName = new QName(DEFAULT_NAMESPACE, DEFAULT_PORT);

                port = service.getPort(portName, Set10Purchases.class);

                //переопределяем адрес endpoint'а
                Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
                requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, location);

                //авторизация
                requestContext.put(BindingProvider.USERNAME_PROPERTY, props.getProperty(AUTH_USERNAME));
                requestContext.put(BindingProvider.PASSWORD_PROPERTY, props.getProperty(AUTH_PASSWORD));

                Binding binding = ((BindingProvider) port).getBinding();
                List<Handler> handlerList = binding.getHandlerChain();
                handlerList.add(new SOAPWSHandler(DEFAULT_NAMESPACE, clientNamespace));
                binding.setHandlerChain(handlerList);

                //Подпишемся на события техпроцесса, если удалось соединиться с вебсервисом выгрузки
                if (port != null) {
                    techProcessEvents.addPurchaseStateListener(this);
                }
                LOG.info("Purchase ws exporter started successfully!");
            } catch (Exception e) {
                LOG.error("Failed to initialize purchase exporter ws client: ", e);
            }
        } else {
            LOG.info("Purchase WS export disabled");
        }
    }

    /**
     * Обработчик вызывается перед фискализацией чека Выгружать нужно только чеки продажи
     */
    @Override
    public void eventPurchaseEnd(PurchaseEntity purchase) {
        LOG.info("New purchase event handle: {}", purchase);
        LOG.info("isSale: {}", purchase.isSale());
        if (purchase.isSale()) {
            sendPurchase(purchase);
        }
    }

    @Override
    public void eventPurchaseDefer(PurchaseEntity purchase) {
        //nothing to do
    }
}
