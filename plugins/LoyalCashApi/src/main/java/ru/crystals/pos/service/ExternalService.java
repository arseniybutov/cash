package ru.crystals.pos.service;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import ru.crystals.cm.utils.JAXBContextFactory;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.configurator.core.CoreConfigurator;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Родительский класс для классов доступа к внешним сервисам с реализацией механизма обработки серверных и локальных настроек
 * Created by v.osipov on 03.09.2017.
 */
public abstract class ExternalService<T extends ExtServiceSettings> implements TransportMessageEvent {

    /**
     * Для чтения настроек из реестра внешних процессингов
     */
    @Autowired
    private CoreConfigurator configurator;

    /**
     * Для чтения настроек из sales_management_properties
     */
    @Autowired
    private PropertiesManager propertiesManager;

    @Autowired
    private TechProcessEvents techProcessEvents;

    /**
     * Флаг-признак: надо перечитать настройки процессинга: т.к. они могли обновиться
     */
    protected AtomicBoolean needReloadSettings = new AtomicBoolean(true);

    /**
     * Настройки работы с сервисом
     */
    protected T settings;

    /**
     * Логгер
     */
    public abstract Logger getLog();

    /**
     * Имя файла (в каталоге {@link Constants#PATH_CONFIG_MODULES}) в котором хранятся
     * не-модифицируемые (через визуализацию) настройки соединения с процессингом
     */
    public abstract String getConfigFile();

    /**
     * Код данного процессинга в реестре внешних процессингов
     */
    public abstract String getProviderName();

    /**
     * Включен/доступен ли процессинг
     */
    public abstract boolean isEnabled();

    /**
     * Инициализация и чтение настроек
     */
    @PostConstruct
    public void startService() {
        getLog().trace("entering init()");

        // 1. считывание настроек
        reloadSettings();

        // 2. подписка на события прихода настроек данных с сервера
        techProcessEvents.addTransportMessageListener(ExternalService.this);

        getLog().trace("leaving init()");
    }

    @Override
    public void eventTransportMessageReceived(TransportMessageType type) {
        if (TransportMessageType.EXTERNAL_SYSTEM_REGISTER.equals(type)) {
            needReloadSettings.set(true);
        }
    }

    /**
     * Вернет настройки работы с сервисом
     *
     * @return не {@code null}
     */
    public T getSettings() {
        reloadSettings();
        return settings;
    }

    /**
     * Перечитывает настройки данного модуля.
     */
    protected synchronized void reloadSettings() {
        if (!needReloadSettings.getAndSet(false)) {
            // настройки актуальны - просто выйдем
            return;
        }
        // настройки [возможно] изменились - надо ..перенастроиться
        getLog().info("updating settings!");

        // 1. читаем локальные (не модифицируемые) настройки:
        settings = readLocalSettingsIntoObject();

        // 2. читаем настройки процессинга (те, что были отмодифицированы и пришли с сервера):
        //  настройки с сервера более приоритетны. нормально. если перетрут локальные
        readExternalProcessingSettingsInto(settings);

        getLog().trace("settings were reloaded. The result is: {}", settings);
    }

    /**
     * считает локальные настройки и вернет их в виде понятного нам объекта.
     *
     * @return не {@code null}; в крайнем случае (если файл с настройками не удалось считать/распарсить, например) вернет пустой (дефолтный) объект
     */
    protected T readLocalSettingsIntoObject() {
        T result = createSettings();

        getLog().trace("entering readLocalSettingsIntoObject()");
        XmlModuleConfig xmlConfig = readLocalSettings();
        if (xmlConfig == null) {
            getLog().warn("leaving readLocalSettingsIntoObject(). FAILED TO READ LOCAL SETTINGS FILE. Maybe it is absent?");
            return result;
        }

        // интересуют только свойства первого/рутового уровня
        // ключ, значение:
        Map<String, Object> props = new HashMap<>();
        for (XmlProperty p : xmlConfig.getProperties()) {
            if (p == null || StringUtils.isBlank(p.getKey())) {
                getLog().error("local settings file is corrupted: an empty property was detected");
                continue;
            }
            props.put(p.getKey(), p.getValue());
        } // for p

        setSettings(result, props);

        getLog().trace("leaving readLocalSettingsIntoObject(). the result is: {}", result);

        return result;
    }

    /**
     * Считает локальные (не настраиваемые на сервере) настройки из {@link #getConfigFile()}
     *
     * @return {@code null}, если не удалось считать файл с настройками по любой причине
     */
    private XmlModuleConfig readLocalSettings() {
        XmlModuleConfig result = null;

        if (getConfigFile() != null) {
            try (InputStream is = new FileInputStream(new File(Constants.PATH_CONFIG_MODULES + getConfigFile()))) {
                JAXBContext ctx = JAXBContextFactory.getContext(XmlSettingsType.class.getPackage().getName());
                Unmarshaller u = ctx.createUnmarshaller();

                result = (XmlModuleConfig) u.unmarshal(is);
            } catch (Exception t) {
                getLog().error("failed to read " + getConfigFile(), t);
            }
        }

        return result;
    }

    /**
     * Считает настройки внешнего процессинга в указанный объект-настройку.
     *
     * @param settings объект-настройка, куда считать настройки внешнего процессинга
     */
    protected void readExternalProcessingSettingsInto(T settings) {
        if (settings == null) {
            getLog().error("the object to read ext/ processing settings into is NULL!");
            return;
        }

        Map<String, String> extProperties = null;
        try {
            extProperties = configurator.getProcessingProperties(getProviderName());
        } catch (Exception t) {
            getLog().error("failed to parse ext. processing properties", t);
        }

        Map<String, String> salesManagementProperties = null;
        if (getSalesManagementPropertiesModuleName() != null) {
            try {
                salesManagementProperties = propertiesManager.getByModulePlugin(getSalesManagementPropertiesModuleName(), "");
            } catch (Exception t) {
                getLog().error("failed to read sales_management_properties for processing", t);
            }
        }

        Map<String, Object> properties = new HashMap<>();
        if (MapUtils.isNotEmpty(extProperties)) {
            properties.putAll(extProperties);
        } else if (MapUtils.isNotEmpty(salesManagementProperties)) {
            properties.putAll(salesManagementProperties);
        }

        setSettings(settings, properties);
        onSettingsUpdated(settings);
    }

    /**
     * Перельет указанные свойства в указанный объект-настройку.
     *
     * @param settings   объект-настройка, куда надо перелить свойства
     * @param properties свойства, что надо "перелить"
     */
    @SuppressWarnings("unchecked")
    private void setSettings(T settings, Map<String, Object> properties) {
        if (settings == null || MapUtils.isEmpty(properties)) {
            return;
        }

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key;
            if (entry == null || (key = entry.getKey()) == null) {
                continue;
            }
            Object value = entry.getValue();

            try {
                setSetting(settings, key, value);
            } catch (Exception t) {
                getLog().error(String.format("failed to parse property: key: \"%s\"; value: %s", key, value), t);
            }
        } // for key
    }

    /**
     * Создание объекта настроек
     */
    protected abstract T createSettings();

    /**
     * Установка настройки
     *
     * @param settings все настройки
     * @param key      название настройки
     * @param value    устанавливаемое значение
     */
    protected abstract void setSetting(T settings, String key, Object value);

    /**
     * Получить название модуля в таблице sales_management_properties.
     *
     * @return если null, то данный сервис не поддерживает настройки через sales_management_properties
     */
    protected String getSalesManagementPropertiesModuleName() {
        // по умолчанию никто не поддерживает sales_management_properties
        return null;
    }

    /**
     * Чтобы перестать ссылаться на settings
     */
    protected void onSettingsUpdated(T settings) {

    }

}
