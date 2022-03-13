package ru.crystals.pos.scale.bizerba.ecoasia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.scale.exception.ScaleException;

public class Parser {
    private static final int INDEX_FIRST_STABLE_BYTE = 0;
    private static final int INDEX_SECOND_STABLE_BYTE = 1;
    private static final int INDEX_FIRST_SEPARATOR = 2;
    private static final int INDEX_FIRST_TARA_BYTE = 3;
    private static final int INDEX_SECOND_TARA_BYTE = 4;
    private static final int INDEX_SECOND_SEPARATOR = 5;
    private static final int INDEX_BYTE_SIGN = 6;
    private static final int INDEX_START_BYTE_WEIGHT = 7;
    private static final int INDEX_END_BYTE_WEIGHT = 13;
    private static final int INDEX_CR = 17;
    private static final int INDEX_LF = 18;

    private static final int LENGTH_MESSAGE = 19;
    private static final int SEPARATOR = 0x2C;
    private static final int CR = 0x0D;
    private static final int LF = 0x0A;
    private static final int BYTE_S = 0x53;
    private static final int BYTE_T = 0x54;
    private static final int BYTE_N = 0x4E;
    private static final int POINT = 0x2E;
    private static final int SPACE = 0x20;
    private static final int INDEX_0 = 0x30;
    private static final int INDEX_9 = 0x39;
    private static final int SIGN_MINUS = 0x2D;

    private static final Logger LOG = LoggerFactory.getLogger(BizebraEcoAsiaScalesServiceImpl.class);

    public static WeightData parseWeight(int[] rawWeightData) throws ScaleException {
        WeightData weightData = new WeightData();

        if (rawWeightData.length != LENGTH_MESSAGE) {
            throw new ScaleException("Неверная длина пакета");
        }

        if (!checkPacket(rawWeightData)) {
            throw new ScaleException("Ошибка целостности пакета");
        }

        int weight = getData(rawWeightData, INDEX_START_BYTE_WEIGHT, INDEX_END_BYTE_WEIGHT);
        weight = (rawWeightData[INDEX_BYTE_SIGN] == SIGN_MINUS) ? -weight : weight;

        weightData.setScalesIsStable(((rawWeightData[INDEX_FIRST_STABLE_BYTE] == BYTE_S) && (rawWeightData[INDEX_SECOND_STABLE_BYTE] == BYTE_T)));
        weightData.setWeight(weight);
        weightData.setTarePresent(((rawWeightData[INDEX_FIRST_TARA_BYTE] == BYTE_N) && (rawWeightData[INDEX_SECOND_TARA_BYTE] == BYTE_T)));

        LOG.debug("================= GET WEIGHT ===================");
        LOG.debug("WEIGHT:" + weightData.getWeight());
        LOG.debug("SCALES_IS_STABLE:" + weightData.isScalesIsStable());
        LOG.debug("TARE_IS_PRESENT:" + weightData.isTarePresent());

        return weightData;
    }

    private static int getData(int[] rawData, int startByte, int endByte) {
        int total = 0;
        for (int i = startByte; i <= endByte; i++) {
            if ((rawData[i] != POINT) && (rawData[i] != SPACE)) {
                total *= 10;
                total += rawData[i] - INDEX_0;
            }
        }
        return total;
    }

    private static boolean checkPacket(int[] rawData) {
        boolean isOk = true;
        for (int i = INDEX_START_BYTE_WEIGHT; i <= INDEX_END_BYTE_WEIGHT; i++) {
            if ((rawData[i] != POINT) && (rawData[i] != SPACE)) {
                isOk &= ((rawData[i] >= INDEX_0) && (rawData[i] <= INDEX_9));
            }
        }

        return (isOk && (rawData[INDEX_FIRST_SEPARATOR] == SEPARATOR) && (rawData[INDEX_SECOND_SEPARATOR] == SEPARATOR) && (rawData[INDEX_CR] == CR) &&
                (rawData[INDEX_LF] == LF));
    }

    protected static String intArray2String(int[] bytes) {
        StringBuilder result = new StringBuilder("[");
        int i = 0;
        for (int b : bytes) {
            result.append(b);
            if (i < bytes.length - 1) {
                result.append(",");
            }
            i++;
        }
        return result.append("]").toString();
    }
}
