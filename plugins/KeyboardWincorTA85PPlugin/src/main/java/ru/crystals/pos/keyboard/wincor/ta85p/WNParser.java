package ru.crystals.pos.keyboard.wincor.ta85p;

import java.util.ArrayList;
import java.util.List;

import ru.crystals.pos.keyboard.Parser;

public class WNParser implements Parser {

	private List<Integer> cardPrefix = new ArrayList<>();
	private List<Integer> cardSufix = new ArrayList<>();

	private List<Integer> cardPrefix2 = new ArrayList<>();

	private List<Integer> keyLockPrefix = new ArrayList<>();

	private boolean osTypeIs(String osType) {
		return System.getProperty("os.name").toLowerCase().startsWith(osType);
	}

	@Override
	public List<Integer> getCardPrefix() {
		if (cardPrefix.size() == 0) {
			if (osTypeIs("win")) {
				cardPrefix.add(17);
			}
			cardPrefix.add(17);
			cardPrefix.add(67);// 0x43 -> 'c'
			cardPrefix.add(49);// 0x31 -> '1'
			cardPrefix.add(48);// 0x30 -> '0'
		}
		return cardPrefix;
	}

	@Override
	public List<Integer> getCardPrefix2() {
		if (cardPrefix2.size() == 0) {
			if (osTypeIs("win")) {
				cardPrefix2.add(17);
			}
			cardPrefix2.add(17);// 0x11
			cardPrefix2.add(67);// 0x43 -> 'c'
			cardPrefix2.add(49);// 0x31 -> '1'
			cardPrefix2.add(49);// 0x31 -> '1'
			cardPrefix2.add(48);// 0x30 -> '0'
			cardPrefix2.add(48);// 0x30 -> '0'
			cardPrefix2.add(50);// 0x32 -> '2'
			cardPrefix2.add(48);// 0x30 -> '0'
		}
		return cardPrefix2;
	}

	@Override
	public List<Integer> getCardSufix() {
		if (cardSufix.size() == 0) {
			cardSufix.add(10);// 0x0A
		}
		return cardSufix;
	}

	@Override
	public String[] parseCard(String tracks) {
		StringBuilder track1Builder = new StringBuilder();
		StringBuilder track2Builder = new StringBuilder();
		String track3 = null;

		String[] _tracks = tracks.split("\\?");
		if (_tracks.length > 2) {
			track1Builder.append(_tracks[0]);
			track2Builder.append(_tracks[1].substring(4));
			track3 = (_tracks[2].length() > 4) ? _tracks[2].substring(4) : null;
		} else {
			track2Builder.append(_tracks[0]);
		}

		String track1 = track1Builder.toString();
		if (track1.equals(""))
			track1 = null;
		String track2 = track2Builder.toString();

		return new String[] { track1, track2, track3, null };
	}

	@Override
	public List<Integer> getKeyLockPrefix() {
		if (keyLockPrefix.size() == 0) {
			if (osTypeIs("win")) {
			    keyLockPrefix.add(17);
			}
			keyLockPrefix.add(17);
			keyLockPrefix.add(75);
		}
		return keyLockPrefix;
	}

	@Override
	public List<Integer> getKeyLockSufix() {
		return null;
	}

	@Override
	public int parseKeyPosition(String keyPosition) {
		return Integer.parseInt(keyPosition);
	}

}
