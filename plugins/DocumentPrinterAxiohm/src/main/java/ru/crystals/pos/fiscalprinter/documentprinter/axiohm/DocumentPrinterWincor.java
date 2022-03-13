package ru.crystals.pos.fiscalprinter.documentprinter.axiohm;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

/**
 * Реализация плагина для работы с ПЧ Wincore TH250, отличается от Axiohm используемой кодировкой
 * Также в начале работы происходит сохранение 'Ə' и логотипа в принтер для дальнейшей печати на чеках
 */
@PrototypedComponent
public class DocumentPrinterWincor extends DocumentPrinterAxiohm {

    /**
     * Самая близкая к Азербайджанскому алфавиту ASCII кодирова - Турецкая "Windows-1254"
     */
    private static final String TURKISH_PAGE_CODE = "cp1254";
    /**
     * Индекс F0 - это номер логотипа в flash памяти принтера Wincor, который печатается сразу после отрезания чека.
     */
    private static final int LOGO_NUM = 0xF0;

    private final WincorProperties properties = new WincorProperties();

    private Font lastUsedFont = Font.NORMAL;

    @Override
    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        defaultConfig.setPrinterEncoding(TURKISH_PAGE_CODE);
    }

    @Override
    public void open() throws FiscalPrinterException {
        useSerialProxy = true;
        super.open();
        if (config.isUseLogo()) {
            if (!properties.isPrintLogoAfterCutActivated()) {
                activatePrintLogoAfterCut();
                properties.setPrintLogoAfterCutActivated(true);
            }
            if (needToReloadLogo(properties)) {
                downloadLogoBmp(LOGO_NUM);
                properties.updateLogoFileLastModified(getLogoFileLastModified());
            }
        } else {
            if (properties.isPrintLogoAfterCutActivated()) {
                deactivatePrintLogoAfterCut();
                properties.setPrintLogoAfterCutActivated(false);
            }
        }
    }

    @Override
    protected void setCharSettings() throws FiscalPrinterException {
        // init
        connector.sendData(ESC, '@');
        //0x10 - Code Page 1254
        connector.sendData(ESC, 'R', 0x10);
        try {
            // для сохранения символов выбираем flash память (энерго-независимую)
            connector.sendData(GS, '"', 0x33);
            replaceCharacters(CharacterReplacement.WINCOR_NORMAL);
            replaceCharacters(CharacterReplacement.WINCOR_SMALL);
        } catch (Exception e) {
            throw new FiscalPrinterException("Error while sending letter 'Ə' to printer", e);
        }
        // активируем кастомные символы
        connector.sendData(ESC, '%', 1);
    }

    @Override
    protected byte[] getTextBytes(String text) {
        return adaptToTurkishCharSet(text).getBytes(encoding);
    }

    @Override
    public void setFont(Font font) throws FiscalPrinterException {
        lastUsedFont = font;
        super.setFont(font);
    }

    @Override
    public String getDeviceName() {
        return ResBundleDocPrinterAxiohm.getString("DEVICE_WINCOR");
    }

    /**
     * Земена не поддерживаемых в Турецкой кодировке символов
     *
     * @return текст на печать
     */
    private String adaptToTurkishCharSet(String text) {
        // Т.к. в Турецкой кодировке отстуствует буква Шва, используестся аналог
        // В зависимости от шрифта используются символы либо 10x24, либо 13x24, они записаны на место разных символов
        return lastUsedFont == Font.SMALL
                ? replaceSymbols(text, CharacterReplacement.WINCOR_SMALL)
                : replaceSymbols(text, CharacterReplacement.WINCOR_NORMAL);
    }

    /**
     * Активирует печать логотипа, загруженного с индексом F0, каждый раз после отрезания чека.
     * ВАЖНО: Для того, чтобы данная настройка вступила в силу, нужно после выполнения команды отключить и включить питание принтера.
     * 0x01 - номер фукнции для данной команды (link print logo after knife cut)
     * 0x70 - подобранный параметр: количество точек лого, которые печатаются перед отрезкой чека
     */
    private void activatePrintLogoAfterCut() throws FiscalPrinterException {
        connector.sendData(US, ETX, SYN, 0x01, 0x70, 0x01);
    }

    /**
     * Деактивирует печать логотипа сразу после отрезания чека.
     * ВАЖНО: Если до этого на принтере использовался логотип, то для того, чтобы настройка вступила в силу,
     * нужно после выполнения команды отключить и включить питание принтера
     */
    private void deactivatePrintLogoAfterCut() throws FiscalPrinterException {
        connector.sendData(US, ETX, SYN, 0x00);
    }
}
