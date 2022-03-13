package ru.crystals.pos.keyboard.wincor.ta85p.v2;

import com.google.common.collect.ImmutableList;
import ru.crystals.pos.msr.MSRSentinels;

import java.util.Collections;
import java.util.List;

public class Ta85MsrConfig implements MSRSentinels {

    private static final List<Integer> CARD_PREFIX_1 = ImmutableList.of(99, 49, 48); // c10
    private static final List<Integer> CARD_PREFIX_2 = ImmutableList.of(99, 49, 49, 48, 48, 50, 48); // c110020
    private static final List<Integer> CARD_SUFFIX = Collections.singletonList(63); // '?'

    @Override
    public List<Integer> getCardPrefix1() {
        return CARD_PREFIX_1;
    }

    @Override
    public List<Integer> getCardSuffix1() {
        return CARD_SUFFIX;
    }

    @Override
    public List<Integer> getCardPrefix2() {
        return CARD_PREFIX_2;
    }

    @Override
    public List<Integer> getCardSuffix2() {
        return CARD_SUFFIX;
    }

    @Override
    public List<Integer> getCardPrefix3() {
        // префикс есть только в начале всей последовательности; с 3 дорожки последовательность не начинается
        return Collections.emptyList();
    }

    @Override
    public List<Integer> getCardSuffix3() {
        return CARD_SUFFIX;
    }
}
