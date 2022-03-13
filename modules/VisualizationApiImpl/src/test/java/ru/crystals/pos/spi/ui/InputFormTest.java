package ru.crystals.pos.spi.ui;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.api.ui.listener.InputScanNumberFormListener;
import ru.crystals.pos.spi.ui.forms.InputForm;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class InputFormTest {

    @Test
    public void permitScanningFlagTest() {
        InputForm form = new InputForm();
        InputScanNumberFormListener listener = mock(InputScanNumberFormListener.class);
        form.setScanListener(listener);
        String barcode = "123456";

        // when
        form.setPermitScanning(false);
        // then
        Assert.assertTrue(form.barcodeScanned(barcode));
        verifyZeroInteractions(listener);

        // when
        form.setPermitScanning(true);
        // then
        Assert.assertTrue(form.barcodeScanned(barcode));
        verify(listener).eventBarcodeScanned(eq(barcode));
    }
}
