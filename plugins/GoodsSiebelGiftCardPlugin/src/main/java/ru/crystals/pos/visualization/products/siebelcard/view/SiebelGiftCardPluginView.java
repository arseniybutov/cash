package ru.crystals.pos.visualization.products.siebelcard.view;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.cards.siebel.exception.SiebelServiceException;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.products.siebelcard.ResBundleGoodsSiebelGiftCard;
import ru.crystals.pos.visualization.products.siebelcard.controller.SiebelGiftCardPluginController;
import ru.crystals.pos.visualization.products.siebelcard.form.SiebelGiftCardFixedAmountForm;
import ru.crystals.pos.visualization.products.siebelcard.model.SiebelGiftCardState;

import java.awt.event.KeyEvent;

/**
 * @author s.pavlikhin
 */
public class SiebelGiftCardPluginView extends CommonAbstractView<SiebelGiftCardPluginController> {
    private static final Logger log = LoggerFactory.getLogger(SiebelGiftCardPluginView.class);
    private final CommonSpinnerForm spinnerForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private final SiebelGiftCardFixedAmountForm fixedAmountForm;

    public SiebelGiftCardPluginView() {
        SiebelGiftCardPluginView.this.setName("ru.crystals.pos.visualization.products.siebelcard.view.SiebelGiftCardPluginView");
        this.spinnerForm = new CommonSpinnerForm(this, "");
        this.deletePositionForm = new CommonDeletePositionConfirmForm(this);
        this.fixedAmountForm = new SiebelGiftCardFixedAmountForm(this);

        this.addPanel(spinnerForm);
        this.addPanel(deletePositionForm);
        this.addPanel(fixedAmountForm);
    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD_CURRENT:
            case ADD:
                if (getController().getModel().getInternalState() == SiebelGiftCardState.ADD_GIFT_CARD) {
                    if (getProduct().getBarCode() == null) {
                        log.error("Product barcode is null");
                        showErrorMessage(ResBundleGoodsSiebelGiftCard.getString("ERROR_GOOD_TYPE"), CommonMessageForm.ExitState.TO_EXIT);
                        return;
                    }
                    findAndAddGiftCardInfo(getProduct().getBarCode().getBarCode(), CommonMessageForm.ExitState.TO_EXIT);
                } else if (getController().getModel().getInternalState() == SiebelGiftCardState.FIXED_RATE_GIFT_CARD) {
                    fixedAmountForm.setAmount(getController().getModel().getCard().getCardRate());
                    setCurrentForm(fixedAmountForm);
                } else if (getController().getModel().getInternalState() == SiebelGiftCardState.NOT_FIXED_RATE_GIFT_CARD) {
                    // Необходимо будет добавить поддержку ПК с нефикс номиналом (SR-4005)
                    log.warn("Gift card with non-fixed rate is not supported currently.");
                    showErrorMessage(ResBundleGoodsSiebelGiftCard.getString("ERROR_GOOD_TYPE"), CommonMessageForm.ExitState.TO_EXIT);
                }
                break;
            case EDIT_OR_DELETE:
            case DELETE:
            case QUICK_DELETE:
                setCurrentForm(deletePositionForm);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyPressed(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
            return true;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (currentForm == fixedAmountForm) {
                if (fixedAmountForm.isYes()) {
                    saleGiftCard();
                } else {
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return true;
            } else if (isCurrentForm(deletePositionForm)) {
                if (deletePositionForm.deleteConfirmed()) {
                    deleteGiftCard();
                } else {
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return true;
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (currentForm == spinnerForm) {
                return true;
            } else if (currentForm == messageForm) {
                if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                    getController().getAdapter().dispatchCloseEvent(false);
                } else {
                    if (getController().getModel().getInternalState() == SiebelGiftCardState.FIXED_RATE_GIFT_CARD) {
                        setCurrentForm(fixedAmountForm);
                    } else {
                        getController().getAdapter().dispatchCloseEvent(false);
                    }
                }
                return true;
            }
            return false;
        }

        return false;
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        return true;
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return true;
    }

    @Override
    public boolean checkDataBeforeAdd() {
        return true;
    }

    @Override
    public long getCurrentPositionCount() {
        return BigDecimalConverter.getQuantityMultiplier();
    }

    private void findAndAddGiftCardInfo(String cardNumber, CommonMessageForm.ExitState exitState) {
        showSpinnerForm(ResBundleGoodsSiebelGiftCard.getString("GETTING_CARD_INFORMATION"));

        // Проверим карту. Если не подходит, то вылетит ошибка
        try {
            getController().findAndAddGiftCardInfo(cardNumber);
        } catch (SiebelServiceException e) {
            showErrorMessage(ResBundleGoodsSiebelGiftCard.getString("NO_CONNECTION"), e.getMessage(), exitState);
        } catch (PositionAddingException e) {
            showErrorMessage(e.getMessage(), exitState);
        }
    }

    /**
     * Продажа подарочной карты.
     */
    private void saleGiftCard() {
        showSpinnerForm(ResBundleGoodsSiebelGiftCard.getString("CARD_ACTIVATION"));
        try {
            String cashierMessage = getController().saleGiftCard();
            if (StringUtils.isBlank(cashierMessage)) {
                return;
            }
            showMessageForm(cashierMessage, CommonMessageForm.ExitState.TO_EXIT);
        } catch (SiebelServiceException e) {
            showErrorMessage(e.getMessage(), CommonMessageForm.ExitState.TO_LAST);
        }
    }

    /**
     * Удаление подарочной карты из чека.
     */
    private void deleteGiftCard() {
        // Удалим позицию из чека. Запрос на отмену продажи ПК в Siebel реализован в SiebelExternalProcessing#deletePosition
        getController().cashDeletePosition(getController().getModel().getPosition());
    }

    private void showErrorMessage(String message, CommonMessageForm.ExitState exitState) {
        showErrorMessage(message, null, exitState);
    }

    private void showErrorMessage(String message, String cashierMessage, CommonMessageForm.ExitState exitState) {
        String errorMessage = message;
        if (StringUtils.isNotBlank(cashierMessage)) {
            errorMessage += "\n" + cashierMessage;
        }
        showMessageForm(errorMessage, exitState);
    }

    private void showMessageForm(String message, CommonMessageForm.ExitState exitState) {
        messageForm.setMessage(message);
        messageForm.setExitState(exitState);
        setCurrentForm(messageForm);
    }

    private void showSpinnerForm(String msg) {
        spinnerForm.setTextMessage(msg);
        setCurrentForm(spinnerForm);
    }

    private ProductEntity getProduct() {
        return getController().getModel().getProduct();
    }

}
