package ru.crystals.pos.keyboard.wincor.ta85p.v2;

import ru.crystals.pos.keyboard.MsrProcessor;
import ru.crystals.pos.keyboard.ScanCodesProcessor;
import ru.crystals.pos.keyboard.plugin.v2.KeyEventsSequenceResolver;
import ru.crystals.pos.keyboard.plugin.v2.KeyLockConfig;
import ru.crystals.pos.keyboard.plugin.v2.KeySequenceQueue;
import ru.crystals.pos.keylock.KeyLockEvent;
import ru.crystals.pos.msr.MSREvent;
import ru.crystals.pos.msr.MSRSentinels;

import java.util.Map;

public class Ta85SequenceResolver extends KeyEventsSequenceResolver {

    public Ta85SequenceResolver(MSRSentinels keyboardMsrConfig,
                                KeyLockConfig keyLockConfig,
                                KeySequenceQueue keysQueue,
                                ScanCodesProcessor keyboardModule,
                                KeyLockEvent keyLockListener,
                                MSREvent msrListener,
                                Map<Long, Long> virtualMap) {
        super(keyboardMsrConfig, keyLockConfig, keysQueue, keyboardModule, keyLockListener, msrListener, virtualMap);
    }

    @Override
    protected MsrProcessor getMsrProcessor() {
        // здесь будет тот же Ta85MsrConfig, который был передан в конструктор
        return new Ta85MsrProcessor(keyboardMsrConfig);
    }
}
