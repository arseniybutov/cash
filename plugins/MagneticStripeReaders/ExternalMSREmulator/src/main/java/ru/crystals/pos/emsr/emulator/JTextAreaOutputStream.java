package ru.crystals.pos.emsr.emulator;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.io.OutputStream;

/**
 * OutputStream, который печатает данные напрямую в {@link JTextArea}.
 */
public class JTextAreaOutputStream extends OutputStream {

    private JTextArea textArea;

    /**
     * Конструкто класса. Создаёт новый экземпляр класса {@link JTextAreaOutputStream}
     *
     * @param target экземпляр {@link JTextArea}, в который будут выводиться данные.
     */
    public JTextAreaOutputStream(JTextArea target) {
        this.textArea = target;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b}, 0, 1);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        final String text = new String(buffer, offset, length);
        SwingUtilities.invokeLater(() -> textArea.append(text));
    }
}
