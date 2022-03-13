package ru.crystals.pos.fiscalprinter.transport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.currency.CurrencyUtil;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DataPacket {

    private static final Logger log = LoggerFactory.getLogger(DataPacket.class);

    public static final DataPacket EMPTY = new DataPacket();

    /**
     * То что возвращает фискальник, на запрос даты последнего неотправленного документа, если таких документов нет
     */
    private static final String UNSPECIFIED_DATE = "000000";

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("ddMMyy");

    private final List<String> params = new ArrayList<>();
    public static final String FS = "\u001C";

    public DataPacket() {
    }

    public DataPacket(String data) {
        if (data.equals(FS) || data.isEmpty()) {
            return;
        }
        this.params.addAll(Arrays.asList(data.split(FS, -1)));
    }

    public DataPacket(String[] data) {
        if (data.length == 0 || (data.length == 1 && data[0].isEmpty())) {
            return;
        }
        this.params.addAll(Arrays.asList(data));
    }

    public DataPacket(byte[] rawData) {
        this(new String(rawData, Charset.forName("cp866")));
    }

    public void clear() {
        this.params.clear();
    }

    public String getDataBuffer() {
        if (params.isEmpty()) {
            return "";
        }
        return String.join(FS, params);
    }

    public int getCountValue() {
        return params.size();
    }

    public boolean isEmpty() {
        return params.isEmpty();
    }

    public boolean hasNonNullValues() {
        return !params.isEmpty() && params.stream().anyMatch(StringUtils::isNotEmpty);
    }

    public void putStringValue(String value) {
        if (value == null) {
            value = "";
        }
        params.add(value);
    }

    public String getStringValue(int index) {
        if (params.size() <= index) {
            final IndexOutOfBoundsException exc = new IndexOutOfBoundsException("Error parse of data: Index=" + index + " out of bound");
            if (params.size() == index) {
                /*
                 Это дополнительная защита от рефакторинга SRTZ-240, после которого мы больше не получаем в ответе
                 данные за последним разделителем FS (их на самом деле и нет там, поскольку разделитель ставится после данных, а не перед ними)
                 Раньше данных в ответе всегда было на одно последнее пусто поле больше (тут мы его и возвращаем,
                 если от нас хотят "последнее" в старом варианте поле)

                 Код будет удален, когда выгребем все потенциально оставшиеся ошибки (две выгребли уже)
                */
                log.error("Using empty string as a fallback (index should be fixed)", exc);
                return "";
            }
            throw exc;
        }
        return params.get(index);
    }

    public String getStringValueNull(int index) {
        if (params.size() <= index) {
            return null;
        }
        return params.get(index);
    }

    public void putDateValue(LocalDate localDate) {
        params.add(localDate.format(dateFormatter));
    }

    public void putDateAndTime(LocalDateTime localDateTime) {
        params.add(localDateTime.format(dateFormatter));
        params.add(localDateTime.format(timeFormatter));
    }

    public Optional<LocalDateTime> getOptionalDateTimeValue(int dateIndex, int timeIndex) throws DateTimeParseException {
        return getOptionalDateValue(dateIndex).map(date -> LocalDateTime.of(date, getOptionalTimeValue(timeIndex).orElse(LocalTime.MIDNIGHT)));
    }

    public Optional<LocalTime> getOptionalTimeValue(int index) throws DateTimeParseException {
        return Optional.ofNullable(getStringValueNull(index)).map(str -> LocalTime.parse(str, timeFormatter));
    }

    public Optional<LocalDate> getOptionalDateValue(int index) throws DateTimeParseException {
        return Optional.ofNullable(getStringValueNull(index))
                .filter(str -> !Objects.equals(str, UNSPECIFIED_DATE))
                .map(str -> LocalDate.parse(str, dateFormatter));
    }

    public void putLongValue(Long value) {
        if (value != null) {
            params.add(value.toString());
        } else {
            params.add("");
        }
    }

    public void putIntValue(Integer value) {
        if (value != null) {
            params.add(value.toString());
        } else {
            params.add("");
        }
    }

    public void putIntValue(int value) {
        params.add(String.valueOf(value));
    }

    public void putIntValue(int value, int index) {
        params.add(index, String.valueOf(value));
    }

    public long getLongValue(int index) throws Exception {
        return Long.parseLong(getStringValue(index));
    }

    public Optional<Integer> getIntegerSafe(int index) {
        if (params.size() <= index) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(params.get(index)));
        } catch (NumberFormatException nfe) {
            return Optional.empty();
        }
    }

    public void putDoubleValue(double value) {
        params.add(String.format("%.3f", value).replace(',', '.'));
    }

    public void putBigDecimalValue(BigDecimal value) {
        params.add(value.toString());
    }

    public void putDoubleValue(double value, int index) {
        params.add(String.format("%." + index + "f", value).replace(',', '.'));
    }

    public long getDoubleToRoundLong(int index) throws Exception {
        String stringValue = getStringValue(index);
        if (stringValue.length() == 0) {
            return 0;
        }
        return new BigDecimal(stringValue).longValue();
    }

    public long getDoubleMoneyToLongValue(int index) throws Exception {
        String stringValue = getStringValue(index);
        int dotIndex = stringValue.indexOf('.');
        if (dotIndex == -1) {
            stringValue = stringValue + ".00";
        }
        if (dotIndex == stringValue.length() - 1) {
            stringValue = stringValue + "0";
        }
        if (dotIndex == stringValue.length() - 2) {
            stringValue = stringValue + "00";
        }
        return CurrencyUtil.convertMoney(new BigDecimal(stringValue));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DataPacket that = (DataPacket) o;
        return params.equals(that.params);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(params);
    }

    @Override
    public String toString() {
        return "[" + String.join(";", params) + "]";
    }

    public List<String> getParams() {
        return params;
    }
}
