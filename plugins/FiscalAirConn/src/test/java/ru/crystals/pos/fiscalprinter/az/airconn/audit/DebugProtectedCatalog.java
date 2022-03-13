package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import java.io.IOException;
import java.nio.file.Paths;

public class DebugProtectedCatalog {

    public static void main(String[] args) throws IOException {
        final ProtectedCatalog checksumService = new ProtectedCatalogImpl(Paths.get("SetRetail10_Cash/plugins/FiscalAirConn/src/main/resources/fiscal"));
        checksumService.init();
    }
}
