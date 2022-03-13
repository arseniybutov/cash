package ru.crystals.pos.emsr.wincor.beetleipos;

import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.emsr.ExternalMSRPluginImpl;
import ru.crystals.pos.keyboard.TrackProcessor;

import java.util.ArrayList;
import java.util.List;

public class WincorBeetleIposMsrServiceImpl extends ExternalMSRPluginImpl {

    private static final String PROVIDER = "wincor_beetleipos";

    private List<Integer> cardPrefix1;

    private List<Integer> cardSuffix1;

    private List<Integer> cardPrefix2;

    private List<Integer> cardSuffix2;

    public WincorBeetleIposMsrServiceImpl() {
        super(PROVIDER);
    }

    private boolean osTypeIs(String osType) {
        return System.getProperty("os.name").toLowerCase().startsWith(osType);
    }

    @Override
    public List<Integer> getCardPrefix1() {
        if (cardPrefix1 == null) {
            cardPrefix1 = new ArrayList<>();
            if (osTypeIs("win")) {
                cardPrefix1.add(17);
                cardPrefix1.add(17);
                cardPrefix1.add(67);
                cardPrefix1.add(49);
                cardPrefix1.add(48);
            } else {
                cardPrefix1.add(17);
                cardPrefix1.add(67);
                cardPrefix1.add(49);
                cardPrefix1.add(48);
            }
        }
        return cardPrefix1;
    }

    @Override
    public List<Integer> getCardSuffix1() {
        if (cardSuffix1 == null) {
            cardSuffix1 = new ArrayList<>();
            cardSuffix1.add(10);
        }
        return cardSuffix1;
    }

    @Override
    public List<Integer> getCardPrefix2() {
        if (cardPrefix2 == null) {
            cardPrefix2 = new ArrayList<>();
            if (osTypeIs("win")) {
                cardPrefix2.add(17);
                cardPrefix2.add(17);
                cardPrefix2.add(67);
                cardPrefix2.add(49);
                cardPrefix2.add(49);
                cardPrefix2.add(48);
                cardPrefix2.add(48);
                cardPrefix2.add(50);
                cardPrefix2.add(48);
            } else {
                cardPrefix2.add(17);
                cardPrefix2.add(67);
                cardPrefix2.add(49);
                cardPrefix2.add(49);
                cardPrefix2.add(48);
                cardPrefix2.add(48);
                cardPrefix2.add(50);
                cardPrefix2.add(48);
            }
        }
        return cardPrefix2;
    }

    @Override
    public List<Integer> getCardSuffix2() {
        if (cardSuffix2 == null) {
            cardSuffix2 = new ArrayList<>();
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
    public String[] getTracks(List<Integer> scanCodeList) {
        String rawTracks = getRawTracks(scanCodeList);

        StringBuilder track1Builder = new StringBuilder();
        StringBuilder track2Builder = new StringBuilder();
        String track3 = null;

        String[] tracks = rawTracks.replace('?', ':').split(":");
        if (tracks.length > 2) {
            track1Builder.append(tracks[0]);
            track2Builder.append(tracks[1].substring(4));
            track3 = (tracks[2].length() > 4) ? tracks[2].substring(4) : null;
        } else {
            track2Builder.append(tracks[0]);
        }

        String track1 = track1Builder.toString();
        if (track1.isEmpty()) {
            track1 = null;
        }
        String track2 = track2Builder.toString();

        return new String[]{track1, track2, track3, null};
    }

    private String getRawTracks(List<Integer> scanCodeList) {
        String[] tracks = BundleManager.get(TrackProcessor.class).getTracks(scanCodeList, this);
        return toString(tracks[0]) + toString(tracks[1]);
    }

    private String toString(String track) {
        return track != null ? track : "";
    }

}
