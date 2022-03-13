package ru.crystals.jpos.services.scale;


public class DivaScaleConfig {

    private String portName = null;
    private String baudRate = null;
    private String dataBits = null;
    private String stopBits = null;
    private String parity = null;
    
    public DivaScaleConfig(){
    	portName = "COM1";
    	baudRate = "9600";
    	dataBits = "8";
    	stopBits = "1";
    	parity = "0";
    }
    
    public DivaScaleConfig(String portName){
    	this.portName = portName;
    	baudRate = "9600";
    	dataBits = "8";
    	stopBits = "1";
    	parity = "0";
    }
    
    public DivaScaleConfig(String portName, String baudRate){
    	this.portName = portName;
    	this.baudRate = baudRate;
    	dataBits = "8";
    	stopBits = "1";
    	parity = "0";
    }
    
	public String getPortName() {
		return portName;
	}
	
	public void setPortName(String portName) {
		this.portName = portName;
	}
	public String getBaudRate() {
		return baudRate;
	}
	public void setBaudRate(String baudRate) {
		this.baudRate = baudRate;
	}
	public String getDataBits() {
		return dataBits;
	}
	public void setDataBits(String dataBits) {
		this.dataBits = dataBits;
	}
	public String getStopBits() {
		return stopBits;
	}
	public void setStopBits(String stopBits) {
		this.stopBits = stopBits;
	}
	public String getParity() {
		return parity;
	}
	public void setParity(String parity) {
		this.parity = parity;
	}
}
