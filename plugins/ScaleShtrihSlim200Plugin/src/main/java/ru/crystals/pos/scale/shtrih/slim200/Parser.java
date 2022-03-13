package ru.crystals.pos.scale.shtrih.slim200;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.scale.exception.ScaleException;

public class Parser {
    private static final int STX = 0x02;
    private static final int START_BYTE_WEIGHT = 6;
    private static final int END_BYTE_WEIGHT = 9;
    private static final int START_BYTE_WEIGHT_TARA = 10;
    private static final int END_BYTE_WEIGHT_TARA = 11;
    private static final int SHIFT_BITS = 8;

    // Первый байт состояний
    private static final int WEIGHT_IS_FIXED = 1;
    private static final int AUTO_ZERO = 2;
    private static final int CHANNEL_IS_ENABLED = 4;
    private static final int TARE_IS_PRESENT = 8;
    private static final int SCALES_IS_STABLE = 16;
    private static final int ZERO_ON_START_ERROR = 32;
    private static final int OVERLOAD_SCALES = 64;
    private static final int MEASURE_ERROR = 128;

    // Второй байт состояний
    private static final int LITTLE_WEIGHT = 1;
    private static final int NO_ANSWER_ADP = 2;

    private static final int BYTE_OF_ERROR = 3;
    private static final int BYTE_OF_NUMBER_CHANNEL = 4;
    private static final int FIRST_BYTE_OF_STATE = 4;
    private static final int SECOND_BYTE_OF_STATE = 5;
    private static final int BYTE_OF_POWER = 7;
    private static final int FIRST_BYTE_OF_MINIMAL_WEIGHT = 10;
    private static final int SECOND_BYTE_OF_MINIMAL_WEIGHT = 11;

    private static final Logger LOG = LoggerFactory.getLogger(ShtrihSlim200ScaleServiceImpl.class);

    private static int findSTX(int[] rawData) {
        for (int i = 0; i < rawData.length; i++) {
            if (rawData[i] == STX) {
                return i;
            }
        }
        return -1;
    }

    public static WeightData parseWeight(int[] rawWeightData) throws ScaleException {
        WeightData weightData = new WeightData();

        int indexOfSTX = findSTX(rawWeightData);
        if (indexOfSTX < 0) {
            throw new ScaleException("Неверное начало пакета");
        }
        int crcByte = rawWeightData[indexOfSTX + 1] + indexOfSTX + 2;

        if (!checkCRC(rawWeightData, indexOfSTX + 1, rawWeightData.length - 1, rawWeightData[crcByte])) {
            throw new ScaleException("Ошибка контрольной суммы CRC");
        }

        int indexByteOfError = indexOfSTX + BYTE_OF_ERROR;
        int indexFirstByteOfState = indexOfSTX + FIRST_BYTE_OF_STATE;
        int indexSecondByteOfState = indexOfSTX + SECOND_BYTE_OF_STATE;
        int indexStartByteWeight = indexOfSTX + START_BYTE_WEIGHT;
        int indexEndByteWeight = indexOfSTX + END_BYTE_WEIGHT;
        int indexStartByteWeightTara = indexOfSTX + START_BYTE_WEIGHT_TARA;
        int indexEndByteWeightTara = indexOfSTX + END_BYTE_WEIGHT_TARA;
        int byteOfError = rawWeightData[indexByteOfError];
        int firstByteOfState = rawWeightData[indexFirstByteOfState];
        int secondByteOfState = rawWeightData[indexSecondByteOfState];

        weightData.setErrorCode(byteOfError);
        weightData.setWeightIsFixed((firstByteOfState & WEIGHT_IS_FIXED) == WEIGHT_IS_FIXED);
        weightData.setAutoZero((firstByteOfState & AUTO_ZERO) == AUTO_ZERO);
        weightData.setChannelIsEnabled((firstByteOfState & CHANNEL_IS_ENABLED) == CHANNEL_IS_ENABLED);
        weightData.setTarePresent((firstByteOfState & TARE_IS_PRESENT) == TARE_IS_PRESENT);
        weightData.setScalesIsStable((firstByteOfState & SCALES_IS_STABLE) == SCALES_IS_STABLE);
        weightData.setZeroOnStartError((firstByteOfState & ZERO_ON_START_ERROR) == ZERO_ON_START_ERROR);
        weightData.setOverloadScales((firstByteOfState & OVERLOAD_SCALES) == OVERLOAD_SCALES);
        weightData.setMeasureError((firstByteOfState & MEASURE_ERROR) == MEASURE_ERROR);
        weightData.setLittleWeight((secondByteOfState & LITTLE_WEIGHT) == LITTLE_WEIGHT);
        weightData.setNoAnswerADP((secondByteOfState & NO_ANSWER_ADP) == NO_ANSWER_ADP);
        weightData.setWeight(getData(rawWeightData, indexStartByteWeight, indexEndByteWeight));
        weightData.setTaraWeight(getData(rawWeightData, indexStartByteWeightTara, indexEndByteWeightTara));

        LOG.debug("================= GET WEIGHT ===================");
        LOG.debug("WEIGHT:" + weightData.getWeight() + "   WEIGHT_TARA:" + weightData.getTaraWeight());
        LOG.debug("ERROR_CODE:" + weightData.getErrorCode());
        LOG.debug("WEIGHT_IS_FIXED:" + weightData.isWeightIsFixed());
        LOG.debug("SCALES_IS_STABLE:" + weightData.isScalesIsStable());
        LOG.debug("TARE_IS_PRESENT:" + weightData.isTarePresent());
        LOG.debug("OVERLOAD_SCALES:" + weightData.isOverloadScales());
        LOG.debug("AUTO_ZERO:" + weightData.isAutoZero());
        LOG.debug("CHANNEL_IS_ENABLED:" + weightData.isChannelIsEnabled());
        LOG.debug("LITTLE_WEIGHT:" + weightData.isLittleWeight());
        LOG.debug("NO_ANSWER_ADP:" + weightData.isNoAnswerADP());

        return weightData;
    }

    public static int parseChannel(int[] rawData) throws ScaleException {
        int indexOfSTX = findSTX(rawData);
        if (indexOfSTX < 0) {
            throw new ScaleException("Неверное начало пакета");
        }
        int crcByte = rawData[indexOfSTX + 1] + indexOfSTX + 2;

        if (!checkCRC(rawData, indexOfSTX + 1, rawData.length - 1, rawData[crcByte])) {
            throw new ScaleException("Ошибка контрольной суммы CRC");
        }

        int indexByteOfError = indexOfSTX + BYTE_OF_ERROR;
        int indexByteOfNumberChannel = indexOfSTX + BYTE_OF_NUMBER_CHANNEL;
        int errorCode = rawData[indexByteOfError];
        int numberChannel = rawData[indexByteOfNumberChannel];

        LOG.debug("================ CHANNEL NUMBER =================");
        LOG.debug("ERROR_CODE:" + errorCode);
        LOG.debug("NUMBER_CHANNEL:" + numberChannel);

        return numberChannel;
    }

    public static int parseChannelSettings(int[] rawData) throws ScaleException {
        int indexOfSTX = findSTX(rawData);
        if (indexOfSTX < 0) {
            throw new ScaleException("Неверное начало пакета");
        }

        int crcByte = rawData[indexOfSTX + 1] + indexOfSTX + 2;
        if (!checkCRC(rawData, indexOfSTX + 1, rawData.length - 1, rawData[crcByte])) {
            throw new ScaleException("Ошибка контрольной суммы CRC");
        }

        int indexByteOfError = indexOfSTX + BYTE_OF_ERROR;
        int indexByteOfPower = indexOfSTX + BYTE_OF_POWER;
        int indexFirstByteOfMinimalWeight = indexOfSTX + FIRST_BYTE_OF_MINIMAL_WEIGHT;
        int indexSecondByteOfMinimalWeight = indexOfSTX + SECOND_BYTE_OF_MINIMAL_WEIGHT;
        int errorCode = rawData[indexByteOfError];
        byte power = (byte) rawData[indexByteOfPower];
        int minimalWeight = getData(rawData, indexFirstByteOfMinimalWeight, indexSecondByteOfMinimalWeight);

        LOG.debug("=============== CHANNEL SETTINGS ================");
        LOG.debug("ERROR_CODE:" + errorCode);
        LOG.debug("POWER_OF_WEIGHT:" + power);
        LOG.debug("MINIMAL_WEIGHT:" + minimalWeight);
        LOG.debug("MAXIMAL_WEIGHT:" + getData(rawData, indexFirstByteOfMinimalWeight - 2, indexSecondByteOfMinimalWeight - 2));

        return minimalWeight;
    }

    private static int getData(int[] rawData, int startByte, int endByte) {
        int total = 0;
        for (int i = startByte; i <= endByte; i++) {
            total += rawData[i] << ((i - startByte) * SHIFT_BITS);
        }
        return total;
    }

    private static boolean checkCRC(int[] rawData, int startByte, int endByte, int crc) {
        int total = 0;
        for (int i = startByte; i < endByte; i++) {
            total ^= rawData[i];
        }
        return (crc == total);
    }

    protected static int calcCRC(byte[] rawData, int startByte, int endByte) {
        int total = 0;
        for (int i = startByte; i < endByte; i++) {
            total ^= rawData[i];
        }
        return total;
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
