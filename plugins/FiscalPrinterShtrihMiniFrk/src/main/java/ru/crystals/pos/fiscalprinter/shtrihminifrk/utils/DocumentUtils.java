package ru.crystals.pos.fiscalprinter.shtrihminifrk.utils;

public class DocumentUtils {
    private static final String SPACE = " ";
    private static final String DOUBLE_SPACE = "  ";


    public static String makeHeader(String header, int maxCharRow, char decorateSymbol) {
        if (header.length() > maxCharRow)
            return header.substring(0, maxCharRow);

        StringBuilder decorateText = new StringBuilder();
        StringBuilder headerText = new StringBuilder();
        int availableSpace = maxCharRow - header.length();

        for (int i = 0; i < availableSpace / 2 - 1; i++) {
            decorateText.append(decorateSymbol);
        }

        headerText.append(decorateText.toString());
        headerText.append(SPACE);
        headerText.append(header);
        if (availableSpace % 2 == 0)
            headerText.append(SPACE);
        else
            headerText.append(DOUBLE_SPACE);
        headerText.append(decorateText.toString());

        return headerText.toString();
    }

}