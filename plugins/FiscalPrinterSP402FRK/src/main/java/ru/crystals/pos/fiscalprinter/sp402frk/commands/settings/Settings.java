package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Главный контейнер команды установки/запроса настроек, содержить объекты параметров.
 * Только инициализированные объекты устанавливаются/запрашиваются.
 */
@XmlRootElement(name = "pa")
@XmlAccessorType(XmlAccessType.NONE)
public class Settings {
    @XmlAttribute(name = "n")
    private String name = "200004";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private MacroSettings macroSettings;
    @XmlElement(name = "pa")
    private OFDSettings ofdSettings;
    @XmlElement(name = "pa")
    private POSSettings posSettings;
    @XmlElement(name = "pa")
    private PRNSettings prnSettings;
    @XmlElement(name = "pa")
    private IMSSettings imsSettings;

    public MacroSettings getMacroSettings() {
        return macroSettings;
    }

    public void setMacroSettings(MacroSettings macroSettings) {
        this.macroSettings = macroSettings;
    }

    public OFDSettings getOfdSettings() {
        return ofdSettings;
    }

    public void setOfdSettings(OFDSettings ofdSettings) {
        this.ofdSettings = ofdSettings;
    }

    public POSSettings getPosSettings() {
        return posSettings;
    }

    public void setPosSettings(POSSettings posSettings) {
        this.posSettings = posSettings;
    }

    public PRNSettings getPrnSettings() {
        return prnSettings;
    }

    public void setPrnSettings(PRNSettings prnSettings) {
        this.prnSettings = prnSettings;
    }

    public IMSSettings getImsSettings() {
        return imsSettings;
    }

    public void setImsSettings(IMSSettings imsSettings) {
        this.imsSettings = imsSettings;
    }
}
