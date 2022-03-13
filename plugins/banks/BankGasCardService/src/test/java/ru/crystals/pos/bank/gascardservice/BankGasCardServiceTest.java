package ru.crystals.pos.bank.gascardservice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperationParameter;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.filebased.ResponseData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BankGasCardServiceTest {
    private static final String GET_WORKING_KEY = "5";
    private static final String TEST_CASH_NUMBER = "8";
    private static final long TEST_SALE_AMOUNT = 10012L;
    private static final int OPERATION_TYPE_SALE = 1;
    private static final int OPERATION_TYPE_REVERSAL = 2;
    private static final int OPERATION_TYPE_REFUND = 3;
    private static final String GET_SLIPS_COPY_COMMAND = "32";
    private static final String GET_FULL_REPORT_COMMAND = "6";
    private static final long TEST_CHECK_NUMBER = 0L;
    private static final String DAILY_LOG_OPERATION = "4";
    private static final String TEST_REPORT_AMOUNT = "0";
    private static final String GCS_EXECUTABLE_FILE_NAME = "gcsgatew";
    private static final Long TEST_INVOICE_NUMBER = 50L;
    private static final String TEST_RRN = "123456789012";
    private static final String UPDATE_TERMINAL_SOFTWARE = "36";
    private static final String UPDATE_TERMINAL_PARAMS = "35";
    private static final String[] SLIP_COPY_FILE_EXAMPLE =
        { "Test terminal \\сКристаллСервис", "Терминал: 00600256  Чек 53", "Мерчант: 700000", "               ОПЛАТА", "          ОДОБРЕНО ОФФЛАЙН",
            "СУММА: 10.00 РУБ", "КОМИССИЯ ГПБ(ОАО): 0.00 РУБ", "ИТОГО: 10.00 РУБ", "AID: A0000000031010 Visa Debit",
            "   Карта: VISA  ************0005", "Ссылка: 000015000096", "Код ответа: Y1", "Дата: 24/12/13   ВРЕМЯ: 17:09:13",
            "Дата ПЦ: 24/12/13  ВРЕМЯ ПЦ:", "17:09:13", "", "(Клиент)__________________", "(Кассир)__________________", "",
            "========================" };
    private static final String[] FULL_REPORT_EXAMPLE =
        { "========================", "ПОЛНЫЙ ОТЧЕТ", "Дата:2013/12/26 Время:11:30:02", "Test terminal \\сКристаллСервис", "",
            "ЧЕК 65            ТЕРМИНАЛ: 00600256", "ОПЕРАЦИЯ:ОПЛАТА", "ИТОГО:                    100.00 РУБ", "СУММА:                    100.00 РУБ",
            "КОМИССИЯ ГПБ(ОАО):           0.00 РУБ", "AID: A0000000031010       Visa Debit", "ТИП КАРТЫ: VISA", "КАРТА: ************0005",
            "RRN: 002503629925", "ДАТА/ВРЕМЯ  26/12/13 11:29:21", "КОД АВТ.: 629925     КОД ОТВЕТА: 000", "------------------------", "",
            "ЧЕК 66            ТЕРМИНАЛ: 00600256", "ОПЕРАЦИЯ:ОПЛАТА", "ИТОГО:                     10.00 РУБ", "СУММА:                     10.00 РУБ",
            "AID: A0000000031010       Visa Debit", "ТИП КАРТЫ: VISA", "КАРТА: ************0005", "RRN: 002503629927",
            "ДАТА/ВРЕМЯ  26/12/13 11:29:30", "КОД АВТ.: 629927     КОД ОТВЕТА: 000", "------------------------", "",
            "ЧЕК 67            ТЕРМИНАЛ: 00600256", "ОПЕРАЦИЯ:ОПЛАТА", "ИТОГО:                    331.82 РУБ", "СУММА:                    331.82 РУБ",
            "AID: A0000000031010       Visa Debit", "ТИП КАРТЫ: VISA", "КАРТА: ************0005", "RRN: 002503629926",
            "ДАТА/ВРЕМЯ  26/12/13 11:29:50", "КОД АВТ.: 629926     КОД ОТВЕТА: 000", "------------------------", "", "========================",
            "Общий итог:", "========================", "ОПЛАТА (D)(3) 441.82 РУБ", "В Т.Ч. КОМИССИЯ: (2) 0.00 РУБ", "========================",
            "ИТОГО:", "Дебет:441.82 РУБ", "КОМИССИЯ:+0.00 РУБ", "СКИДКА:-0.00 РУБ", "Кредит:-0.00 РУБ", "Продажа ПДК:-0.00 РУБ",
            "Комиссия ПДК:+0.00 РУБ", " 441.82 РУБ", "========================", "ОТЧЕТ ОКОНЧЕН", "========================" };
    public static final String PROTOCOL_NUMBER = "15";
    @Mock
    private SaleData saleData;
    @Mock
    private RefundData refundData;
    @Mock
    private ReversalData reversalData;
    @Mock
    private DailyLogData dailyLogData;
    @Spy @InjectMocks
    private BankGasCardService bank = new BankGasCardService();

    @Before
    public void setUp() throws Exception {
        doReturn(TEST_CASH_NUMBER).when(bank).getCashNumber();
        when(reversalData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
        when(reversalData.getAuthCode()).thenReturn("123456");
        when(reversalData.getHostTransId()).thenReturn(TEST_CHECK_NUMBER);
        doNothing().when(bank).runExecutable(anyList());
    }

    @Test
    public void testStart() throws BankException {

        // given
        doReturn(false).when(bank).isSplittedUpdateOperations();

        List<ServiceBankOperationType> expectedOperationTypes = new ArrayList<>();
        Collections.addAll(expectedOperationTypes, ServiceBankOperationType.values());
        expectedOperationTypes.remove(ServiceBankOperationType.UPDATE_TERMINAL_SOFTWARE);
        expectedOperationTypes.remove(ServiceBankOperationType.LOAD_TERMINAL_PARAMS);

        // when
        bank.start();

        // then
        verify(bank).setExecutableFileName(GCS_EXECUTABLE_FILE_NAME);
        verify(bank).setResponseData(any(GasCardServiceResponseData.class));

        List<ServiceBankOperationType> types = new ArrayList<>();
        List<ServiceOperation> operations = bank.getAvailableServiceOperations();
        for (ServiceOperation operation : operations) {
            types.add(operation.getType());
        }

        assertThat(expectedOperationTypes.containsAll(expectedOperationTypes)).isTrue();
    }

    @Test
    public void testStartWithSplittedOperations() throws BankException {

        // given
        doReturn(true).when(bank).isSplittedUpdateOperations();

        List<ServiceBankOperationType> expectedOperationTypes = new ArrayList<>();
        Collections.addAll(expectedOperationTypes, ServiceBankOperationType.values());
        expectedOperationTypes.remove(ServiceBankOperationType.LOAD_TERMINAL_PARAMS_AND_SOFTWARE);

        // when
        bank.start();

        // then
        verify(bank).setExecutableFileName(GCS_EXECUTABLE_FILE_NAME);
        verify(bank).setResponseData(any(GasCardServiceResponseData.class));

        List<ServiceBankOperationType> types = new ArrayList<>();
        List<ServiceOperation> operations = bank.getAvailableServiceOperations();
        for (ServiceOperation operation : operations) {
            types.add(operation.getType());
        }

        assertThat(expectedOperationTypes.containsAll(expectedOperationTypes)).isTrue();
    }

    @Test
    public void testCashierMenu() throws BankException {

        // given
        doNothing().when(bank).runExecutable(anyString());
        bank.start();

        List<String> params = new ArrayList<>();
        ArrayList<String> answer = new ArrayList<>();
        answer.add("0 \"OK\"");
        doReturn(answer).when(bank).readResponseFile();
        params.add(PROTOCOL_NUMBER);
        params.add(TEST_CASH_NUMBER);
        params.add(GET_WORKING_KEY);
        params.add("0");
        params.add("0");

        // when
        bank.processServiceOperation(new ServiceOperation(ServiceBankOperationType.GET_WORKING_KEY));

        // then
        verify(bank).runExecutable(params);
    }

    @Test
    public void testMakeSlipDefault() {
        // given
        AuthorizationData authorizationData = mock(AuthorizationData.class);
        ResponseData responseData = mock(GasCardServiceResponseData.class);
        when(responseData.isSuccessful()).thenReturn(true);
        BankOperationType type = BankOperationType.SALE;
        List<String> slip = new ArrayList<>();
        List<List<String>> result = new ArrayList<>();
        result.add(slip);
        result.add(new ArrayList<>(slip));

        // when
        bank.makeSlip(authorizationData, responseData, slip, type);

        // then
        verify(authorizationData).setSlips(result);
    }

    @Test
    public void testMakeSlipOne() {
        // given
        AuthorizationData authorizationData = mock(AuthorizationData.class);
        ResponseData responseData = mock(GasCardServiceResponseData.class);
        when(responseData.isSuccessful()).thenReturn(true);
        BankOperationType type = BankOperationType.SALE;
        List<String> slip = new ArrayList<>();
        List<List<String>> result = new ArrayList<>();
        result.add(slip);

        bank.setInnerSlipCount(1);
        // when
        bank.makeSlip(authorizationData, responseData, slip, type);

        // then
        verify(authorizationData).setSlips(result);
    }

    @Test
    public void testMakeSlipThree() {
        // given
        AuthorizationData authorizationData = mock(AuthorizationData.class);
        ResponseData responseData = mock(GasCardServiceResponseData.class);
        when(responseData.isSuccessful()).thenReturn(true);
        BankOperationType type = BankOperationType.SALE;
        List<String> slip = new ArrayList<>();
        List<List<String>> result = new ArrayList<>();
        result.add(slip);
        result.add(new ArrayList<>(slip));
        result.add(new ArrayList<>(slip));

        bank.setInnerSlipCount(3);
        // when
        bank.makeSlip(authorizationData, responseData, slip, type);

        // then
        verify(authorizationData).setSlips(result);
    }

    @Test
    public void testFillSpecificFields() {

        // given
        AuthorizationData authorizationData = mock(AuthorizationData.class);
        GasCardServiceResponseData responseData = mock(GasCardServiceResponseData.class);
        when(responseData.getRRN()).thenReturn(TEST_RRN);
        when(responseData.getInvoiceNumber()).thenReturn(TEST_INVOICE_NUMBER);
        BankOperationType type = BankOperationType.SALE;

        // when
        bank.fillSpecificFields(authorizationData, responseData, type);

        // then
        verify(authorizationData).setRefNumber(TEST_RRN);
        verify(authorizationData).setHostTransId(TEST_INVOICE_NUMBER);
    }

    @Test
    public void testPrepareExecutableParametersForSale() throws BankException {

        // given
        when(saleData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
        when(saleData.getCashTransId()).thenReturn(TEST_CHECK_NUMBER);

        Object[] expectedResult = { String.valueOf(TEST_CASH_NUMBER), String.valueOf(OPERATION_TYPE_SALE), String.valueOf(TEST_SALE_AMOUNT),
            String.valueOf(TEST_CHECK_NUMBER) };

        // when
        List<String> parameters = bank.prepareExecutableParameters(saleData, BankOperationType.SALE);

        // then
        assertThat(parameters).containsSequence(expectedResult);
    }

    @Test
    public void testPrepareExecutableParametersForRefund() throws BankException {

        // given
        when(refundData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
        when(refundData.getCashTransId()).thenReturn(TEST_CHECK_NUMBER);

        Object[] expectedResult =
            { PROTOCOL_NUMBER, String.valueOf(TEST_CASH_NUMBER), String.valueOf(OPERATION_TYPE_REFUND), String.valueOf(TEST_SALE_AMOUNT),
                String.valueOf(TEST_CHECK_NUMBER) };
        bank.start();

        // when
        List<String> parameters = bank.prepareExecutableParameters(refundData, BankOperationType.REFUND);

        // then
        assertThat(parameters).containsSequence(expectedResult);
    }

    @Test
    public void testPrepareExecutableParametersForReversal() {

        // given
        when(reversalData.getRefNumber()).thenReturn(TEST_RRN);
        Object[] expectedResult = { String.valueOf(TEST_CASH_NUMBER), String.valueOf(OPERATION_TYPE_REVERSAL), String.valueOf(TEST_SALE_AMOUNT),
            String.valueOf(TEST_CHECK_NUMBER), String.valueOf(TEST_RRN) };

        // when
        List<String> parameters = bank.prepareExecutableParameters(reversalData, BankOperationType.REVERSAL);

        // then
        assertThat(parameters).containsSequence(expectedResult);
    }

    @Test
    public void testPrepareExecutableParametersForPartialRefund() {

        // given
        when(reversalData.isPartial()).thenReturn(true);

        Object[] expectedResult = { String.valueOf(TEST_CASH_NUMBER), String.valueOf(OPERATION_TYPE_REFUND), String.valueOf(TEST_SALE_AMOUNT),
            String.valueOf(TEST_CHECK_NUMBER) };

        // when
        List<String> parameters = bank.prepareExecutableParameters(reversalData, BankOperationType.REVERSAL);

        // then
        assertThat(parameters).containsSequence(expectedResult);
    }

    @Test
    public void testPrepareParametersForDailyLog() {

        // given
        Object[] expectedResult = { String.valueOf(TEST_CASH_NUMBER), DAILY_LOG_OPERATION, TEST_REPORT_AMOUNT, String.valueOf(TEST_CHECK_NUMBER) };

        // when
        List<String> parameters = bank.prepareParametersForDailyLog(TEST_CHECK_NUMBER);

        // then
        assertThat(parameters).containsSequence(expectedResult);
    }

    @Test
    public void testReadResponseFile() throws Exception {

        // given
        List<String> e = singletonList("000 \"Без ошибок!\"");
        List<String> emvtrans = singletonList("1");

        doReturn(e).doReturn(emvtrans).when(bank).readFileAndDelete(anyString(), anyString());

        // when
        List<String> result = bank.readResponseFile();

        // then
        ArrayList<String> expectedResult = new ArrayList<>();
        expectedResult.addAll(e);
        expectedResult.addAll(emvtrans);
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    public void testReadResponseFileThrowsIOException() throws Exception {

        // given
        doThrow(new IOException()).when(bank).readFileAndDelete(anyString(), anyString());

        // when
        try {
            bank.readResponseFile();
        } catch (BankCommunicationException e) {
            assertThat(e.getMessage()).isEqualTo(ResBundleBank.getString("RESPONSE_FILE_NOT_FOUND"));
        }
    }

    @Test
    public void testGetSlipCopy() throws BankException {

        // given
        Collection<String> response = new ArrayList<>();
        response.add("0 \"OK\"");

        when(reversalData.getCashTransId()).thenReturn(TEST_CHECK_NUMBER);
        when(reversalData.getRefNumber()).thenReturn(TEST_RRN);

        List<String> params = new ArrayList<>();
        params.add(PROTOCOL_NUMBER);
        params.add(bank.getCashNumber());
        params.add(GET_SLIPS_COPY_COMMAND);
        params.add(String.valueOf(reversalData.getHostTransId()));
        params.add(String.valueOf(reversalData.getRefNumber()));

        doNothing().when(bank).runExecutable(anyList());
        doReturn(response).when(bank).readResponseFile();

        doReturn(Arrays.asList(SLIP_COPY_FILE_EXAMPLE)).when(bank).readSlipFile();
        bank.start();

        List<String> strings = new ArrayList<>();
        Collections.addAll(strings, SLIP_COPY_FILE_EXAMPLE);

        ServiceOperation operation = new ServiceOperation(ServiceBankOperationType.GET_SLIP_COPY);
        ServiceBankOperationParameter parameter = new ServiceBankOperationParameter("", "");
        parameter.setInputValue("123456789012");
        operation.setParameter(parameter);

        // when
        List<List<String>> slipsCopy = bank.processServiceOperation(operation);

        // then
        verify(bank).runExecutable(params);
        assertThat(strings.containsAll(slipsCopy.get(0))).isTrue();
    }

    @Test
    public void testUpdateTerminalSoftware() throws BankException {

        // given
        String[] expecetdCommand = { PROTOCOL_NUMBER, TEST_CASH_NUMBER, UPDATE_TERMINAL_SOFTWARE, "0", "0" };
        doNothing().when(bank).runExecutable(anyList());
        bank.start();
        ArrayList<String> answer = new ArrayList<>();
        answer.add("0 \"OK\"");
        doReturn(answer).when(bank).readResponseFile();

        // when
        bank.processServiceOperation(new ServiceOperation(ServiceBankOperationType.UPDATE_TERMINAL_SOFTWARE));

        // then
        verify(bank).runExecutable(Arrays.asList(expecetdCommand));
    }

    @Test
    public void testUpdateTerminalParamsWith957Code() throws BankException {

        // given
        String[] expectedCommand = { PROTOCOL_NUMBER, TEST_CASH_NUMBER, UPDATE_TERMINAL_PARAMS, "0", "0" };
        GasCardServiceResponseData responseData = mock(GasCardServiceResponseData.class);
        when(responseData.getResponseCode()).thenReturn("957");
        when(responseData.isSuccessful()).thenReturn(false);
        doReturn(responseData).when(bank).runExecutableAndGetResponseData(anyList());
        ArrayList<String> answer = new ArrayList<>();
        answer.add("957 \"ОШИБКА ОТВЕТА\"");
        doReturn(answer).when(bank).readResponseFile();
        doReturn(new ArrayList<String>()).when(bank).readSlipFile();
        bank.start();

        // when
        List<List<String>> result = bank.processServiceOperation(new ServiceOperation(ServiceBankOperationType.LOAD_TERMINAL_PARAMS));

        // then
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(expectedCommand));
        assertThat(result).isEmpty();
    }

    @Test
    public void testUpdateTerminalParams() throws BankException {

        // given
        String[] expectedCommand = { PROTOCOL_NUMBER, TEST_CASH_NUMBER, UPDATE_TERMINAL_PARAMS, "0", "0" };
        GasCardServiceResponseData responseData = mock(GasCardServiceResponseData.class);
        when(responseData.isSuccessful()).thenReturn(true);
        doReturn(responseData).when(bank).runExecutableAndGetResponseData(anyList());
        ArrayList<String> answer = new ArrayList<>();
        answer.add("0 \"OK\"");
        doReturn(answer).when(bank).readResponseFile();
        doReturn(new ArrayList<String>()).when(bank).readSlipFile();
        bank.start();

        // when
        List<List<String>> result = bank.processServiceOperation(new ServiceOperation(ServiceBankOperationType.LOAD_TERMINAL_PARAMS));

        // then
        verify(bank).runExecutableAndGetResponseData(Arrays.asList(expectedCommand));
        assertThat(result.get(0)).isEmpty();
    }

    @Test
    public void testGetFullReport() throws BankException {

        // given
        Collection<String> response = new ArrayList<>();
        response.add("0 \"OK\"");

        List<String> params = new ArrayList<>();
        params.add(PROTOCOL_NUMBER);
        params.add(bank.getCashNumber());
        params.add(GET_FULL_REPORT_COMMAND);
        params.add(TEST_REPORT_AMOUNT);
        params.add(String.valueOf(TEST_CHECK_NUMBER));

        doNothing().when(bank).runExecutable(anyList());
        doReturn(response).when(bank).readResponseFile();

        doReturn(Arrays.asList(FULL_REPORT_EXAMPLE)).when(bank).readSlipFile();
        bank.start();

        Collection<String> rows = new ArrayList<>();
        Collections.addAll(rows, FULL_REPORT_EXAMPLE);

        // when
        List<List<String>> report = bank.processServiceOperation(new ServiceOperation(ServiceBankOperationType.GET_FULL_REPORT));

        // then
        verify(bank).runExecutable(params);
        assertThat(rows.containsAll(report.get(0))).isTrue();
    }
}
