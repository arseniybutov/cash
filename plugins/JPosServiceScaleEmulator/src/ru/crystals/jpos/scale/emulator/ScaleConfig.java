package ru.crystals.jpos.scale.emulator;

public class ScaleConfig {

	private long delayWeightChanges = 5000;
	private long maxWeight = 15000;
	private long minWeight = 0;

	public Long getDelayWeightChanges() {
		return delayWeightChanges;
	}

	public void setDelayWeightChanges(Long delayWeightChanges) {
		this.delayWeightChanges = delayWeightChanges;
	}

	public Long getMaxWeight() {
		return maxWeight;
	}

	public void setMaxWeight(Long maxWeight) {
		this.maxWeight = maxWeight;
	}

	public Long getMinWeight() {
		return minWeight;
	}

	public void setMinWeight(Long minWeight) {
		this.minWeight = minWeight;
	}

}
