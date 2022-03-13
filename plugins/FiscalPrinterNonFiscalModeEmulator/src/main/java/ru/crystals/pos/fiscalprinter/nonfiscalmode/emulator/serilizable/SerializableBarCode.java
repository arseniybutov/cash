package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable;

import java.io.Serializable;

import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TextPosition;

public class SerializableBarCode implements Serializable {
	private static final long serialVersionUID = 7063658218208954405L;
	private long width;
	private long height;
	private TextPosition textPosition;
	private BarCodeType type;
	private String value;

	public SerializableBarCode(BarCode barcode) {
		this.setWidth(barcode.getWidth());
		this.setHeight(barcode.getHeight());
		this.setTextPosition(barcode.getTextPosition());
		this.setType(barcode.getType());
		this.setValue(barcode.getValue());
	}

	public SerializableBarCode() {
		super();
	}

	public long getWidth() {
		return width;
	}

	public void setWidth(long width) {
		this.width = width;
	}

	public long getHeight() {
		return height;
	}

	public void setHeight(long height) {
		this.height = height;
	}

	public TextPosition getTextPosition() {
		return textPosition;
	}

	public void setTextPosition(TextPosition textPosition) {
		this.textPosition = textPosition;
	}

	public BarCodeType getType() {
		return type;
	}

	public void setType(BarCodeType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
