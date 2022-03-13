package ru.crystals.jpos.scale.emulator;

public class Timer {
	long delay = 5000;
	long startTime = System.currentTimeMillis();

	boolean isOverflow() {
		if ((System.currentTimeMillis() - startTime) > delay) {
			return true;
		} else {
			return false;
		}
	}

	void setDelay(long delay) {
		this.delay = delay;
	}

	void reset() {
		startTime = System.currentTimeMillis();
	}
}
