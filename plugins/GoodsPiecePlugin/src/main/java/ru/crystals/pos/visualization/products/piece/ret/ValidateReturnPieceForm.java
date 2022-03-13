package ru.crystals.pos.visualization.products.piece.ret;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.ScreenSaverEvents;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.mark.MarkData;
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
import ru.crystals.pos.visualization.products.piece.controller.PiecePluginController;
import ru.crystals.pos.visualizationtouch.components.XCardLayout;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.event.HierarchyEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_PIECE_ENTITY)
public class ValidateReturnPieceForm  extends JPanel implements ScreenSaverEvents, XListener {
    public static final Logger log = LoggerFactory.getLogger(ValidateReturnPieceForm.class);

    /**
     * Основной layout на котором будут отображены все остальные view
     */
    private final XCardLayout cardLayout = new XCardLayout();

    /**
     * Панель отображающаяся при попытке выхода из возврата
     */
    private final CommitCancelPieceComponent commitCancelPieceComponent = new CommitCancelPieceComponent();

    /**
     * Панель запрашивающая считывание акцизной марки при частичном возврате
     */
    private final CommonScanExcisePanel scanExcisePanelPartly = new CommonScanExcisePanel();

    /**
     * Панель запрашивающая считывание акцизных марок при полном возврате
     */
    private final ValidateReturnPieceInfoPanel scanExcisePanelFull = new ValidateReturnPieceInfoPanel();

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
    private final PiecePluginController controller;

    /**
     * Список позиций уже прошедших валидацию
     */
    private List<PositionEntity> validatedPositions;

    private SelectReturnTypeContainer selectReturnTypeContainer;
    private ReturnPositionContainer positionContainer;

    @Autowired
    public ValidateReturnPieceForm(PiecePluginController controller) {
        this.controller = controller;
        this.setLayout(cardLayout);
        this.add(commitCancelPieceComponent, commitCancelPieceComponent.getClass().getName());
        this.add(scanExcisePanelPartly, scanExcisePanelPartly.getClass().getName());
        this.add(scanExcisePanelFull, scanExcisePanelFull.getClass().getName());
        this.setPreferredSize(new Dimension(640, 250));

        new XListenerAdapter(this, 0) {
            @Override
            protected void show(HierarchyEvent e) {
                EventProxyFactory.addEventListener(ValidateReturnPieceForm.this);
                super.show(e);
            }

            @Override
            protected void hide(HierarchyEvent e) {
                EventProxyFactory.removeEventListener(ValidateReturnPieceForm.this);
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
                commitCancelPieceComponent.setCommit(false);
                currentPosition = checkReturnPositions.get(0);
                resultList.clear();
                if (currentPosition.getExciseToken() != null) {
                    String previousPanel;
                    if (fullReturn) {
                        commitCancelPieceComponent.selectCancelButton();
                        scanExcisePanelFull.fillData(currentPosition);
                        cardLayout.show(this, scanExcisePanelFull.getClass().getName());
                        previousPanel = selectReturnTypeContainer.showLockComponent(this);
                    } else {
                        commitCancelPieceComponent.selectCancelButton();
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
                        return null;
                    }
                } else {
                    resultList.add(currentPosition);
                }
                return resultList;
            }
        } catch (Exception ex) {
            log.error("", ex);
            return null;
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
            cardLayout.show(this, commitCancelPieceComponent.getClass().getName());
        }
    }

    /**
     * Обработка нажатий "стрелочных" клавиш
     */
    private void onArrowButtonsPressed() {
        if (cardLayout.getCurrentCardName().equals(commitCancelPieceComponent.getClass().getName())) {
            commitCancelPieceComponent.changeCommitSelection();
        }
    }

    /**
     * Обработка нажатия клавиши Enter
     */
    private void onEnterPressed() {
        if (cardLayout.getCurrentCardName().equals(commitCancelPieceComponent.getClass().getName())) {
            if (commitCancelPieceComponent.isCommit()) {
                unlock();
            } else {
                if (fullReturn) {
                    cardLayout.show(ValidateReturnPieceForm.this, scanExcisePanelFull.getClass().getName());
                } else {
                    cardLayout.show(ValidateReturnPieceForm.this, scanExcisePanelPartly.getClass().getName());
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
