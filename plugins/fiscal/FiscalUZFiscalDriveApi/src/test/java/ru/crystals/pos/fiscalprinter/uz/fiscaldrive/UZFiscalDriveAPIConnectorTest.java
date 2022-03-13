package ru.crystals.pos.fiscalprinter.uz.fiscaldrive;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.fiscalprinter.Report;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FnInfo;
import ru.crystals.pos.fiscalprinter.datastruct.info.FnDocInfo;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.PosApi;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.PosApiResponse;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.NotSentDocInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.RegisteredReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ShiftVO;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.PosApiResponse.response;

@RunWith(MockitoJUnitRunner.class)
public class UZFiscalDriveAPIConnectorTest {

    @Mock
    private PosApi api;
    @Mock
    private TimeSupplier timeSupplier;
    @Mock
    private EmulatedCounters counters;

    private UZFiscalDriveAPIConnector connector = new UZFiscalDriveAPIConnector();

    @Before
    public void setUp() throws Exception {
        final FiscalDriveConfig config = new FiscalDriveConfig();
        config.setSerialNumber("1234567890");
        connector.setConfig(config);
        connector.init(api, counters, timeSupplier);
        when(api.openShift()).thenReturn(response(ShiftVO.builder().build()));
        when(api.resendUnsent()).thenReturn(response(null));
    }

    @Test
    public void saleDeniedOnNotSentMore24HoursTest() {
        final String currentDate = "2020-05-31T10:00:01";
        final String firstDocDate = "2020-05-30T10:00:00";
        when(timeSupplier.now()).thenReturn(LocalDateTime.parse(currentDate));
        when(api.getNotSentDocCount()).thenReturn(response(1));
        when(api.getFirstNotSentDoc()).thenReturn(makeNotSentResponse(firstDocDate));

        try {
            connector.registerCheck(new Check());
            Assert.fail("No expected exception");
        } catch (FiscalPrinterException e) {
            Assert.assertEquals(ResBundleUZFiscalDrive.getString("ERROR_NOT_SENT_24_HOURS"), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Not expected exception");
        }
    }

    @Test
    public void openAndCloseShiftWithReceipt() throws FiscalPrinterException {
        ShiftVO shiftVO = getShiftVO();
        when(api.getNotSentDocCount()).thenReturn(response(0));
        when(api.getFirstNotSentDoc()).thenReturn(response(Optional.empty()));
        when(api.getCurrentShift()).thenReturn(response(shiftVO));
        when(api.closeShift()).thenReturn(response(getShiftVO(LocalDateTime.now(), LocalDateTime.now())));
        when(api.registerSale(any(ReceiptVO.class))).thenReturn(response(RegisteredReceiptVO.builder().build()));
        when(counters.isSoftShiftOpened()).thenReturn(true);

        Check check = new Check();
        check.setType(CheckType.SALE);
        check.setCheckSumEnd(10000L);
        check.setCashier(new Cashier());
        check.setDiscountValueTotal(0L);

        connector.openShift(new Cashier());
        // Открываем смена. Открывается только программная смена
        verify(counters).openShift(shiftVO);
        // Проверяем что смена ФР не открылась
        verify(api, never()).openShift();

        // Пробиваем чек и открывается смена ФР
        connector.registerCheck(check);
        verify(api).openShift();

        ShiftVO openShiftVO = getShiftVO(LocalDateTime.now());
        when(api.getCurrentShift()).thenReturn(response(openShiftVO));
        // Пробиваем еще один чек и понимаем, что попытки открыть смену в ФР больше не происходит
        connector.registerCheck(check);
        verify(api).openShift();

        Report report = new Report();
        report.setZReport(true);
        // Закрываем смену. Но закрываются на самом деле две - программная смена и ФР смена.
        connector.registerReport(report);
        verify(counters).closeShift();
        verify(api).closeShift();
    }

    @Test
    public void openAndCloseShiftWithoutReceipt() throws FiscalPrinterException {
        when(api.getNotSentDocCount()).thenReturn(response(0));
        when(api.getFirstNotSentDoc()).thenReturn(response(Optional.empty()));
        ShiftVO data = getShiftVO();
        when(counters.isSoftShiftOpened()).thenReturn(true);
        when(api.getCurrentShift()).thenReturn(response(data));
        // Открываем смену. Открывается только программная
        connector.openShift(new Cashier());

        // Проверяем что октроется только программная смена
        verify(counters).openShift(data);
        // Проверяем что смена ФР не открылась
        verify(api, never()).openShift();

        Report report = new Report();
        report.setZReport(true);
        connector.registerReport(report);

        // Закрываем смену. Но закрываются только программная смена.
        verify(counters).closeShift();
        verify(api, never()).closeShift();
    }

    @Test
    public void openShiftDeniedOnNotSentMore24HoursTest() {
        final String currentDate = "2020-05-31T10:00:01";
        final String firstDocDate = "2020-05-30T10:00:00";
        when(timeSupplier.now()).thenReturn(LocalDateTime.parse(currentDate));
        when(api.getNotSentDocCount()).thenReturn(response(1));
        when(api.getFirstNotSentDoc()).thenReturn(makeNotSentResponse(firstDocDate));

        try {
            connector.openShift(new Cashier());
            Assert.fail("No expected exception");
        } catch (FiscalPrinterException e) {
            Assert.assertEquals(ResBundleUZFiscalDrive.getString("ERROR_NOT_SENT_24_HOURS"), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Not expected exception");
        }
    }

    @Test
    public void openShiftAllowedWhenNotSentLess24HoursTest() throws FiscalPrinterException {
        final String currentDate = "2020-05-31T10:00:00";
        final String firstDocDate = "2020-05-30T10:00:01";
        when(timeSupplier.now()).thenReturn(LocalDateTime.parse(currentDate));
        when(api.getNotSentDocCount()).thenReturn(response(1));
        when(api.getFirstNotSentDoc()).thenReturn(makeNotSentResponse(firstDocDate));
        when(api.getCurrentShift()).thenReturn(response(getShiftVO()));

        connector.openShift(new Cashier());

        verify(api).getCurrentShift();
    }

    @Test
    public void openShiftAllowedWhenNoNotSentDocsTest_zero() throws FiscalPrinterException {
        when(api.getNotSentDocCount()).thenReturn(response(0));
        when(api.getCurrentShift()).thenReturn(response(getShiftVO()));

        connector.openShift(new Cashier());

        verify(api).getCurrentShift();
        verify(api, never()).getFirstNotSentDoc();
    }

    @Test
    public void openShiftAllowedWhenNoNotSentDocsTest_empty() throws FiscalPrinterException {
        when(api.getNotSentDocCount()).thenReturn(response(1));
        when(api.getFirstNotSentDoc()).thenReturn(response(Optional.empty()));
        when(api.getCurrentShift()).thenReturn(response(getShiftVO()));
        connector.openShift(new Cashier());

        verify(api).getCurrentShift();
    }

    @Test
    public void getFnInfoTest() throws FiscalPrinterException {
        final String firstDocDate = "2020-05-30T10:00:00";
        when(api.getNotSentDocCount()).thenReturn(response(5));
        when(api.getFirstNotSentDoc()).thenReturn(makeNotSentResponse(firstDocDate));

        final FnInfo fnInfo = connector.getFnInfo();

        Assert.assertEquals(5, (int) fnInfo.getNotSentDocCount());
        Assert.assertEquals(new FnDocInfo(10L, firstDocDate), fnInfo.getFirstNotSentDoc());
    }

    @Test
    public void getFnInfoTest_noNotSent() throws FiscalPrinterException {
        when(api.getNotSentDocCount()).thenReturn(response(0));

        final FnInfo fnInfo = connector.getFnInfo();

        Assert.assertEquals(0, (int) fnInfo.getNotSentDocCount());
        Assert.assertNull(fnInfo.getFirstNotSentDoc());
    }

    private ShiftVO getShiftVO() {
        return getShiftVO(null, null);
    }

    private ShiftVO getShiftVO(LocalDateTime openTime) {
        return getShiftVO(openTime, null);
    }

    private ShiftVO getShiftVO(LocalDateTime openTime, LocalDateTime closeTime) {
        return ShiftVO.builder()
                .number(1)
                .openTime(openTime)
                .closeTime(closeTime)
                .build();
    }

    private PosApiResponse<Optional<NotSentDocInfo>> makeNotSentResponse(String firstDocDate) {
        return response(Optional.of(new NotSentDocInfo(LocalDateTime.parse(firstDocDate), 10L)));
    }
}