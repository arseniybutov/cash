package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.check.PositionSpiritsEntity;
import ru.crystals.pos.check.PurchaseExciseBottleEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.products.spirits.controller.SpiritProductController;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Форма удаления акцизного товара из чека при работающем ЕГАИС
 * Для удаления вместо подтверждения нужно сканировать акцизные марки
 */
public class SpiritDeletePositionForm extends CommonDeletePositionConfirmForm<ProductSpiritsEntity, PositionSpiritsEntity> {
    protected SpiritExcisePanel excisePanel;
    protected List<PurchaseExciseBottleEntity> bottles = new ArrayList<>();
    protected int quantity = 0;

    public SpiritDeletePositionForm(XListener outerListener) {
        super(outerListener);
        this.remove(yesNoPanel);
        excisePanel = new SpiritExcisePanel();
        excisePanel.setMessage(ResBundleVisualization.getString("IS_DELETE_SPIRIT_POSITION"));
        add(excisePanel, BorderLayout.SOUTH);
    }

    @Override
    public void showForm(ProductSpiritsEntity product, PositionSpiritsEntity position) {
        super.showForm(product, position);
        bottles.clear();
        if (position.getKit()) {
            quantity = 0;
            excisePanel.setWarning(false);
            excisePanel.setQuantity(0, position.getExciseBottles().size());
        } else {
            quantity = position.getQntyBigDecimal().intValue();
            excisePanel.setWarning(false);
            excisePanel.setQuantity(position.getQntyBigDecimal().intValue());
        }
        excisePanel.setMessage(ResBundleVisualization.getString("IS_DELETE_SPIRIT_POSITION"));
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
            if(excisePanel.isWarning()) {
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                excisePanel.setWarning(false);
                excisePanel.setMessage(ResBundleVisualization.getString("IS_DELETE_SPIRIT_POSITION"));
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        SpiritProductController co = (SpiritProductController)controller;
        try{
            PurchaseExciseBottleEntity bottleForDelete = co.checkExciseBeforeDelete(barcode, position, bottles);
            if(bottleForDelete!=null){
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                if(excisePanel.isWarning()){
                    excisePanel.setWarning(false);
                    excisePanel.setMessage(ResBundleVisualization.getString("IS_DELETE_SPIRIT_POSITION"));
                }
                if (position.getKit()) {
                    // бутылка нашлась, но из другой позиции
                    if (getModel().getPosition() !=  position) {
                        position = (PositionSpiritsEntity) getModel().getPosition();
                    }
                    bottles.add(bottleForDelete);
                    quantity++;
                    if(quantity == position.getExciseBottles().size()) {
                        return false;
                    }else {
                        excisePanel.setQuantity(quantity, position.getExciseBottles().size());
                    }
                } else {
                    bottles.add(bottleForDelete);
                    quantity--;
                    if(quantity == 0) {
                        return false;
                    }else {
                        excisePanel.setQuantity(quantity);
                    }
                }
            }
        }catch (Exception e){
            excisePanel.setWarning(true);
            excisePanel.setMessage(e.getMessage());
        }
        return true;
    }

    @Override
    public void clear() {

    }

    public void setQuantity(int i) {
        quantity = i;
        excisePanel.setQuantity(i);
    }

    @Override
    public BigDecimal getQuantity() {
        //кол-во удаленных штук равно кол-ву бутылок на данной форме
        return BigDecimal.valueOf(bottles!=null?bottles.size():0);
    }

    public List<PurchaseExciseBottleEntity> getBottles() {
        return bottles;
    }
}
