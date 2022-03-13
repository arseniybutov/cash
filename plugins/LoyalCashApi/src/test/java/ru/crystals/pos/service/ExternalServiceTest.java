package ru.crystals.pos.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import ru.crystals.pos.configurator.core.CoreConfigurator;
import ru.crystals.pos.properties.PropertiesManager;

@RunWith(MockitoJUnitRunner.class)
public class ExternalServiceTest {

    public final static String PROVIDER_NAME = "provider_name";
    public final static String MODULE_NAME = "module_name";

    @InjectMocks
    @Spy
    private ExternalService<TestExternalServiceSettings> sut = new ExternalService<TestExternalServiceSettings>() {
        @Override
        public Logger getLog() {
            return null;
        }

        @Override
        public String getConfigFile() {
            return null;
        }

        @Override
        public String getProviderName() {
            return PROVIDER_NAME;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }

        @Override
        protected TestExternalServiceSettings createSettings() {
            return null;
        }

        @Override
        protected void setSetting(TestExternalServiceSettings settings, String key, Object value) {

        }

    };

    @Mock
    private CoreConfigurator coreConfigurator;

    @Mock
    private PropertiesManager propertiesManager;


    @Test
    public void propertiesManagerDisabledTest() {
        final String key1 = "key1";
        final String val1 = "val1";

        TestExternalServiceSettings settings = new TestExternalServiceSettings();
        doReturn(new HashMap<String, String>(){{put(key1, val1);}}).when(coreConfigurator).getProcessingProperties(any());

        // when
        sut.readExternalProcessingSettingsInto(settings);

        // then
        verify(coreConfigurator).getProcessingProperties(PROVIDER_NAME);
        verify(sut).setSetting(settings, key1, val1);

    }

    @Test
    public void propertiesManagerEnabled_noSettings_Test() {
        final String key1 = "key1";
        final String val1 = "val1";

        TestExternalServiceSettings settings = new TestExternalServiceSettings();
        doReturn(new HashMap<String, String>(){{put(key1, val1);}}).when(coreConfigurator).getProcessingProperties(any());
        doReturn(MODULE_NAME).when(sut).getSalesManagementPropertiesModuleName();
        doReturn(new HashMap<>()).when(propertiesManager).getByModulePlugin(any(), any());


        // when
        sut.readExternalProcessingSettingsInto(settings);

        // then
        verify(sut).setSetting(settings, key1, val1);
        verify(sut, atLeastOnce()).getSalesManagementPropertiesModuleName();
        verify(coreConfigurator).getProcessingProperties(PROVIDER_NAME);
        verify(propertiesManager).getByModulePlugin(MODULE_NAME, "");

        verifyNoMoreInteractions(coreConfigurator, propertiesManager);
    }

    @Test
    public void propertiesManagerEnabled_withSMP_Test() {
        final String key1 = "key1";
        final String val1 = "val1";
        final String key2 = "key2";
        final String val2 = "val2";

        TestExternalServiceSettings settings = new TestExternalServiceSettings();
        doReturn(new HashMap<String, String>(){{put(key1, val1);}}).when(coreConfigurator).getProcessingProperties(any());
        doReturn(MODULE_NAME).when(sut).getSalesManagementPropertiesModuleName();
        doReturn(new HashMap<String, String>(){{put(key2, val2);}}).when(propertiesManager).getByModulePlugin(any(), any());


        // when
        sut.readExternalProcessingSettingsInto(settings);

        // then
        verify(sut).setSetting(settings, key1, val1);
        verify(sut, atLeastOnce()).getSalesManagementPropertiesModuleName();
        verify(coreConfigurator).getProcessingProperties(PROVIDER_NAME);
        verify(propertiesManager).getByModulePlugin(MODULE_NAME, "");

        verifyNoMoreInteractions(coreConfigurator, propertiesManager);
    }

    @Test
    public void propertiesManagerEnabled_noXML_Test() {
        final String key1 = "key1";
        final String val1 = "val1";
        final String key2 = "key2";
        final String val2 = "val2";

        TestExternalServiceSettings settings = new TestExternalServiceSettings();
        doReturn(new HashMap<String, String>()).when(coreConfigurator).getProcessingProperties(any());
        doReturn(MODULE_NAME).when(sut).getSalesManagementPropertiesModuleName();
        doReturn(new HashMap<String, String>(){{put(key2, val2);}}).when(propertiesManager).getByModulePlugin(any(), any());


        // when
        sut.readExternalProcessingSettingsInto(settings);

        // then
        verify(sut).setSetting(settings, key2, val2);
        verify(sut, atLeastOnce()).getSalesManagementPropertiesModuleName();
        verify(coreConfigurator).getProcessingProperties(PROVIDER_NAME);
        verify(propertiesManager).getByModulePlugin(MODULE_NAME, "");

        verifyNoMoreInteractions(coreConfigurator, propertiesManager);
    }



}