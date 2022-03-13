package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;

import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class BaseRequest {

    private Map<String, Object> methodParams = new HashMap<>();

    /**
     * Пароль оператора наименование поля в коллекции параметров.
     */
    protected String operatorPasswordParamName = "operatorPassword";

    /**
     * Пароль оператора
     */
    protected String operatorPasswordDefault = "30";

    public String getOperatorPassword() {
        return (String) getMethodParam(operatorPasswordParamName);
    }

    public void setOperatorPassword(String operatorPassword) {
        putMethodParam(operatorPasswordParamName, operatorPassword);
    }

    public abstract String getMethodName();

    public abstract Class<? extends BaseResponse> getClassResponse();

    public Map<String, Object> getMethodParams() {
        return methodParams;
    }

    public int getMethodParamsSize() {
        return methodParams.size();
    }

    public boolean isMethodParamsEmpty() {
        return methodParams.isEmpty();
    }

    public boolean containsMethodParamsKey(Object key) {
        return methodParams.containsKey(key);
    }

    public Object getMethodParam(Object key) {
        return methodParams.get(key);
    }

    public Object putMethodParam(String key, Object value) {
        if (key != null && value != null) {
            return methodParams.put(key, value);
        } else {
            return null;
        }
    }

    public Object removegetMethodParam(Object key) {
        return methodParams.remove(key);
    }

    public void clearMethodParam() {
        methodParams.clear();
    }

    public Set<Map.Entry<String, Object>> getMethodParamsentrySet() {
        return methodParams.entrySet();
    }

    @Override
    public String toString() {
        return getMethodName() + " {" +
                "methodParams=" + methodParams +
                '}';
    }
}
