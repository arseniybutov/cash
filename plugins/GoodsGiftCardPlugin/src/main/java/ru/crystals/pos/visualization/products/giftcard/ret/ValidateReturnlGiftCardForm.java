package ru.crystals.pos.visualization.products.giftcard.ret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.ScreenSaverEvents;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionGiftCardEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.events.EventProxyFactory;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponent;
import ru.crystals.pos.visualization.admin.components.CommitCancelComponentType;
import ru.crystals.pos.visualization.admin.components.cashinventory.CashInventoryComponent;
import ru.crystals.pos.visualization.check.ret.ReturnPositionContainer;
import ru.crystals.pos.visualization.check.ret.SelectReturnTypeContainer;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Style;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author dalex
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_GIFT_CARD_ENTITY)
public class ValidateReturnlGiftCardForm extends JPanel implements XListener, ScreenSaverEvents {
    private static final Logger LOG = LoggerFactory.getLogger(CashInventoryComponent.class);
    private final XCardLayout cardLayout = new XCardLayout();
    private final ValidateReturnlGiftCardInfoPanel infoPanel = new ValidateReturnlGiftCardInfoPanel();
    private final ValidateReturnlGiftCardInfoPanelShort infoPanelShort = new ValidateReturnlGiftCardInfoPanelShort();
    private final CommitCancelComponent commitCancelComponent
            = new CommitCancelComponent(CommitCancelComponentType.YES_NO, ResBundleVisualization.getString("OK"), ResBundleVisualization.getString("CANCEL"));
    private final Map<String, PositionGiftCardEntity> resultMap = new HashMap<>();
    private final Map<String, PositionGiftCardEntity> source = new HashMap<>();
    private SelectReturnTypeContainer selectReturnTypeContainer;
    private ReturnPositionContainer positionContainer;
    private boolean fullReturn = false;
    private final AtomicBoolean waitValidation = new AtomicBoolean(false);
    private final AtomicBoolean emergencyExit = new AtomicBoolean(false);
    private final Object processLock = new Object();

   public ValidateReturnlGiftCardForm() {
        this.setLayout(cardLayout);
        this.add(infoPanel, infoPanel.getClass().getName());
        this.add(infoPanelShort, infoPanelShort.getClass().getName());
        this.add(commitCancelComponent, commitCancelComponent.getClass().getName());

        Label confirmLabel = new Label(ResBundleGoodsGiftCard.getString("RETURN_GIFT_CARD_WARNING_SKIP_CARDS"));
        Style.setDialogTitleStyle(confirmLabel);
        confirmLabel.setFont(new XFont(MyriadFont.getItalic(37F), 1.0f));
        confirmLabel.setPreferredSize(new ScaleDimension(620, 190));
        confirmLabel.setAligmentY(ElementFactory.AligmentY.Y_ALIGMENT_CENTER);
        confirmLabel.setAligmentX(ElementFactory.AligmentX.X_ALIGMENT_CENTER);
        confirmLabel.setOpaque(false);
        commitCancelComponent.addNorthComponent(confirmLabel);
        commitCancelComponent.setSelected(true);
        commitCancelComponent.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        this.setPreferredSize(new Dimension(640, 240));
        XListenerAdapter adapter = new XListenerAdapter(this, 0) {
            @Override
            protected void show(HierarchyEvent e) {
                EventProxyFactory.addEventListener(ValidateReturnlGiftCardForm.this);
                super.show(e);
            }

            @Override
            protected void hide(HierarchyEvent e) {
                EventProxyFactory.removeEventListener(ValidateReturnlGiftCardForm.this);
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
     * Валидация подарочных карт
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
                source.clear();
                resultMap.clear();
                for (PositionEntity position : fullReturn ? returnPurchase.getPositions() : checkReturnPositions) {
                    if (position instanceof PositionGiftCardEntity) {
                        PositionGiftCardEntity pgc = (PositionGiftCardEntity) position;
                        if (!pgc.isReturnProcessed()) {
                            pgc.setCardNumberScanned(false);
                        }
                        if (!pgc.isReturnProcessed() && !pgc.isCardNumberScanned()) {
                            pgc.setReturnProcessed(fullReturn);
                            source.put(pgc.getCardNumber(), pgc);
                        }
                    }
                }

                List<PositionEntity> result = new ArrayList<>();
                if (source.size() > 0) {
                    String previousPanel;
                    if (fullReturn) {
                        infoPanel.setCardsCount(resultMap.size(), source.size());
                        infoPanel.showWarning(null);
                        commitCancelComponent.selectCancelButton();
                        cardLayout.show(this, infoPanel.getClass().getName());
                        previousPanel = selectReturnTypeContainer.showLockComponent(this);
                    } else {
                        infoPanelShort.setRequestCardNumber(maskCard(source.values().iterator().next().getCardNumber()));
                        infoPanelShort.showWarning(null);
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

                    result.addAll(resultMap.values());
                    return result;
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
            if (fullReturn) {
                cardLayout.show(this, commitCancelComponent.getClass().getName());
            } else {
                unlock();
            }
        } else if (cardLayout.getCurrentCardName().equals(commitCancelComponent.getClass().getName())) {
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                commitCancelComponent.changeCommitSelection();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (commitCancelComponent.isCommit()) {
                    unlock();
                } else {
                    if (fullReturn) {
                        cardLayout.show(ValidateReturnlGiftCardForm.this, infoPanel.getClass().getName());
                    } else {
                        cardLayout.show(ValidateReturnlGiftCardForm.this, infoPanelShort.getClass().getName());
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        String number = Factory.getTechProcessImpl().getCards().parseCardNumber(track1, track2, track3, track4, PresentCards.MODULE_NAME);
        return barcodeScanned(number);
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        PositionGiftCardEntity cardPosition = resultMap.get(barcode);
        if (cardPosition == null) {
            cardPosition = source.get(barcode);
            if (cardPosition != null) {
                cardPosition.setCardNumberScanned(true);
                resultMap.put(barcode, cardPosition);
                int expectedCount = source.size() - resultMap.size();
                if (expectedCount == 0) {
                    unlock();
                    return true;
                }
                if (fullReturn) {
                    infoPanel.showWarning(null);
                    infoPanel.setCardsCount(resultMap.size(), source.size());
                }
            } else {
                if (fullReturn) {
                    infoPanel.showWarning(String.format(ResBundleGoodsGiftCard.getString("CARD_NOT_FOUND_IN_CHECK"), maskCard(barcode)));
                } else {
                    infoPanelShort.showWarning(String.format(ResBundleGoodsGiftCard.getString("WRONG_CARD"), maskCard(barcode)));
                }
            }
        } else {
            if (fullReturn) {
                infoPanel.showWarning(ResBundleGoodsGiftCard.getString("CARD_ALREADY_SCANNED"));
            }
        }
        return true;
    }

    private String maskCard(String barcode) {
        String resultCode = barcode;
        if (barcode.length() > 4) {
            resultCode = barcode.substring(barcode.length() - 4, barcode.length());
        }
        return "*" + resultCode;
    }
}
