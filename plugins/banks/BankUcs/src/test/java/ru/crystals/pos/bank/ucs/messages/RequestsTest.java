package ru.crystals.pos.bank.ucs.messages;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.ucs.messages.requests.CreditRequest;
import ru.crystals.pos.bank.ucs.messages.requests.FinalizeDayTotalsRequest;
import ru.crystals.pos.bank.ucs.messages.requests.GetReportRequest;
import ru.crystals.pos.bank.ucs.messages.requests.GetTransactionDetailsRequest;
import ru.crystals.pos.bank.ucs.messages.requests.LoginRequest;
import ru.crystals.pos.bank.ucs.messages.requests.ReversalRequest;
import ru.crystals.pos.bank.ucs.messages.requests.SaleRequest;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static ru.crystals.pos.bank.ucs.messages.requests.GetReportRequest.ReportType;

@RunWith(MockitoJUnitRunner.class)
public class RequestsTest {
    public static final long TEST_SALE_AMOUNT = 1000L;
    public static final String TEST_URN = "12345";
    @Mock
    private SaleData saleData;
    @Mock
    private ReversalData reversalData;

    @Before
    public void setUp() throws Exception {
        when(saleData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
        when(reversalData.getRefNumber()).thenReturn(TEST_URN);
        when(reversalData.getAmount()).thenReturn(TEST_SALE_AMOUNT);
    }

    @Test
    public void testCreditRequest() {

        // when
        CreditRequest request = new CreditRequest(saleData);

        // then
        assertThat(request.toString()).isEqualTo("1400000000000C000000001000");
    }

    @Test
    public void testFinalizeDayTotalsRequest() {

        // when
        FinalizeDayTotalsRequest request = new FinalizeDayTotalsRequest();

        // then
        assertThat(request.toString()).isEqualTo("21000000000000");
    }

    @Test
    public void testFullGetReportRequest() {

        // when
        GetReportRequest request = new GetReportRequest(ReportType.FULL);

        // then
        assertThat(request.toString()).isEqualTo("250000000000013");
    }

    @Test
    public void testShortGetReportRequest() {

        // when
        GetReportRequest request = new GetReportRequest(ReportType.SHORT);

        // then
        assertThat(request.toString()).isEqualTo("250000000000012");
    }

    @Test
    public void testGetTransactionDetailsRequest() {

        // when
        GetTransactionDetailsRequest request = new GetTransactionDetailsRequest(TEST_URN);

        // then
        assertThat(request.toString()).isEqualTo("2000000000000C000000012345");
    }

    @Test
    public void testLoginRequest() {

        // when
        LoginRequest request = new LoginRequest();

        // then
        assertThat(request.toString()).isEqualTo("300000000000011");
    }

    @Test
    public void testLoginRequestWithPredefinedTerminalID() {

        // when
        LoginRequest request = new LoginRequest("19999715");

        // then
        assertThat(request.toString()).isEqualTo("30001999971500");
    }

    @Test
    public void testReversalRequest() {

        // when
        ReversalRequest request = new ReversalRequest(reversalData);

        // then
        assertThat(request.toString()).isEqualTo("1A00000000001112345000000001000");
    }

    @Test
    public void testSaleRequest() {

        // when
        SaleRequest request = new SaleRequest(saleData);

        // then
        assertThat(request.toString()).isEqualTo("1000000000000C000000001000");
    }
}
