package ru.crystals.pos.emsr.cipher;

import ru.crystals.pos.msr.MSRSentinels;

import java.util.Collections;
import java.util.List;

public class CipherMsrSentinels implements MSRSentinels {

    @Override
    public List<Integer> getCardPrefix1() {
        return Collections.singletonList(37);
    }

    @Override
    public List<Integer> getCardSuffix1() {
        return Collections.singletonList(63);
    }

    @Override
    public List<Integer> getCardPrefix2() {
        return Collections.singletonList(59);
    }

    @Override
    public List<Integer> getCardSuffix2() {
        return Collections.singletonList(63);
    }

    @Override
    public List<Integer> getCardPrefix3() {
        return Collections.singletonList(59);
    }

    @Override
    public List<Integer> getCardSuffix3() {
        return Collections.singletonList(63);
    }
}
