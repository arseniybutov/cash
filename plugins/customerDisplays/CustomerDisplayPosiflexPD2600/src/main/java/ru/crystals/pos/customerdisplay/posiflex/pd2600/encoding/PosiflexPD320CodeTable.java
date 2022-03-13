package ru.crystals.pos.customerdisplay.posiflex.pd2600.encoding;

public class PosiflexPD320CodeTable {
    //Младший и старший по значению кириллические символы в CP866
    private static final int FIRST_CHAR = 0x80;
    private static final int LAST_CHAR = 0xF1;

    //Таблица соответствия символов кириллицы кодировки PD-320U символам кодировки CP866
    private static final byte[] cyrillicTable = new byte[LAST_CHAR - FIRST_CHAR + 1];

    static {
        //В качестве номера элемента используется значечие символа в CP866 - 'А'
        cyrillicTable[0x80 - FIRST_CHAR] = (byte) 0x41; // А
        cyrillicTable[0x81 - FIRST_CHAR] = (byte) 0xA0; // Б
        cyrillicTable[0x82 - FIRST_CHAR] = (byte) 0x42; // В
        cyrillicTable[0x83 - FIRST_CHAR] = (byte) 0xA1; // Г
        cyrillicTable[0x84 - FIRST_CHAR] = (byte) 0xE0; // Д
        cyrillicTable[0x85 - FIRST_CHAR] = (byte) 0x45; // Е
        cyrillicTable[0x86 - FIRST_CHAR] = (byte) 0xA3; // Ж
        cyrillicTable[0x87 - FIRST_CHAR] = (byte) 0xA4; // З
        cyrillicTable[0x88 - FIRST_CHAR] = (byte) 0xA5; // И
        cyrillicTable[0x89 - FIRST_CHAR] = (byte) 0xA6; // Й
        cyrillicTable[0x8A - FIRST_CHAR] = (byte) 0x4B; // К
        cyrillicTable[0x8B - FIRST_CHAR] = (byte) 0xA7; // Л
        cyrillicTable[0x8C - FIRST_CHAR] = (byte) 0x4D; // М
        cyrillicTable[0x8D - FIRST_CHAR] = (byte) 0x48; // Н
        cyrillicTable[0x8E - FIRST_CHAR] = (byte) 0x4F; // О
        cyrillicTable[0x8F - FIRST_CHAR] = (byte) 0xA8; // П

        cyrillicTable[0x90 - FIRST_CHAR] = (byte) 0x50; // Р
        cyrillicTable[0x91 - FIRST_CHAR] = (byte) 0x43; // С
        cyrillicTable[0x92 - FIRST_CHAR] = (byte) 0x54; // Т
        cyrillicTable[0x93 - FIRST_CHAR] = (byte) 0xA9; // У
        cyrillicTable[0x94 - FIRST_CHAR] = (byte) 0xAA; // Ф
        cyrillicTable[0x95 - FIRST_CHAR] = (byte) 0x58; // Х
        cyrillicTable[0x96 - FIRST_CHAR] = (byte) 0xE1; // Ц
        cyrillicTable[0x97 - FIRST_CHAR] = (byte) 0xAB; // Ч
        cyrillicTable[0x98 - FIRST_CHAR] = (byte) 0xAC; // Ш
        cyrillicTable[0x99 - FIRST_CHAR] = (byte) 0xE2; // Щ
        cyrillicTable[0x9A - FIRST_CHAR] = (byte) 0xAD; // Ъ
        cyrillicTable[0x9B - FIRST_CHAR] = (byte) 0xAE; // Ы
        cyrillicTable[0x9C - FIRST_CHAR] = (byte) 0x62; // Ь
        cyrillicTable[0x9D - FIRST_CHAR] = (byte) 0xAF; // Э
        cyrillicTable[0x9E - FIRST_CHAR] = (byte) 0xB0; // Ю
        cyrillicTable[0x9F - FIRST_CHAR] = (byte) 0xB1; // Я

        cyrillicTable[0xA0 - FIRST_CHAR] = (byte) 0x61; // а
        cyrillicTable[0xA1 - FIRST_CHAR] = (byte) 0xB2; // б
        cyrillicTable[0xA2 - FIRST_CHAR] = (byte) 0xB3; // в
        cyrillicTable[0xA3 - FIRST_CHAR] = (byte) 0xB4; // г
        cyrillicTable[0xA4 - FIRST_CHAR] = (byte) 0xE3; // д
        cyrillicTable[0xA5 - FIRST_CHAR] = (byte) 0x65; // е
        cyrillicTable[0xA6 - FIRST_CHAR] = (byte) 0xB6; // ж
        cyrillicTable[0xA7 - FIRST_CHAR] = (byte) 0xB7; // з
        cyrillicTable[0xA8 - FIRST_CHAR] = (byte) 0xB8; // и
        cyrillicTable[0xA9 - FIRST_CHAR] = (byte) 0xB9; // й
        cyrillicTable[0xAA - FIRST_CHAR] = (byte) 0xBA; // к
        cyrillicTable[0xAB - FIRST_CHAR] = (byte) 0xBB; // л
        cyrillicTable[0xAC - FIRST_CHAR] = (byte) 0xBC; // м
        cyrillicTable[0xAD - FIRST_CHAR] = (byte) 0xBD; // н
        cyrillicTable[0xAE - FIRST_CHAR] = (byte) 0x6F; // о
        cyrillicTable[0xAF - FIRST_CHAR] = (byte) 0xBE; // п

        cyrillicTable[0xE0 - FIRST_CHAR] = (byte) 0x70; // р
        cyrillicTable[0xE1 - FIRST_CHAR] = (byte) 0x63; // с
        cyrillicTable[0xE2 - FIRST_CHAR] = (byte) 0xBF; // т
        cyrillicTable[0xE3 - FIRST_CHAR] = (byte) 0x79; // у
        cyrillicTable[0xE4 - FIRST_CHAR] = (byte) 0xE4; // ф
        cyrillicTable[0xE5 - FIRST_CHAR] = (byte) 0x78; // х
        cyrillicTable[0xE6 - FIRST_CHAR] = (byte) 0xE5; // ц
        cyrillicTable[0xE7 - FIRST_CHAR] = (byte) 0xC0; // ч
        cyrillicTable[0xE8 - FIRST_CHAR] = (byte) 0xC1; // ш
        cyrillicTable[0xE9 - FIRST_CHAR] = (byte) 0xE6; // щ
        cyrillicTable[0xEA - FIRST_CHAR] = (byte) 0xC2; // ъ
        cyrillicTable[0xEB - FIRST_CHAR] = (byte) 0xC3; // ы
        cyrillicTable[0xEC - FIRST_CHAR] = (byte) 0xC4; // ь
        cyrillicTable[0xED - FIRST_CHAR] = (byte) 0xC5; // э
        cyrillicTable[0xEE - FIRST_CHAR] = (byte) 0xC6; // ю
        cyrillicTable[0xEF - FIRST_CHAR] = (byte) 0xC7; // я

        cyrillicTable[0xF0 - FIRST_CHAR] = (byte) 0xA5; // Ё
        cyrillicTable[0xF1 - FIRST_CHAR] = (byte) 0xB5; // ё
    }

    //Перекодировка из CP866 в кодировку дисплея Posiflex PD-320U
    public static byte[] convertForPD320(byte[] textBytes) {
        for (int i = 0; i < textBytes.length; i++) {
            int id = (textBytes[i] & 0xFF) - PosiflexPD320CodeTable.FIRST_CHAR;
            if (id >= 0 && id < PosiflexPD320CodeTable.cyrillicTable.length) {
                byte replace = PosiflexPD320CodeTable.cyrillicTable[id];
                textBytes[i] = replace == 0x00 ? textBytes[i] : replace;
            }
        }
        return textBytes;
    }
}
