package ru.crystals.pos.emsr.posiflex;

import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.emsr.ExternalMSRPluginImpl;
import ru.crystals.pos.keyboard.PosiflexMSRUtils;
import ru.crystals.pos.keyboard.TrackProcessor;
import ru.crystals.pos.msr.MSRSentinels;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

public class PosiflexMsrServiceImpl extends ExternalMSRPluginImpl {

    private static final String PROVIDER = "posiflex";

    private List<Integer> cardPrefix1;

    private List<Integer> cardSuffix1;

    private List<Integer> cardPrefix2;

    private List<Integer> cardSuffix2;

    private List<Integer> cardPrefix3;

    private List<Integer> cardSuffix3;

    private MSRSentinels fakeMSR = new MSRSentinels() {
        private List<Integer> cardPrefix1;

        private List<Integer> cardSuffix1;

        private List<Integer> cardPrefix2;

        private List<Integer> cardSuffix2;

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
            }
            return cardSuffix3;
        }
    };

    public PosiflexMsrServiceImpl() {
        super(PROVIDER);
    }

    @Override
    public List<Integer> getCardPrefix1() {
        if (cardPrefix1 == null) {
            cardPrefix1 = new ArrayList<>();
            cardPrefix1.add(48);
            cardPrefix1.addAll(PosiflexMSRUtils.CARDPREFIX);
        }
        return cardPrefix1;
    }

    @Override
    public List<Integer> getCardSuffix1() {
        if (cardSuffix1 == null) {
            cardSuffix1 = new ArrayList<>();
            cardSuffix1.add(48);
            cardSuffix1.addAll(PosiflexMSRUtils.CARDSUFIX);
            cardSuffix1.add(10);
        }
        return cardSuffix1;
    }

    @Override
    public List<Integer> getCardPrefix2() {
        if (cardPrefix2 == null) {
            cardPrefix2 = new ArrayList<>();
            cardPrefix2.add(48);
            cardPrefix2.addAll(PosiflexMSRUtils.CARDPREFIX2);
        }
        return cardPrefix2;
    }

    @Override
    public List<Integer> getCardSuffix2() {
        if (cardSuffix2 == null) {
            cardSuffix2 = new ArrayList<>();
            cardSuffix2.add(48);
            cardSuffix2.addAll(PosiflexMSRUtils.CARDSUFIX2);
            cardSuffix2.add(10);
        }
        return cardSuffix2;
    }

    @Override
    public List<Integer> getCardPrefix3() {
        return null;
    }

    @Override
    public List<Integer> getCardSuffix3() {
        return null;
    }

    @Override
    public MSRSentinels getConfig() {
        return fakeMSR.getConfig();
    }

    /**
     * Новый клавиатурный модуль уже умеет сам разбирать последовательность alt-кодов,
     * поэтому здесь только отрежем лишний enter в конце.
     */
    @Override
    public String[] getTracksForNewKeyboardPlugin(List<Integer> scanCodeList) {
        if (scanCodeList.get(scanCodeList.size() - 1) == KeyEvent.VK_ENTER) {
            return getRawTracks(scanCodeList.subList(0, scanCodeList.size() - 1));
        }
        return getRawTracks(scanCodeList);
    }

    @Override
    public String[] getTracks(List<Integer> scanCodeList) {
        removeSeparator(scanCodeList);
        Queue<Integer> parsedQueue = PosiflexMSRUtils.parseQueue(scanCodeList.toArray(new Integer[scanCodeList.size()]));
        return getRawTracks(new ArrayList<>(parsedQueue));
    }

    private String[] getRawTracks(List<Integer> scanCodeList) {
        return BundleManager.get(TrackProcessor.class).getTracks(scanCodeList, fakeMSR);
    }

    private void removeSeparator(final List<Integer> scanCodes) {
        Iterator<Integer> iter = scanCodes.iterator();
        int step = 0;
        while (iter.hasNext()) {
            iter.next();
            if (step == 0 || step % 3 == 0) {
                iter.remove();
            }
            step++;
        }
    }
}
