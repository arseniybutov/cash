package ru.crystals.pos.visualization.products.cftcard.view;

import ru.crystals.pos.cards.cft.CardType;
import ru.crystals.pos.cards.cft.exception.CFTException;
import ru.crystals.pos.check.PositionCFTGiftCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.CommonAbstractView;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonSpinnerForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDeletePositionConfirmForm;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.cftcard.CFTGiftCardEditOrDeleteForm;
import ru.crystals.pos.visualization.products.cftcard.CFTGiftCardEnterNumberForm;
import ru.crystals.pos.visualization.products.cftcard.CFTGiftCardViewForm;
import ru.crystals.pos.visualization.products.cftcard.ResBundleGoodsCFTGiftCard;
import ru.crystals.pos.visualization.products.cftcard.controller.CFTGiftCardPluginController;

import java.awt.event.KeyEvent;

public class CFTGiftCardPluginView extends CommonAbstractView<CFTGiftCardPluginController> {
    private final CFTGiftCardEnterNumberForm enterNumberForm;
    private final CFTGiftCardEditOrDeleteForm editOrDeleteForm;
    private final CFTGiftCardViewForm viewForm;
    private final CommonSpinnerForm spinnerForm;
    private final CommonDeletePositionConfirmForm deletePositionForm;
    private CardType type = CardType.NONE;
    private String trackNumber = "XXXX";


    public CFTGiftCardPluginView() {
        CFTGiftCardPluginView.this.setName("ru.crystals.pos.visualization.products.cftcard.view.CFTGiftCardPluginView");
        this.enterNumberForm = new CFTGiftCardEnterNumberForm(this);
        this.editOrDeleteForm = new CFTGiftCardEditOrDeleteForm(this);
        this.viewForm = new CFTGiftCardViewForm(this);
        this.deletePositionForm = new CommonDeletePositionConfirmForm(this);

        this.spinnerForm = new CommonSpinnerForm(this, ResBundleGoodsCFTGiftCard.getString("ACTIVATING_CFT_CARD"));

        this.add(enterNumberForm, enterNumberForm.getClass().getName());
        this.add(editOrDeleteForm, editOrDeleteForm.getClass().getName());
        this.add(spinnerForm, spinnerForm.getClass().getName());
        this.add(viewForm, viewForm.getClass().getName());
        this.add(deletePositionForm, deletePositionForm.getClass().getName());

    }

    @Override
    public void modelChanged() {
        switch (getController().getModel().getState()) {
            case ADD:
                String errMessage = getController().isPossibleToAddPosition();
                if (errMessage == null) {
                    PositionEntity position = new PositionEntity();
                    getController().fillDefaultPosition(enterNumberForm.getQuantity(), enterNumberForm.getPrice(), getController().getModel().getProduct(), position);
                    getController().getAdapter().dispatchEventAddGoods(position);
                    setCurrentForm(enterNumberForm);
                    getController().getModel().setPosition(position);
                } else {
                    messageForm.setMessage(errMessage);
                    messageForm.setExitState(CommonMessageForm.ExitState.TO_EXIT);
                    setCurrentForm(messageForm);
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

        //если подтверждаем выход нажатием enter
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (getController().getModel().getState() == ProductContainer.ProductState.VIEW) {
                return true;
            }

            if (currentForm == editOrDeleteForm) {
                getController().returnPosition(getController().getModel().getPosition());
                return true;
            }

            //если состояние "добавление позиции" и getQuantity() и getPrice() сформированы, т.е. не null
            //добавляем позицию в чек.
            if (getController().getModel() != null &&
                    getController().getModel().getState() == ProductContainer.ProductState.ADD &&
                    currentForm.getQuantity() != null &&
                    currentForm.getPrice() != null) {
                setCurrentForm(spinnerForm);
                try {
                    PositionCFTGiftCardEntity position = new PositionCFTGiftCardEntity();
                    getController().fillDefaultPosition(enterNumberForm.getQuantity(), enterNumberForm.getPrice(), getController().getModel().getProduct(), position);
                    getController().activateCFTCard(position, enterNumberForm.getEnteredNumber(), CardType.PAN);
                    getController().getAdapter().dispatchCloseEvent(true);
                    return true;
                } catch (CFTException ex) {
                    ex.printStackTrace();
                    String msg = ex.getLocalizedMessage();
                    if (msg == null || msg.length() == 0) {
                        msg = ResBundleGoodsCFTGiftCard.getString("ACTIVATION_ERROR");
                    }
                    messageForm.setMessage(msg);
                    messageForm.setExitState(CommonMessageForm.ExitState.TO_LAST);
                    setCurrentForm(messageForm);
                    return true;
                }
            }

            if(currentForm == deletePositionForm){
                //на форме подтверждения нажали ентер - проанализируем
                if(deletePositionForm.deleteConfirmed()){
                    getController().cashDeletePosition(getController().getModel().getPosition());
                } else{
                    getController().getAdapter().dispatchCloseEvent(false);
                }
                return true;
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Factory.getTechProcessImpl().stopCriticalErrorBeeping();
            if (currentForm == spinnerForm) {
                return true;
            }
            if (currentForm == messageForm) {
                if (messageForm.getExitState() == CommonMessageForm.ExitState.TO_EXIT) {
                    getController().getAdapter().dispatchCloseEvent(false);
                } else {
                    setCurrentForm(enterNumberForm);
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean dispatchBarcodeScanned(String barcode) {
        if (currentForm == messageForm) {
            return true;
        }
        setCurrentForm(spinnerForm);
        try {
            PositionCFTGiftCardEntity position = new PositionCFTGiftCardEntity();
            getController().fillDefaultPosition(enterNumberForm.getQuantity(), enterNumberForm.getPrice(), getController().getModel().getProduct(), position);
            getController().activateCFTCard(position, barcode, CardType.BARCODE);
            return true;
        } catch (CFTException ex) {
            ex.printStackTrace();
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                msg = ResBundleGoodsCFTGiftCard.getString("ACTIVATION_ERROR");
            }
            messageForm.setMessage(msg);
            setCurrentForm(messageForm);
            return true;
        }
    }

    @Override
    public boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        if (currentForm == messageForm) {
            return true;
        }
        getFirstNotEmpty(new String[]{track1, track2, track3, track4});
        setCurrentForm(spinnerForm);
        try {
            PositionCFTGiftCardEntity position = new PositionCFTGiftCardEntity();
            getController().fillDefaultPosition(enterNumberForm.getQuantity(), enterNumberForm.getPrice(), getController().getModel().getProduct(), position);
            getController().activateCFTCard(position, trackNumber, type);
            return true;
        } catch (CFTException ex) {
            ex.printStackTrace();
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                msg = ResBundleGoodsCFTGiftCard.getString("ACTIVATION_ERROR");
            }
            messageForm.setMessage(msg);
            setCurrentForm(messageForm);
            return true;
        }
    }

    private void getFirstNotEmpty(String[] tracks) {
        for (String track : tracks) {
            if ((track != null) && !track.isEmpty()) {
                trackNumber = track;
                if (track == tracks[0]) {
                    type = CardType.TRACK1;
                } else if (track == tracks[1]) {
                    type = CardType.TRACK2;
                } else if (track == tracks[2]) {
                    type = CardType.TRACK3;
                }
            }
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
