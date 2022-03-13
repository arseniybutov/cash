// copy of ru.crystals.pos.fiscalprinter.atol.support.BarCodeGenerator

package ru.crystals.pos.fiscalprinter.atol3;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import org.krysalis.barcode4j.HumanReadablePlacement;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.impl.upcean.UPCABean;
import org.krysalis.barcode4j.impl.upcean.UPCEANLogicImpl;
import org.krysalis.barcode4j.impl.upcean.UPCEBean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;

public class BarCodeGenerator {
    private static final int dpi = 300;

    private static byte[] generate(BarCode barcode, float moduleWidth) {
        AbstractBarcodeBean bean = null;

        String barCodeStr = barcode.getValue();
        char checkSum = 0;
        switch (barcode.getType()) {
            case EAN13:
                checkSum = UPCEANLogicImpl.calcChecksum(barCodeStr.substring(0, 12));
                if (barCodeStr.charAt(12) != checkSum)
                    return null;
                bean = new EAN13Bean();
                break;
            case EAN8:
                checkSum = UPCEANLogicImpl.calcChecksum(barCodeStr.substring(0, 7));
                if (barCodeStr.charAt(7) != checkSum)
                    return null;
                bean = new EAN8Bean();
                break;
            case UPCA:
                checkSum = UPCEANLogicImpl.calcChecksum(barCodeStr.substring(0, 11));
                if (barCodeStr.charAt(11) != checkSum)
                    return null;
                bean = new UPCABean();
                break;
            case UPCE:
                checkSum = UPCEANLogicImpl.calcChecksum(barCodeStr.substring(0, 7));
                if ((barCodeStr.charAt(0) != '0' || barCodeStr.charAt(0) != '1') && barCodeStr.charAt(7) != checkSum)
                    return null;
                bean = new UPCEBean();
                break;
            case Code39:
                bean = new Code128Bean();
                break;
            default:
                break;
        }

        if (bean == null) {
            return null;//new byte[0];
        }

        bean.doQuietZone(false);
        bean.setBarHeight(UnitConv.in2mm(1.0f / dpi));
        bean.setMsgPosition(HumanReadablePlacement.HRP_NONE);
        bean.setModuleWidth(UnitConv.in2mm(moduleWidth / dpi));

        BitmapCanvasProvider canvas = new BitmapCanvasProvider(dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
        bean.generateBarcode(canvas, barcode.getValue());
        try {
            canvas.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage bitmap = canvas.getBufferedImage();
        DataBufferByte bytes = (DataBufferByte) bitmap.getData().getDataBuffer();
        return bytes.getData();
    }

    public static byte[] getBarcodeBytes(BarCode barcode, int maxSize) throws IOException {
        float moduleWidth = 1.0f;
        byte[] result = generate(barcode, moduleWidth);
        if (result == null || result.length == 0)
            return result;

        moduleWidth = (float) maxSize / (float) result.length;
        result = generate(barcode, moduleWidth);
        return result;
    }

    public static void main(String[] args) {
        BarCode barcode = new BarCode("0607070570044");
        barcode.setType(BarCodeType.EAN13);
        //barcode.setValue("46088888");
        //barcode.setValue("06088880");
        try {
            byte[] bytes = BarCodeGenerator.getBarcodeBytes(barcode, 66);
            System.out.println(bytes == null ? "NULL" : bytes.length);
            System.out.println(bytes == null ? "NULL" : UtilsAtol.byteArray2Sring(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
