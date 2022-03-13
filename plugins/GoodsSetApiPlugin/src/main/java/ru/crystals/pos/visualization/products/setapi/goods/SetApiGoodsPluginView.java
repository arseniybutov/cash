package ru.crystals.pos.visualization.products.setapi.goods;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.api.commons.RemoveFromSaleRequestEntity;
import ru.crystals.api.loader.payments.PluginDescription;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.api.plugin.GoodsPlugin;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.spi.ui.forms.UIFormsAdapter;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.UIContainer;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.forms.CheckAgePanel;
import ru.crystals.pos.visualization.products.forms.CheckAgePanelListener;
import ru.crystals.pos.visualization.products.setapi.goods.i18n.ResBundleSetApiGoods;

import java.awt.event.KeyEvent;
import java.util.Date;

public class SetApiGoodsPluginView extends CommonAbstractView<SetApiGoodsPluginController> {
    private static final Logger log = LoggerFactory.getLogger(SetApiGoodsPluginView.class);

    /**
     * Панель просмотра информации о товаре.
     */
    private final SetApiGoodsPluginViewForm viewForm;

    /**
     * Панель проверки возраста
     */
    private final CheckAgePanel checkAgePanel;

    /**
     * Форма быстрого удаления позиции
     */
    private final CommonDeletePositionConfirmForm<ProductEntity, PositionEntity> deletePositionForm;

    /**
     * Форма удаления позиции из чека
     */
    private final SetApiGoodsEditOrDeleteForm deleteForm;

    /**
     * Контейнер для UI плагина.
     */
    private final UIContainer setApiGoodPluginGuiPanel = new UIContainer();

    protected PluginDescription<GoodsPlugin> goodsPluginDescriptor;
    protected UIFormsAdapter uiFormsAdapter;

    protected InternalCashPoolExecutor executor;
    protected PluginDialogsRoutine pluginDialogsRoutine;

    public SetApiGoodsPluginView(Properties properties) {
        viewForm = new SetApiGoodsPluginViewForm(this);
        pluginDialogsRoutine = new PluginDialogsRoutine(Factory.getTechProcessImpl());

        checkAgePanel = new CheckAgePanel(properties, new CheckAgePanelListener() {
            @Override
            public void esc() {
                // возраст не подтвержден
                getController().ageNotConfirmed();
                getController().onAgeCheckingCompleted();
            }

            @Override
            public void select(boolean confirm, int age, Date birthDate) {
                if (confirm) {
                    // подтвержден
                    getController().ageConfirmed();
                } else {
                    // возраст не подтвержден - реагируем так же. как если бы нажали ESC
                    esc();
                }
                getController().onAgeCheckingCompleted();
            }
        });

        deletePositionForm = new CommonDeletePositionConfirmForm<>(this);
        deleteForm = new SetApiGoodsEditOrDeleteForm(this);

        this.addPanel(checkAgePanel);
        this.addPanel(viewForm);
        this.addPanel(setApiGoodPluginGuiPanel.getVisualPanel());
        this.addPanel(deletePositionForm);
        this.addPanel(deleteForm);
        uiFormsAdapter = new UIFormsAdapter(setApiGoodPluginGuiPanel.getVisualPanel());
    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD:
                setCurrentForm(null);
                modelChangedSaleScenario(getController().getModel());
                break;
            case ADD_CURRENT:
                getController().beepError("Cannot add plugin product by SUBTOTAL");
                getController().getModel().setState(ProductContainer.ProductState.ADD);
                break;
            case VIEW:
                // Просмотр информации о товаре.
                setCurrentForm(viewForm, false);
                break;
            case DELETE:
            case QUICK_DELETE:
                // быстрое удаление позиции чека
                // сначала проверим привилегию:
                if (getController().hasRightToDeletePosition(getController().getModel().getPosition())) {
                    setCurrentForm(deletePositionForm, false);
                } else {
                    // пользователь не имеет права на удаление позиции
                    getController().beepError("The user has no right to delete position!");
                    String msg = ResBundleSetApiGoods.getString("set.api.goods.the.user.has.no.right.to.delete.position");
                    showMessageForm(msg, CommonMessageForm.ExitState.TO_LAST);
                }
                break;
            case QUICK_EDIT:
            case EDIT:
                getController().beepError("The position is not editable");
                showMessageForm(ResBundleSetApiGoods.getString("set.api.goods.position.is.not.editable"), CommonMessageForm.ExitState.TO_LAST);
                break;
            case EDIT_OR_DELETE:
                // форма удаления позиции (редактировать плагинные позиции пока (2018-05-24) не предполагается)
                if (getController().hasRightToDeletePosition(getController().getModel().getPosition())) {
                    setCurrentForm(deleteForm, false);
                } else {
                    // пользователь не имеет права на удаление позиции
                    getController().beepError("The user has no right to delete position!");
                    String msg = ResBundleSetApiGoods.getString("set.api.goods.the.user.has.no.right.to.delete.position");
                    showMessageForm(msg, CommonMessageForm.ExitState.TO_LAST);
                }
                break;
            case REFUND:
                setApiGoodPluginGuiPanel.getVisualPanel().setVisible(false);
                getController().finishRefund(true);
                break;
            case EXPENSE:
                setCurrentForm(null);
                getController().finishRefund(true);
                break;
            default:
                // на остальные события пока не реагируем
                log.warn("model state [{}] handler is not implemented yet!", getController().getModel().getState());
                break;
        } // switch

    }

    @Override
    public boolean checkDataBeforeAdd() {
        return true;
    }

    @Override
    public long getCurrentPositionCount() {
        return 0;
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (processEnterOnQuickDeleteForm(e)) {
            return true;
        }
        return processEnterDeleteForm(e);
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        return setApiGoodPluginGuiPanel.barcodeScanned(barcode);
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return setApiGoodPluginGuiPanel.eventMSR(track1, track2, track3, track4);
    }

    /**
     * Вернет {@code true}, если текущая форма - это форма быстрого удаления позиции из чека и указанное событие - это нажатие клавиши "ВВОД".
     *
     * @param e событие нажатия клавиши
     * @return {@code false}, если аргумент невалиден, либо это не "ВВОД" на форме быстрого удаления позиции из чека
     */
    private boolean processEnterOnQuickDeleteForm(XKeyEvent e) {
        if (e == null || e.getKeyCode() != KeyEvent.VK_ENTER) {
            return false;
        }

        if (currentForm != deletePositionForm) {
            return false;
        }

        // таки это ENTER на форме быстрого удаления позиции
        log.info("processing <ENTER> on the quick delete form...");
        processQuickDelete();

        return true;
    }

    /**
     * Вернет {@code true}, если текущая форма - это удаления (вызванная через меню) позиции из чека и указанное событие - это нажатие клавиши "ВВОД".
     *
     * @param e событие нажатия клавиши
     * @return {@code false}, если аргумент невалиден, либо это не "ВВОД" на форме удаления позиции из чека
     */
    private boolean processEnterDeleteForm(XKeyEvent e) {
        if (e == null || e.getKeyCode() != KeyEvent.VK_ENTER) {
            return false;
        }

        if (currentForm != deleteForm) {
            return false;
        }

        // таки это ENTER на форме удаления позиции
        log.info("processing <ENTER> on the delete form...");
        processDelete();

        return true;
    }

    /**
     * Завершает техпроцесс удаления позиции из чека.
     *
     * @param success флаг-признак: завершить процесс по позитивному сценарию (true) (с реальным удалением позиции),
     *                или по негативному (false) (позиция в чеке останется)
     */
    private void finishDelete(boolean success) {
        log.trace("entering finishDelete(boolean). The argument is: {}", success);

        if (success) {
            getController().returnPosition(getController().getModel().getPosition());
        } else {
            getController().getAdapter().dispatchCloseEvent(false);
        }

        log.trace("leaving finishDelete(boolean)");
    }

    /**
     * Обработка событий на форме удаления позиции из чека
     */
    private void processDelete() {
        // Удалим позицию из чека
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        if (purchaseEntity == null) {
            log.error("processDelete: failed to get the current receipt!");
            finishQuickDelete(false);
            return;
        }

        PositionEntity position = getController().getModel().getPosition();
        if (position == null) {
            log.error("processDelete: failed to get position to remove");
            finishQuickDelete(false);
            return;
        }

        ProductEntity product = position.getProduct();
        if (product == null) {
            log.error("processDelete: failed to infer product to remove");
            finishQuickDelete(false);
            return;
        }

        // сначала диалоги с плагином
        setCurrentPanel(setApiGoodPluginGuiPanel.getVisualPanel());
        goodsPluginDescriptor = getController().getPluginAdapter().getPluginDescriptor(GoodsPlugin.class, product.getDiscriminator());
        goodsPluginDescriptor.setUiForms(uiFormsAdapter);
        try {
            goodsPluginDescriptor.getPlugin().removeFromSale(new RemoveFromSaleRequestEntity(position, purchaseEntity, false, result -> {
                getController().saveExtendedReceiptAttributes(result.getExtendedReceiptAttributes());
                if (result.isRemoveAllowed()) {
                    log.warn("plugin allowed to remove position [{}]", position);
                    finishDelete(true);
                } else {
                    log.warn("plugin forbids to remove position [{}]", position);
                    finishDelete(false);
                }
            }));
        } catch (Exception ex) {
            log.error("An error has occurred during plugin invocation", ex);
            Factory.getTechProcessImpl().error(ResBundleSetApiGoods.getString("set.api.goods.add.position.error"));
            finishDelete(false);
        }
    }

    /**
     * Завершает техпроцесс быстрого удаления позиции из чека.
     *
     * @param success флаг-признак: завершить процесс по позитивному сценарию (true) (с реальным удалением позиции),
     *                или по негативному (false) (позиция в чеке останется)
     */
    private void finishQuickDelete(boolean success) {
        log.trace("entering finishQuickDelete(boolean). The argument is: {}", success);

        if (success) {
            getController().cashDeletePosition(getController().getModel().getPosition());
        } else {
            getController().getAdapter().dispatchCloseEvent(false);
        }

        log.trace("leaving finishQuickDelete(boolean)");
    }

    /**
     * Обработка событий на форме быстрого удаления позиции из чека
     */
    private void processQuickDelete() {
        if (!deletePositionForm.deleteConfirmed()) {
            // нажали "отмена" на диалоге "удалить позицию?"
            log.trace("<ESC>/NO was selected on the \"delete position\" dialog");
            finishQuickDelete(false);
            return;
        }
        // Выбрали "ДА" на диалоге

        // Удалим позицию из чека
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        if (purchaseEntity == null) {
            log.error("processQuickDelete: failed to get the current receipt!");
            finishQuickDelete(false);
            return;
        }

        PositionEntity position = getController().getModel().getPosition();
        if (position == null) {
            log.error("processQuickDelete: failed to get position to remove");
            finishQuickDelete(false);
            return;
        }

        ProductEntity product = position.getProduct();
        if (product == null) {
            log.error("processQuickDelete: failed to infer product to remove");
            finishQuickDelete(false);
            return;
        }

        // сначала диалоги с плагином
        setCurrentPanel(setApiGoodPluginGuiPanel.getVisualPanel());
        goodsPluginDescriptor = getController().getPluginAdapter().getPluginDescriptor(GoodsPlugin.class, product.getDiscriminator());
        goodsPluginDescriptor.setUiForms(uiFormsAdapter);
        try {
            goodsPluginDescriptor.getPlugin().removeFromSale(new RemoveFromSaleRequestEntity(position, purchaseEntity, true, result -> {
                getController().saveExtendedReceiptAttributes(result.getExtendedReceiptAttributes());
                if (result.isRemoveAllowed()) {
                    log.warn("plugin allowed to remove position [{}]", position);
                    finishQuickDelete(true);
                } else {
                    log.warn("plugin forbids to remove position [{}]", position);
                    finishQuickDelete(false);
                }
            }));
        } catch (Exception ex) {
            log.error("An error has occurred during plugin invocation", ex);
            Factory.getTechProcessImpl().error(ResBundleSetApiGoods.getString("set.api.goods.add.position.error"));
            finishQuickDelete(false);
        }
    }

    /**
     * Событие изменения модели в сценарии продажи (здесь: добавления позиции в чек)
     *
     * @param model
     */
    private void modelChangedSaleScenario(SetApiGoodsPluginModel model) {
        log.trace("entering modelChangedSaleScenario(SetApiGoodsPluginModel). The argument is: {}", model);

        SetApiGoodsPluginState addState = model.getAddState();
        switch (addState) {
            case START:
                // запускаем процесс
                getController().startSaleProcess();
                break;
            case CHECK_AGE:
                // надо показать панель ввода возраста покупателя
                checkAgePanel.reset();
                checkAgePanel.setMinAge(model.getMinAge());
                setCurrentPanel(checkAgePanel);
                getController().onAgeChecking();
                break;
            case PLUGIN_DIALOGS:
                pluginDialogsRoutine.perform(this, model, getController());
                break;
            case ERROR:
                // завершение процесса по негативному сценарию
                Factory.getInstance().showMessage(getController().getModel().getErrorMessage());
                getController().finishSale(false);
                break;
            case FINISH:
                // успешное завершение процесса
                getController().finishSale(true);
                break;
            default:
                // ничего.
                log.warn("handling of model switching to state [{}] is not implemented [yet]!", addState);
                break;
        } // switch

        log.trace("leaving modelChangedSaleScenario(SetApiGoodsPluginModel)");
    }

    void setupPluginViewPanel(String pluginId) {
        setCurrentPanel(setApiGoodPluginGuiPanel.getVisualPanel());
        goodsPluginDescriptor = getController().getPluginAdapter().getPluginDescriptor(GoodsPlugin.class, pluginId);
        goodsPluginDescriptor.setUiForms(uiFormsAdapter);
    }

    private void showMessageForm(String msg, CommonMessageForm.ExitState exitState) {
        messageForm.setMessage(msg);
        messageForm.setExitState(exitState);
        setCurrentForm(messageForm);
    }

    /**
     * Вернет {@code true}, если текущий чек - это чек возврата (или если чека еще нет, но находимся
     * в окне редактирования чека возврата - тогда наша позиция будет первой в возвратном чеке).
     *
     * @return
     */
    private boolean isReturnPurchase() {
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        if (purchaseEntity != null) {
            return purchaseEntity.isReturn();
        } else {
            return Factory.getInstance().getMainWindow().getCheckContainer().getState() == CheckState.RETURN_CHECK;
        }
    }
}
