package ru.crystals.pos.visualization.products.giftcard.product.view;

import ru.crystals.pos.cards.PresentCards;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.check.PositionGiftCardEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.exception.NoPermissionException;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.product.GiftCardEditOrDeleteForm;
import ru.crystals.pos.visualization.products.giftcard.product.GiftCardEnterNumberForm;
import ru.crystals.pos.visualization.products.giftcard.product.GiftCardFixedAmountForm;
import ru.crystals.pos.visualization.products.giftcard.product.GiftCardNonFixedAmountForm;
import ru.crystals.pos.visualization.products.giftcard.product.GiftCardViewForm;
import ru.crystals.pos.visualization.products.giftcard.product.controller.GiftCardPluginController;
import ru.crystals.pos.visualization.products.giftcard.product.model.GiftCardPluginState;

import java.awt.event.KeyEvent;

public class GiftCardPluginView extends CommonAbstractView<GiftCardPluginController> {
    private final GiftCardEnterNumberForm enterNumberForm;
    private final GiftCardEditOrDeleteForm editOrDeleteForm;
    private final GiftCardViewForm viewForm;
    private final CommonSpinnerForm spinnerForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private final GiftCardFixedAmountForm fixedAmountForm;
    private final GiftCardNonFixedAmountForm nonFixedAmountForm;


    public GiftCardPluginView() {
        GiftCardPluginView.this.setName("ru.crystals.pos.visualization.products.giftcard.view.GiftCardPluginView");
        this.enterNumberForm = new GiftCardEnterNumberForm(this);
        this.fixedAmountForm = new GiftCardFixedAmountForm(this);
        this.nonFixedAmountForm = new GiftCardNonFixedAmountForm(this);
        this.deletePositionForm = new CommonDeletePositionConfirmForm(this);

        this.editOrDeleteForm = new GiftCardEditOrDeleteForm(this);
        this.viewForm = new GiftCardViewForm(this);

        this.spinnerForm = new CommonSpinnerForm(this, "");

        this.addPanel(enterNumberForm);
        this.addPanel(fixedAmountForm);
        this.addPanel(nonFixedAmountForm);
        this.addPanel(editOrDeleteForm);
        this.addPanel(spinnerForm);
        this.addPanel(viewForm);
        this.addPanel(deletePositionForm);
    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD_CURRENT:
            case ADD:
                if (GiftCardPluginState.ENTER_CARD_NUMBER == getController().getModel().getInternalState()) {
                    String msg = getController().isPossibleToAddPosition();
                    if (msg == null) {
                        PositionGiftCardEntity position = new PositionGiftCardEntity();
                        getController().fillDefaultPosition(enterNumberForm.getQuantity(), enterNumberForm.getPrice(), getController().getModel().getProduct(), position);
                        getController().getAdapter().dispatchEventAddGoods(position);
                        setCurrentForm(enterNumberForm);
                    } else {
                        showMessageForm(msg, CommonMessageForm.ExitState.TO_EXIT);
                    }
                } else if (GiftCardPluginState.FIX_GIFT_CARD == getController().getModel().getInternalState()) {
                    fixedAmountForm.setAmount(getController().getModel().getPresentCard().getAmount());
                    setCurrentForm(fixedAmountForm);
                } else if (GiftCardPluginState.NOT_FIX_GIFT_CARD == (getController().getModel().getInternalState())) {
                    nonFixedAmountForm.setCardInfo(getController().getModel().getPresentCard());
                    setCurrentForm(nonFixedAmountForm);
                }
                break;
            case EDIT_OR_DELETE:
                setCurrentForm(editOrDeleteForm);
                break;
            case VIEW:
                setCurrentForm(viewForm, false);
                break;
            case DELETE:
            case QUICK_DELETE:
                setCurrentForm(deletePositionForm, false);
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
            if (currentForm == editOrDeleteForm) {
                getController().returnPosition(getController().getModel().getPosition());
                return true;
            }

            if (currentForm == enterNumberForm) {
                showSpinnerForm(ResBundleGoodsGiftCard.getString("GETTING_CARD_INFORMATION"));
                try {
                    getController().findAndAddGiftCardInfo(enterNumberForm.getEnteredNumber(), false);
                    return true;
                } catch (CardsException ex) {
                    processCardsException(ex);
                    return true;
                }
            } else if (currentForm == fixedAmountForm) {
                //добавляем позицию в чек.
                doActivateCard(true);
                return true;
            } else if (currentForm == nonFixedAmountForm) {
                //добавляем позицию в чек.
                doActivateCard(false);
                return true;
            }

            if(isCurrentForm(deletePositionForm)){
                //на форме подтверждения нажали ентер - проанализируем
                if(deletePositionForm.deleteConfirmed()){
                    boolean checkPermission;
                    try {
                        checkPermission = getController().tryRequestPermissionDeletePosition(getController().getModel().getPosition(),
                                getController().getModel().getState() == ProductContainer.ProductState.DELETE);
                    } catch (NoPermissionException ex) {
                        getController().beepError(ex.getMessage());
                        return true;
                    }
                    getController().cashDeletePosition(getController().getModel().getPosition(), null, !checkPermission);
                } else{
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return true;
            }

            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (currentForm == spinnerForm) {
                return true;
            }
            if (currentForm == messageForm) {
                if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                    getController().getAdapter().dispatchCloseEvent(false);
                } else {
                    getController().getModel().setInternalState(getController().getModel().getInternalState());
                }
                return true;
            }
            if (currentForm == fixedAmountForm || currentForm == nonFixedAmountForm) {
                getController().getModel().setInternalState(GiftCardPluginState.ENTER_CARD_NUMBER);
                return true;
            }
            return false;
        }
        return false;
    }

    private void doActivateCard(final boolean isFixGiftCard) {
        showSpinnerForm(ResBundleGoodsGiftCard.getString("CARD_ACTIVATION"));
        try {
            if (isFixGiftCard) {
                getController().activateAndAddFixGiftCard();
            } else {
                getController().activateAndAddNonFixedGiftCard(nonFixedAmountForm.getPriceLong());
            }
            //зачистим перед следующим использованием
            enterNumberForm.clear();
            getController().getAdapter().dispatchCloseEvent(true);
        } catch (CardsException ex) {
            processCardsException(ex);
        }
    }

    private void processCardsException(CardsException ex) {
        ex.printStackTrace();
        showMessageForm(ex.getLocalizedMessage(), CommonMessageForm.ExitState.TO_LAST);
    }

    private void showMessageForm(String msg, CommonMessageForm.ExitState exitState) {
        messageForm.setMessage(msg);
        messageForm.setExitState(exitState);
        setCurrentForm(messageForm);
    }

    private void showSpinnerForm(String msg) {
        spinnerForm.setTextMessage(msg);
        setCurrentForm(spinnerForm);
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        if (currentForm == messageForm) {
            return true;
        }
        if (!getController().isAllowedToScanBarcode()) {
            enterNumberForm.clear();
            showMessageForm(ResBundleGoodsGiftCard.getString("BARCODE_SCANNING_NOT_ALLOWED"), CommonMessageForm.ExitState.TO_LAST);
            return true;
        }
        showSpinnerForm(ResBundleGoodsGiftCard.getString("GETTING_CARD_INFORMATION"));
        try {
            getController().findAndAddGiftCardInfo(barcode, false);
            return true;
        }  catch (CardsException ex) {
            processCardsException(ex);
            return true;
        }
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        if (currentForm == messageForm) {
            return true;
        }
        showSpinnerForm(ResBundleGoodsGiftCard.getString("GETTING_CARD_INFORMATION"));
        try {
            String cardNumber = Factory.getTechProcessImpl().getCards().parseCardNumber(track1, track2, track3, track4, PresentCards.MODULE_NAME);
            getController().findAndAddGiftCardInfo(cardNumber, true);
            return true;
        } catch (CardsException ex) {
            processCardsException(ex);
            return true;
        }
    }

    @Override
    public boolean checkDataBeforeAdd() {
        return true;
    }

    @Override
    public long getCurrentPositionCount() {
        return 0;
    }
}
