package ru.crystals.pos.customerdisplay.epson.dm110;

import ru.crystals.pos.customerdisplay.exception.CustomerDisplayPluginException;
import ru.crystals.pos.customerdisplay.plugin.TextCustomerDisplayPluginAbstract;
import ru.crystals.pos.customerdisplay.templates.TemplateProcessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Customer display EPSON DM-110-111 M58DB
 * <p>
 * Display size 20 * 2
 */
public class CDEpsonDM110Plugin extends TextCustomerDisplayPluginAbstract {

    private static final int CLEAR = 0x0C;
    private static final int ESC = 0x1B;

    private final static int ROW_SIZE = 20;

    /**
     * Germany charset
     */
    private Map<Character, Integer> deCharSetMap = new HashMap<>();

    public CDEpsonDM110Plugin() {
        templateProcessor = new TemplateProcessor("epson-dm110-templates.xml");

        deCharSetMap.put('Ä', 0x5B);
        deCharSetMap.put('Ö', 0x5C);
        deCharSetMap.put('Ü', 0x5D);

        deCharSetMap.put('ä', 0x7B);
        deCharSetMap.put('ö', 0x7C);
        deCharSetMap.put('ü', 0x7D);
        deCharSetMap.put('ß', 0x7E);
    }

    @Override
    protected void displayTextAt(int row, int column, String text) throws CustomerDisplayPluginException {

        // Check limits
        if (column < 0 || column > ROW_SIZE - 1 || row < 0 || row > 1 || text == null) {
            throw new CustomerDisplayPluginException("Incorrect input parameters.");
        }

        text = clipString(column, text);

        // Move cursor to the specified position
        byte[] hex = {0x1F, 0x24, (byte) (column + 1), (byte) (row + 1)};

        // Execute
        try {
            executeCommand(convertString(hex, text));
        } catch (Exception ex) {
            throw new CustomerDisplayPluginException(ex.getMessage());
        }

        // Turn off cursor
        executeCommand(new byte[]{0x1F, 0x43, 0});
    }

    private byte[] convertString(byte[] command, String text) throws UnsupportedEncodingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(command);
        byte[] result = text.getBytes(Charset.forName("ISO_8859-15"));
        int i = 0;
        Integer c;
        char[] cText = text.toCharArray();
        while (i < cText.length) {
            c = deCharSetMap.get(cText[i]);
            if (c != null) {
                result[i] = ((byte) (c & 0xFF));
            }
            i++;
        }
        baos.write(result);
        return baos.toByteArray();
    }

    private String clipString(int column, String text) {
        int clipSize = ROW_SIZE - column;
        if (text.length() >= clipSize && clipSize > 0) {
            text = text.substring(0, clipSize);
        }
        return text;
    }

    @Override
    protected int getMaxCharPerLine() {
        return getConfig().getColumnsCount();
    }

    @Override
    public void clearText() throws CustomerDisplayPluginException {
        executeCommand(new byte[]{CLEAR});
    }

    @Override
    public void verifyDevice() throws CustomerDisplayPluginException {
        // not used
    }

    /*

Character Code Table {ESC, 0x74, ...

0 Page 0 [PC437 (U.S.A., Standard Europe)]
1 Page 1 [Katakana]
2 Page 2 [PC850 (Multilingual)]
3 Page 3 [PC860 (Portuguese)]
4 Page 4 [PC863 (Canadian-French)]
5 Page 5 [PC865 (Norwegian)]
16 WPC1252
17 PC866 [Cyrillic #2]
18 PC852 [Latin 2]
19 Page 19 [PC858]
254 Page 254 (user-defined code page)
255 Page 255 (user-defined code page)


Character Set {ESC, 0x52, ...

0 U.S.A.
1 France
2 Germany
3 U.K.
4 Denmark I
5 Sweden
6 Italy
7 Spain I
8 Japan
9 Norway
10 Denmark II
11 Spain II
12 Latin America
13 Korea

     */
    @Override
    public void configureDisplay() throws CustomerDisplayPluginException {
        // code table = Standard Europe (0x00)
        executeCommand(new byte[]{ESC, 0x74, 0x00});

        // character set = Germany(0x02)
        executeCommand(new byte[]{ESC, 0x52, 0x02});
        clearText();
    }

    private void executeCommand(byte[] hex) throws CustomerDisplayPluginException {
        executeCommand(new String(hex));
    }
}
