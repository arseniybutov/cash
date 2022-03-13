package ru.crystals.pos.card.replacement.visualization.view;

import java.awt.CardLayout;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import ru.crystals.pos.card.replacement.i18n.MLReplaceCardResourceBundle;
import ru.crystals.pos.card.replacement.visualization.ReplaceCardController;
import ru.crystals.pos.card.replacement.visualization.listener.ModelChangedListener;
import ru.crystals.pos.card.replacement.visualization.model.ReplaceCardModelChangedEvent;
import ru.crystals.pos.card.replacement.visualization.model.ReplaceCardState;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonYesNoForm;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.components.WaitComponent;

/**
 * Created by agaydenger on 08.08.16.
 */
public class ReplaceCardView extends VisualPanel implements XListener, ModelChangedListener {

    private CardLayout layout = new CardLayout();
    private ReplaceCardController controller;
    private WaitComponent spinnerForm;
    private ReplaceCardEnterCardNumberForm enterCardNumberForm;
    private ReplaceCardState lastState;
    private ReplaceCardState prevState;
    private CommonYesNoForm dialogForm;
    private CommonMessageForm messageForm;
    private JPanel currentForm;

    public ReplaceCardView() {
        setLayout(layout);
        spinnerForm = new WaitComponent("", this);
        enterCardNumberForm = new ReplaceCardEnterCardNumberForm(this);
        dialogForm = new CommonYesNoForm(this);
        messageForm = new CommonMessageForm(this);
        add(spinnerForm, spinnerForm.getClass().getName());
        add(enterCardNumberForm, enterCardNumberForm.getClass().getName());
        add(dialogForm, dialogForm.getClass().getName());
        add(messageForm, messageForm.getClass().getName());
    }

    @Override
    public void modelChanged(ReplaceCardModelChangedEvent event) {
        prevState = lastState;
        lastState = event.getState();
        switch (lastState) {
            case SEARCH_CONTACT:
                spinnerForm.setActionStatusText(MLReplaceCardResourceBundle.getLocalValue("search.contact"));
                showForm(spinnerForm);
                break;
            case ENTER_NEW_CARD_NUMBER:
                enterCardNumberForm.setText(MLReplaceCardResourceBundle.getLocalValue("input.card.number"), MLReplaceCardResourceBundle.getLocalValue("new.card.number"));
                enterCardNumberForm.clear();
                showForm(enterCardNumberForm);
                break;
            case ENTER_OLD_CARD_NUMBER:
                enterCardNumberForm.setText(MLReplaceCardResourceBundle.getLocalValue("input.card.number"), MLReplaceCardResourceBundle.getLocalValue("old.card.number"));
                enterCardNumberForm.clear();
                showForm(enterCardNumberForm);
                break;
            case ENTER_CONTACT_ID:
                enterCardNumberForm.setText(MLReplaceCardResourceBundle.getLocalValue("input.questionnaire.number"), MLReplaceCardResourceBundle.getLocalValue("questionnaire.number"));
                enterCardNumberForm.clear();
                showForm(enterCardNumberForm);
                break;
            case ERROR_TRY_REPEAT:
                dialogForm.setMessageLabelText(event.getMessageText());
                dialogForm.setButtonsCaptions(ResBundleVisualization.getString("REPEAT_PENDING"), ResBundleVisualization.getString("CANCEL"));
                showForm(dialogForm);
                break;
            case ACTIVATE_CARD:
                spinnerForm.setActionStatusText(MLReplaceCardResourceBundle.getLocalValue("card.activation"));
                showForm(spinnerForm);
                break;
            case GO_TO_REPLACE:
                dialogForm.setMessageLabelText(MLReplaceCardResourceBundle.getLocalValue("replace.card.question"));
                dialogForm.setButtonsCaptions(ResBundleVisualization.getString("BUTTON_YES"), ResBundleVisualization.getString("BUTTON_NO"));
                showForm(dialogForm);
                break;
            case SHOW_ERROR:
                messageForm.setMessage(event.getMessageText());
                showForm(messageForm);
                break;
            case APPROACH_TO_REPLACE:
                dialogForm.setMessageLabelText(MLReplaceCardResourceBundle.getLocalValue("replace.card.approach"));
                dialogForm.setButtonsCaptions(MLReplaceCardResourceBundle.getLocalValue("replace.by.phone.number"), (MLReplaceCardResourceBundle.getLocalValue("replace.by.card.number")));
                showForm(dialogForm);
                break;
            case ENTER_MOBILE_PHONE:
                enterCardNumberForm.setText(MLReplaceCardResourceBundle.getLocalValue("input.phone.number"), MLReplaceCardResourceBundle.getLocalValue("phone.number"));
                enterCardNumberForm.clear();
                showForm(enterCardNumberForm);
                break;
        }
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (currentForm == enterCardNumberForm) {
            if (lastState == ReplaceCardState.ENTER_CONTACT_ID) {
                controller.contractEntered(barcode);
            } else if (lastState == ReplaceCardState.ENTER_OLD_CARD_NUMBER) {
                controller.oldCardEntered(barcode);
            } else if (lastState == ReplaceCardState.ENTER_NEW_CARD_NUMBER) {
                controller.newCardEntered(barcode);
            }
        }
        return true;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            onEnter();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            onEsc();
        }
        return e.isFunctionalKey();
    }

    private void onEnter() {
        if (currentForm == enterCardNumberForm) {
            // Константы из ReplaceCardState
            // Здесь обзательно нужно выходить после каждого ифа, в противном случае
            // возможна ситуация, когда одно и то же событие придёт сразу в две формы:
            // когда одна форма откликается на события и ставит вместо себя нижележащую форму.
            // Тогда, когда мы снова окажемся здесь, у формы автоматически зажжется событие.
            switch (lastState) {
                case ENTER_CONTACT_ID:
                    controller.contractEntered(enterCardNumberForm.getEnteredValue());
                    break;
                case ENTER_OLD_CARD_NUMBER:
                    controller.oldCardEntered(enterCardNumberForm.getEnteredValue());
                    break;
                case ENTER_NEW_CARD_NUMBER:
                    controller.newCardEntered(enterCardNumberForm.getEnteredValue());
                    break;
                case ENTER_MOBILE_PHONE:
                    controller.mobilePhoneEntered(enterCardNumberForm.getEnteredValue());
                    break;
                default:
                    onExitFromShowError();
                    break;
            }
        } else if (currentForm == dialogForm) {
            onEnterOnDialog();
        } else if(lastState == ReplaceCardState.SHOW_ERROR) {
            onExitFromShowError();
        }
    }

    private void onEnterOnDialog() {
        if(lastState == ReplaceCardState.GO_TO_REPLACE) {
            if (dialogForm.isYes()) {
                controller.toState(ReplaceCardState.SEARCH_CONTACT);
                controller.searchContact();
            } else {
                controller.abort();
            }
        }  else if(lastState == ReplaceCardState.APPROACH_TO_REPLACE) {
            //Выбирали способ поиска карты
            if (dialogForm.isYes()) {
                //По номеру телефона
                controller.toState(ReplaceCardState.ENTER_MOBILE_PHONE);
            } else {
                //По номеру карты старой
                controller.toState(ReplaceCardState.ENTER_OLD_CARD_NUMBER);
            }
        }else{
            if (dialogForm.isYes()) {
                if(prevState == ReplaceCardState.SEARCH_CONTACT) {
                    controller.toState(prevState);
                    controller.searchContact();
                } else if(prevState == ReplaceCardState.ACTIVATE_CARD){
                    controller.toState(prevState);
                    controller.activateCard();
                }
            } else {
                controller.finish(false);
            }
        }

    }

    private void onEsc() {
        if (currentForm == enterCardNumberForm) {
            if (lastState == ReplaceCardState.ENTER_CONTACT_ID) {
                controller.toState(ReplaceCardState.ENTER_NEW_CARD_NUMBER);
            } else if (lastState == ReplaceCardState.ENTER_OLD_CARD_NUMBER) {
                if(prevState == ReplaceCardState.APPROACH_TO_REPLACE){
                    controller.toState(ReplaceCardState.APPROACH_TO_REPLACE);
                }else{
                    controller.finish(false);
                }
            } else if (lastState == ReplaceCardState.ENTER_MOBILE_PHONE) {
                controller.toState(ReplaceCardState.APPROACH_TO_REPLACE);
            } else if (lastState == ReplaceCardState.ENTER_NEW_CARD_NUMBER) {
                if (controller.isEnterOldCardAllowed()) {
                    controller.toState(ReplaceCardState.ENTER_OLD_CARD_NUMBER);
                } else {
                    controller.finish(false);
                }
            }
        } else if (lastState == ReplaceCardState.SHOW_ERROR) {
            onExitFromShowError();
        }else{
            controller.finish(false);
        }
    }

    private void onExitFromShowError() {
        if (prevState == ReplaceCardState.ACTIVATE_CARD) {
            controller.toState(ReplaceCardState.ENTER_NEW_CARD_NUMBER);
        } else if(prevState == ReplaceCardState.ENTER_OLD_CARD_NUMBER
                || prevState == ReplaceCardState.ENTER_NEW_CARD_NUMBER
                || prevState == ReplaceCardState.ENTER_CONTACT_ID
                || prevState == ReplaceCardState.ENTER_MOBILE_PHONE){
            controller.toState(prevState);
        } else {
            controller.finish(false);
        }
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        if (currentForm == enterCardNumberForm) {
            if (lastState == ReplaceCardState.ENTER_OLD_CARD_NUMBER) {
                controller.oldCardEntered(Factory.getTechProcessImpl().getCards().parseCardNumber(track1, track2, track3, track4));
            } else if (lastState == ReplaceCardState.ENTER_NEW_CARD_NUMBER) {
                controller.newCardEntered(Factory.getTechProcessImpl().getCards().parseCardNumber(track1, track2, track3, track4));
            }
        }
        return true;
    }

    public void setController(ReplaceCardController controller) {
        this.controller = controller;
    }

    private void showForm(JPanel v) {
        currentForm = v;
        layout.show(this, v.getClass().getName());
        v.validate();
    }

}
