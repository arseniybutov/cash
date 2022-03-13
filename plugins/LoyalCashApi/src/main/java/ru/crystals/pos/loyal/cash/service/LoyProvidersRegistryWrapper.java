package ru.crystals.pos.loyal.cash.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.crystals.loyal.providers.LoyProvidersRegistry;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.properties.PropertiesManager;

import javax.annotation.PostConstruct;
import java.util.Objects;

/**
 * Хранитель действующего реестра провайдеров лояльности
 * Для связывания, чтобы получить реестр, нужно использовать этот компонент
 */
@Component
public class LoyProvidersRegistryWrapper {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String EXT_LOY_PROVIDERS_PROPERTY = "ext.loyalty.providers";

    /**
     * Реестр поставщиков "услуг лояльности". Изначально берётся из файла ext-loyalty-providers.xml, потом может обновиться из БД
     */
    @Autowired
    private LoyProvidersRegistry loyProviders;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PropertiesManager propertiesManager;

    private String extLoyProvidersString;

    /**
     * @return получение действующего реестра провайдеров лояльности
     */
    public LoyProvidersRegistry getLoyProviders() {
        return loyProviders;
    }

    @PostConstruct
    public void init() {
        String extLoyProvidersValue = propertiesManager.getProperty(Loyal.MODULE_NAME, null, EXT_LOY_PROVIDERS_PROPERTY, null);
        if (StringUtils.isNotBlank(extLoyProvidersValue) && setNewExtLoyaltyProviders(extLoyProvidersValue)) {
            extLoyProvidersString = extLoyProvidersValue;
        }
        // добавим слушатель изменений
        propertiesManager.addListener(Loyal.MODULE_NAME, null, changedProperties -> {
            String newValue = changedProperties.get(EXT_LOY_PROVIDERS_PROPERTY);
            if (StringUtils.isNotBlank(newValue) && !Objects.equals(extLoyProvidersString, newValue)
                    && setNewExtLoyaltyProviders(newValue)) {
                extLoyProvidersString = newValue;
            }
        });
    }

    private boolean setNewExtLoyaltyProviders(String extLoyProvidersString) {
        try {
            Resource resource = new ByteArrayResource(extLoyProvidersString.getBytes());
            AnnotationConfigApplicationContext springContext = new AnnotationConfigApplicationContext();
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(springContext);
            reader.loadBeanDefinitions(resource);
            springContext.setParent(applicationContext);
            springContext.refresh();
            LoyProvidersRegistry prevLoyProviders = getLoyProviders();
            loyProviders = (LoyProvidersRegistry) springContext.getBean("loy-providers-registry");
            if (prevLoyProviders != null) {
                applicationContext.getAutowireCapableBeanFactory().destroyBean(prevLoyProviders);
            }
            return true;
        } catch (Exception e) {
            log.error("Could not load LoyProvidersRegistry from DB, still using default ext-loyalty-providers.xml!", e);
            return false;
        }
    }
}
