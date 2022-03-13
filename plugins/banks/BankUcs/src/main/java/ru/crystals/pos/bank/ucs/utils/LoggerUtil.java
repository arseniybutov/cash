package ru.crystals.pos.bank.ucs.utils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LoggerUtil {

    private List<String> logList = new ArrayList<>();
    private String title = "";

    public LoggerUtil(Class<?> logableClass) {
        title = logableClass.getSimpleName();
    }

    public void add(String key, Object value) {
        logList.add(StringUtils.rightPad(key, 21, ' ') + ": " + (value != null ? value.toString() : "null"));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append(" [");
        for (String logLine : logList) {
            sb.append("\n    ").append(logLine);
        }
        sb.append("\n]");
        return sb.toString();
    }

}
