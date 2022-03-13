package ru.crystals.pos.fiscalprinter.shtrihminifrk;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentType;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihConnector;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFNStateOne;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class ShtrihServiceFNTest {

    @Mock
    private ShtrihConnector connector;

    @Spy
    @InjectMocks
    private BaseShtrihServiceFN shtrihService = new ShtrihServiceFN100();

    @Test
    public void getLastFiscalDocumentDataTest() throws Exception {
        // given
        ShtrihFNStateOne fnState = new ShtrihFNStateOne();
        fnState.setFnNum("666123456");
        fnState.setLastFdNum(203L);
        doReturn(fnState).when(connector).getFNState();
        FiscalDocumentData fdd = new FiscalDocumentData();
        fdd.setNumFD(963L);
        fdd.setSum(8_49L);
        fdd.setType(FiscalDocumentType.SALE);
        fdd.setFiscalSign(4448880L);
        fdd.setOperationDate(new Date());

        doReturn(fdd).when(connector).getLastDocInfo(eq(fnState.getLastFdNum()));
        ShtrihStateDescription state = new ShtrihStateDescription();
        state.setCurrentDocNo(27);
        doReturn(state).when(connector).getState();

        // when
        FiscalDocumentData result = shtrihService.getLastFiscalDocumentData();

        // then
        Assert.assertSame(fdd, result);
        Assert.assertEquals(fnState.getFnNum(), result.getFnNumber());
        Assert.assertEquals("t=" + new SimpleDateFormat("yyyyMMdd'T'HHmm'00'").format(fdd.getOperationDate())
                + "&s=8.49&fn=666123456&i=963&fp=4448880&n=1", result.getQrCode());
    }
}
