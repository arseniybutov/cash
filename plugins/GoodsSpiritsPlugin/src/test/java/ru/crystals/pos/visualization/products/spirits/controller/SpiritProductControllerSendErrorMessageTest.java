package ru.crystals.pos.visualization.products.spirits.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.egais.excise.validation.ExciseValidation;
import ru.crystals.pos.egais.excise.validation.ds.ErrorCode;
import ru.crystals.pos.egais.excise.validation.ds.ErrorData;
import ru.crystals.pos.egais.excise.validation.ds.ErrorExciseData;
import ru.crystals.pos.egais.excise.validation.ds.OperationType;
import ru.crystals.pos.visualization.products.spirits.model.SpiritProductModel;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author Tatarinov Eduard
 */
@RunWith(MockitoJUnitRunner.class)
public class SpiritProductControllerSendErrorMessageTest extends SpiritProductControllerTestBase{


    @Mock
    private ExciseValidation egaisExciseCheckValidation;

    @Mock
    private SpiritProductModel model;

    @InjectMocks
    private SpiritProductController controller;

    @Test
    public void sendErrorDataTest() {
        when(model.getProduct()).thenReturn(getProduct(false));

        controller.sendErrorMessage(getPosition(true), EXCISE, ErrorCode.PRICE_LESS_MRC, OperationType.SALE);

        verify(egaisExciseCheckValidation, times(1)).sendErrorData(eq(getErrorData(false)));
    }

    @Test
    public void sendErrorDataPositionIsKitTest() {
        when(model.getProduct()).thenReturn(getProduct(true));

        controller.sendErrorMessage(getKitPosition(), EXCISE, ErrorCode.PRICE_LESS_MRC, OperationType.SALE);

        verify(egaisExciseCheckValidation, times(1)).sendErrorData(eq(getErrorData(true)));
    }

    @Test
    public void sendErrorDataPositionIsNull() {
        controller.sendErrorMessage(null, EXCISE, ErrorCode.PRICE_LESS_MRC, OperationType.SALE);
        verify(egaisExciseCheckValidation, never()).sendErrorData(any(ErrorData.class));
    }

    @Test
    public void sendErrorDataExciseIsNull() {
        controller.sendErrorMessage(getPosition(true), null, ErrorCode.PRICE_LESS_MRC, OperationType.SALE);
        verify(egaisExciseCheckValidation, never()).sendErrorData(any(ErrorData.class));
    }

    @Test
    public void sendErrorDataErrorCodeIsNull() {
        controller.sendErrorMessage(getPosition(true), EXCISE, null, OperationType.SALE);
        verify(egaisExciseCheckValidation, never()).sendErrorData(any(ErrorData.class));
    }

    @Test
    public void sendErrorDataOperationTypeIsNull() {
        controller.sendErrorMessage(getPosition(true), EXCISE, ErrorCode.PRICE_LESS_MRC, null);
        verify(egaisExciseCheckValidation, never()).sendErrorData(any(ErrorData.class));
    }

    private ErrorData getErrorData(boolean isKit) {
        ErrorExciseData exciseData = new ErrorExciseData(EXCISE, BARCODE, isKit ? PROMO_BARCODE : null);
        ErrorData data = new ErrorData( Stream.of(exciseData).collect(Collectors.toList()) , ErrorCode.PRICE_LESS_MRC, OperationType.SALE);
        return data;
    }
}
