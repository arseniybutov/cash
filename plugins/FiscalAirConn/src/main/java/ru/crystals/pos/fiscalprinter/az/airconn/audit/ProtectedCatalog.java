package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import java.io.IOException;

public interface ProtectedCatalog {
    void init() throws IOException;

    boolean validate();
}
