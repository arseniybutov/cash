package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.simurg.SimurgShtrihConnector;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика по созданию {@link ShtrihConnector средств} для инфо-обмена с ФР семейства "Штрих".
 *
 * @author aperevozchikov
 */
public abstract class ShtrihConnectorFactory {
    private static final Logger log = LoggerFactory.getLogger(ShtrihConnectorFactory.class);

    /**
     * Существующие разновидности коннекторов (за исключением базового).
     * <p/>
     * Ключ - код модели ФР (например, "shtrih-fr-k"), значение - сама реализация коннектора для этой модели
     */
    private static Map<String, ShtrihConnector> CONNECTORS;
    static {
        CONNECTORS = new HashMap<>();
        // actual
        CONNECTORS.put("shtrih-mini-01f", new ShtrihMini01FConnector());
        CONNECTORS.put("retail-01f", new ShtrihRetailFConnector());
        CONNECTORS.put("shtrih-m-01f", new ShtrihMini01FConnector());
        CONNECTORS.put("simurg", new SimurgShtrihConnector());
        CONNECTORS.put("shtrih_kyrgyz", new ShtrihMiniFRKConnector());

        // rndis
        CONNECTORS.put("retail-01f-rndis", new DefaultShtrihRNDISConnector());
        CONNECTORS.put("shtrih-m-01f-rndis", new DefaultShtrihRNDISConnector());
        CONNECTORS.put("shtrih-light-01f-rndis", new DefaultShtrihRNDISConnector());
    }

    /**
     * Создаст, настроит и вернет {@link ShtrihConnector коннектор} для "общения" с указанной моделью ФР "Штрих".
     *
     * @param deviceId
     *            идентификатор модели ФР, коннектор для общения с которым надо вернуть
     *            <p/>
     *            Например, "shtrih-m-ptk", "retail-01k", "shtrih-fr-k" и проч.
     * @param properties
     *            настройки, что должен получить этот возвращаемый коннектор; если <code>null</code> (не рекомендуется!) возвращенный коннектор будет
     *            иметь дефолтные настройки
     * @return не <code>null</code> - в крайнем случае будет возвращена некая "базовая" реализация коннектора
     */
    public static ShtrihConnector createConnector(String deviceId, ShtrihConnectorProperties properties) {
        ShtrihConnector result = null;

        log.trace("entering createConnector(String, ShtrihConnectorProperties). The arguments are: deviceId [{}], properties [{}]",
            deviceId, properties);

        // 1. сначала определимся с реализацией
        if (deviceId == null) {
            log.warn("createConnector(String, ShtrihConnectorProperties): the \"deviceId\" is NULL. The default connector will be returned!");
        } else {
            result = CONNECTORS.get(deviceId.toLowerCase());
        }
        if (result == null) {
            // нормально: возможно, для этой модели подойдет и базовая реализация коннектора
            log.info("device-specific connector for device [id: \"{}\"] was not detected. The default one will be used", deviceId);
            result = new BaseShtrihConnector();
        }

        // 2. а теперь настроим этот коннектор
        if (properties != null) {
            BaseShtrihConnector resultAsBase = (BaseShtrihConnector) result;
            resultAsBase.setPortName(properties.getPortName());
            resultAsBase.setBaudRate(properties.getBaudRate());
            resultAsBase.setIpAddress(properties.getIpAddress());
            resultAsBase.setTcpPort(properties.getTcpPort());
            resultAsBase.setNeedRevertBytes(properties.isNeedRevertBytes());
            resultAsBase.setParametersFilePath(properties.getParametersFilePath());
            resultAsBase.setMaxCharsInRow(properties.getMaxCharsInRow());
            resultAsBase.setPrintStringFont(properties.getPrintStringFont());
            resultAsBase.setHighQualityGraphics(properties.isHighQualityGraphics());
            resultAsBase.setPrintLineTime(properties.getPrintLineTime());
            resultAsBase.setMaxScale(properties.getMaxScale());
            resultAsBase.setBarcodeHeight(properties.getBarcodeHeight());
            resultAsBase.setPassword(properties.getPassword());
            resultAsBase.setMaxLoadGraphicsLines(properties.getMaxLoadGraphicsLines());
            resultAsBase.setImageFirstLine(properties.getImageFirstLine());
            resultAsBase.setImageLastLine(properties.getImageLastLine());
            resultAsBase.setLineSpacingSupported(properties.isLineSpacingSupported());
            resultAsBase.setByteWaitTime(properties.getByteWaitTime());
            resultAsBase.setPrintLegalEntityHeader(properties.isPrintLegalEntityHeader());
        } else {
            log.error("createConnector(String, ShtrihConnectorProperties): the \"properties\" is NULL. The default properties will be applied!");
        }

        log.trace("leaving createConnector(String, ShtrihConnectorProperties). The result is: {}", result);

        return result;
    }
}