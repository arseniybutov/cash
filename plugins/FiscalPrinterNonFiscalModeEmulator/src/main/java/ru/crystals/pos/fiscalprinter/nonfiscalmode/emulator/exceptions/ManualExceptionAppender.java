package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions;

import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ManualExceptionAppender extends Remote {

    void throwException(ManualFiscalPrinterException exception) throws RemoteException;

    void resetException() throws RemoteException;

    void setCashDrawerOpen(boolean open) throws RemoteException;

    boolean isDrawerOpened() throws RemoteException;

    String getPrintedDocumentWithOffset(int offset) throws RemoteException;

    void setCashAmount(long cashAmount) throws RemoteException;

    void setManualRegNumber(String regNumber) throws RemoteException;

    void setManualNotSentDocCount(int count) throws RemoteException;

    void addManualMarkValidationResult(String rawMark, FiscalMarkValidationResult validationResult) throws RemoteException;

    void resetManualMarkValidationResults() throws RemoteException;
}
