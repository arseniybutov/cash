package ru.crystals.pos.visualization.products.clothing.model;

import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;


public class ClothingProductModel extends CommonProductPluginModel {
    private boolean scanExciseLabelsMode;
    private String cis = null;

    public boolean isScanExciseLabelsMode() {
        return scanExciseLabelsMode;
    }

    public void setScanExciseLabelsMode(boolean useKiS) {
        this.scanExciseLabelsMode = useKiS;
    }

    public void setCis(String cisCode) {
        this.cis = cisCode;
    }

    public String getCis() {
        return cis;
    }

}
