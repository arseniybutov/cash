package ru.crystals.pos.scale.cas.ad;

public class Parser {
    public static WeightData parse(int[] rawWeightData) {
        WeightData weightData = new WeightData();

        if ((char) rawWeightData[2] == 'S') {
            weightData.setStable(true);
        }

        if ((char) rawWeightData[3] == '-') {
            weightData.setNegativeWeight(true);
        } else if ((char) rawWeightData[3] == 'F') {
            weightData.setOverload(true);
        }

        weightData.setWeight(getWeight(rawWeightData));
        weightData.setUnit(getWeightUnit(rawWeightData));

        return weightData;
    }

    private static String getWeight(int[] rawWeightData) {
        return getString(rawWeightData, 4, 10);
    }

    private static String getWeightUnit(int[] rawWeightData) {
        return getString(rawWeightData, 10, 12);
    }

    private static String getString(int[] rawData, int beginIndex, int endIndex) {
        StringBuilder buffer = new StringBuilder();
        for (int i = beginIndex; i < endIndex; i++) {
            buffer.append((char) rawData[i]);
        }
        return buffer.toString();
    }
}
