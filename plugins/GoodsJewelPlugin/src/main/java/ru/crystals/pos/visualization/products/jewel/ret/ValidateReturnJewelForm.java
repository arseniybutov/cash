package ru.crystals.pos.visualization.products.jewel.ret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.ScreenSaverEvents;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.catalog.service.ResBundleGoods;
import ru.crystals.pos.catalog.utils.ValidateExciseException;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.events.EventProxyFactory;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.check.ret.ReturnPositionContainer;
import ru.crystals.pos.visualization.check.ret.SelectReturnTypeContainer;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonScanExcisePanel;
import ru.crystals.pos.visualization.products.jewel.controller.JewelPluginController;
import ru.crystals.pos.visualizationtouch.components.XCardLayout;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_JEWEL_ENTITY)
public class ValidateReturnJewelForm extends JPanel implements ScreenSaverEvents, XListener {

    private static final Logger LOG = LoggerFactory.getLogger(ValidateReturnJewelForm.class);

    /**
     * Основной layout на котором будут отображены все остальные view
     */
    private final XCardLayout cardLayout = new XCardLayout();

    /**
     * Панель отображающаяся при попытке выхода из возврата
     */
    private final CommitCancelJewelComponent commitCancelJewelComponent = new CommitCancelJewelComponent();

    /**
     * Панель запрашивающая считывание акцизной марки при частичном возврате
     */
    private final CommonScanExcisePanel scanExcisePanelPartly = new CommonScanExcisePanel();

    /**
     * Панель запрашивающая считывание акцизных марок при полном возврате
     */
    private final ValidateReturnJewelInfoPanel scanExcisePanelFull = new ValidateReturnJewelInfoPanel();

    /**
     * Текущая позиция возврат которой производим в данный момент
     */
    private PositionEntity currentPosition = null;

    /**
     * Флаг указывающий на то, что это полный возврат
     */
    private boolean fullReturn = false;

    /**
     * Всякие штуки для синхронизации позволяющие стрельнуть себе в ногу
     */
    private final AtomicBoolean waitValidation = new AtomicBoolean(false);
    private final AtomicBoolean emergencyExit = new AtomicBoolean(false);
    private final Object processLock = new Object();

    /**
     * Список позиций прошедших валидацию
     */
    private final List<PositionEntity> resultList = new ArrayList<>();

    /**
     * Контроллер
     */
    private final JewelPluginController controller;

    /**
     * Список позиций уже прошедших валидацию
     */
    private List<PositionEntity> validatedPositions;

    private SelectReturnTypeContainer selectReturnTypeContainer;
    private ReturnPositionContainer positionContainer;

    @Autowired
    public ValidateReturnJewelForm(JewelPluginController controller) {
        this.controller = controller;
        this.setLayout(cardLayout);
        this.add(commitCancelJewelComponent, commitCancelJewelComponent.getClass().getName());
        this.add(scanExcisePanelPartly, scanExcisePanelPartly.getClass().getName());
        this.add(scanExcisePanelFull, scanExcisePanelFull.getClass().getName());
        this.setPreferredSize(new Dimension(640, 250));

        new XListenerAdapter(this, 0) {
            @Override
            protected void show(HierarchyEvent e) {
                EventProxyFactory.addEventListener(ValidateReturnJewelForm.this);
                super.show(e);
            }

            @Override
            protected void hide(HierarchyEvent e) {
                EventProxyFactory.removeEventListener(ValidateReturnJewelForm.this);
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

    public List<PositionEntity> validatePositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions,
                                                  boolean fullReturn, List<PositionEntity> validatedPositions) {
        synchronized (waitValidation) {
            //надо отпустить предыдущий поток - возможно он завис здесь после screen saver`a
            if (waitValidation.get()) {
                waitValidation.set(false);
                emergencyExit.set(true);
                CashEventSource.getInstance().fullyUnlockEvent();
            }
        }

        try {
            synchronized (processLock) {
                this.fullReturn = fullReturn;
                this.validatedPositions = validatedPositions;
                commitCancelJewelComponent.setCommit(false);
                currentPosition = checkReturnPositions.get(0);
                resultList.clear();
                if (currentPosition.getExciseToken() != null) {
                    String previousPanel;
                    if (fullReturn) {
                        commitCancelJewelComponent.selectCancelButton();
                        scanExcisePanelFull.fillData(currentPosition);
                        cardLayout.show(this, scanExcisePanelFull.getClass().getName());
                        previousPanel = selectReturnTypeContainer.showLockComponent(this);
                    } else {
                        commitCancelJewelComponent.selectCancelButton();
                        scanExcisePanelPartly.restoreDefaultState();
                        cardLayout.show(this, scanExcisePanelPartly.getClass().getName());
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
                        return Collections.emptyList();
                    }
                } else {
                    resultList.add(currentPosition);
                }
                return resultList;
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            return Collections.emptyList();
        }
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
        //  nothing
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (cardLayout.isCurrentCard(scanExcisePanelPartly.getClass().getName()) || cardLayout.isCurrentCard(scanExcisePanelFull.getClass().getName())) {
            try {
                final MarkData markData = controller.parseAndValidateOnAdd(barcode, currentPosition, true, validatedPositions);

                if (!currentPosition.getExciseToken().equals(markData.getRawMark())) {
                    throw new ValidateExciseException(ResBundleGoods.getString("MARK_ANOTHER_PRODUCT"));
                }

                currentPosition.fillByMarkData(markData);
                resultList.add(currentPosition);
                unlock();
            } catch (ValidateExciseException e) {
                showError(e.getMessage());
            }
        }
        return true;
    }

    private void showError(String message) {
        if (fullReturn) {
            scanExcisePanelFull.showWarning(message);
        } else {
            scanExcisePanelPartly.setWarning(true);
            scanExcisePanelPartly.setMessage(message);
        }
        Factory.getTechProcessImpl().startCriticalErrorBeeping(message);
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            onEscPressed();
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
            onArrowButtonsPressed();
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            onEnterPressed();
        }

        return true;
    }

    /**
     * Обработка нажатия клавиши Escape
     */
    private void onEscPressed() {
        if (scanExcisePanelPartly.isWarning() || scanExcisePanelFull.isWarning()) {
            scanExcisePanelPartly.restoreDefaultState();
            scanExcisePanelFull.showWarning(null);
            Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        } else {
            if (!fullReturn && waitValidation.get()) {
                unlock();
                return;
            }

            cardLayout.show(this, commitCancelJewelComponent.getClass().getName());
        }
    }

    /**
     * Обработка нажатий "стрелочных" клавиш
     */
    private void onArrowButtonsPressed() {
        if (cardLayout.getCurrentCardName().equals(commitCancelJewelComponent.getClass().getName())) {
            commitCancelJewelComponent.changeCommitSelection();
        }
    }

    /**
     * Обработка нажатия клавиши Enter
     */
    private void onEnterPressed() {
        if (cardLayout.getCurrentCardName().equals(commitCancelJewelComponent.getClass().getName())) {
            if (commitCancelJewelComponent.isCommit()) {
                unlock();
            } else {
                if (fullReturn) {
                    cardLayout.show(ValidateReturnJewelForm.this, scanExcisePanelFull.getClass().getName());
                } else {
                    cardLayout.show(ValidateReturnJewelForm.this, scanExcisePanelPartly.getClass().getName());
                }
            }
        }
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return true;
    }

    private void unlock() {
        synchronized (waitValidation) {
            waitValidation.set(false);
            CashEventSource.getInstance().fullyUnlockEvent();
        }
    }
}
