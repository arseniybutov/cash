package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

@PrototypedComponent
public class DocumentPrinterNCR extends DocumentPrinterAxiohm {

    /**
     * Наиболее подходящая для азербайджанского алфавита кодировка,
     * доступная на принтере NCR 7199
     */
    private static final String CODE_PAGE_852 = "cp852";
    private static final String CHOOSE_CODE_PAGE = new String(new byte[]{ESC, 't', 0x02});
    private static final String ACTIVATE_CUSTOM_CHARACTERS = new String(new byte[]{ESC, '%', 0x01});
    private static final String DEACTIVATE_CUSTOM_CHARACTERS = new String(new byte[]{ESC, '%', 0x00});
    private static final int LOGO_NUM = 0x00;

    private final NCRProperties properties = new NCRProperties();

    private Font lastUsedFont = Font.NORMAL;

    @Override
    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        defaultConfig.setPrinterEncoding(CODE_PAGE_852);
    }

    @Override
    public void open() throws FiscalPrinterException {
        useSerialProxy = true;
        super.open();
        if (config.isUseLogo() && needToReloadLogo(properties)) {
            downloadLogoBmp(LOGO_NUM);
            properties.updateLogoFileLastModified(getLogoFileLastModified());
        }
    }

    @Override
    protected void setCharSettings() throws FiscalPrinterException {
        // init
        connector.sendData(ESC, '@');
        try {
            // для сохранения символов выбираем flash память (энерго-независимую)
            connector.sendData(GS, '"', 0x33);
            replaceCharacters(CharacterReplacement.NCR_NORMAL);
            replaceCharacters(CharacterReplacement.NCR_SMALL);
        } catch (Exception e) {
            throw new FiscalPrinterException("Error while sending letters to printer", e);
        }
    }

    @Override
    protected byte[] getTextBytes(String text) {
        // В принтере NCR невозможно поменять шрифт, если включены кастомные символы.
        // Поэтому будем включать их перед строкой и выключать после.
        return activateDeactivate(adaptToCharSet(text)).getBytes(encoding);
    }

    /**
     * Земена не поддерживаемых в 852 кодировке символов
     *
     * @return текст на печать
     */
    private String adaptToCharSet(String text) {
        // Т.к. в кодировке 852 отстуствуют некоторые символы, они заменяются
        // В зависимости от шрифта используются символы либо 10x24, либо 13x24, они записаны на место разных символов
        return lastUsedFont == Font.SMALL
                ? replaceSymbols(text, CharacterReplacement.NCR_SMALL)
                : replaceSymbols(text, CharacterReplacement.NCR_NORMAL);
    }

    /**
     * NCR сбрасывает кодировку после выбора шрифта, поэтому нужно передать команду для смены кодировки в начале строки после выбора шрифта.
     * Также при включенных кастомных символах не работает смена шрифта, поэтому строку окружаем
     * командами "активировать кастомные символы" и "деактивировать кастомные символы": ESC % 1 [строка] ESC % 0
     */
    private String activateDeactivate(String textToReplace) {
        return CHOOSE_CODE_PAGE
                + ACTIVATE_CUSTOM_CHARACTERS
                + textToReplace
                + DEACTIVATE_CUSTOM_CHARACTERS;
    }

    @Override
    public void setFont(Font font) throws FiscalPrinterException {
        lastUsedFont = font;
        super.setFont(font);
    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        feed();
        cut();
        if (config.isUseLogo()) {
            printLogo();
        }
        printHeaders();
    }

    @Override
    public String getDeviceName() {
        return ResBundleDocPrinterAxiohm.getString("DEVICE_NCR");
    }

    /**
     * 1. Выбираем лого под номером LOGO_NUM
     * 2. Выравниванием по центру
     * 3. Печатаем лого "нормально" (0)
     * 4. Возвращаем выравнивание по левому краю
     */
    private void printLogo() throws FiscalPrinterException {
        connector.sendData(GS, '#', LOGO_NUM);
        setTextAlign(1);
        connector.sendData(GS, '/', 0);
        setTextAlign(0);
    }
}
