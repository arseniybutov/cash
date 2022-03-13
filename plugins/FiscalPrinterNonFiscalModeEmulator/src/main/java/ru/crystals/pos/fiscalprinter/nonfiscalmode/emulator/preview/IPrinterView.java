package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableBarCode;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableFontLine;

public interface IPrinterView extends Remote {

	void ping() throws RemoteException;
	void appendText(SerializableFontLine line) throws RemoteException;
	void appendText(List<SerializableFontLine> text) throws RemoteException;
	void appendFiscal(long shiftNumber, long docNumber, long kpk) throws RemoteException;
	void appendLogo() throws RemoteException;
	void appendBarcode(SerializableBarCode barCode) throws RemoteException;
	void appendCutter() throws RemoteException;

	void setMaxCharRow(int maxCharRow) throws RemoteException;
	int getMaxRowChars(Font font) throws RemoteException;

}
