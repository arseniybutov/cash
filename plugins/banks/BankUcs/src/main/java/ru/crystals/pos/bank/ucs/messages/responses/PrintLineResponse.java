package ru.crystals.pos.bank.ucs.messages.responses;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrintLineResponse extends Response {
    private static final String LAST_LINE = "1";
    private static final String LINE_SEPARATOR = String.valueOf((char) 0x0A);
    private static final String SLIP_DELIMITER = String.valueOf((char) 0x0D);
    private boolean isLastLine;
    private boolean isSlipDelimiter;
    private String textLine = "";
    private List<String> textLines = new ArrayList<String>();

    public PrintLineResponse(String response) {
        super(response);
        if (getLength() > 0) {
            isLastLine = getData().substring(0, 1).equals(LAST_LINE);
            if (getLength() > 1) {
                textLine = getData().substring(1);
                if (textLine.equals(SLIP_DELIMITER)) {
                    isSlipDelimiter = true;
                } else {
                    textLines = Arrays.asList(StringUtils.split(textLine, LINE_SEPARATOR));
                }
            }
        }
    }

    /**
     * Флаг последней команды печати чека
     */
    public boolean isLastLine() {
        return isLastLine;
    }

    /**
     * Флаг разделителя слипа
     */
    public boolean isSlipDelimiter() {
        return isSlipDelimiter;
    }

    /**
     * Текст для печати. Может содержать несколько строк, разделенных символом Line Feed (0x0A). Передача символа разделения чеков всегда должна
     * осуществляться отдельной командой 3-2, в которой в качестве данных передается только один символ - Carriage Return – 0x0D.
     */
    public String getTextLine() {
        return textLine;
    }

    public List<String> getTextLines() {
        return textLines;
    }

    @Override
    public void setLoggableFields() {
        getLoggerUtil().add("isLastLine", Boolean.valueOf(isLastLine));
        getLoggerUtil().add("isSlipDelimiter", Boolean.valueOf(isSlipDelimiter));
        getLoggerUtil().add("textLine", getTextLine());
    }
}
