package ru.crystals.cards.internal.good.processing.i18n;

import com.google.common.base.Strings;
import java.util.Locale;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.crystals.util.ExtendedResourceBundleControl;

/**
 * i18n support
 */
public class Set5DCSellerResourceBundle {
    private static final Logger log = LoggerFactory.getLogger(Set5DCSellerResourceBundle.class);

    private static final String RESOURCE_BUNDLE_NAME = "set5-dc-seller-messages";

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
     * @return локализованное сообщение с указанным ключом в указанной локали
     */
    public static String getLocalValue(String key, Locale locale) {
        ResourceBundle resourceBundle;
        if (locale != null) {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, locale, new ExtendedResourceBundleControl());
        } else {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, new ExtendedResourceBundleControl());
        }

        String result = resourceBundle.getString(key);
        if (Strings.isNullOrEmpty(result)) {
            log.info("Couldn't get localized string for key: " + key);
        }
        return result;
    }
}
