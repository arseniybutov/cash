package ru.crystals.pos.scale.bizerba.ecoasia;

public class WeightData {
	private int weight = 0;
	/** Признак тары */
	private boolean tarePresent = false;
	/** Признак успокоения веса */
	private boolean scalesIsStable = false;

	@Override
	public String toString() {
		return "weight = " + weight + " | scalesIsStable = " + scalesIsStable
				+ " | tarePresent = " + tarePresent;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isTarePresent() {
		return tarePresent;
	}

	public void setTarePresent(boolean tarePresent) {
		this.tarePresent = tarePresent;
	}

	public boolean isScalesIsStable() {
		return scalesIsStable;
	}

	public void setScalesIsStable(boolean scalesIsStable) {
		this.scalesIsStable = scalesIsStable;
	}
}
