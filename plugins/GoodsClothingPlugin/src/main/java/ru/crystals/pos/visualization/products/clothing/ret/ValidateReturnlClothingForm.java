package ru.crystals.pos.visualization.products.clothing.ret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.ScreenSaverEvents;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.check.PositionClothingEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.cis.validation.CisValidation;
import ru.crystals.pos.cis.validation.CisValidationState;
import ru.crystals.pos.events.EventProxyFactory;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponent;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponentType;
import ru.crystals.pos.visualization.admin.components.cashinventory.CashInventoryComponent;
import ru.crystals.pos.visualization.check.ret.ReturnPositionContainer;
import ru.crystals.pos.visualization.check.ret.SelectReturnTypeContainer;
import ru.crystals.pos.visualization.products.clothing.ResBundleGoodsClothing;
import ru.crystals.pos.visualization.products.clothing.integration.ClothingPluginAdapter;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualizationtouch.components.ElementFactory;
import ru.crystals.pos.visualizationtouch.components.XCardLayout;
import ru.crystals.pos.visualizationtouch.components.XFont;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author Tatarinov Eduard
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ClothingPluginAdapter.PRODUCT_TYPE)
public class ValidateReturnlClothingForm extends JPanel implements XListener, ScreenSaverEvents {

    private static final Logger LOG = LoggerFactory.getLogger(CashInventoryComponent.class);

    private XCardLayout cardLayout = new XCardLayout();
    private ValidateReturnClothingInfoPanel infoPanel = new ValidateReturnClothingInfoPanel();
    private ValidateReturnClotingInfoPanelShort infoPanelShort = new ValidateReturnClotingInfoPanelShort();
    private CommitCancelComponent commitCancelComponent
            = new CommitCancelComponent(CommitCancelComponentType.YES_NO, ResBundleVisualization.getString("OK"), ResBundleVisualization.getString("CANCEL"));
    private PositionClothingEntity source = null;
    private PositionClothingEntity target = null;
    private SelectReturnTypeContainer selectReturnTypeContainer;
    private ReturnPositionContainer positionContainer;
    private CisValidation cisValidation;
    private boolean fullReturn = false;
    private final AtomicBoolean waitValidation = new AtomicBoolean(false);
    private final AtomicBoolean emergencyExit = new AtomicBoolean(false);
    private final Object processLock = new Object();

    public ValidateReturnlClothingForm() {
        Label confirmLabel = new Label(ResBundleGoodsClothing.getString("RETURN_SKIP_CIS_SCAN"));
        confirmLabel.setFont(new XFont(MyriadFont.getItalic(37F), 1.0f));
        confirmLabel.setPreferredSize(new ScaleDimension(620, 190));
        confirmLabel.setAligmentY(ElementFactory.AligmentY.Y_ALIGMENT_CENTER);
        confirmLabel.setAligmentX(ElementFactory.AligmentX.X_ALIGMENT_CENTER);
        confirmLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
        confirmLabel.setOpaque(true);
        confirmLabel.setForeground(Color.secondTitleForeGround);
        confirmLabel.setBackground(Color.greyBackground);
        commitCancelComponent.addNorthComponent(confirmLabel);
        commitCancelComponent.setSelected(true);
        commitCancelComponent.setBorder(BorderFactory.createEmptyBorder(1, 16, 16, 16));
        commitCancelComponent.setOpaque(true);
        commitCancelComponent.setBackground(Color.greyBackground);

        this.setLayout(cardLayout);
        this.add(infoPanel, infoPanel.getClass().getName());
        this.add(infoPanelShort, infoPanelShort.getClass().getName());
        this.add(commitCancelComponent, commitCancelComponent.getClass().getName());
        this.setPreferredSize(new Dimension(640, 250));

        XListenerAdapter adapter = new XListenerAdapter(this, 0) {
            @Override
            protected void show(HierarchyEvent e) {
                EventProxyFactory.addEventListener(ValidateReturnlClothingForm.this);
                super.show(e);
            }

            @Override
            protected void hide(HierarchyEvent e) {
                EventProxyFactory.removeEventListener(ValidateReturnlClothingForm.this);
                super.hide(e);
            }
        };
    }

    @Autowired
    void setSelectReturnTypeContainer(SelectReturnTypeContainer selectReturnTypeContainer) {
        this.selectReturnTypeContainer = selectReturnTypeContainer;
    }

    @Autowired
    void setPositionContainer(ReturnPositionContainer positionContainer) {
        this.positionContainer = positionContainer;
    }

    @Autowired
    void setCisValidation(CisValidation cisValidation) {
        this.cisValidation = cisValidation;
    }

    @Override
    public void startSleep() {
        synchronized (waitValidation) {
            emergencyExit.set(true);
            waitValidation.set(false);
            CashEventSource.getInstance().fullyUnlockEvent();
        }
    }

    @Override
    public void stopSleep() {
    }

    /**
     * Валидация позиции одежды с КиЗ
     *
     * @param returnPurchase
     * @param checkReturnPositions
     * @param fullReturn
     * @return
     */
    public List<PositionEntity> validatePositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions, boolean fullReturn) {

        synchronized (waitValidation) {
            if (waitValidation.get()) {//надо отпустить предыдущий поток - возможно он завис здесь после screen saver`a
                waitValidation.set(false);
                emergencyExit.set(true);
                CashEventSource.getInstance().fullyUnlockEvent();
            }
        }
        try {
            synchronized (processLock) {
                this.fullReturn = fullReturn;
                commitCancelComponent.setCommit(false);
                source =  (PositionClothingEntity) checkReturnPositions.get(0);
                List<PositionEntity> result = new ArrayList<>();
                if (source.getCis() != null) {
                    String previousPanel;
                    if (fullReturn) {
                        infoPanel.fillData(source);
                        infoPanel.showWarning(null);
                        commitCancelComponent.selectCancelButton();
                        cardLayout.show(this, infoPanel.getClass().getName());
                        previousPanel = selectReturnTypeContainer.showLockComponent(this);
                    } else {
                        infoPanelShort.showWarning(null);
                        commitCancelComponent.selectCancelButton();
                        cardLayout.show(this, infoPanelShort.getClass().getName());
                        previousPanel = positionContainer.showLockComponent(this);
                    }

                    synchronized (waitValidation) {
                        waitValidation.set(true);
                        emergencyExit.set(false);
                    }
                    while (waitValidation.get()) {
                        CashEventSource.getInstance().processEventManually();
                    }

                    if (fullReturn) {
                        selectReturnTypeContainer.removeLockComponent(this, previousPanel);
                    } else {
                        positionContainer.removeLockComponent(this, previousPanel);
                    }
                    if (emergencyExit.get()) {
                        return null;
                    }
                    if (target != null) {
                        result.add(target);
                    }
                } else {
                    result.add(source);
                }
                return result;
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            return null;
        }
    }

    private void unlock() {
        synchronized (waitValidation) {
            waitValidation.set(false);
            CashEventSource.getInstance().fullyUnlockEvent();
        }
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (infoPanel.isWarning() || infoPanelShort.isWarning()) {
                infoPanel.showWarning(null);
                infoPanelShort.showWarning(null);
            } else {
                cardLayout.show(this, commitCancelComponent.getClass().getName());
            }
        } else if (cardLayout.getCurrentCardName().equals(commitCancelComponent.getClass().getName())) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                commitCancelComponent.changeCommitSelection();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (commitCancelComponent.isCommit()) {
                    source.setCis(null);
                    target = source;
                    unlock();
                } else {
                    if (fullReturn) {
                        cardLayout.show(ValidateReturnlClothingForm.this, infoPanel.getClass().getName());
                    } else {
                        cardLayout.show(ValidateReturnlClothingForm.this, infoPanelShort.getClass().getName());
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return true;
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (source != null) {
            CisValidationState state = cisValidation.isCanReturn(barcode, source);
            switch (state) {
                case CIS_NO_CORRECT:
                case CIS_IN_CHECK:
                    if (fullReturn) {
                        infoPanel.showWarning(ResBundleGoodsClothing.getString(state.name()));
                    } else {
                        infoPanelShort.showWarning(ResBundleGoodsClothing.getString(state.name()));
                    }
                    break;
                case IS_CAN_RETURN:
                    source.setCis(barcode);
                    target = source;
                    unlock();
                    break;
            }
        }
        return true;
    }

}
