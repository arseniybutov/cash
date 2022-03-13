package ru.crystals.pos.fiscalprinter.documentprinter.epson;

import org.junit.Ignore;
import org.junit.Test;
import ru.crystals.pos.fiscalprinter.documentprinter.system.DocumentPrinterSystemConfig;
import ru.crystals.soket.transport.TransportRequester;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

@Ignore
public class DocumentPrinterEpsonTest {

    /**
     * Print mode values
     */
    // first font = 0x0 (normal size), second font = 0x1 (small size)
    private static final int SECOND_FONT = 0x1;
    private static final int EMPHASIZED_MODE = 0x8;
    private static final int DOUBLE_HEIGHT = 0x10;
    private static final int DOUBLE_WIDTH = 0x20;
    private static final int UNDERLINE_MODE = 0x80;

    private static final byte[] SET_DEFAULT_LINE_SPACING = new byte[]{0x1B, 0x32};

    private static final byte[] OPEN_DRAWER = new byte[]{0x1B, 0x70, 0x30, 0x40, 0x50};

    private static final byte[] SET_PAGE_MODE = new byte[]{0x1B, 0x4C};
    private static final byte[] BATCH_PRINT = new byte[]{0x1B, 0x0C};
    private static final byte[] SET_STANDART_MODE = new byte[]{0x1B, 0x53};

    private static final byte[] PARTIAL_CUT = new byte[]{0x1B, 0x6D};

    // BARCODE_HEIGHT + {size}
    private static final byte[] BARCODE_HEIGHT = new byte[]{0x1D, 0x68};
    // BARCODE_WIDTH + {size}
    private static final byte[] BARCODE_WIDTH = new byte[]{0x1D, 0x77};

    /*
     *   n       Font of HRI characters
     * 0 or 48 - Font A
     * 1 or 49 - Font B
     * 2 or 50 - Font C
     */
    // BARCODE_FONT + {font}
    private static final byte[] BARCODE_FONT = new byte[]{0x1D, 0x66};

    /*
     *   n       Print position
     * 0 or 48 - Not printed
     * 1 or 49 - Above the bar code
     * 2 or 50 - Below the bar code
     * 3 or 51 - Both above and below the bar code
     */
    // BARCODE_POSITION + {position}
    private static final byte[] BARCODE_POSITION = new byte[]{0x1D, 0x66};

    /*
     * Barcodes (BARCODE_TYPE)
     *
     *   4 CODE39 Can be changed 1 ≤ k 0~9, A~Z
     *            SP, $, %, *, +, -, ., / 48 £ d £ 57, 65 £ d £ 90,
     *            d = 32, 36, 37, 42, 43, 45, 46, 47
     *   5 ITF Can be changed 1 ≤ k (even number) 0~9 48 ≤ d ≤ 57
     *   6 CODABAR (NW-7) Can be changed 1 ≤ k 0~9, A~D, a~ d
     *            $, +, -, ., /,: 48 ≤ d ≤ 57, 65 ≤ d ≤ 68, 97 ≤ d ≤ 100
     *            d = 36, 43, 45, 46, 47, 58
     *            (65 ≤ d1 ≤ 68, 65 ≤ dk ≤ 68, 97 ≤ d1 ≤
     *            100, 97 ≤ dk ≤ 100)
     *
     *   65 UPC-A Fixed n = 11, 12 0~9 48 ≤ d ≤ 57
     *   66 UPC-E Fixed 6 ≤ n ≤ 8
     *            n = 11, 12 0~9 48 ≤ d ≤ 57 [However, d 1 = 48 when
     *            n = 7, 8, 11, 12 is specified] )
     *   67 JAN13 (EAN13) Fixed n = 12, 13 0~9 48 ≤ d ≤ 57
     *   68 JAN8 (EAN8) Fixed n = 7, 8 0~9 48 ≤ d ≤ 57
     *   69 CODE39 Can be changed 1 ≤ n ≤ 255 0~9, A~Z
     *            SP, $, %, *, +, -, ., / 48 ≤ d ≤ 57, 65 ≤ d ≤ 90,
     *            d = 32, 36, 37, 42, 43, 45, 46, 47
     *
     *   70 ITF Can be changed 2 ≤ n ≤ 255 0~9 48 ≤ d ≤ 57
     *   71 CODABAR (NW-7) Can be changed 1 ≤ n ≤ 255 0~9, A~D, a~d
     *              $, +, -, ., /, : 48 ≤ d ≤ 57, 65 ≤ d ≤ 68, 97 ≤ d ≤ 100
     *              d = 36, 43, 45, 46, 47, 58
     *              (65 ≤ d1 ≤ 68, 65 ≤ dn ≤ 68, 97 ≤ d1 ≤
     *              100, 97 ≤ dn ≤ 100)
     *   72 CODE93 Can be changed 1 ≤ n ≤ 255 00H~7FH 0 ≤ d ≤ 127
     *   73 CODE128 Can be changed 2 ≤ n ≤ 255 00H~7FH 0 ≤ d ≤ 127 [However d1 = 123, 65 ≤ d2 ≤ 67]
     *   74 UCC/EAN128 Can be changed 2 ≤ n ≤ 255 NUL~SP(7FH) 0 ≤ d ≤ 127
     *   75 RSS-14 Can be changed n = 13 0~9 48 ≤ d ≤ 57
     *   76 RSS-14 Truncated Can be changed n = 13 0~9 48 ≤ d ≤ 57
     *   77 RSS Limited Can be changed n = 13 0~9 48 ≤ d ≤ 57 [However d1= 48, 49]
     *   78 RSS Expanded
     */
    // BARCODE_POSITION + {BARCODE_TYPE} +
    private static final byte[] BARCODE_PRINT = new byte[]{0x1D, 0x6B};

    @Test
    public void testCall() throws Exception {
        TransportRequester r = new TransportRequester("172.29.17.20", 8008);
        DocumentPrinterEpson impl = r.find(DocumentPrinterEpson.class, "DocumentPrinterEpson");
        final DocumentPrinterSystemConfig config = new DocumentPrinterSystemConfig();
        config.setPort("/dev/usb/lp0");
        impl.setConfig(config);
        impl.openDocument();

        // open drawer



        ByteArrayOutputStream b = new ByteArrayOutputStream();

        // Select print mode
        b.write(new byte[]{0x1B, 0x21, (byte) (0xFF & (SECOND_FONT))});

        for (int i = 0; i < 20; i++) {
            b.write(("AAAAAAA" + i).getBytes(Charset.forName("ISO_8859-15")));
            b.write(0x0A);
        }


         b.write(BARCODE_HEIGHT);
         b.write(0x50);

         b.write(BARCODE_PRINT);
         b.write(0x32);// CODE39

         b.write(("496595707379").getBytes(Charset.forName("ISO_8859-15")));// CODE39
         b.write(0x30);
         b.write(0x0A);

        // space for cutter
        for (int i = 0; i < 4; i++) {
            b.write(0x0A);
        }

        b.write(PARTIAL_CUT);


        impl.closeDocument();

        System.out.println("OK");
    }
}
