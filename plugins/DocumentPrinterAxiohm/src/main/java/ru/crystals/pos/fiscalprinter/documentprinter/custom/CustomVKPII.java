package ru.crystals.pos.fiscalprinter.documentprinter.custom;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.datastruct.state.StatusFP;
import ru.crystals.pos.fiscalprinter.documentprinter.ResBundleDocPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.DocumentPrinterAxiohm;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.ByteSequence;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.DefaultPrinterConfig;
import ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config.PrinterCommandType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.utils.Timer;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;

@PrototypedComponent
public class CustomVKPII extends DocumentPrinterAxiohm {

    private static final Logger LOG = LoggerFactory.getLogger(FiscalPrinter.class);

    private Timer clearOutPutTimer = new Timer("CustomVKPII retraction timer.");

    @Override
    protected void customizeDefaultConfig(DefaultPrinterConfig defaultConfig) {
        defaultConfig.setMaxCharRowMap(ImmutableMap.of(
                Font.NORMAL, 42,
                Font.SMALL, 54,
                Font.DOUBLEWIDTH, 20
        ));
        defaultConfig.addCommand(PrinterCommandType.CUT, ByteSequence.of(0x1B, 0x69));
        defaultConfig.addCommand(PrinterCommandType.STATUS, ByteSequence.of(0x10, 0x4, 20));

        defaultConfig.setPrinterStatusMap(Collections.singletonMap(
                // для этого принтера полностью кастомная обработка результата запроса статуса, но чтобы для обработки вычитывать ровно 6 байт,
                // здесь мы задаем заглушку на 6 байт (по ней будет рассчитана длина)
                new ByteSequence(new byte[]{0, 0, 0, 0, 0, 0}), StatusFP.Status.NORMAL
        ));

    }

    @Override
    public void skipAndCut() throws FiscalPrinterException {
        LOG.info(" skipAndCut ");
        cut();
        byte[] present = {0x1D, 0x65, 0x3, 0xC};
        sendDataWaitingDSR(present);
    }

    @Override
    public void openDocument() throws FiscalPrinterException {
        LOG.info(" openDocument ");
        super.openDocument();
        while (isTicketPresentInTheOutput() && clearOutPutTimer.isNotExpired()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        if (isTicketPresentInTheOutput()) {
            retractDocument();
        }
    }

    @Override
    public void closeDocument() throws FiscalPrinterException {
        super.closeDocument();
        clearOutPutTimer.restart(config.getClearOutPutTimeOut());
    }

    private void retractDocument() throws FiscalPrinterException {
        LOG.info(" retracting previous purchase ");
        byte[] retract = {0x1D, 0x65, 0x2};
        sendDataWaitingDSR(retract);
    }

    private boolean isTicketPresentInTheOutput() throws FiscalPrinterException {
        byte[] getStatusByte = {0x1D, 0x65, 0x6};
        sendDataWaitingDSR(getStatusByte);
        byte[] statusByte = connector.readData(1);
        return (statusByte[0] & 0x8) == 8;
    }

    @Override
    public String getDeviceName() {
        return ResBundleDocPrinterAxiohm.getString("DEVICE_CUSTOM_VKP");
    }

    @Override
    public StatusFP getStatus() throws FiscalPrinterException {
        StatusFP status = new StatusFP();
        byte[] result;
        try {
            if (config.isUseUsb()) {
                result = getPrinterStateWithWait();
            } else {
                result = getPrinterState();
            }
        } catch (FiscalPrinterException e) {
            //если произошли проблемы с получением статуса - просто логируем ошибку
            //и даём принтеру второй шанс
            LOG.error(e.getMessage(), e);
            return status;
        }
        LOG.debug("Received status bytes: {}.", result);
        if (result[0] != 0x10 || result[1] != 0x0F) {
            //Контрольные разряды пришли не верные - просто логируем ошибку
            //и даём принтеру второй шанс
            LOG.error("Received wrong status control bytes: {}, continue printing.", result);
            return status;
        }
        status.setLongStatus(ByteBuffer.wrap(new byte[]{result[2], result[3],
                result[4], result[5], 0, 0, 0, 0}).getLong());
        if ((result[3] & 1) == 1 || (result[3] & 2) == 2) {
            // если первый или второй бит третьего байта == 1, значит крышка открыта
            status.addDescription(ResBundleDocPrinterAxiohm.getString("PRINTER_COVER_OPENED"));
            status.setStatus(StatusFP.Status.OPEN_COVER);
        } else if ((result[2] & 1) == 1) {
            // если первый бит второго байта == 1, значит кончилась бумага
            status.addDescription(ResBundleDocPrinterAxiohm.getString("PRINTER_END_OF_PAPER"));
            status.setStatus(StatusFP.Status.END_PAPER);
        } else if (result[4] != 0 || result[5] != 0) {
            // 4-й байт по битам:
            // 0 - Head temperature error.
            // 1 - RS232 COM error
            // 3 - Power supply voltage error
            // 5 - Not acknowledge command error
            // 6 - Paper jam
            // 5-й байт по битам:
            // 0 - Cutter error
            // 2 - RAM error
            // 3 - EEPROM error.
            // 6 - Flash error
            status.addDescription(ResBundleDocPrinterAxiohm.getString("PRINTER_FATAL_ERROR"));
            status.setStatus(StatusFP.Status.FATAL);
        }
        return status;
    }

    /**
     * Команда "Загрузка данных QR-кода". Команда загружает k байт QR-кода в
     * принтер.
     */
    @Override
    protected void loadQRData(String code) {
        try (ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            buf.write(0x1D);
            buf.write(0x28);
            buf.write(0x6B);
            //k = (pL + pH × 256) - 3, k - длина баркода
            //pL
            buf.write((code.length() + 3) % 256);
            //pH
            buf.write((code.length() + 3) / 256);
            buf.write(0x31);
            buf.write(0x50);
            buf.write(0x31);
            //f1..fk
            buf.write(code.getBytes());

            sendDataWaitingDSR(buf.toByteArray());
        } catch (Exception e) {
            LOG.error("Cannot print QRCode", e);
        }
    }

    /**
     * Команда "Печать QR-кода". Команда печатает предварительно загруженный
     * QR-код в соответствии с уже установленными размерами модуля и уровнем
     * коррекции ошибок.
     */
    @Override
    protected void printQRCodeCommand() {
        try {
            byte[] cmd = {0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x31};
            sendDataWaitingDSR(cmd);
        } catch (Exception e) {
            LOG.error("Cannot print QRCode", e);
        }
    }
}
