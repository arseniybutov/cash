package ru.crystals.pos.scale.massak.rp;

public class ScaleMassaKRPConfig {

    private String ip;
    private int tcpPort = 5001;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }
}
