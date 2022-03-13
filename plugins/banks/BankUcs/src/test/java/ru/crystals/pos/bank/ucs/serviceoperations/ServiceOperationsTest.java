package ru.crystals.pos.bank.ucs.serviceoperations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.datastruct.ServiceBankOperationParameter;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.ucs.messages.requests.GetReportRequest;
import ru.crystals.pos.bank.ucs.messages.requests.GetTransactionDetailsRequest;
import ru.crystals.pos.bank.ucs.messages.requests.Request;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ServiceOperationsTest {
    public static final String TEST_PARAMETER_VALUE = "123";
    @Mock
    private ServiceBankOperationParameter parameter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(parameter.getInputValue()).thenReturn(TEST_PARAMETER_VALUE);
    }

    @Test
    public void testGetFullReportOperation() throws BankException {

        // given
        GetFullReportOperation operation = new GetFullReportOperation();

        // when
        Request request = operation.createRequest();

        // then
        assertThat(operation.hasInitialResponse()).isFalse();
        assertThat(request).isInstanceOf(GetReportRequest.class);
        assertThat(((GetReportRequest) request).getReportType()).isEqualTo(GetReportRequest.ReportType.FULL);
    }

    @Test
    public void testGetShortReportOperation() throws BankException {

        // given
        GetShortReportOperation operation = new GetShortReportOperation();

        // when
        Request request = operation.createRequest();

        // then
        assertThat(operation.hasInitialResponse()).isFalse();
        assertThat(request).isInstanceOf(GetReportRequest.class);
        assertThat(((GetReportRequest) request).getReportType()).isEqualTo(GetReportRequest.ReportType.SHORT);
    }

    @Test
    public void testGetSlipCopyOperation() throws BankException {

        // given
        GetSlipCopyOperation operation = new GetSlipCopyOperation();
        operation.setParameter(parameter);

        // when
        Request request = operation.createRequest();

        // then
        assertThat(operation.hasInitialResponse()).isTrue();
        assertThat(request).isInstanceOf(GetTransactionDetailsRequest.class);
        assertThat(request.toString()).isEqualTo("2000000000000C000000000123");
    }
}
