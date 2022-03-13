package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import com.google.zxing.common.BitMatrix;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.commands.PrintScaledGraphicsCommand;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.fiscalprinter.utils.GraphicsUtils;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.util.List;

/**
 * Коннектор для Штрих Мини ФР-К используемом в Киргизии.
 * ККТ отличается не корректно работающей командой {@link PrintScaledGraphicsCommand}
 * и общим буфером памяти у лого и шк
 */
public class ShtrihMiniFRKConnector extends BaseShtrihConnector {

    /**
     * Печатать битовой матрицы через загрузку и печать графики
     *
     * @param matrix
     *            картинка на печать
     * @param alignment
     *            [горизонтальное] выравнивание этой картинки на чековой ленте
     * @return <code>false</code>, если эту картинку не удалось распечатать по любой причине
     */
    @Override
    protected boolean printPictureAsGraphics(BitMatrix matrix, ShtrihAlignment alignment) throws IOException, PortAdapterException, ShtrihException {
        // максимально возможная ширина картинки/графики, в пикселях:
        int ribbonWidthInPx = getPictureWidth();

        // во сколько раз эту картинку надо отмасштабировать, чтоб получилась максимально большое/читабельное изображение:
        int scale = getScale(ribbonWidthInPx, matrix.getWidth());
        log.trace("the picture should be scaled by {} times for optimal readability", scale);

        if (scale < 1) {
            log.error("printPictureAsGraphics: unable to print matrix 'cause the resulting scale is non-positive");
            return false;
        }

        // отмасштабированая по ширине картинка по-строково:
        List<byte[]> pictureLines = GraphicsUtils.getPictureLines(matrix, getAlignment(alignment), scale, ribbonWidthInPx, false);

        // сколько раз надо повторить каждую линию
        int repeats = scale;
        if (pictureLines.size() == 1) {
            // а это одномерный ШК - для "высоты" таких ШК есть своя настройка:
            repeats = getBarcodeHeightInPx();
            if (repeats < scale) {
                repeats = scale;
            }
        }

        // загружаем картинку в буфер после последней строки логотипа - imageLastLine
        final int firstBarcodeLineNo = getImageLastLine() != null ? (getImageLastLine() + 1) : 0;
        return printPictureLines(pictureLines, repeats, firstBarcodeLineNo);
    }
}
