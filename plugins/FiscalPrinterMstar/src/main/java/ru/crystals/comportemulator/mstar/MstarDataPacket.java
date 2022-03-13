package ru.crystals.comportemulator.mstar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MstarDataPacket {

    private static final Logger LOG = LoggerFactory.getLogger(MstarDataPacket.class);

    private ByteBuffer data;
    private String[] params;
    private String param;

    public MstarDataPacket(ByteBuffer data, String[] params, String param) {
        this.data = data;
        this.params = params;
        this.param = param;
    }

    public MstarDataPacket(ByteBuffer data) {
        setData(data);
    }

    public MstarDataPacket(byte[] data) {
        ByteBuffer dataBuff = ByteBuffer.allocate(data.length);
        dataBuff.put(data);
        setData(dataBuff);
    }

    public MstarDataPacket(String[] params, String param) {
        this.params = params;
        this.param = param;
        byte[] byteData;
        try {
            byteData = paramsAsByteArray();
            this.data = ByteBuffer.allocate(byteData.length);
            this.data.put(byteData);
        } catch (UnsupportedEncodingException ex) {
            LOG.error("", ex);
        }
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
        setParams(data.array());
    }

    public String[] getParams() {
        if (params == null && param != null) {
            params = new String[]{param};
        }
        return params;
    }

    private void setParams(byte[] byteData) {
        try {
            if (byteData.length == 1 && byteData[0] == MstarRequestPacket.FS) {
                params = new String[]{""};
            }

            if (byteData.length == 0) {
                params = new String[0];
            }

            String flatDataString = new String(byteData, MstarRequestPacket.ENCODING);
            if (!flatDataString.contains(MstarRequestPacket.SPLITTER)) {
                param = flatDataString;
            } else {
                Pattern pattern = Pattern.compile("([^" + MstarRequestPacket.SPLITTER + "]*)" + MstarRequestPacket.SPLITTER);
                Matcher matcher = pattern.matcher(flatDataString);
                List<String> result = new LinkedList<>();
                while (matcher.find()) {
                    String substr = matcher.group(1);
                    result.add(substr);
                }
                params = result.toArray(new String[0]);
            }
        } catch (Exception ex) {
            LOG.error("", ex);
        }
    }

    public String getParam() {
        if (param == null && params != null && params.length == 1) {
            return params[0];
        }
        return param;
    }

    public byte[] paramsAsByteArray() throws UnsupportedEncodingException {
        if (param != null) {
            return param.getBytes(MstarRequestPacket.ENCODING);
        }

        StringBuilder sb = new StringBuilder();
        for (String s : params) {
            sb.append(s);
            sb.append((char) MstarRequestPacket.FS);
        }
        return sb.toString().getBytes(MstarRequestPacket.ENCODING);
    }

    public String paramsToString() {
        if (param != null) {
            return "\"" + param + "\"";
        }

        if (params.length == 0) {
            return "new String[0]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("new String[]{\"");
        for (String s : params) {
            sb.append(s.replace("\"", "\\\""));
            sb.append("\", \"");
        }
        sb = sb.delete(sb.length() - 3, sb.length());
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        try {
            return new String(data.array(), MstarRequestPacket.ENCODING);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
