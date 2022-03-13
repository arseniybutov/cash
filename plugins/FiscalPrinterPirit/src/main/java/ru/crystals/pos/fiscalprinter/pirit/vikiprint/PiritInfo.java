package ru.crystals.pos.fiscalprinter.pirit.vikiprint;

import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand;
import ru.crystals.pos.fiscalprinter.pirit.vikiprint.models.VikiPrintEnvdWithoutFP;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;
import ru.crystals.pos.utils.ByteUtils;

public class PiritInfo {
    private boolean isErrorCloseDoc;
    private boolean isOpenDoc;
    private boolean isNeedStartWork;
    private boolean isFiscalMode;
    private boolean isAuthorized;
    private PiritModel model = PiritModel.UNKNOWN;
    private long firmwareId;
    private PiritConnector pc;

    public PiritInfo(PiritConnector pc) throws FiscalPrinterException {
        this.pc = pc;
        DataPacket status = pc.sendRequest(PiritCommand.GET_STATUS);
        if (status.getCountValue() < 3) {
            throw new RuntimeException("Invalid length of PiritInfo GET_STATUS");
        }
        try {
            isFiscalMode = !ByteUtils.hasBit(status.getIntegerSafe(1).orElse(0), 1);

            isErrorCloseDoc = (status.getIntegerSafe(2).orElse(0) & 0x80) != 0;
            isOpenDoc = (status.getIntegerSafe(2).orElse(0) & 0x1F) != 0;
            isNeedStartWork = ByteUtils.hasBit(status.getIntegerSafe(1).orElse(0), 0);
            isAuthorized = !ByteUtils.hasBit(status.getIntegerSafe(0).orElse(0), 5);
            firmwareId = pc.sendRequest(ExtendedCommand.GET_INFO_FW_ID).getLongValue(1);
            if (firmwareId >= 600) {
                // Команда запроса модели поддерживается только в новых версиях
                model = PiritModel.getModelByIndex((int) pc.sendRequest(ExtendedCommand.GET_INFO_MODEL_ID).getLongValue(1));
            }
        } catch (Exception e) {
            throw new FiscalPrinterException("Unable to get pirit info", e);
        }
    }


    public boolean isNeedStartWork() {
        return isNeedStartWork;
    }

    public boolean isFiscalMode() {
        return isFiscalMode;
    }

    public boolean isErrorCloseDoc() {
        return isErrorCloseDoc;
    }

    public boolean isOpenDoc() {
        return isOpenDoc;
    }

    public boolean isFiscalPrinter() {
        return (model == PiritModel.UNKNOWN && !VikiPrintEnvdWithoutFP.isEnvdFirmware(firmwareId)) || model.getCapabilities().isFiscalDevice();
    }

    public boolean isIfOfdDevice () {
        return (model == PiritModel.UNKNOWN && !VikiPrintEnvdWithoutFP.isEnvdFirmware(firmwareId)) || model.getCapabilities().isOfdDevice();
    }

    public boolean isFM15() {
        return model != PiritModel.UNKNOWN;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public PiritModel getModel() {
        return model;
    }

}