package ru.crystals.sco.fiscalprinter.pulse;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.pirit.core.PiritPluginConfig;

import java.lang.reflect.Field;

public class PulseFAServiceImplTests {

    private final static String IP = "128.0.0.1";
    private final static int PORT = 123;

    @Test
    public void testInitPiritConnector() throws IllegalAccessException {
        PulseFAServiceImpl service = new PulseFAServiceImpl();
        PiritPluginConfig config = new PiritPluginConfig();
        config.setIpAddress(IP);
        config.setTcpPort(PORT);
        service.setConfig(config);

        service.initPiritConnector();

        PulseFAConnector connector = (PulseFAConnector) getFieldSuper("pc", service);
        Assert.assertEquals("Неверный IP", IP, getFieldSuper("ip", connector));
        Assert.assertEquals("Неверный port", PORT, getFieldSuper("port", connector));
    }

    private Object getFieldSuper(String fieldName, Object src) throws IllegalAccessException {
        Field field = null;
        Class clazz = src.getClass();
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
            } catch (Exception e) {
            }
            if (field == null) {
                clazz = clazz.getSuperclass();
            } else {
                break;
            }
        }
        field.setAccessible(true);
        return field.get(src);
    }
}
