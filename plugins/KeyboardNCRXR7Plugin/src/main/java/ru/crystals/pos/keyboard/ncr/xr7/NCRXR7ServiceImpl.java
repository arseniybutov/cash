package ru.crystals.pos.keyboard.ncr.xr7;

import ru.crystals.pos.keyboard.plugin.KeyboardPluginImpl;

import java.awt.event.KeyEvent;
import java.util.function.IntPredicate;

public class NCRXR7ServiceImpl extends KeyboardPluginImpl {


    /**
     * От клавиатуры с некотрых клавиш идут символы с префиксами 16 и 17, их пропускаем
     * */
    @Override
    public void keyReleased(KeyEvent e) {
        IntPredicate isIgnoredCode = ch -> (ch == 16 || ch == 17);
        int scanCode = e.getKeyCode();
        if (isIgnoredCode.negate().test(scanCode)) {
            super.keyReleased(e);
        }
    }
}
