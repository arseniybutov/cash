package ru.crystals.pos.fiscalprinter.shtrihminifrk.utils;

import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.crystals.util.ExtendedResourceBundleControl;

/**
 * i18n - локализация команд и ошибок Штриха.
 * 
 * @author aperevozchikov
 */
public class ShtrihResourceBundleWorker {
    private static final Logger log = LoggerFactory.getLogger(ShtrihResourceBundleWorker.class);

    /**
     * Локализация Штриховых команд
     */
    private static final String COMMANDS_BUNDLE_NAME = "fiscal-printer-shtrih-commands";

    /**
     * Локализация Штриховых ошибок
     */
    private static final String ERRORS_BUNDLE_NAME = "fiscal-printer-shtrih-errors";

    public static String getLocalValue(String key) {
        return getLocalValue(key, null);
    }

    /**
     * Извлекает локализованное сообщение с указанным ключом в указанной локали.
     * 
     * @param key
     *            ключ локализованного сообщения, что надо вернуть
     * @param locale
     *            локаль в какой надо вернуть информацию, если <code>null</code> - будет использована локаль по-уму
     * @return <code>null</code>, если по указанному ключу для указанной локали не найден .. локализованный текст
     */
    public static String getLocalValue(String key, Locale locale) {
        String result = null;
        
        // сначала поищем в ошибках:
        result = getString(key, ERRORS_BUNDLE_NAME, locale);
        
        // а потом - в командах (если еще не нашли):
        if (result == null) {
            result = getString(key, COMMANDS_BUNDLE_NAME, locale);
        }
        
        return result;
    }
    
    /**
     * Вернет локализованное сообщени под указанным ключом для указанной локали с указанным базовым именем.
     * 
     * @param key 
     *            ключ локализованного сообщения, что надо вернуть
     * @param baseName
     *            базовое имя ресурса, что надо вернуть
     * @param locale
     *            локаль, в которой надо вернуть сообщение; если <code>null</code> - будет использована локаль по уму
     * @return <code>null</code>, если сообщение найти не удалось
     */
    private static String getString(String key, String baseName, Locale locale) {
        String result = null;
        
        ResourceBundle bundle = getBundle(baseName, locale);
        if (bundle == null) {
            // значит ,файла с локализацией не нашли - и в логе об это м ошибка уже есть
            return null;
        }
        try {
            result = bundle.getString(key);
        } catch (Throwable t) {
            // видимо, локализации для этого ключа нету - нормально: публичный метод к этому готов
            result = null;
        }

        return result;
    }

    /**
     * Вернет {@link ResourceBundle} для указанной локали с указанным базовым именем.
     * 
     * @param baseName
     *            базовое имя ресурса, что надо вернуть
     * @param locale
     *            локаль, для которой надо вернуть ресурс; если <code>null</code> - будет возвращена локаль по уму
     * @return <code>null</code>, если ресурс с указанным базовым именем для указанной локали отсутствует
     */
    private static ResourceBundle getBundle(String baseName, Locale locale) {
        ResourceBundle result = null;
        try {
            if (locale != null) {
                result = ResourceBundle.getBundle(baseName, locale, new ExtendedResourceBundleControl());
            } else {
                result = ResourceBundle.getBundle(baseName, new ExtendedResourceBundleControl());
            }
        } catch (Throwable t) {
            // видимо, ресурс отсутствует?
            log.error(String.format("failed to locate resource [base-name: %s; locale: %s]", baseName, locale), t);
            result = null;
        }
        
        return result;
    }
}