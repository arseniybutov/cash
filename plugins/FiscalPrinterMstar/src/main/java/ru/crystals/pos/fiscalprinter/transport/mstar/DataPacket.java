package ru.crystals.pos.fiscalprinter.transport.mstar;

import ru.crystals.pos.currency.CurrencyUtil;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DataPacket {

    private List<String> params = new ArrayList<>();
    public static final String FS = "\u001C";

    public DataPacket() {
    }

    public DataPacket(String data) {
        this.params.addAll(Arrays.asList(data.split(FS, -1)));
    }

    public void clear() {
        this.params.clear();
    }

    public String getDataBuffer() {
        return String.join(FS, params) + FS;
    }

    public int getCountValue() {
        return params.size();
    }

    public void putStringValue(String value) {
        if (value == null) {
            value = "";
        }
        params.add(value);
    }

    public String getStringValue(int index) throws Exception {
        if (params.size() <= index) {
            throw new Exception("Error parse of data: index=" + index + " out of bound");
        }
        return params.get(index);
    }

    public void putDateValue(Date value) {
        DateFormat df = new SimpleDateFormat("ddMMyy");
        params.add(df.format(value));
    }

    public Date getDateValue(int index) throws Exception {
        DateFormat df = new SimpleDateFormat("ddMMyy");
        return df.parse(getStringValue(index));
    }

    public void putTimeValue(Date value) {
        DateFormat df = new SimpleDateFormat("HHmmss");
        params.add(df.format(value));
    }

    public Date getTimeValue(int index) throws Exception {
        DateFormat df = new SimpleDateFormat("HHmmss");
        return df.parse(getStringValue(index));
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

    public long getLongValue(int index) throws Exception {
        return Long.parseLong(getStringValue(index));
    }

    public void putDoubleValue(double value) {
        params.add(String.format("%.3f", value).replace(',', '.'));
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
        return getDataBuffer().equals(that.getDataBuffer());
    }

    @Override
    public int hashCode() {
        int result = getDataBuffer().hashCode();
        result = 31 * result + FS.hashCode();
        return result;
    }

    @Override
    public String toString() {
        String stringParams = String.join(", ", params);
        return "DataPacket{"
                + "data=" + stringParams
                + ", FS='" + FS + '\''
                + '}';
    }
}
