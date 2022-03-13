package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import ru.crystals.pos.api.ui.listener.ScanFormListener;
import ru.crystals.pos.spi.ui.UIForms;

public class ScanFormShowcaseScenario extends ShowcaseScenario {

    public ScanFormShowcaseScenario(UIForms formManager) {
        super(formManager);
    }

    @Override
    public void run() {
        showScanForm();
    }

    private void showScanForm() {
       formManager.getInputForms().showScanForm("Сканирование баркода", "Сканируйте баркод товара", new ScanFormListener() {
           @Override
           public void eventBarcodeScanned(String s) {
               // None;
           }

           @Override
           public void eventCanceled() {
                cancelScan();
           }
       });
    }

    private void cancelScan() {formManager.showErrorForm("Отмена", this::showScanForm);}
}
