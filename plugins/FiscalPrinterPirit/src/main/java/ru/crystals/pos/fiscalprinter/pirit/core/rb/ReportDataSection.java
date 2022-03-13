package ru.crystals.pos.fiscalprinter.pirit.core.rb;

public abstract class ReportDataSection extends AbstractSection {
    private int structureType;
    private int CRC;

    public ReportDataSection(byte[] data) {
        super(data);
        this.structureType = getByte(data[0]);
        this.CRC = getByte(data[getSize() - 1]);
    }

    public int getStructureType() {
        return structureType;
    }

    public void setStructureType(int structureType) {
        this.structureType = structureType;
    }

    public int getCRC() {
        return CRC;
    }

    public void setCRC(int CRC) {
        this.CRC = CRC;
    }

}
