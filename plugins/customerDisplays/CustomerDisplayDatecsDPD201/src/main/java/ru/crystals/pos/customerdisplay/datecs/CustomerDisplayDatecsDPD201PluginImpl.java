package ru.crystals.pos.customerdisplay.datecs;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;

public class CustomerDisplayDatecsDPD201PluginImpl extends TextCustomerDisplayPluginAbstract {

    private static final int MAX_CHAR_PER_LINE = 20;

    private static final int CLR = 0x0C;

    private static final int ESC = 0x1B;

    private static final int US = 0x1F;

    private static final int PC850 = 0x01;

    //Команды включения/выключения отображения пользовательских символов
    private static final String USER_CHARS_ON = new String(new byte[]{ESC, 0x25, 0x01});
    private static final String USER_CHARS_OFF = new String(new byte[]{ESC, 0x25, 0x00});

    //Младший и сташий по значению кириллические символы в UTF-8
    private static final char FIRST_CHAR = 'Ё';
    private static final char LAST_CHAR = 'ё';

    //Таблица перекодировки кириллицы из UTF-8 в кодировку PC850 дисплея Datecs
    private static final String[] cyrillicTable = new String[LAST_CHAR - FIRST_CHAR + 1];
    //Закодированное в 5 символах кодировки PC850 изображение для пользвательского символа 'Ь' соответствующие матрице 5x7
    private static final char[] SOFT_SIGN_IMG = {'B'/*0x42*/, 0x10, 'õ'/*0xE4*/, 'Ñ'/*0xA5*/, '└'/*0xC0*/};
    //Символ вместо которого будет выводится пользовательский 'Ь' после выполнения команды "USER_CHARS_ON"
    private static final char SOFT_SIGN_ID = '#';

    //В Datecs DPD-201 для отображения кириллицы в кодировке PC850 используются латинские аналоги
    //и специально зарезервированные символы
    static {
        cyrillicTable['А' - FIRST_CHAR] = "A";
        cyrillicTable['Б' - FIRST_CHAR] = "░"; //Char code in PC850: 0xB0
        cyrillicTable['В' - FIRST_CHAR] = "B";
        cyrillicTable['Г' - FIRST_CHAR] = "▒"; //0xB1
        cyrillicTable['Д' - FIRST_CHAR] = "▓"; //0xB2
        cyrillicTable['Е' - FIRST_CHAR] = "E";
        cyrillicTable['Ё' - FIRST_CHAR] = "E";
        cyrillicTable['Ж' - FIRST_CHAR] = "│"; //0xB3
        cyrillicTable['З' - FIRST_CHAR] = "┤"; //0xB4
        cyrillicTable['И' - FIRST_CHAR] = "©"; //0xB8
        cyrillicTable['Й' - FIRST_CHAR] = "╣"; //0xB9
        cyrillicTable['К' - FIRST_CHAR] = "K";
        cyrillicTable['Л' - FIRST_CHAR] = "║"; //0xBA
        cyrillicTable['М' - FIRST_CHAR] = "M";
        cyrillicTable['Н' - FIRST_CHAR] = "H";
        cyrillicTable['О' - FIRST_CHAR] = "O";
        cyrillicTable['П' - FIRST_CHAR] = "╗"; //0xBB
        cyrillicTable['Р' - FIRST_CHAR] = "P";
        cyrillicTable['С' - FIRST_CHAR] = "C";
        cyrillicTable['Т' - FIRST_CHAR] = "T";
        cyrillicTable['У' - FIRST_CHAR] = "╝"; //0xBC
        cyrillicTable['Ф' - FIRST_CHAR] = "┐"; //0xBF
        cyrillicTable['Х' - FIRST_CHAR] = "X";
        cyrillicTable['Ц' - FIRST_CHAR] = "└"; //0xC0
        cyrillicTable['Ч' - FIRST_CHAR] = "┴"; //0xC1
        cyrillicTable['Ш' - FIRST_CHAR] = "┬"; //0xC2
        cyrillicTable['Щ' - FIRST_CHAR] = "├"; //0xC3
        cyrillicTable['Ъ' - FIRST_CHAR] = "─"; //0xC4
        cyrillicTable['Ы' - FIRST_CHAR] = "┼"; //0xC5
        //для вывода 'Ь' используется сохраненный в defineUserChar() пользовательский символ
        cyrillicTable['Ь' - FIRST_CHAR] = USER_CHARS_ON + SOFT_SIGN_ID + USER_CHARS_OFF;
        cyrillicTable['Э' - FIRST_CHAR] = "╚"; //0xC8
        cyrillicTable['Ю' - FIRST_CHAR] = "╔"; //0xC9
        cyrillicTable['Я' - FIRST_CHAR] = "╩"; //0xCA

        // Копируем значения таблици из UpperCase в LowerCase, т.к. DPD-201 отображает кириллицу только в UpperCase
        for (int i = 0; i < cyrillicTable.length; i++) {
            char upper = (char) (i + FIRST_CHAR);
            char lower = Character.toLowerCase(upper);
            if (cyrillicTable[i] != null) {
                cyrillicTable[lower - FIRST_CHAR] = cyrillicTable[i];
            }
        }
    }

    @Override
    public void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {
        if (column < 0 || column >= MAX_CHAR_PER_LINE || row < 0 || row > 1) {
            throw new CustomerDisplayPluginException("Incorrect input parameters.");
        }

        byte[] hex = {US, 0x24, (byte) (column + 1), (byte) (row + 1)};
        executeCommand(new String(hex) + convertForPC850(text));
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        byte[] hex = {CLR};
        executeCommand(new String(hex));
    }

    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        clearText();
        setCodePage();
        setOverwriteMode();
        defineUserChar(SOFT_SIGN_ID, SOFT_SIGN_IMG);
    }

    public void setCodePage() throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x74, PC850};
        executeCommand(new String(hex));
    }

    public void setOverwriteMode() throws CustomerDisplayPluginException {
        byte[] hex = {US, 0x01};
        executeCommand(new String(hex));
    }

    //Команда сохранения пользовательских символов
    public void defineUserChar(char id, char[] charImage) throws CustomerDisplayPluginException {
        byte[] hex = {ESC, 0x26};
        executeCommand(new String(hex) + id + new String(charImage));
    }

    /**
     * Устройство нельзя проверить т.к. оно не возвращает ответ на команды
     *
     * @throws CustomerDisplayPluginException
     */
    @Override
    public void verifyDevice() throws CustomerDisplayPluginException {
    }

    @Override
    protected int getMaxCharPerLine() {
        return MAX_CHAR_PER_LINE;
    }

    //Замена кириллицы в строке на латинские буквы и спец символы
    private String convertForPC850(String text) {
        char[] charBuffer = text.toCharArray();
        StringBuilder sb = new StringBuilder(text.length());
        for (char symbol : charBuffer) {
            int idx = symbol - FIRST_CHAR;
            if (idx >= 0 && idx < cyrillicTable.length) {
                String replace = cyrillicTable[idx];
                sb.append(replace == null ? symbol : replace);
            } else {
                sb.append(symbol);
            }
        }
        return sb.toString();
    }
}
