package ru.crystals.pos.barcodescanner.serial;

import ru.crystals.pos.HardwareCOMPortConfig;
import ru.crystals.rxtxadapter.ComPortUtil;

public class BarcodeScannerConfig extends HardwareCOMPortConfig {

	private String barcodePrefix;
	private String barcodeSuffix;

	private boolean ean13LeadingZero;
	private boolean ean13ControlNumber;
	private boolean ean8ControlNumber;
	private boolean upcaLeadingZero;
	private boolean upcaControlNumber;
	private boolean upceLeadingZero;
	private boolean upceControlNumber;
	private long timeOut = 25;

	public String getBarcodePrefix() {
		return barcodePrefix;
	}

	public void setBarcodePrefix(String barcodePrefix) {
		this.barcodePrefix = barcodePrefix;
	}

	public String getBarcodeSuffix() {
		return barcodeSuffix;
	}

	public void setBarcodeSuffix(String barcodeSuffix) {
		this.barcodeSuffix = barcodeSuffix;
	}

	public boolean isEan13LeadingZero() {
		return ean13LeadingZero;
	}

	public void setEan13LeadingZero(boolean ean13LeadingZero) {
		this.ean13LeadingZero = ean13LeadingZero;
	}

	public boolean isEan13ControlNumber() {
		return ean13ControlNumber;
	}

	public void setEan13ControlNumber(boolean ean13ControlNumber) {
		this.ean13ControlNumber = ean13ControlNumber;
	}

	public boolean isEan8ControlNumber() {
		return ean8ControlNumber;
	}

	public void setEan8ControlNumber(boolean ean8ControlNumber) {
		this.ean8ControlNumber = ean8ControlNumber;
	}

	public boolean isUpcaLeadingZero() {
		return upcaLeadingZero;
	}

	public void setUpcaLeadingZero(boolean upcaLeadingZero) {
		this.upcaLeadingZero = upcaLeadingZero;
	}

	public boolean isUpcaControlNumber() {
		return upcaControlNumber;
	}

	public void setUpcaControlNumber(boolean upcaControlNumber) {
		this.upcaControlNumber = upcaControlNumber;
	}

	public boolean isUpceLeadingZero() {
		return upceLeadingZero;
	}

	public void setUpceLeadingZero(boolean upceLeadingZero) {
		this.upceLeadingZero = upceLeadingZero;
	}

	public boolean isUpceControlNumber() {
		return upceControlNumber;
	}

	public void setUpceControlNumber(boolean upceControlNumber) {
		this.upceControlNumber = upceControlNumber;
	}

    @Override
    public void setPort(String port) {
        super.setPort(ComPortUtil.getRealSystemPortName(port));
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

}
