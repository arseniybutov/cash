package ru.crystals.pos.fiscalprinter.sp402frk.commands.settings;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class POSSettings {
    @XmlAttribute(name = "n")
    private String name = "POS";
    @XmlAttribute(name = "t")
    private String type = "7";

    @XmlElement(name = "pa")
    private NetworkParams networkParams;
    @XmlElement(name = "pa")
    private ProxyParams proxyParams;
    @XmlElement(name = "pa")
    private COMPortParams comPortParams;

    public NetworkParams getNetworkParams() {
        return networkParams;
    }

    public void setNetworkParams(NetworkParams networkParams) {
        this.networkParams = networkParams;
    }

    public ProxyParams getProxyParams() {
        return proxyParams;
    }

    public void setProxyParams(ProxyParams proxyParams) {
        this.proxyParams = proxyParams;
    }

    public COMPortParams getComPortParams() {
        return comPortParams;
    }

    public void setComPortParams(COMPortParams comPortParams) {
        this.comPortParams = comPortParams;
    }
}
