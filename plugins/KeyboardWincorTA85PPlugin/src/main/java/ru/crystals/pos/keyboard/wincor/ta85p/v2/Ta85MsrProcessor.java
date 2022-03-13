package ru.crystals.pos.keyboard.wincor.ta85p.v2;

import ru.crystals.pos.keyboard.MsrProcessor;
import ru.crystals.pos.msr.MSRSentinels;

import java.util.Collections;
import java.util.List;

public class Ta85MsrProcessor extends MsrProcessor {

    public Ta85MsrProcessor(MSRSentinels msrConfig) {
        super(msrConfig);
    }

    @Override
    public String[] getTracks(List<Integer> sequence) {
        String[] tracks = new String[4];
        // если последовательность начинается с 1 дорожки, внутри нее уже не будет префикса для 2 дорожки,
        // поэтому проверяем отдельно оба случая
        if (startsWith(sequence, msrConfig.getCardPrefix1())) {
            tracks[0] = getFirstTrack(sequence, msrConfig.getCardPrefix1(), msrConfig.getCardSuffix1());
            tracks[1] = getFirstTrackAfter4(sequence);
            tracks[2] = getFirstTrackAfter4(sequence);
        } else if (startsWith(sequence, msrConfig.getCardPrefix2())) {
            tracks[1] = getFirstTrack(sequence, msrConfig.getCardPrefix2(), msrConfig.getCardSuffix2());
            tracks[2] = getFirstTrackAfter4(sequence);
        }

        return tracks;
    }

    /**
     * В начале каждой следующей дорожки есть "лишние" 4 символа (что-то вроде LRC)
     * @return первый трек в последовательности за исключением первых 4 символов
     */
    private String getFirstTrackAfter4(List<Integer> sequence) {
        if (sequence.size() > 4) {
            sequence.subList(0, 4).clear();
            // суффиксы у всех дорожек одинаковые, берем любой
            return getFirstTrack(sequence, Collections.emptyList(), msrConfig.getCardSuffix1());
        }
        return null;
    }
}
