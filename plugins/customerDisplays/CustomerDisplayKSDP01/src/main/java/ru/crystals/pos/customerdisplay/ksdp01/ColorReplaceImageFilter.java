package ru.crystals.pos.customerdisplay.ksdp01;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class ColorReplaceImageFilter extends RGBImageFilter {

    private Color replaceFromColor = null;
    private Color replaceToColor = null;

    public ColorReplaceImageFilter(Color replaceFromColor, Color replaceToColor) {
        super();
        setReplaceFromColor(replaceFromColor);
        setReplaceToColor(replaceToColor);
    }

    public void setReplaceFromColor(Color replaceFromColor) {
        this.replaceFromColor = replaceFromColor;
    }

    public Color getReplaceFromColor() {
        return replaceFromColor;
    }

    public void setReplaceToColor(Color replaceToColor) {
        this.replaceToColor = replaceToColor;
    }

    public Color getReplaceToColor() {
        return replaceToColor;
    }

    @Override
    public int filterRGB(int arg0, int arg1, int arg2) {
        int newColor = arg2;
        if ((arg2 % 0xffffff00) == getReplaceFromColor().getRGB()) {
            newColor = getReplaceToColor().getRGB();
        }
        return newColor;
    }

}
