package ru.crystals.pos.service;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.crystals.cm.utils.JAXBContextFactory;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.configurator.core.CoreConfigurator;
import ru.crystals.pos.configurator.jaxb.externalSystem.XmlExternalSystems;
import ru.crystals.pos.configurator.jaxb.moduleConfig.CProperty;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.transport.TransportMessageEvent;
import ru.crystals.pos.transport.TransportMessageType;
import ru.crystals.setretailx.cash.settings.xml.datatypes.XmlModuleConfig;
import ru.crystals.setretailx.cash.settings.xml.datatypes.XmlProperty;
import ru.crystals.setretailx.cash.settings.xml.datatypes.XmlSettingsType;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExtServiceConfigManagerImpl implements ExtServiceConfigManager, TransportMessageEvent {

    private static final Logger log = LoggerFactory.getLogger(ExtServiceConfigManagerImpl.class);

    /**
     * Для чтения настроек из реестра внешних процессингов
     */
    private final CoreConfigurator configurator;

    /**
     * Для чтения настроек из sales_management_properties
     */
    private final PropertiesManager propertiesManager;

    private final TechProcessEvents tpe;

    private final InternalCashPoolExecutor poolExecutor;

    private final Set<SettingsHandler> handlers = new HashSet<>();

    @Autowired
    public ExtServiceConfigManagerImpl(CoreConfigurator configurator,
                                       PropertiesManager propertiesManager,
                                       TechProcessEvents tpe,
                                       InternalCashPoolExecutor poolExecutor) {
        this.configurator = configurator;
        this.propertiesManager = propertiesManager;
        this.tpe = tpe;
        this.poolExecutor = poolExecutor;
    }

    @PostConstruct
    void init() {
        // подписка на события прихода настроек данных с сервера
        tpe.addTransportMessageListener(this);
    }

    @Override
    public synchronized void subscribe(SettingsHandler settingsHandler) {
        handlers.add(settingsHandler);
        reloadSettings(settingsHandler);
    }

    @Override
    public void eventTransportMessageReceived(TransportMessageType type) {
        if (TransportMessageType.EXTERNAL_SYSTEM_REGISTER != type) {
            return;
        }
        if (handlers.isEmpty()) {
            return;
        }
        /*
        как было раньше - см. ExternalService (фактически конфигурация происходила в момент первого использования)
        хочется к моменту использования уже иметь переконфигуренный сервис, чтобы:
        а) не добавлять ненужные задержки,
        б) получить проблемы конфигурации в логах в момент ее изменения, по факту получения, а не когда-то в будущем

        Идеально было бы не перечитывать настройки, если они не изменились, а также в самом событии сразу слать фактические изменения
        (сейчас мы в момент события записывам конфиг в файл, а тут будем его сразу читать)
        */
        poolExecutor.execute(this::reloadSettings);
    }

    private void reloadSettings() {
        final XmlExternalSystems extSysRegister;
        try {
            extSysRegister = configurator.getExternalSystemsRegister();
        } catch (Exception e) {
            log.error("Error on reload external system register", e);
            return;
        }
        final Set<String> providerNames = handlers.stream()
                .map(SettingsHandler::getProviderName)
                .collect(Collectors.toSet());
        final Map<String, Map<String, String>> propertiesByProvider = extSysRegister.getServiceProviders()
                .stream()
                .filter(p -> providerNames.contains(p.getProcessingProvider().getName()))
                .collect(Collectors.toMap(p -> p.getProcessingProvider().getName(),
                        p -> mapProperties(p.getProperties())));
        for (SettingsHandler handler : handlers) {
            reloadSettings(handler, propertiesByProvider.getOrDefault(handler.getProviderName(), Collections.emptyMap()));
        }
    }

    private void reloadSettings(SettingsHandler handler) {
        try {
            reloadSettings(handler, null);
        } catch (Exception e) {
            log.trace("Error on reloading settings for provider {}", handler.getProviderName(), e);
        }
    }

    private Map<String, String> mapProperties(List<CProperty> properties) {
        return properties.stream().collect(Collectors.toMap(CProperty::getKey, CProperty::getValue, (a, b) -> a));
    }

    /**
     * @param extSysProperties уже считанные параметры из реестра внешних процессингов (один раз для всех при событии получения настроек)
     */
    private void reloadSettings(SettingsHandler handler, Map<String, String> extSysProperties) {
        log.trace("Reloading settings for provider {}", handler.getProviderName());

        // 1. читаем локальные (не модифицируемые) настройки:
        Map<String, String> resultSettings = new HashMap<>(readLocalSettings(handler));

        // 2. читаем настройки процессинга (те, что были отмодифицированы и пришли с сервера):
        //  настройки с сервера более приоритетны. нормально. если перетрут локальные
        if (extSysProperties != null) {
            resultSettings.putAll(extSysProperties);
        } else {
            resultSettings.putAll(readIncomingFromExtSystemRegister(handler));
        }
        resultSettings.putAll(readIncomingFromSMP(handler));

        handler.onSettingsReloaded();
        setSettings(handler, resultSettings);

        log.trace("Settings reloaded for provider {}: {}", handler.getProviderName(), resultSettings);
    }

    /**
     * считает локальные настройки
     */
    private Map<String, String> readLocalSettings(SettingsHandler handler) {
        XmlModuleConfig xmlConfig = readLocalSettingsXml(handler);
        if (xmlConfig == null) {
            return Collections.emptyMap();
        }

        // интересуют только свойства первого/рутового уровня
        // ключ, значение:
        Map<String, String> props = new HashMap<>();
        for (XmlProperty p : xmlConfig.getProperties()) {
            if (p == null || StringUtils.isBlank(p.getKey())) {
                log.error("local settings file is corrupted: an empty property was detected (provider {})", handler.getProviderName());
                continue;
            }
            props.put(p.getKey(), p.getValue());
        }
        return props;
    }

    /**
     * Считает локальные (не настраиваемые на сервере) настройки из {@link SettingsHandler#getConfigFileName()}
     *
     * @return {@code null}, если не удалось считать файл с настройками по любой причине
     */
    private XmlModuleConfig readLocalSettingsXml(SettingsHandler handler) {
        if (handler.getConfigFileName() == null) {
            return null;
        }
        try (InputStream is = new FileInputStream(new File(Constants.PATH_CONFIG_MODULES + handler.getConfigFileName()))) {
            JAXBContext ctx = JAXBContextFactory.getContext(XmlSettingsType.class.getPackage().getName());
            Unmarshaller u = ctx.createUnmarshaller();
            return (XmlModuleConfig) u.unmarshal(is);
        } catch (Exception e) {
            log.error("Unable to load local settings from {} (provider {})", handler.getConfigFileName(), handler.getProviderName(), e);
        }
        return null;
    }

    private Map<String, String> readIncomingFromExtSystemRegister(SettingsHandler handler) {
        try {
            return configurator.getProcessingProperties(handler.getProviderName());
        } catch (Exception t) {
            log.error("failed to parse ext. processing properties (provider {})", handler.getProviderName(), t);
        }
        return Collections.emptyMap();
    }

    private Map<String, String> readIncomingFromSMP(SettingsHandler handler) {
        if (handler.getSmpModuleName() == null) {
            return Collections.emptyMap();
        }
        try {
            return propertiesManager.getByModulePlugin(handler.getSmpModuleName(), "");
        } catch (Exception t) {
            log.error("failed to read sales_management_properties for processing (provider {})", handler.getProviderName(), t);
        }
        return Collections.emptyMap();
    }

    private void setSettings(SettingsHandler handler, Map<String, String> properties) {
        if (MapUtils.isEmpty(properties)) {
            return;
        }
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key;
            if (entry == null || (key = entry.getKey()) == null) {
                continue;
            }
            String value = entry.getValue();

            try {
                if (!handler.setSetting(key, value)) {
                    log.warn("unknown property in the settings file was detected (provider {}): key: '{}'; value: {}", handler.getProviderName(), key, value);
                }
            } catch (Exception t) {
                log.error("failed to parse property (provider {}): key: '{}'; value: {}", handler.getProviderName(), key, value, t);
            }
        }
    }
}
