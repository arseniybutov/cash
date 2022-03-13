package ru.crystals.pos.plugins.converters;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import ru.crystals.plugins.interfaces.converters.IVersionGetter;

/**
 * Фабрика конверторов для различных типом выгружаемых данных и версий
 * Реализации "подтягиваются" через ServiceLoader
 */
public class ConvertersFactory {
    private static Map<String, IPurchasesConverter> purchasesConverters;

    static {
        purchasesConverters = loadConverters(IPurchasesConverter.class);
    }

    private static <T extends IVersionGetter> Map<String, T> loadConverters(Class<T> converter){
        Map<String, T> converterMap = new HashMap<>();
        ServiceLoader<T> converters = ServiceLoader.load(converter);
        for (Iterator<T> it = converters.iterator(); it.hasNext(); ) {
            T conv = it.next();
            if (conv != null) {
                converterMap.put(conv.getVersion(), conv);
            }
        }
        return converterMap;
    }

    /**
     * Получение всех возможных конверторов для данного типа выгружаемых данных
     * @return Map с конверторами по версиям
     */
    public static Map<String, IPurchasesConverter> getPurchasesConverters() {
        return purchasesConverters;
    }
}
