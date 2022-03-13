package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Краткое описание состояния ФР семейства "Щтрих".
 * 
 * @author aperevozchikov
 *
 */
public class ShtrihShortStateDescription {
    
    /**
     * флаги ФР
     */
    private ShtrihFlags flags;
    
    /**
     * Режим ФР
     */
    private ShtrihMode state;
    
    /**
     * Подрежим ФР
     */
    private ShtrihSubState subState;
    
    /**
     * Количество операций в чеке
     */
    private int operationsCount;

    /**
     * Напряжение резервной батареи
     */
    private int upsSupplyVoltage;
    
    /**
     * Напряжение источника питания
     */
    private int mainSupplyVoltage;
    
    /**
     * Код ошибки ФП (Фискальной Платы)
     */
    private byte fiscalBoardErrorCode;
    
    /**
     * Код ошибки ЭКЛЗ
     */
    private byte eklzErrorCode;
    
    @Override
    public String toString() {
        return String.format("shtrih-state-desc [flags: %s; state: %s; sub-state: %s; operations-count: %d; ups-v: %d; main-v: %d; p-error: 0x%02X; e-error: 0x%02X]", 
            flags, state, subState, operationsCount, upsSupplyVoltage, mainSupplyVoltage, fiscalBoardErrorCode, eklzErrorCode);
    }
    
    // getters & setters
    
    public ShtrihFlags getFlags() {
        return flags;
    }

    public void setFlags(ShtrihFlags flags) {
        this.flags = flags;
    }

    public ShtrihMode getState() {
        return state;
    }

    public void setState(ShtrihMode state) {
        this.state = state;
    }

    public ShtrihSubState getSubState() {
        return subState;
    }

    public void setSubState(ShtrihSubState subState) {
        this.subState = subState;
    }

    public int getOperationsCount() {
        return operationsCount;
    }

    public void setOperationsCount(int operationsCount) {
        this.operationsCount = operationsCount;
    }

    public int getUpsSupplyVoltage() {
        return upsSupplyVoltage;
    }

    public void setUpsSupplyVoltage(int upsSupplyVoltage) {
        this.upsSupplyVoltage = upsSupplyVoltage;
    }

    public int getMainSupplyVoltage() {
        return mainSupplyVoltage;
    }

    public void setMainSupplyVoltage(int mainSupplyVoltage) {
        this.mainSupplyVoltage = mainSupplyVoltage;
    }

    public byte getFiscalBoardErrorCode() {
        return fiscalBoardErrorCode;
    }

    public void setFiscalBoardErrorCode(byte fiscalBoardErrorCode) {
        this.fiscalBoardErrorCode = fiscalBoardErrorCode;
    }

    public byte getEklzErrorCode() {
        return eklzErrorCode;
    }

    public void setEklzErrorCode(byte eklzErrorCode) {
        this.eklzErrorCode = eklzErrorCode;
    }
}