package ru.crystals.pos.fiscalprinter.atol3.highlevel.results;

import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;

public abstract class Result {
    private final int errorCode;

    public Result() {
        this(0);
    }

    public Result(Response response) {
        this(response.getData()[response.getDataOffset() + 1]);
    }

    public Result(int errorCode) {
        this.errorCode = errorCode;

        if (errorCode != 0) {
            throw new RuntimeException(String.format("ASYNC_ERROR: 0x%02X", errorCode));
        }
    }

    public final int getErrorCode() {
        return errorCode;
    }
}
