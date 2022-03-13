package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos;

import com.googlecode.jsonrpc4j.JsonRpcClientException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.FiscalDriveAPI;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.lowlevel.dto.ReceiptInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.NotSentDocInfo;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PosApiImplTest {

    @Mock
    private FiscalDriveAPI api;

    private PosApiImpl posApi;

    @Before
    public void setUp() {
        posApi = new PosApiImpl(api);
    }

    @Test
    public void firstNotSentDoc() throws IOException {
        final ReceiptInfo ri = new ReceiptInfo();
        ri.setReceiptSeq("10");
        ri.setTransactionTime(LocalDateTime.parse("2020-05-31T10:10:10"));

        when(api.getReceiptInfo(1)).thenReturn(ri);

        final NotSentDocInfo firstNotSentDoc = posApi.getFirstNotSentDoc()
                .orElseThrow(AssertionError::new)
                .orElse(null);

        assertNotNull(firstNotSentDoc);
        assertEquals(new NotSentDocInfo(LocalDateTime.parse("2020-05-31T10:10:10"), 10L), firstNotSentDoc);
    }

    @Test
    public void firstNotSentDocOnNoDocsError_COUNT_ZERO() throws IOException {
        verifyOnNoDocsError(36870, "ERROR_RECEIPT_COUNT_ZERO");
    }

    @Test
    public void firstNotSentDocOnNoDocsError_INDEX_OUT_OF_BOUNDS() throws IOException {
        verifyOnNoDocsError(36871, "ERROR_RECEIPT_INDEX_OUT_OF_BOUNDS");
    }

    private void verifyOnNoDocsError(int code, String message) throws IOException {
        when(api.getReceiptInfo(1))
                .thenThrow(new JsonRpcClientException(code, message, null));
        final NotSentDocInfo firstNotSentDoc = posApi.getFirstNotSentDoc()
                .orElseThrow(AssertionError::new)
                .orElse(null);
        assertNull(firstNotSentDoc);
    }

}