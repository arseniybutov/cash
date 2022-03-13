package ru.crystals.pos.fiscalprinter.pirit.vikiprint;

import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;

//todo добавить старые модели Пиритов
public enum PiritModel {

    VIKI_PRINT_57_F(3, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint57F"), PrinterCapabilities.fiscal().ofd()),
    VIKI_PRINT_57_PLUS_F(4, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint57PlusF"), PrinterCapabilities.fiscal().ofd()),
//    VIKI_MINI_K(5, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiMiniK"), PrinterCapabilities.fiscal().internal()),
//    VIKI_TOWER_K(6, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiTowerK"), PrinterCapabilities.fiscal().internal()),
//    VIKI_MINI_ENVD(8, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiMiniENVD"), PrinterCapabilities.nonFiscal().internal()),
//    VIKI_TOWER_ENVD(9, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiTowerENVD"), PrinterCapabilities.nonFiscal().internal()),
    VIKI_PRINT_57_K(11, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint57K"), PrinterCapabilities.fiscal().withMaxQRCode(100)),
    VIKI_PRINT_57_PLUS_K(12, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint57PlusK"), PrinterCapabilities.fiscal().withMaxQRCode(100)),
    VIKI_PRINT_80_PLUS_K(13, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint80PlusK"), PrinterCapabilities.fiscal()),
    VIKI_PRINT_57_ENVD(14, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint57ENVD"), PrinterCapabilities.nonFiscal().withMaxQRCode(100)),
    VIKI_PRINT_57_PLUS_ENVD(15, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint57PlusENVD"), PrinterCapabilities.nonFiscal().withMaxQRCode(100)),
    VIKI_PRINT_80_PLUS_ENVD(16, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint80PlusENVD"), PrinterCapabilities.nonFiscal()),
    VIKI_PRINT_80_PLUS_F(5, ResBundleFiscalPrinterPirit.getString("Models.Printers.VikiPrint80PlusF"), PrinterCapabilities.fiscal().ofd()),
    UNKNOWN(0, ResBundleFiscalPrinterPirit.getString("Models.UnknownModel"), PrinterCapabilities.nonFiscal());
    private int modelIndex;
    private String modelName;
    private PrinterCapabilities capabilities;

    PiritModel(int modelIndex, String modelName, PrinterCapabilities capabilities) {
        this.modelIndex = modelIndex;
        this.modelName = modelName;
        this.capabilities = capabilities;
    }

    public int getModelIndex() {
        return modelIndex;
    }

    public static PiritModel getModelByIndex(int index) {
        for (PiritModel model : PiritModel.values()) {
            if (model.getModelIndex() == index) {
                return model;
            }
        }
        return UNKNOWN;
    }

    public String getModelName() {
        return modelName;
    }

    public PrinterCapabilities getCapabilities() {
        return capabilities;
    }
}
