package ru.crystals.pos.keyboard.wincor.ta85p.v2;

import ru.crystals.pos.keyboard.plugin.v2.KeyEventsSequenceResolver;
import ru.crystals.pos.keyboard.plugin.v2.KeyLockChecker;
import ru.crystals.pos.keyboard.plugin.v2.KeyboardPluginV2Impl;

import java.util.function.Function;

public class Ta85pKeyboardV2ServiceImpl extends KeyboardPluginV2Impl {

    private static final String KEY_READER_DRIVER = "./lib/keyboard/keyLockReaderDrv_ta85";

    @Override
    public void start() {
        super.start();
        new KeyLockChecker(
                KEY_READER_DRIVER,
                Function.identity(),
                getKeyLockMap(),
                keyLockListener
        ).check();
    }

    @Override
    protected KeyEventsSequenceResolver createSequenceResolver() {
        return new Ta85SequenceResolver(new Ta85MsrConfig(), keyLockConfig, keysQueue, keyboardModule, keyLockListener, msrListener,
                virtualMap);
    }
}