package ru.crystals.pos.emsr.magtek;

import ru.crystals.pos.emsr.ExternalMSRPluginImpl;
import ru.crystals.pos.keyboard.TrackProcessor;
import ru.crystals.pos.msr.MSRSentinels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MagTekMsrServiceImpl extends ExternalMSRPluginImpl {

    private static final String PROVIDER = "magtek";

    /**
     * С помощью данных суффиксов/префиксов модуль клавиатуры определяет,
     * что последовательность символов относится к внешнему считываетлю магнитных карт.
     */
    private List<Integer> cardPrefix1;

    private List<Integer> cardSuffix1;

    private List<Integer> cardPrefix2;

    private List<Integer> cardSuffix2;

    private List<Integer> cardPrefix3;

    private List<Integer> cardSuffix3;

    /**
     * С помощью данных суффиксов/префиксов разбиваем строку на треки.
     * Сделано из-за символа 10, который приходит всегда в конце последовательности.
     */
    private MSRSentinels sentinels;

    public MagTekMsrServiceImpl() {
        this(null);
    }

    public MagTekMsrServiceImpl(TrackProcessor trackProcessor) {
        super(PROVIDER, trackProcessor);

        sentinels = new MSRSentinels() {

            private List<Integer> mgCardPrefix1;
            private List<Integer> mgCardSuffix1;
            private List<Integer> mgCardPrefix2;
            private List<Integer> mgCardSuffix2;
            private List<Integer> mgCardPrefix3;
            private List<Integer> mgCardSuffix3;

            @Override
            public List<Integer> getCardPrefix1() {
                if (mgCardPrefix1 == null) {
                    mgCardPrefix1 = Collections.singletonList(37);
                }
                return mgCardPrefix1;
            }

            @Override
            public List<Integer> getCardSuffix1() {
                if (mgCardSuffix1 == null) {
                    mgCardSuffix1 = Collections.singletonList(63);
                }
                return mgCardSuffix1;
            }

            @Override
            public List<Integer> getCardPrefix2() {
                if (mgCardPrefix2 == null) {
                    mgCardPrefix2 = Collections.singletonList(59);
                }
                return mgCardPrefix2;
            }

            @Override
            public List<Integer> getCardSuffix2() {
                if (mgCardSuffix2 == null) {
                    mgCardSuffix2 = Collections.singletonList(63);
                }
                return mgCardSuffix2;
            }

            @Override
            public List<Integer> getCardPrefix3() {
                if (mgCardPrefix3 == null) {
                    mgCardPrefix3 = Collections.singletonList(43);
                }
                return mgCardPrefix3;
            }

            @Override
            public List<Integer> getCardSuffix3() {
                if (mgCardSuffix3 == null) {
                    mgCardSuffix3 = Collections.singletonList(63);
                }
                return mgCardSuffix3;
            }
        };
    }

    @Override
    public List<Integer> getCardPrefix1() {
        if (cardPrefix1 == null) {
            cardPrefix1 = new ArrayList<>();
            cardPrefix1.add(37);
        }
        return cardPrefix1;
    }

    @Override
    public List<Integer> getCardSuffix1() {
        if (cardSuffix1 == null) {
            cardSuffix1 = new ArrayList<>();
            cardSuffix1.add(63);
            cardSuffix1.add(10);
        }
        return cardSuffix1;
    }

    @Override
    public List<Integer> getCardPrefix2() {
        if (cardPrefix2 == null) {
            cardPrefix2 = new ArrayList<>();
            cardPrefix2.add(59);
        }
        return cardPrefix2;
    }

    @Override
    public List<Integer> getCardSuffix2() {
        if (cardSuffix2 == null) {
            cardSuffix2 = new ArrayList<>();
            cardSuffix2.add(63);
            cardSuffix2.add(10);
        }
        return cardSuffix2;
    }

    @Override
    public List<Integer> getCardPrefix3() {
        if (cardPrefix3 == null) {
            cardPrefix3 = new ArrayList<>();
            cardPrefix3.add(43);
        }
        return cardPrefix3;
    }

    @Override
    public List<Integer> getCardSuffix3() {
        if (cardSuffix3 == null) {
            cardSuffix3 = new ArrayList<>();
            cardSuffix3.add(63);
            cardSuffix3.add(10);
        }
        return cardSuffix3;
    }

    @Override
    public String[] getTracks(List<Integer> scanCodeList) {
        if (scanCodeList == null || scanCodeList.isEmpty()) {
            return new String[4];
        }

        if (scanCodeList.get(scanCodeList.size() - 1) == 10) {
            // в конце строки всегда 10 приходит, она не относится ни к какому треку
            scanCodeList.remove(scanCodeList.size() - 1);
        }

        return getRawTracks(scanCodeList);
    }

    private String[] getRawTracks(List<Integer> scanCodeList) {
        return getRawTracks(scanCodeList, sentinels);
    }
}
