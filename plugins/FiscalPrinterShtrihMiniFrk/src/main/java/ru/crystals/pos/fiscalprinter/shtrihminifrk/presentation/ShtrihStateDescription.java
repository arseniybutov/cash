package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import java.util.Date;

/**
 * [Полное] описание состояния ФР семейства "Штрих".
 * 
 * @author aperevozchikov
 */
public class ShtrihStateDescription {

    /**
     * Версия ПО ФР.
     */
    private String softwareVersion;

    /**
     * Сборка ПО ФР (0..65535)
     */
    private int softwareBuild;

    /**
     * Дата выпуска ПО ФР (только день - без времени)
     */
    private Date softwareReleaseDate;

    /**
     * Номер в зале (1..99)
     */
    private byte number;

    /**
     * сквозной номер текущего документа (0..9999)
     */
    private int currentDocNo;

    /**
     * флаги ФР
     */
    private ShtrihFlags flags;

    /**
     * Режим ФР
     */
    private ShtrihMode mode;

    /**
     * Подрежим ФР
     */
    private ShtrihSubState subState;

    /**
     * Номер порта ФР, к которому подключен хост (0..255)
     */
    private byte port;

    /**
     * Версия ПО ФП.
     */
    private String fiscalBoardSoftwareVersion;

    /**
     * Сборка ПО ФП (0..65535)
     */
    private int fiscalBoardSoftwareBuild;

    /**
     * Дата выпуска ПО ФП (только день - без времени)
     */
    private Date fiscalBoardSoftwareReleaseDate;

    /**
     * показания внутренних часов ККМ
     */
    private Date currentTime;

    /**
     * Флаги фискальной платы
     */
    private ShtrihBoardFlags boardFlags;

    /**
     * заводской номер ФР (0..99999999); отрицательное число означает, что заводской номер не введен
     */
    private long deviceNo;

    /**
     * Номер последней закрытой смены (00..2100).
     * <p/>
     * NOTE: Всегда до фискализации ФП и до снятия первого суточного отчета с гашением после фискализации ФП номер последней закрытой смены равен
     * <code>0</code>
     */
    private int lastClosedShiftNo;

    /**
     * Количество свободных записей в ФП (фискальной памяти(?)) (0..2100)
     */
    private int freeFiscalRecords;

    /**
     * Количество перерегистраций (фискализаций) ККМ (0..16).
     * <p/>
     * NOTE: До фискализации ФП количество перерегистраций (фискализаций) равно <code>0</code>.
     */
    private byte fiscalizedCount;

    /**
     * Количество оставшихся перерегистраций (фискализаций) (0..16)
     */
    private byte fiscalizeCountRemaining;

    /**
     * ИНН (Taxpayer Identification Number). 0..999999999999; отрицательное число означает, что ИНН не введен
     */
    private long tin;
    
    // @formatter:off
    @Override
    public String toString() {
        return String.format("shtrih-state-desc[ver: %s; build: %s; release-date: %s; num: %s; doc-no: %s; flags: %s; mode: %s; sub-state: %s; " +
            "port: %s; fb-ver: %s; fb-build: %s; fb-release-date: %s; time: %s; b-flags: %s; dev-no: %s; last-closed-shift-no: %s; shifts-remaining: %s;" +
            "fiscalized-count: %s; fiscalize-remaining: %s; tin: %s]", 
            getSoftwareVersion(), getSoftwareBuild(), getSoftwareReleaseDate() == null ? "(NULL)" : String.format("%tF", getSoftwareReleaseDate()),
            getNumber(), getCurrentDocNo(), getFlags(), getMode(), getSubState(), getPort(),
            getFiscalBoardSoftwareVersion(), getFiscalBoardSoftwareBuild(), getFiscalBoardSoftwareReleaseDate() == null ? "(NULL)" : String.format("%tF", getFiscalBoardSoftwareReleaseDate()),
            getCurrentTime() == null ? "(NULL)" : String.format("%1$tF %1$tT", getCurrentTime()),
            getBoardFlags(), getDeviceNo(), getLastClosedShiftNo(), getFreeFiscalRecords(), getFiscalizedCount(), getFiscalizeCountRemaining(), getTin());
    }
    // @formatter:on
    
    // getters & setters

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public int getSoftwareBuild() {
        return softwareBuild;
    }

    public void setSoftwareBuild(int softwareBuild) {
        this.softwareBuild = softwareBuild;
    }

    public Date getSoftwareReleaseDate() {
        return softwareReleaseDate;
    }

    public void setSoftwareReleaseDate(Date softwareReleaseDate) {
        this.softwareReleaseDate = softwareReleaseDate;
    }

    public byte getNumber() {
        return number;
    }

    public void setNumber(byte number) {
        this.number = number;
    }

    public int getCurrentDocNo() {
        return currentDocNo;
    }

    public void setCurrentDocNo(int currentDocNo) {
        this.currentDocNo = currentDocNo;
    }

    public ShtrihFlags getFlags() {
        return flags;
    }

    public void setFlags(ShtrihFlags flags) {
        this.flags = flags;
    }

    public ShtrihMode getMode() {
        return mode;
    }

    public void setMode(ShtrihMode mode) {
        this.mode = mode;
    }

    public ShtrihSubState getSubState() {
        return subState;
    }

    public void setSubState(ShtrihSubState subState) {
        this.subState = subState;
    }

    public byte getPort() {
        return port;
    }

    public void setPort(byte port) {
        this.port = port;
    }

    public String getFiscalBoardSoftwareVersion() {
        return fiscalBoardSoftwareVersion;
    }

    public void setFiscalBoardSoftwareVersion(String fiscalBoardSoftwareVersion) {
        this.fiscalBoardSoftwareVersion = fiscalBoardSoftwareVersion;
    }

    public int getFiscalBoardSoftwareBuild() {
        return fiscalBoardSoftwareBuild;
    }

    public void setFiscalBoardSoftwareBuild(int fiscalBoardSoftwareBuild) {
        this.fiscalBoardSoftwareBuild = fiscalBoardSoftwareBuild;
    }

    public Date getFiscalBoardSoftwareReleaseDate() {
        return fiscalBoardSoftwareReleaseDate;
    }

    public void setFiscalBoardSoftwareReleaseDate(Date fiscalBoardSoftwareReleaseDate) {
        this.fiscalBoardSoftwareReleaseDate = fiscalBoardSoftwareReleaseDate;
    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    public ShtrihBoardFlags getBoardFlags() {
        return boardFlags;
    }

    public void setBoardFlags(ShtrihBoardFlags boardFlags) {
        this.boardFlags = boardFlags;
    }

    public long getDeviceNo() {
        return deviceNo;
    }

    public void setDeviceNo(long deviceNo) {
        this.deviceNo = deviceNo;
    }

    public int getLastClosedShiftNo() {
        return lastClosedShiftNo;
    }

    public void setLastClosedShiftNo(int lastClosedShiftNo) {
        this.lastClosedShiftNo = lastClosedShiftNo;
    }

    public int getFreeFiscalRecords() {
        return freeFiscalRecords;
    }

    public void setFreeFiscalRecords(int freeFiscalRecords) {
        this.freeFiscalRecords = freeFiscalRecords;
    }

    public byte getFiscalizedCount() {
        return fiscalizedCount;
    }

    public void setFiscalizedCount(byte fiscalizedCount) {
        this.fiscalizedCount = fiscalizedCount;
    }

    public byte getFiscalizeCountRemaining() {
        return fiscalizeCountRemaining;
    }

    public void setFiscalizeCountRemaining(byte fiscalizeCountRemaining) {
        this.fiscalizeCountRemaining = fiscalizeCountRemaining;
    }

    public long getTin() {
        return tin;
    }

    public void setTin(long tin) {
        this.tin = tin;
    }
}
