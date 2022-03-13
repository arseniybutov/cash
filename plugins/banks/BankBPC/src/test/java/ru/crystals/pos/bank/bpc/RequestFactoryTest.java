package ru.crystals.pos.bank.bpc;

import org.junit.Test;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;

import java.util.HashMap;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestFactoryTest {
    public static final long TEST_SALE_AMOUNT = 10000L;
    public static final String TEST_ECR_NUMBER = "01";
    public static final String TEST_ERN = "0000000001";
    public static final String TEST_REF_NUMBER = "1";
    public static final String TEST_RRN_NUMBER = "000000000002";
    public static final String BYN = "BYN";
    private static final byte[] TEST_SALE_REQUEST_BYTES =
        { 0, 40, 1, 3, 80, 85, 82, 2, 2, 48, 49, 3, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 4, 12, 48, 48, 48, 48, 48, 48, 48, 49, 48, 48, 48, 48,
            27, 3, 57, 51, 51 };
    private static final byte[] TEST_REFUND_REQUEST_BYTES =
        { 0, 40, 1, 3, 82, 69, 70, 2, 2, 48, 49, 3, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 4, 12, 48, 48, 48, 48, 48, 48, 48, 49, 48, 48, 48, 48,
            27, 3, 57, 51, 51 };
    private static final byte[] TEST_REFUND_REQUEST_WITH_RRN_BYTES =
        { 0, 54, 1, 3, 82, 69, 70, 2, 2, 48, 49, 3, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 4, 12, 48, 48, 48, 48, 48, 48, 48, 49, 48, 48, 48, 48, 27, 3, 57, 51, 51,
                24, 12, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 48, 50 };
    private static final byte[] TEST_REVERSAL_REQUEST_BYTES =
        { 0, 40, 1, 3, 86, 79, 73, 2, 2, 48, 49, 3, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49, 4, 12, 48, 48, 48, 48, 48, 48, 48, 49, 48, 48, 48, 48, 27, 3, 57, 51, 51 };
    private static final byte[] TEST_DAILY_LOG_REQUEST_BYTES =
        { 0, 21, 1, 3, 83, 84, 76, 2, 2, 48, 49, 3, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49 };
    private static final byte[] TEST_JRN_OPERATION_REQUEST_BYTES =
        { 0, 21, 1, 3, 74, 82, 78, 2, 2, 48, 49, 3, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49 };
    private static final byte[] TEST_HOST_OPERATION_REQUEST_BYTES = { 0, 12, 1, 3, 83, 82, 86, 2, 2, 48, 49, 26, 1, 4 };
    private static final byte[] TEST_PINPAD_OPERATION_REQUEST_BYTES = { 0, 12, 1, 3, 83, 82, 86, 2, 2, 48, 49, 26, 1, 3 };
    private static final byte[] TEST_SERVICE_MENU_OPERATION_REQUEST_BYTES = { 0, 12, 1, 3, 83, 82, 86, 2, 2, 48, 49, 26, 1, 12 };
    private static final byte[] TEST_AUTO_REVERSAL_OPERATION_REQUEST_BYTES =
        { 0, 21, 1, 3, 86, 79, 73, 2, 2, 48, 49, 3, 10, 48, 48, 48, 48, 48, 48, 48, 48, 48, 49 };

    @Test
    public void testCreateSaleRequest() {

        // given
        SaleData saleData = mock(SaleData.class);
        when(saleData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
        when(saleData.getCurrencyCode()).thenReturn(BYN);
        ;

        // when
        Request saleRequest = RequestFactory.createSaleRequest(saleData, TEST_ECR_NUMBER, TEST_ERN);

        // then
        assertThat(saleRequest.toBytes()).isEqualTo(TEST_SALE_REQUEST_BYTES);
    }

    @Test
    public void testCreateRefundRequest() {

        // given
        RefundData refundData = mock(RefundData.class);
        when(refundData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
        when(refundData.getCurrencyCode()).thenReturn(BYN);
        Map<String, String> data = new HashMap<>();
        data.put(RequestFactory.RRN, TEST_RRN_NUMBER);
        when(refundData.getExtendedData()).thenReturn(data);

        // when
        Request refundRequest = RequestFactory.createRefundRequest(refundData, TEST_ECR_NUMBER, TEST_ERN, false);

        // then
        assertThat(refundRequest.toBytes()).isEqualTo(TEST_REFUND_REQUEST_BYTES);

        // when
        refundRequest = RequestFactory.createRefundRequest(refundData, TEST_ECR_NUMBER, TEST_ERN, true);

        // then
        assertThat(refundRequest.toBytes()).isEqualTo(TEST_REFUND_REQUEST_WITH_RRN_BYTES);
    }

    @Test
    public void testCreateReversalRequest() {

        // given
        ReversalData reversalData = mock(ReversalData.class);
        when(reversalData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
        when(reversalData.getRefNumber()).thenReturn(TEST_REF_NUMBER);
        when(reversalData.getCurrencyCode()).thenReturn(BYN);

        // when
        Request reversalRequest = RequestFactory.createReversalRequest(reversalData, TEST_ECR_NUMBER);

        // then
        assertThat(reversalRequest.toBytes()).isEqualTo(TEST_REVERSAL_REQUEST_BYTES);
    }

    @Test
    public void testCreateDailyLogRequest() {

        // when
        Request reversalRequest = RequestFactory.createDailyLogRequest(TEST_ECR_NUMBER, TEST_ERN);

        // then
        assertThat(reversalRequest.toBytes()).isEqualTo(TEST_DAILY_LOG_REQUEST_BYTES);
    }

    @Test
    public void testCreateJRNOperationRequest() {

        // when
        Request jrnRequest = RequestFactory.createJRNOperationRequest(TEST_ECR_NUMBER, TEST_ERN);

        // then
        assertThat(jrnRequest.toBytes()).isEqualTo(TEST_JRN_OPERATION_REQUEST_BYTES);
    }

    @Test
    public void testCreateHostTestRequest() {
        // when
        Request hostTestRequest = RequestFactory.createServiceOperationRequest(TEST_ECR_NUMBER, 0x04);

        // then
        assertThat(hostTestRequest.toBytes()).isEqualTo(TEST_HOST_OPERATION_REQUEST_BYTES);
    }

    @Test
    public void testCreatePinpadTestRequest() {
        // when
        Request hostTestRequest = RequestFactory.createServiceOperationRequest(TEST_ECR_NUMBER, 0x03);

        // then
        assertThat(hostTestRequest.toBytes()).isEqualTo(TEST_PINPAD_OPERATION_REQUEST_BYTES);
    }

    @Test
    public void testCreateServiceMenuOperationRequest() {
        // when
        Request hostTestRequest = RequestFactory.createServiceOperationRequest(TEST_ECR_NUMBER, 0x0C);

        // then
        assertThat(hostTestRequest.toBytes()).isEqualTo(TEST_SERVICE_MENU_OPERATION_REQUEST_BYTES);
    }

    @Test
    public void testCreateAutoReversalOperationRequest() {
        // when
        Request hostTestRequest = RequestFactory.createAutoReversalRequest(TEST_ECR_NUMBER, "1");

        // then
        assertThat(hostTestRequest.toBytes()).isEqualTo(TEST_AUTO_REVERSAL_OPERATION_REQUEST_BYTES);
    }
}
