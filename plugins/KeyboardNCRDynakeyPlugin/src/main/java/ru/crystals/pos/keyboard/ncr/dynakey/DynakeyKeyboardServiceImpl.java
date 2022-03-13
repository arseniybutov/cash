package ru.crystals.pos.keyboard.ncr.dynakey;

import ru.crystals.pos.keyboard.plugin.KeyboardPluginImpl;

import java.awt.event.KeyEvent;
import java.util.function.IntPredicate;

public class DynakeyKeyboardServiceImpl extends KeyboardPluginImpl {

    private boolean wasPrefix = false;
    private int prefixCode = 0;
    private int codeCoef = 1000;

    /**
     * От клавиатуры с некотрых клавиш идут символы с префиксами 16 и 17
     */
    @Override
    public void keyReleased(KeyEvent e) {
        IntPredicate isIgnoredCode = ch -> (ch == 16 || ch == 17);
        int scanCode = e.getKeyCode();
        if (isIgnoredCode.test(scanCode)) {
            wasPrefix = true;
            prefixCode = scanCode;
        }
        if (isIgnoredCode.negate().test(scanCode)) {
            if (wasPrefix) {
                e.setKeyCode(e.getKeyCode() + (prefixCode * codeCoef));
                resetPrefixState();
            }
            super.keyReleased(e);
        }
    }

    private void resetPrefixState() {
        wasPrefix = false;
        prefixCode = 0;
    }

}