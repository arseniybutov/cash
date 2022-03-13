package ru.crystals.pos.card.replacement;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.menu.MenuCommand;
import ru.crystals.pos.ml.MLService;
import ru.crystals.pos.techprocess.ReplaceCardScenarioInterface;
import ru.crystals.pos.techprocess.StatePurchase;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.menu.commands.AbstractCommand;

/**
 * Created by agaydenger on 12.08.16.
 */
@MenuCommand(name = "command_MLReplace")
public class MLReplaceCardMenuCommand extends AbstractCommand {

    private MLReplaceCardScenario scenario;

    @Autowired
    private CheckService checkService;

    @Autowired(required = false)
    private void setScenario(@Qualifier(MLService.PROVIDER_NAME) ReplaceCardScenarioInterface scenario) {
        this.scenario = (MLReplaceCardScenario) scenario;
    }

    @Override
    public void execute() {
        try {
            getFactory().getMainWindow().setCurrentContainer(getFactory().getMainWindow().getCheckContainer());
            MLReplaceCardResult replaceCardResult = scenario.doReplace(Factory.getTechProcessImpl(), checkService, null, null);
            if (replaceCardResult != null && replaceCardResult.getCardPositionToAdd() != null) {
                checkService.addCard(replaceCardResult.getNewCardNumber(), replaceCardResult.getCardType(), checkService.getCurrentCheckNum(), null);
                getFactory().showCardIcon(replaceCardResult.getCardType());
            }
        } catch (Exception e) {
            getFactory().showMessage(e.getMessage());
        }
    }

    @Override
    public boolean isCommandAvailable() {
        PurchaseEntity check = getFactory().getTechProcessImpl().getCheck();
        return scenario != null && getFactory().getMainWindow().getCheckContainer().getState() == CheckState.SEARCH_PRODUCT &&
                (check == null || (check.isSale() && checkService.getCurrentPurchaseEntry().getCurrentState() == StatePurchase.WAIT_GOODS && ! isMLCardAlreadyApplied(check))) && Factory.getTechProcessImpl().isShiftOpen();

    }

    private boolean isMLCardAlreadyApplied(PurchaseEntity cheque) {
        if (cheque != null && CollectionUtils.isNotEmpty(cheque.getCards())) {
            for (PurchaseCardsEntity cardsEntity : cheque.getCards()) {
                if (CardTypes.ExternalCard == cardsEntity.getType() && MLService.PROVIDER_NAME.equals(cardsEntity.getProcessingName())) {
                    return true;
                }
            }
        }
        return false;
    }
}
