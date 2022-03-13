package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.domains;

import java.util.HashMap;
import java.util.Map;

/**
 * Вид отрасли/
 */
public class CommonDomain {

    protected String type;

    private Map<String, Object> params = new HashMap<>();

    public String getType() {
        return type;
    }

    protected void addParam(String key, Object value) {
        params.put(key, value);
    }

    protected Object getParam(String key) {
        return params.get(key);
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
