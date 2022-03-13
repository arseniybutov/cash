package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos;

import java.util.function.Function;

public class PosApiResponse<R> {

    private final R response;
    private final PosApiException error;

    private PosApiResponse(R response, PosApiException error) {
        this.response = response;
        this.error = error;
    }

    public static <R> PosApiResponse<R> response(R response) {
        return new PosApiResponse<>(response, null);
    }

    public static <R> PosApiResponse<R> error(PosApiException error) {
        return new PosApiResponse<R>(null, error);
    }

    public <X extends Throwable> R orElseThrow(Function<PosApiException, ? extends X> exceptionSupplier) throws X {
        if (response != null) {
            return response;
        }
        throw exceptionSupplier.apply(error);
    }

    public R getResponse() {
        return response;
    }

    public PosApiException getError() {
        return error;
    }
}
