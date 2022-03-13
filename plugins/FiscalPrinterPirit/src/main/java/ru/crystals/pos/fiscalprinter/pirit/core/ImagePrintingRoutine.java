package ru.crystals.pos.fiscalprinter.pirit.core;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.image.context.ImageConverter;
import ru.crystals.image.context.fiscal.FiscalDevice;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

/**
 * Надстройка для Пирит 1Ф/2Ф для печати изображений. Занимается конверсией изображений в пригодный для передачи на печать принтеру формат.
 */
public class ImagePrintingRoutine {
    private static final Logger logger = LoggerFactory.getLogger(ImagePrintingRoutine.class);

    /**
     * Пирит, изображения печатать будут на котором
     */
    private AbstractPirit pirit;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link ImagePrintingRoutine}
     * @param pirit пирит, для которого этот класс будет подготавливать и передавать на печать изображения
     */
    public ImagePrintingRoutine(AbstractPirit pirit) {
        this.pirit = pirit;
    }

    /**
     * Выполняет печать изображения.
     * @param image изображение, которое требуется напечатать.
     * @param align выравнивание изображения. См. {@link AbstractPirit#IMAGE_ALIGN_DEFAULT}, {@link AbstractPirit#IMAGE_ALIGN_LEFT}, etc.
     * @return true если изображение удалось привести в пригодный формат и напечатать, false в противном случае.
     * @throws FiscalPrinterException если во время печати изображения произошли ошибки
     */
    @SuppressWarnings("JavadocReference")
    public boolean printImage(BufferedImage image, long align) throws FiscalPrinterException {
        logger.info("Incoming image: {}x{} ({})", image.getWidth(), image.getHeight(), image.getColorModel());
        FiscalDevice fiscalDevice = pirit.getImagePrintingType();
        if (fiscalDevice == null) {
            logger.error("Unable to print image: unknown Pirit version");
            return false;
        }
        logger.info("Pirit model: \"{}\"", fiscalDevice);
        int paperWidth = ImageConverter.PAPER_WIDTH_80_MM;
        try {
            // Пирит 1Ф не может обработать эту команду, если её отослать, фискальник почему-то
            // выдаёт ошибку при попытке закрыть документ в дальнейшем. Посему решим, что Пирит 1Ф печатает только на 80мм бумаге.
            if (fiscalDevice == FiscalDevice.PIRIT_2) {
                paperWidth = pirit.isUseWidePaper() ? ImageConverter.PAPER_WIDTH_80_MM : ImageConverter.PAPER_WIDTH_57_MM;
            } else {
                logger.warn("This is not a \"{}\" model (actually it is \"{}\"), so we assume the paper width is {} mm",
                        FiscalDevice.PIRIT_2, fiscalDevice, ImageConverter.PAPER_WIDTH_80_MM);
                paperWidth = ImageConverter.PAPER_WIDTH_80_MM;
            }
        } catch (Exception ex) {
            logger.warn("Failed to determine paper width, failing back to {} mm width", ImageConverter.PAPER_WIDTH_80_MM, ex);
        }
        logger.info("Paper width: {} mm", paperWidth);
        int maxWidth = paperWidth == ImageConverter.PAPER_WIDTH_80_MM ? ImageConverter.MAX_IMAGE_WIDTH_PAPER_80 : ImageConverter.MAX_IMAGE_WIDTH_PAPER_57;
        int maxHeight = ImageConverter.MAX_RASTER_SIZE_PIRIT / 8;
        logger.info("Image limitations: width: {}, height: {}", maxWidth, maxHeight);
        BufferedImage converted = new ImageConverter(fiscalDevice).convertImage(image, maxWidth, maxHeight); // общая конвертация
        logger.info("Converted image: width: {}, height: {}", converted.getWidth(), converted.getHeight());

        // для Пирит 2Ф можно напечатать PNG
        boolean isPngPirit2FCommand = FiscalDevice.PIRIT_2 == fiscalDevice;
        if(isPngPirit2FCommand) {
            byte[] imageBytes;
            try(ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                ImageIO.write(converted, "png", output); // на печать надо подать именно PNG формат, а не растр
                imageBytes = output.toByteArray();
            } catch (IOException e) {
                logger.error("Unable to create PNG byte array while printing image", e);
                imageBytes = null;
            }
            if(imageBytes != null) {
                pirit.printImagePNGBase(converted.getWidth(), converted.getHeight(), align, imageBytes);
            }
        } else {
            invertImage(converted); // дополнительная инверсия цвета под пирит
            pirit.printImageBase(converted.getWidth(), converted.getHeight(), align, ((DataBufferByte) converted.getRaster().getDataBuffer()).getData());
        }

        return true;
    }

    /**
     * Инвентирует изображение
     * @param source изображение которое требуется инвертировать
     */
    private void invertImage(BufferedImage source) {
        // Прошивка фискальника воспринимате белый цвет как значимый. Однако печатает-то принтер черным цветом!
        // И пользователь хочет, чтобы напечатано было ровно то, как оно было на картинке. Посему здесь
        // мы инвертируем цвета, благо на этом этапе изображение уже бинарное, чтобы черный цвет стал белым.
        for( int i = 0; i < ((DataBufferByte) source.getRaster().getDataBuffer()).getData().length; ++i) {
            ((DataBufferByte) source.getRaster().getDataBuffer()).getData()[i] ^= 0xFF;
        }
    }
}
