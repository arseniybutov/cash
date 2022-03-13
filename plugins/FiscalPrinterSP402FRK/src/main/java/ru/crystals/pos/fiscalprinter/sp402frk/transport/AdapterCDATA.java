package ru.crystals.pos.fiscalprinter.sp402frk.transport;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Переопределение методов сериализации/десериализации для обработки CDATA в XML
 */
public class AdapterCDATA extends XmlAdapter<String, String> {

    @Override
    public String marshal(String arg0) throws Exception {
        return "<![CDATA[" + arg0 + "]]>";
    }

    @Override
    public String unmarshal(String arg0) throws Exception {
        return arg0;
    }

}
