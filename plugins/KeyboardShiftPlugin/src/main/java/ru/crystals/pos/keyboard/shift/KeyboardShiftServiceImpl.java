package ru.crystals.pos.keyboard.shift;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.keyboard.Key;
import ru.crystals.pos.keyboard.plugin.KeyboardPluginImpl;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.stream.Collectors;

public class KeyboardShiftServiceImpl extends KeyboardPluginImpl {

    private static final Logger LOG = LoggerFactory.getLogger(KeyboardShiftServiceImpl.class);

    @Override
    public void keyPressed(KeyEvent e) {
        // ignore
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == 37) {
            keyCode = 3737;
        }
        if (keyCode != SHIFT_KEY) {
            int keyChar = e.getKeyChar();
            LOG.trace("original keyCode: {}, keyChar: {}", keyCode, keyChar);
            Integer sourceScanCode = null;
            // для поддержки спец символов в штрихкоде
            if (isSpecialChar.test(keyChar)) {
                keyCode = keyChar;
                // для всяких спец кнопок типа F1 или стрелок (нет символа, который надо печатать)
            } else if (keyChar != KeyEvent.CHAR_UNDEFINED) {
                sourceScanCode = keyChar;
            }
            if (e.isShiftDown()) {
                keyCode = Integer.parseInt("1600" + keyCode);
                LOG.trace("modified scanCode: {}", keyCode);
            }
            keysQueue.add(keyCode, sourceScanCode);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // ignore
    }

    @Override
    protected int modifyMSRScanCode(Key key) {
        if (key.getScanCode() > 160000 && !allDelimiters.contains(key.getScanCode())) {
            return key.getScanCode() % 160000;
        }
        return key.getScanCode();
    }

    @Override
    protected Collection<Integer> modifyMSRScanCodes(Collection<Key> keys) {
        return keys.stream().map(this::modifyMSRScanCode).collect(Collectors.toList());
    }
}
