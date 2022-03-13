package ru.crystals.pos.card.replacement.visualization;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.card.replacement.i18n.MLReplaceCardResourceBundle;
import ru.crystals.pos.card.replacement.visualization.listener.MLReplaceStatusListener;
import ru.crystals.pos.card.replacement.visualization.model.ReplaceCardModel;
import ru.crystals.pos.card.replacement.visualization.model.ReplaceCardState;
import ru.crystals.pos.ml.MLService;
import ru.crystals.pos.ml.exception.MLConnectionException;
import ru.crystals.pos.ml.exception.MLException;
import ru.crystals.pos.ml.models.ContractInfo;

/**
 * Created by agaydenger on 08.08.16.
 */
public class ReplaceCardController {
    private MLReplaceStatusListener statusListener;
    private ReplaceCardModel model;
    private MLService mlService;

    public ReplaceCardController(MLReplaceStatusListener listener, MLService mlService) {
        this.statusListener = listener;
        this.mlService = mlService;
    }

    public void setModel(ReplaceCardModel model) {
        this.model = model;
    }

    public void searchContact() {
        try {
            model.setState(ReplaceCardState.SEARCH_CONTACT);

            String oldCardNumber = model.getModelInfo().getOldCardNumber();
            String mobilePhone = model.getModelInfo().getMobilePhone();

            ContractInfo contractInfo = mlService.getContractInfo(oldCardNumber, mobilePhone);

            model.getModelInfo().setContractId(contractInfo.getContractId());
            // В случае, если карту искали по номеру карты, придёт номер карты "0"
            if(!"0".equals(contractInfo.getOldCardNumber())) {
                model.getModelInfo().setOldCardNumber(contractInfo.getOldCardNumber());
            }
            model.setState(ReplaceCardState.ENTER_NEW_CARD_NUMBER);
        } catch (MLException e) {
            onMLException(e);
        }
    }

    public void finish(boolean completed) {
        if (completed) {
            model.fillResult(true);
        }
        statusListener.replaceComplete();
    }

    public void abort() {
        model.fillResult(false);
        statusListener.replaceComplete();
    }

    public void toState(ReplaceCardState prevState) {
        model.setState(prevState);
    }

    public void oldCardEntered(String enteredValue) {
        if(mlService.isMlCard(enteredValue)) {
            model.getModelInfo().setOldCardNumber(enteredValue);
            model.setState(ReplaceCardState.SEARCH_CONTACT);
            searchContact();
        } else {
            model.setMessageText(String.format(MLReplaceCardResourceBundle.getLocalValue("not.ml.card"), enteredValue));
            model.setState(ReplaceCardState.SHOW_ERROR);
        }
    }

    public void contractEntered(String enteredValue) {
        String questionnaireCode = enteredValue;
        //Если не ввели код анкеты
        if (StringUtils.isEmpty(questionnaireCode) ) {
            //Если пустой ввод не разрешен
            if (!isEmptyContactAllowed()) {
                model.setMessageText(MLReplaceCardResourceBundle.getLocalValue("questionnaire.required"));
                model.setState(ReplaceCardState.SHOW_ERROR);
                return;
            } else {
                //Согласно SR-1580
                questionnaireCode = getNobarcodePlaceholder();
            }
        }
        model.getModelInfo().setApplicationNumber(questionnaireCode);
        model.setState(ReplaceCardState.ACTIVATE_CARD);
        activateCard();
    }

    protected String getNobarcodePlaceholder() {
        return MLService.NOBARCODE_PLACEHOLDER;
    }

    public void newCardEntered(String enteredValue) {
        if(mlService.isMlCard(enteredValue)) {
            model.getModelInfo().setNewCardNumber(enteredValue);
            if(StringUtils.isBlank(model.getModelInfo().getMobilePhone()) || mlService.needToReplaceCard(model.getModelInfo().getOldCardNumber())) {
                model.setState(ReplaceCardState.ENTER_CONTACT_ID);
            } else {
                model.setState(ReplaceCardState.ACTIVATE_CARD);
                activateCard();
            }
        } else {
            model.setMessageText(String.format(MLReplaceCardResourceBundle.getLocalValue("not.ml.card"), enteredValue));
            model.setState(ReplaceCardState.SHOW_ERROR);
        }
    }

    public void activateCard() {
        try {
            mlService.replaceCard(
                    model.getModelInfo().getOldCardNumber(),
                    model.getModelInfo().getNewCardNumber(),
                    model.getModelInfo().getContractId(),
                    model.getModelInfo().getApplicationNumber());
            finish(true);
        } catch (MLException e) {
            onMLException(e);
        }
    }

    private void onMLException(MLException e) {
        model.setMessageText(e.getMessage());
        if (e instanceof MLConnectionException) {
            model.setState(ReplaceCardState.ERROR_TRY_REPEAT);
        } else {
            model.setState(ReplaceCardState.SHOW_ERROR);
        }
    }

    public boolean isEnterOldCardAllowed() {
        return model.isNeedToEnterOldCard();
    }

    public boolean isEmptyContactAllowed() {
        return !mlService.isHolderMandatory(model.getModelInfo().getNewCardNumber());
    }

    public void mobilePhoneEntered(String enteredValue) {
        if(mlService.validateMobilePhone(enteredValue)){
            enteredValue = "+".concat(enteredValue);
            model.getModelInfo().setMobilePhone(enteredValue);
            model.setState(ReplaceCardState.SEARCH_CONTACT);
            searchContact();
        } else {
            model.setMessageText(String.format(MLReplaceCardResourceBundle.getLocalValue("not.valid.phone.number"), enteredValue));
            model.setState(ReplaceCardState.SHOW_ERROR);
        }
    }
}
