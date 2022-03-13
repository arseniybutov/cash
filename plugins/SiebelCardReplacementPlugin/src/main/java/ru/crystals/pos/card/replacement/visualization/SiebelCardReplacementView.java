package ru.crystals.pos.card.replacement.visualization;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonYesNoForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonInputPanelExt;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.components.WaitComponent;
import ru.crystals.siebel.SiebelCardController;

import javax.swing.JPanel;
import java.awt.CardLayout;

/**
 * Гуй для взаимодействия с пользователем и отображения процесса замены карты.
 *
 * @since 10.2.83.0
 */
public class SiebelCardReplacementView extends VisualPanel implements XListener, SiebelCardReplacementModelListener {
    private CardLayout layout = new CardLayout();
    private CommonYesNoForm cardReplacementChoiceDialog;
    private WaitComponent spinner;
    private CommonMessageForm messageForm;
    private CardInputFormDecorator cardInputFormContainer;
    private CommonInputPanelExt cardInputForm;

    private JPanel currentForm;
    private SiebelCardReplacementController controller;
    private SiebelCardController issueCardController;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SiebelCardReplacementView}.
     */
    public SiebelCardReplacementView() {
        setLayout(layout);
        cardReplacementChoiceDialog = new CommonYesNoForm(this);
        cardReplacementChoiceDialog.setMessageLabelText(Strings.OFFER_CARD.get());
        cardReplacementChoiceDialog.setButtonsCaptions(Strings.BUTTON_TEXT_OFFER_CARD.get(), Strings.BUTTON_TEXT_CANCEL.get());
        spinner = new WaitComponent(Strings.SPINNER_TEXT.get());
        cardInputForm = CommonInputPanelExt.createCardNumberInputPanel();
        cardInputForm.setEnterListener(number -> onPositiveChoiceSelected());
        cardInputForm.setEscListener(this::onNegativeChoiceSelected);

        cardInputFormContainer = new CardInputFormDecorator(cardInputForm);

        messageForm = new CommonMessageForm(this);
        this.add(cardReplacementChoiceDialog, cardReplacementChoiceDialog.getClass().getName());
        this.add(spinner, spinner.getClass().getName());
        this.add(cardInputFormContainer, cardInputFormContainer.getClass().getName());
        this.add(messageForm, messageForm.getClass().getName());
        showForm(cardReplacementChoiceDialog);
    }

    public void reset() {
        cardInputForm.clear();
        showForm(cardReplacementChoiceDialog);
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        if (currentForm == cardInputFormContainer) {
            if (controller != null) {
                controller.onCardNumberEntered(barcode);
            }
            if (issueCardController != null) {
                issueCardController.onCardNumberEntered(barcode);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        switch (e.getKeyCode()) {
            case XKeyEvent.VK_ENTER:
                return onPositiveChoiceSelected();
            case XKeyEvent.VK_ESCAPE:
                return onNegativeChoiceSelected();
            default:
                return e.isFunctionalKey();
        }
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        if (currentForm == cardInputFormContainer) {
            if (controller != null) {
                controller.onCardNumberEntered(track2);
            }
            if (issueCardController != null) {
                issueCardController.onCardNumberEntered(track2);
            }
            return true;
        }
        return false;
    }

    private boolean onPositiveChoiceSelected() {
        if (currentForm == cardReplacementChoiceDialog) {
            if (cardReplacementChoiceDialog.isYes()) {
                showForm(cardInputFormContainer);
            } else {
                notifyControllersAboutFinish();
            }
            return true;
        }
        if (currentForm == cardInputFormContainer) {
            if (StringUtils.isBlank(cardInputForm.getValue())) {
                return true;
            }
            if (controller != null) {
                controller.onCardNumberEntered(cardInputForm.getValue());
            }
            if (issueCardController != null) {
                issueCardController.onCardNumberEntered(cardInputForm.getValue());
            }
            return true;
        }
        if (currentForm == messageForm) {
            notifyControllersAboutFinish();
            return true;
        }
        return false;
    }

    @Override
    public void modelChanged(SiebelCardReplacementModelState newState) {
        switch (newState) {
            case CARD_SCANNING:
                showForm(cardInputFormContainer);
                break;
            case INVALID_CARD_SCANNED_MESSAGE:
                messageForm.setMessage(Strings.INVALID_CARD_TEXT.get());
                showForm(messageForm);
                break;
            case ERROR_RAISED:
                if (controller.getModel().getErrorText() != null) {
                    messageForm.setMessage(controller.getModel().getErrorText());
                    controller.getModel().setErrorText(null);
                } else {
                    messageForm.setMessage(Strings.GENERIC_ERROR_TEXT.get());
                }
                showForm(messageForm);
                break;
            case CARD_REPLACEMENT_IN_PROGRESS:
                showForm(spinner);
                break;
            default:
                break;
        }
    }

    private void notifyControllersAboutFinish() {
        if (controller != null) {
            controller.finish();
        }
        if (issueCardController != null) {
            issueCardController.finish();
        }
    }

    private boolean onNegativeChoiceSelected() {
        if (currentForm == cardReplacementChoiceDialog) {
            notifyControllersAboutFinish();
            return true;
        }
        if (currentForm == cardInputFormContainer) {
            if (!"".equals(cardInputForm.getValue())) {
                cardInputForm.clear();
                return true;
            }
            notifyControllersAboutFinish();
            return true;
        }
        if (currentForm == messageForm) {
            notifyControllersAboutFinish();
            return true;
        }
        return false;
    }

    /**
     * Устанавливает контроллер, который будет управлять состоянием данного представления.
     *
     * @param controller контроллер состояния представления.
     */
    public void setController(SiebelCardReplacementController controller) {
        this.controller = controller;
    }

    public void setIssueCardController(SiebelCardController issueCardController) {
        this.issueCardController = issueCardController;
    }

    private void showForm(JPanel form) {
        this.currentForm = form;
        layout.show(this, form.getClass().getName());
        form.validate();
    }
}
