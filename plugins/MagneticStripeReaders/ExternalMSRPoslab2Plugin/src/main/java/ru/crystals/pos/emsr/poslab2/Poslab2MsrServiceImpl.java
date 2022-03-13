package ru.crystals.pos.emsr.poslab2;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.emsr.ExternalMSRPluginImpl;
import ru.crystals.pos.keyboard.MsrProcessor;
import ru.crystals.pos.keyboard.TrackProcessor;
import ru.crystals.pos.msr.MSRSentinels;

import java.util.Collections;
import java.util.List;

public class Poslab2MsrServiceImpl extends ExternalMSRPluginImpl {

    private static final String PROVIDER = "poslab2";

    private final List<Integer> cardPrefix1 = Collections.singletonList(37);
    private final List<Integer> cardSuffix1 = Collections.singletonList(63);
    private final List<Integer> cardPrefix2 = Collections.singletonList(59);
    private final List<Integer> cardSuffix2 = Collections.singletonList(63);
    private final List<Integer> cardPrefix3 = Collections.singletonList(59);
    private final List<Integer> cardSuffix3 = Collections.singletonList(63);

    public Poslab2MsrServiceImpl() {
        super(PROVIDER);
    }

    public Poslab2MsrServiceImpl(TrackProcessor trackProcessor) {
        super(PROVIDER, trackProcessor);
    }

    @Override
    public List<Integer> getCardPrefix1() {
        return cardPrefix1;
    }

    @Override
    public List<Integer> getCardSuffix1() {
        return cardSuffix1;
    }

    @Override
    public List<Integer> getCardPrefix2() {
        return cardPrefix2;
    }

    @Override
    public List<Integer> getCardSuffix2() {
        return cardSuffix2;
    }

    @Override
    public List<Integer> getCardPrefix3() {
        return cardPrefix3;
    }

    @Override
    public List<Integer> getCardSuffix3() {
        return cardSuffix3;
    }

    @Override
    public String[] getTracks(List<Integer> scanCodeList) {

        if (scanCodeList == null || scanCodeList.isEmpty()) {
            return new String[4];
        }

        String[] rawTracks = getRawTracks(scanCodeList, this);
        String[] result = new String[Math.max(rawTracks.length, 4)];
        for (int i = 0; i < rawTracks.length; i++) {
            String track = rawTracks[i];
            if (StringUtils.isEmpty(track)) {
                track = null;
            }
            result[i] = track;
        }
        return result;
    }

    @Override
    protected String[] getRawTracks(List<Integer> scanCodeList, MSRSentinels sentinels) {
        return MsrProcessor.getTracks(scanCodeList, sentinels);
    }
}
