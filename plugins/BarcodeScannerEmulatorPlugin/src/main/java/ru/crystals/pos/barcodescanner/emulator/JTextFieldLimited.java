package ru.crystals.pos.barcodescanner.emulator;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/**
 * {@link JTextField}, но с ограничением на максимальную длину текста.
 */
public class JTextFieldLimited extends JTextField {
    private int limit;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link JTextFieldLimited}.
     * @param limit максимальная длина текста, содержащегося в поле.
     */
    public JTextFieldLimited(int limit) {
        super();
        this.limit = limit;
    }

    @Override
    protected Document createDefaultModel() {
        return new LimitDocument();
    }

    private class LimitDocument extends PlainDocument {

        @Override
        public void insertString( int offset, String  str, AttributeSet attr ) throws BadLocationException {
            if (str == null) {
                return;
            }
            if ((getLength() + str.length()) <= limit) {
                super.insertString(offset, str, attr);
            }
        }

    }
}