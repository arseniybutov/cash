package ru.crystals.pos.card.replacement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.crystals.cards.CardSearchResult;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.pos.CashException;
import ru.crystals.pos.annotation.ConditionalOnExtSystemProvider;
import ru.crystals.pos.card.replacement.visualization.ReplaceCardAdapter;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.ml.MLService;
import ru.crystals.pos.techprocess.ReplaceCardScenarioInterface;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by agaydenger on 04.08.16.
 */
@Service(MLService.PROVIDER_NAME)
@ConditionalOnExtSystemProvider(MLService.PROVIDER_NAME)
public class MLReplaceCardScenario implements ReplaceCardScenarioInterface<MLReplaceCardResult> {
    private static final Logger LOG = LoggerFactory.getLogger(MLReplaceCardScenario.class);

    @Autowired
    private MLService mlService;

    @Autowired
    protected CatalogService catalogModule;

    @Autowired
    private CardsService cardsService;

    private AtomicBoolean lock = new AtomicBoolean(true);

    @Override
    public MLReplaceCardResult doReplace(final TechProcessInterface techProcess, final CheckService checkService,
                                         String oldCardNumber, CardSearchResult oldCardSearchResult) throws CardsException {
        if (mlService == null || !mlService.isEnabled()) {
            throw new CardsException("service.not.available");
        }
        //Теперь прочекаем продукт ДК
        ProductEntity productEntity = null;
        try {
            productEntity = catalogModule.searchProduct(mlService.getDiscountCardCode());
        } catch (CashException e) {
            LOG.error("Failed to obtain DiscountCardProduct", e);
        }
        if (productEntity == null) {
            return new MLReplaceCardResult(oldCardNumber, CardTypeEntity.getFakeInstanceByType(CardTypes.ExternalCard, MLService.PROVIDER_NAME), null);
        }
        lock = new AtomicBoolean(true);
        ReplaceCardAdapter replaceCardAdapter = new ReplaceCardAdapter(oldCardNumber, productEntity, () -> lock.set(false), mlService);
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(replaceCardAdapter.getVisualPanel(), lock);
        MLReplaceCardResult result = replaceCardAdapter.getResult();
        if (result != null && result.getCardPositionToAdd() != null) {
            try {
                // если карты ML заведены в Set10, необходимо получить их GUID
                result.getCardType().setGuid(cardsService.getGuidFromLocal(result.getNewCardNumber()));

                techProcess.goToWaitGoods();
                Factory.getInstance().getMainWindow().getCheckContainer().setState(CheckState.SEARCH_PRODUCT);
                // При замене манзановской карты новую карту выдают бесплатно.
                result.getCardPositionToAdd().setPriceStart(0L);
                result.getCardPositionToAdd().setPriceEnd(0L);
                techProcess.addPosition(result.getCardPositionToAdd(), InsertType.DIRECTORY);
            } catch (Exception e) {
                LOG.error("Failed to add a new card!");
            }
        }
        return result;
    }
}
