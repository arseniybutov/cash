package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import java.io.Writer;
import java.lang.reflect.InvocationHandler;

/**
 * Переопределение метода сериализации для корректного формирования CDATA в XML
 */
public class CharEscapeInvocationHandler implements InvocationHandler {

    /**
     * Параметры исходного метода: escape(char[] ac, int i, int j, boolean flag, Writer writer)
     */
    @Override
    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws java.lang.Throwable {
        String methodName = method.getName();
        Class<?>[] classes = method.getParameterTypes();

        if ("escape".equals(methodName)) {
            if (classes.length == 5 && classes[4] == Writer.class) {
                ((Writer) args[4]).write((char[]) args[0], (int) args[1], (int) args[2]);
            }
        }

        return null;
    }
}
