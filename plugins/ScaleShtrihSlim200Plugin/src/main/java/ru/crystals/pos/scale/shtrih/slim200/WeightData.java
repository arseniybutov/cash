package ru.crystals.pos.scale.shtrih.slim200;

public class WeightData {
	private int weight = 0;
	private String unit = "g";
	private int taraWeight = 0;
	private int errorCode = 0;
	/** бит 0 - признак фиксации веса */
	private boolean weightIsFixed = false;
	/** бит 1 - признак работы автонуля */
	private boolean autoZero = false;
	/** бит 2 - "0"- канал выключен, "1"- канал включен. */
	private boolean channelIsEnabled = false;
	/** бит 3 - признак тары */
	private boolean tarePresent = false;
	/** бит 4 - признак успокоения веса */
	private boolean scalesIsStable = false;
	/** бит 5 - ошибка автонуля при включении */
	private boolean zeroOnStartError = false;
	/** бит 6 - перегрузка по весу */
	private boolean overloadScales = false;
	/** бит 7 - ошибка при получении измерения */
	private boolean measureError = false;
	/** бит 8 - весы недогружены */
	private boolean littleWeight = false;
	/** бит 9 - нет ответа от АЦП */
	private boolean noAnswerADP = false;

	@Override
	public String toString() {
		return "weight = " + weight + " | unit = " + unit + " | weightIsFixed = " + weightIsFixed + " | scalesIsStable = " + scalesIsStable
				+ " | overloadScales = " + overloadScales + " | tarePresent = " + tarePresent;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public boolean isWeightIsFixed() {
		return weightIsFixed;
	}

	public void setWeightIsFixed(boolean weightIsFixed) {
		this.weightIsFixed = weightIsFixed;
	}

	public boolean isAutoZero() {
		return autoZero;
	}

	public void setAutoZero(boolean autoZero) {
		this.autoZero = autoZero;
	}

	public boolean isChannelIsEnabled() {
		return channelIsEnabled;
	}

	public void setChannelIsEnabled(boolean channelIsEnabled) {
		this.channelIsEnabled = channelIsEnabled;
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

	public boolean isZeroOnStartError() {
		return zeroOnStartError;
	}

	public void setZeroOnStartError(boolean zeroOnStartError) {
		this.zeroOnStartError = zeroOnStartError;
	}

	public boolean isOverloadScales() {
		return overloadScales;
	}

	public void setOverloadScales(boolean overloadScales) {
		this.overloadScales = overloadScales;
	}

	public boolean isMeasureError() {
		return measureError;
	}

	public void setMeasureError(boolean measureError) {
		this.measureError = measureError;
	}

	public boolean isLittleWeight() {
		return littleWeight;
	}

	public void setLittleWeight(boolean littleWeight) {
		this.littleWeight = littleWeight;
	}

	public boolean isNoAnswerADP() {
		return noAnswerADP;
	}

	public void setNoAnswerADP(boolean noAnswerADP) {
		this.noAnswerADP = noAnswerADP;
	}

	public int getTaraWeight() {
		return taraWeight;
	}

	public void setTaraWeight(int taraWeight) {
		this.taraWeight = taraWeight;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
