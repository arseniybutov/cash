package ru.crystals.pos.fiscalprinter.atol3.taskbuffer;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Response {
    private static final Logger logger = LoggerFactory.getLogger(Response.class);

    enum Status {
        PENDING(0xA1),
        IN_PROGRESS(0xA2),
        RESULT(0xA3),
        ERROR(0xA4),
        STOPPED(0xA5),
        ASYNC_RESULT(0xA6),
        ASYNC_ERROR(0xA7),
        WAITING(0xA8),
        OVERFLOW(0xB1),
        ALREADY_EXISTS(0xB2),
        NOT_FOUND(0xB3),
        ILLEGAL_VALUE(0xB4);

        private byte code;
        static Map<Byte, Status> map = new HashMap<>();

        static {
            for (Status status : Status.values()) {
                map.put(status.code, status);
            }
        }

        Status(int code) {
            this.code = (byte)code;
        }
    }
    private final int id;
    private final int tid;
    private final byte[] data;
    private final Status status;
    private final int dataOffset;

    private Response(int id, byte[] data) {
        this.id = id;
        this.data = data;
        Status status = Status.map.get(data[0]);
        if (status == null) {
            throw new RuntimeException("Unknown status");
        }
        this.status = status;
        if (isAsyncError()) {
            tid = 0xFF & data[1];
        } else {
            tid = -1;
        }

        dataOffset = isAsyncResponse() ? 2 : 1;

        logger.trace("{}", status);
    }

    public static Response create(int id, byte[] data) {
        return new Response(id, data);
    }

    public int getTId() {
        return tid;
    }

    public byte[] getData() {
        return data;
    }

    public boolean isError() {
        switch (status) {
            case STOPPED:
            case ERROR:
            case OVERFLOW:
            case ALREADY_EXISTS:
            case NOT_FOUND:
            case ILLEGAL_VALUE:
                return true;
            default:
                return false;
        }
    }

    public boolean isAsyncError() {
        return isAsyncResponse() && status == Status.ASYNC_ERROR;
    }

    public boolean isAsyncResultFor(int tid) {
        return isAsyncResponse() && data[1] == tid;
    }

    public boolean isSyncResultFor(int id) {
        return this.id == id;
    }

    private boolean isAsyncResponse() {
        return id == 0xF0;
    }

    public Status getStatus() {
        return status;
    }

    public int getErrorCode() {
        return 0xFF & getData()[3];
    }

    public int getDataOffset() {
        return dataOffset;
    }
}
