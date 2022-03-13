package ru.crystals.pos.card.replacement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardSearchResult;
import ru.crystals.pos.card.replacement.visualization.SiebelCardReplacementAdapter;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.fiscalprinter.datastruct.documents.SimpleServiceDocument;
import ru.crystals.pos.techprocess.ReplaceCardScenarioInterface;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сценарий замены карты Siebel.
 *
 * @since 10.2.83.0
 */
@Service(SiebelService.PROVIDER_NAME)
public class SiebelCardReplacementScenario implements ReplaceCardScenarioInterface<ReplaceCardScenarioInterface.ReplaceCardResult> {
    private static final Logger logger = LoggerFactory.getLogger(SiebelCardReplacementScenario.class);

    @Autowired(required = false)
    private SiebelService siebelService;

    private AtomicBoolean lock;

    @Override
    public ReplaceCardScenarioInterface.ReplaceCardResult doReplace(TechProcessInterface techProcess, CheckService checkService,
                                                                    String oldCardNumber, CardSearchResult oldCardSearchResult) {
        if (siebelService == null) {
            logger.error("Unable to replace card: Siebel service is not available");
            return null;
        }

        // Покажем сообщения ДО замены карты, т.к. в них информация кассиру о типе карты для выдачи
        showMessages(techProcess, oldCardSearchResult);

        lock = new AtomicBoolean(true);
        SiebelCardReplacementAdapter cardReplacementAdapter = new SiebelCardReplacementAdapter(oldCardNumber, techProcess.getCheck(), siebelService,
                () -> lock.set(false));
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(cardReplacementAdapter.getView(), lock);
        if (cardReplacementAdapter.getResult() == null) {
            return null;
        }
        if (cardReplacementAdapter.getResult().getSlip() != null && !"".equals(cardReplacementAdapter.getResult().getSlip())) {
            SimpleServiceDocument document = new SimpleServiceDocument(Arrays.asList(cardReplacementAdapter.getResult().getSlip().split("\n")));
            document.setPromo(true);
            if (techProcess.getCheck() != null) {
                techProcess.getCheck().getServiceDocs().add(document);
            }
        }
        ReplaceCardResult result = new ReplaceCardScenarioInterface.ReplaceCardResult(
                cardReplacementAdapter.getResult().getCardEntity() == null ? null : cardReplacementAdapter.getResult().getCardEntity().getNumber(),
                cardReplacementAdapter.getResult().getCardEntity() == null ? null : cardReplacementAdapter.getResult().getCardEntity().getNewCardType()
        );

        if (cardReplacementAdapter.getResult().getCardEntity() != null) {
            result.setCardType(cardReplacementAdapter.getResult().getCardEntity().getCardType());
            result.getCardType().getCards().add(cardReplacementAdapter.getResult().getCardEntity());
        }
        return result;
    }

    private void showMessages(TechProcessInterface techProcess, CardSearchResult result) {
        if (result == null) {
            return;
        }
        List<String> messages = new ArrayList<>();
        messages.addAll(result.getMessages());
        // Очистим, чтобы не показывать сообщения повторно
        result.getMessages().clear();
        if (result.getCard() != null && !result.getCard().getCards().isEmpty()) {
            for (CardEntity card : result.getCard().getCards()) {
                if (card.getMessagesFromExternalSystem() != null) {
                    messages.addAll(card.getMessagesFromExternalSystem());
                    // Очистим, чтобы не показывать сообщения повторно
                    card.getMessagesFromExternalSystem().clear();
                }
            }
        }
        messages.forEach(message -> techProcess.showTextMessage(message));
    }
}
