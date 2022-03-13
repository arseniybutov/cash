package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel;

import com.googlecode.jsonrpc4j.JsonRpcClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.BaseResponse;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.Count;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.Info;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.PingResponse;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.Receipt;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.ReceiptInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.SentReceipt;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.ZReportInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.ZReportsStats;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Обертка над {@link FiscalDriveAPI} для отладочного логирования
 */
public class FiscalDriveAPIProxy implements FiscalDriveAPI {

    private static final Logger log = LoggerFactory.getLogger(FiscalDriveAPIProxy.class);

    private FiscalDriveAPI api;

    public FiscalDriveAPIProxy(FiscalDriveAPI api) {
        this.api = api;
    }

    @Override
    public PingResponse ping(String message) throws IOException {
        log.debug("ping: {}", message);
        try {
            final PingResponse result = api.ping(message);
            log.debug("ping result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("ping error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public Info getInfo() throws IOException {
        log.trace("getInfo");
        try {
            final Info result = api.getInfo();
            log.debug("getInfo result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("getInfo error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public Count getZReportCount() throws IOException {
        log.trace("getZReportCount");
        try {
            final Count result = api.getZReportCount();
            log.debug("getZReportCount result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("getZReportCount error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public ZReportInfo getZReportInfo(int number) throws IOException {
        log.debug("getZReportInfo: {}", number);
        try {
            final ZReportInfo result = api.getZReportInfo(number);
            log.debug("getZReportInfo result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("getZReportInfo error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public ZReportInfo getZReportInfoByNumber(int number) throws IOException {
        log.debug("getZReportInfoByNumber: {}", number);
        try {
            final ZReportInfo result = api.getZReportInfoByNumber(number);
            log.debug("getZReportInfoByNumber result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("getZReportInfoByNumber error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public ZReportsStats getZReportsStats() throws IOException {
        log.trace("getZReportsStats");
        try {
            final ZReportsStats result = api.getZReportsStats();
            log.debug("getZReportsStats result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("getZReportsStats error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public BaseResponse closeZReport(LocalDateTime time) throws IOException {
        log.debug("closeZReport: {}", time);
        try {
            final BaseResponse result = api.closeZReport(time);
            log.debug("closeZReport result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("closeZReport error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public BaseResponse openZReport(LocalDateTime time) throws IOException {
        log.debug("openZReport: {}", time);
        try {
            final BaseResponse result = api.openZReport(time);
            log.debug("openZReport result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("openZReport error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public SentReceipt sendSaleReceipt(Receipt receipt) throws IOException {
        log.debug("sendSaleReceipt: {}", receipt);
        try {
            final SentReceipt result = api.sendSaleReceipt(receipt);
            log.debug("sendSaleReceipt result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("sendSaleReceipt error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public SentReceipt sendRefundReceipt(Receipt receipt) throws IOException {
        log.debug("sendRefundReceipt: {}", receipt);
        try {
            final SentReceipt result = api.sendRefundReceipt(receipt);
            log.debug("sendRefundReceipt result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("sendRefundReceipt error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public Count getReceiptCount() throws IOException {
        log.trace("getReceiptCount");
        try {
            final Count result = api.getReceiptCount();
            log.debug("getReceiptCount result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("getReceiptCount error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public ReceiptInfo getReceiptInfo(int number) throws IOException {
        log.debug("getReceiptInfo: {}", number);
        try {
            final ReceiptInfo result = api.getReceiptInfo(number);
            log.debug("getReceiptInfo result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("getReceiptInfo error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }

    @Override
    public BaseResponse resendUnsent() throws IOException {
        log.trace("resendUnsent");
        try {
            final BaseResponse result  = api.resendUnsent();
            log.debug("resendUnsent result: {}", result);
            return result;
        } catch (JsonRpcClientException je) {
            log.debug("resendUnsent error: {} ({})", je.getCode(), je.getMessage());
            throw je;
        }
    }
}
