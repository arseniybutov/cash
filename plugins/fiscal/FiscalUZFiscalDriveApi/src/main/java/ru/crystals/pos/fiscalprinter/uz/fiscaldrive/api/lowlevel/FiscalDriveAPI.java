package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel;

import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;
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
 * Описание API программного фискализатора FiscalDriveAPI для работы через JSON-RPC
 */
public interface FiscalDriveAPI {

    String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";

    @JsonRpcMethod("Api.Ping")
    PingResponse ping(@JsonRpcParam("Message") String message) throws IOException;

    @JsonRpcMethod("Api.GetInfo")
    Info getInfo() throws IOException;

    @JsonRpcMethod("Api.GetZReportCount")
    Count getZReportCount() throws IOException;

    @JsonRpcMethod("Api.GetZReportInfo")
    ZReportInfo getZReportInfo(@JsonRpcParam("Number") int number) throws IOException;

    @JsonRpcMethod("Api.GetZReportInfoByNumber")
    ZReportInfo getZReportInfoByNumber(@JsonRpcParam("Number") int number) throws IOException;

    @JsonRpcMethod("Api.GetZReportsStats")
    ZReportsStats getZReportsStats() throws IOException;

    @JsonRpcMethod("Api.CloseZReport")
    BaseResponse closeZReport(@JsonRpcParam("Time") LocalDateTime time) throws IOException;

    @JsonRpcMethod("Api.OpenZReport")
    BaseResponse openZReport(@JsonRpcParam("Time") LocalDateTime time) throws IOException;

    @JsonRpcMethod("Api.SendSaleReceipt")
    SentReceipt sendSaleReceipt(@JsonRpcParam("Receipt") Receipt receipt) throws IOException;

    @JsonRpcMethod("Api.SendRefundReceipt")
    SentReceipt sendRefundReceipt(@JsonRpcParam("Receipt") Receipt receipt) throws IOException;

    @JsonRpcMethod("Api.GetReceiptCount")
    Count getReceiptCount() throws IOException;

    @JsonRpcMethod("Api.GetReceiptInfo")
    ReceiptInfo getReceiptInfo(@JsonRpcParam("Number") int number) throws IOException;

    @JsonRpcMethod("Api.ResendUnsent")
    BaseResponse resendUnsent() throws IOException;

}