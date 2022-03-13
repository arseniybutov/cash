package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.googlecode.jsonrpc4j.JsonRpcClientException;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.ApiError;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.FiscalDriveAPI;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.FiscalDriveAPIProxy;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.Count;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.Item;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.Receipt;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.ReceiptInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.SentReceipt;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.ZReportInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.NotSentDocInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.PositionVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.RegisteredReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ShiftVO;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PosApiImpl implements PosApi {

    private static final Logger log = LoggerFactory.getLogger(PosApiImpl.class);

    private static final int FIRST_NOT_SENT_RECEIPT = 1;

    /**
     * По умолчанию, используемая реализация JSON-RPC отправляет application/json-rpc, но FiscalDriveAPI ждет исключительно application/json,
     * поэтому мы переопреляем content-type
     */
    private static final Map<String, String> HEADERS = Collections.singletonMap("Content-Type", "application/json; charset=utf-8");
    private static final int MAX_POSITION_NAME_LENGTH = 63;
    private static final int MAX_ITEM_LENGTH = 13;
    private static final int CONNECTION_TIMEOUT_MILLIS = (int) TimeUnit.SECONDS.toMillis(10);
    private static final int READ_TIMEOUT_MILLIS = (int) TimeUnit.SECONDS.toMillis(30);

    private final FiscalDriveAPI api;

    public PosApiImpl(FiscalDriveAPI api) {
        this.api = api;
    }

    public PosApiImpl(URL url) {
        this.api = initApiProxy(url);
    }

    private FiscalDriveAPI initApiProxy(URL url) {
        log.debug("Init API proxy with URL: {}", url);
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(FiscalDriveAPI.DATE_TIME_FORMAT);
        final ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()
                        .addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter))
                        .addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter)))
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        final JsonRpcHttpClient client = new JsonRpcHttpClient(objectMapper, url, HEADERS);
        client.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MILLIS);
        client.setReadTimeoutMillis(READ_TIMEOUT_MILLIS);
        return new FiscalDriveAPIProxy(ProxyUtil.createClientProxy(getClass().getClassLoader(), FiscalDriveAPI.class, client));
    }

    @Override
    public PosApiResponse<String> getTerminalId() {
        try {
            return PosApiResponse.response(api.getInfo().getTerminalID());
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    private <R> PosApiResponse<R> makeErrorResponse(JsonRpcClientException je) {
        log.error("API error ({})", je.getCode(), je);
        return PosApiResponse.error(new PosApiException(je.getMessage(), je.getCode()));
    }

    private <R> PosApiResponse<R> makeErrorResponse(Exception e) {
        log.error("General error", e);
        return PosApiResponse.error(new PosApiException(e.getMessage()));
    }

    @Override
    public PosApiResponse<ShiftVO> openShift() {
        try {
            api.openZReport(LocalDateTime.now());
            return PosApiResponse.response(getZReportInfo());
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    @Override
    public PosApiResponse<ShiftVO> closeShift() {
        try {
            api.closeZReport(LocalDateTime.now());
            return PosApiResponse.response(getZReportInfo());
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    @Override
    public PosApiResponse<ShiftVO> getCurrentShift() {
        try {
            return PosApiResponse.response(getZReportInfo());
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    private ShiftVO getZReportInfo() throws Exception {
        final ZReportInfo zReportInfo = api.getZReportInfo(0);
        return ShiftVO.builder()
                .number(zReportInfo.getNumber())
                .closeTime(zReportInfo.getCloseTime())
                .openTime(zReportInfo.getOpenTime())
                .totalSaleCount(zReportInfo.getTotalSaleCount())
                .totalSaleCash(zReportInfo.getTotalSaleCash())
                .totalSaleCard(zReportInfo.getTotalSaleCard())
                .totalSaleVAT(zReportInfo.getTotalSaleVAT())
                .totalRefundCount(zReportInfo.getTotalRefundCount())
                .totalRefundCash(zReportInfo.getTotalRefundCash())
                .totalRefundCard(zReportInfo.getTotalRefundCard())
                .totalRefundVAT(zReportInfo.getTotalRefundVAT())
                .build();
    }

    @Override
    public PosApiResponse<Long> getLastReceiptSeq() {
        try {
            ZReportInfo currentShift;
            int index = 0;
            while (!Thread.currentThread().isInterrupted()) {
                currentShift = api.getZReportInfo(index);
                if (!Objects.equals("0", currentShift.getLastReceiptSeq())) {
                    return PosApiResponse.response(Long.parseLong(currentShift.getLastReceiptSeq()));
                }
                index++;
                if (index >= currentShift.getCount()) {
                    return PosApiResponse.response(0L);
                }
            }
            return PosApiResponse.response(0L);
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    @Override
    public PosApiResponse<RegisteredReceiptVO> registerSale(ReceiptVO receipt) {
        return registerReceipt(receipt, rcpt -> {
            try {
                return api.sendSaleReceipt(rcpt);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public PosApiResponse<RegisteredReceiptVO> registerRefund(ReceiptVO receipt) {
        return registerReceipt(receipt, rcpt -> {
            try {
                return api.sendRefundReceipt(rcpt);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public PosApiResponse<Integer> getNotSentDocCount() {
        try {
            return PosApiResponse.response(Optional.ofNullable(api.getReceiptCount())
                    .map(Count::getCount)
                    .orElse(0));
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    @Override
    public PosApiResponse<Optional<NotSentDocInfo>> getFirstNotSentDoc() {
        try {
            final ReceiptInfo firstNotSentReceipt = api.getReceiptInfo(FIRST_NOT_SENT_RECEIPT);
            return PosApiResponse.response(Optional.ofNullable(firstNotSentReceipt)
                    .map(rcp -> new NotSentDocInfo(rcp.getTransactionTime(), Long.parseLong(rcp.getReceiptSeq()))));
        } catch (JsonRpcClientException je) {
            if (isNoReceiptsError(je.getMessage())) {
                return PosApiResponse.response(Optional.empty());
            }
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    @Override
    public PosApiResponse<Void> resendUnsent() {
        try {
            api.resendUnsent();
            return PosApiResponse.response(null);
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    private boolean isNoReceiptsError(String message) {
        return ApiError.ERROR_RECEIPT_COUNT_ZERO.equals(message) || ApiError.ERROR_RECEIPT_INDEX_OUT_OF_BOUNDS.equals(message);
    }

    private PosApiResponse<RegisteredReceiptVO> registerReceipt(ReceiptVO receipt, Function<Receipt, SentReceipt> apiMethod) {
        try {
            final Receipt rcpt = new Receipt();
            rcpt.setReceivedCard(receipt.getCardPaymentSum());
            rcpt.setReceivedCash(receipt.getCashPaymentSum());
            rcpt.setTime(LocalDateTime.now());
            rcpt.setItems(mapPositions(receipt.getPositions()));
            final SentReceipt sentReceipt = apiMethod.apply(rcpt);
            final RegisteredReceiptVO result = RegisteredReceiptVO.builder()
                    .dateTime(sentReceipt.getDateTime())
                    .fiscalSign(sentReceipt.getFiscalSign())
                    .qrCodeURL(sentReceipt.getQrCodeURL())
                    .receiptSeq(Long.parseLong(sentReceipt.getReceiptSeq()))
                    .build();
            return PosApiResponse.response(result);
        } catch (JsonRpcClientException je) {
            return makeErrorResponse(je);
        } catch (Exception e) {
            return makeErrorResponse(e);
        }
    }

    private List<Item> mapPositions(List<PositionVO> positions) {
        return positions.stream().map(this::mapPosition).collect(Collectors.toList());
    }

    private Item mapPosition(PositionVO positionVO) {
        final Item item = new Item();
        item.setAmount(positionVO.getQuantity());
        item.setPrice(positionVO.getStartSum());
        item.setBarcode(StringUtils.left(positionVO.getItem(), MAX_ITEM_LENGTH));
        item.setDiscount(positionVO.getDiscount());
        item.setName(StringUtils.left(positionVO.getName(), MAX_POSITION_NAME_LENGTH));
        item.setOther(0L);
        item.setVat(positionVO.getVat());
        return item;
    }
}
