package ru.crystals.pos.fiscalprinter.nfd.transport.commnads;


import ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums.FontType;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.BaseResponse;
import ru.crystals.pos.fiscalprinter.nfd.transport.responses.addstring.AddStringResponse;

/**
 * Печать строки в открытом чеке.
 */
public class AddString extends BaseRequest {

    private static final String METHOD_NAME = "addString";

    /**
     * Печатаемый текст.
     */
    private static final String DATA_PARAM_NAME = "data";

    /**
     * Тип шрифта.
     */
    private static final String FONT_TYPE_PARAM_NAME = "fontType";

    public AddString() {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
    }

    public AddString(String text, FontType fontType) {
        putMethodParam(operatorPasswordParamName, operatorPasswordDefault);
        setData(text);
        setFontType(fontType);
    }


    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    public Class<? extends BaseResponse> getClassResponse() {
        return AddStringResponse.class;
    }

    public String getData() {
        return (String) getMethodParam(DATA_PARAM_NAME);
    }

    public void setData(String data) {
        putMethodParam(DATA_PARAM_NAME, data);
    }

    public FontType getFontType() {
        return (FontType) getMethodParam(FONT_TYPE_PARAM_NAME);
    }

    public void setFontType(FontType fontType) {
        putMethodParam(FONT_TYPE_PARAM_NAME, fontType);
    }


}
