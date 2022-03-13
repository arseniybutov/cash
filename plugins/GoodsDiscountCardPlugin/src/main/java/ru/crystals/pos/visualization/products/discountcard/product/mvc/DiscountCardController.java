package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.internal.good.processing.DCSellSteps;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandler;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandlersRegistry;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodVO;
import ru.crystals.pos.annotation.ConditionalOnProductTypeConfig;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.CardAlreadyAddedException;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.products.discountcard.ResBundleGoodsDiscountCard;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Контроллер для автомата/сценария "Продажа дисконтной карты".
 * <p/>
 * По факту содержит логику техпроцесса продажи ДК.
 * <p/>
 * NOTE: везде в документации под "продажей" понимается процесс добавления ДК в чек как товарной позиции.
 *
 * @author aperevozchikov
 */
@Component
@ConditionalOnProductTypeConfig(typeName = ProductDiscriminators.PRODUCT_DISCOUNT_CARD_ENTITY)
public class DiscountCardController extends AbstractProductController<DiscountCardModel> {
    private static final Logger log = LoggerFactory.getLogger(DiscountCardController.class);

    /**
     * Реестр обработчиков, что могут помочь добавить дисконтную/внутреннюю карту в чек как товарную позицию
     */
    private DiscountCardAsGoodHandlersRegistry registry = null;

    /**
     * А вот это чисто для хаканья: добавить карту в чек
     */
    private CheckService checkService;

    /**
     * Флаг-признак "контроллер на финишной прямой" (т.е., уже идет процесс добавления позиции в чек)
     */
    private boolean onFinishLine = false;

    @Autowired
    void setCheckService(CheckService checkService) {
        this.checkService = checkService;
    }

    /**
     * Реакция на событие ввода номера карты при продаже ДК.
     *
     * @param number номер ДК, что хотят продать
     * @throws CardsException при невозможности продать ДК с указанным номером - по разным причинам: ожидается, что вызвавший этот метод покажет сообщение об
     *                        этой ошибке (и предложит ввести номер ДК еще раз).
     */
    public void setCardNumber(String number) throws CardsException {
        log.trace("entering setCardNumber(String)");

        if (cardAlreadyAddedInThisCheck(number)) {
            throw new CardsException(ResBundleGoodsDiscountCard.getString("CARD_ALREADY_ADDED"));
        }

        // здесь надо будет по номеру определить карточный процессинг, которому "принадлежит" данная карта
        //  и, если процессинг не найдется, выкинуть Exception
        DiscountCardAsGoodHandler handler = getHandlerByCardNumber(number);
        if (handler == null) {
            // ни один карточный процессинг не признал эту карту "своей"
            throw new CardsException(ResBundleGoodsDiscountCard.getString("NO_ONE_CAN_HANDLE_THIS_CARD_AS_GOOD"));
        }

        checkAbilityToAddThisPosition();

        DiscountCardAsGoodVO vo = handler.getCardInfo(number, isRefund());
        if (vo == null) {
            throw new CardsException(ResBundleGoodsDiscountCard.getString("CARD_NOT_FOUND"));
        }

        vo.setApplicableOnCheck(getModel().getProduct().getProductConfig().getIsCardApplicable());
        if (getModel().isNotRefund()) {
            List<String> violations = handler.validateCard(vo);
            if (CollectionUtils.isNotEmpty(violations)) {
                throw new CardsException(violations.get(0));
            }
        }

        getModel().setHandler(handler);
        getModel().setCard(vo);
        clearHolderId();

        // заполним этапы техпроцесса, через которые надо пройти при продаже именно этой карты:
        getModel().getProcessSteps().clear();
        if (getModel().isNotRefund()) {
            getModel().getProcessSteps().addAll(convert(handler.getProcess(vo)));
        } else {
            getModel().getProcessSteps().add(DiscountCardPluginState.ENTER_CARD_NUMBER);
        }

        // и тексты на форме ввода "идентификатора клиента"
        getModel().setHolderIdFormMessages(handler.getHolderIdFormMessages(vo));

        // после успешного ввода номера - переходим к следующему этапу техпроцесса:
        if (getModel().moveForward() && !DiscountCardPluginState.APPLY_CARD.equals(getModel().getStage())) {
            // и событие изменения модели пробросим
            getModel().changed();
        } else {
            // техпроцесс завершен! надо добавить позицию в чек
            finish();
        }

        log.trace("leaving setCardNumber(String)");
    }


    /***
     * Вернёт DiscountCardAsGoodHandler для карты
     * @param cardNumber
     * @return
     * @throws CardsException
     */
    public DiscountCardAsGoodHandler getHandlerByCardNumber(String cardNumber) throws CardsException {
        return getRegistry().getHandler(cardNumber);
    }

    @Override
    public void cashDeletePosition(PositionEntity position, Date productionDate, boolean checkPermission) {
        boolean operationType = checkService.getCheck(checkService.getCurrentCheckNum()).getOperationType();
        if (operationType == PurchaseEntity.OPERATION_TYPE_SALE) {
            deactivateCard(position);
        }
        super.cashDeletePosition(position, productionDate, checkPermission);
    }

    /**
     * Удалит карту из указанной модели из списка примененных в текущем чеке.
     *
     * @param model
     *            модель, хранящая информацию о карте, что надо удалить из списка примененных
     */
    public void removeCardFromAppliedOnes(DiscountCardModel model) {
        log.trace("entering removeCardFromAppliedOnes(DiscountCardModel)");

        if (model == null || !(model.getPosition() instanceof PositionDiscountCardEntity)) {
            log.warn("leaving removeCardFromAppliedOnes(DiscountCardModel): the model does not have position!");
            return;
        }
        PositionDiscountCardEntity pos = (PositionDiscountCardEntity) model.getPosition();
        log.trace("card to removed from applied ones: \"{}\"", pos.getCardNumber());

        if (checkService != null) {
            checkService.removeCard(pos.getCardNumber(), checkService.getCurrentCheckNum());

            // погасить иконку нутренней карты, если надо
            Factory.getInstance().updateCardIcons(checkService.getCheck(checkService.getCurrentCheckNum()));
        } else {
            // не нашли CheckService. Не надо говорить об этом кассиру: бессмысленно: в след. раз будет то же самое
            log.error("Internal error: failed to locate CheckService!");
        }

        log.trace("leaving removeCardFromAppliedOnes(DiscountCardModel)");
    }

    public void deactivateCards(PurchaseEntity purchase) {
        purchase.getPositions().stream()
                .filter(PositionDiscountCardEntity.class::isInstance)
                .forEach(this::deactivateCard);
    }

    private void deactivateCard(PositionEntity position) {
        try {
            String cardNumber = Optional.ofNullable(position)
                    .filter(PositionDiscountCardEntity.class::isInstance)
                    .map(PositionDiscountCardEntity.class::cast)
                    .map(PositionDiscountCardEntity::getCardNumber)
                    .orElseThrow(() -> new IllegalStateException("No card number in position"));
            getHandlerByCardNumber(cardNumber).deactivateCard(cardNumber);
        } catch (Exception e) {
            log.error("Failed to cancel discount card position", e);
        }
    }

    /**
     * Сконвертнет указанные шаги процесса продажи ДК в понятные нам аналоги.
     *
     * @param steps
     *            шаги, в непонятном нам формате
     * @return не {@code null}: будет как минимум 2 элемента: ввод номера карты - первым элементом, и отображение финального состояния добавляемой
     *         позиции - последним элементом (или предпоследним: если последним шагом будет применение карты в чеке)
     */
    private List<DiscountCardPluginState> convert(List<DCSellSteps> steps) {
        List<DiscountCardPluginState> result = new LinkedList<>();

        if (CollectionUtils.isNotEmpty(steps)) {
            for (DCSellSteps s : steps) {
                if (s == null) {
                    continue;
                }
                switch (s) {
                    case ENTER_CARD_NUMBER:
                        result.add(DiscountCardPluginState.ENTER_CARD_NUMBER);
                        break;
                    case FILL_CLIENT_ID_DLG:
                        result.add(DiscountCardPluginState.FILL_HOLDER_APPLICATION_DLG);
                        break;
                    case ENTER_CLIENT_ID:
                        result.add(DiscountCardPluginState.ENTER_HOLDER_ID);
                        break;
                    case ACTIVATE_CARD:
                        result.add(DiscountCardPluginState.ACTIVATE_CARD);
                        break;
                    case APPLY_CARD:
                        result.add(DiscountCardPluginState.APPLY_CARD);
                        break;
                    default:
                        // не знаем (не понимаем), что это за этап - пропустим
                        log.warn("unknown tech process step: {}", s);
                        break;
                }
            } // for s
        } // if not empty

        if (result.size() < 1 || !DiscountCardPluginState.ENTER_CARD_NUMBER.equals(result.get(0))) {
            result.add(0, DiscountCardPluginState.ENTER_CARD_NUMBER);
        }

        return result;
    }

    /**
     * Реакция на событие ввода номера анкеты при продаже ДК.
     *
     * @param holderId
     *            идентификатор клиента (номер анкеты, например), для которого покупается ДК
     * @throws CardsException
     *             при различных ошибках - в т.ч., если номер анкеты просто не валиден
     */
    public void setApplicationNumber(String holderId) throws CardsException {
        // здесь опять же надо дергать определенный "карточный" процессинг - в котором делать валидацию введенных данных
        List<String> errors = getModel().getHandler().validateHolderId(holderId, getModel().getCard());
        if (CollectionUtils.isEmpty(errors)) {
            // успех!
            getModel().getCard().setHolderId(holderId);
            boolean nextStepExists = getModel().moveForward();
            if (nextStepExists && !DiscountCardPluginState.APPLY_CARD.equals(getModel().getStage())) {
                getModel().changed();
            } else {
                // техпроцесс завершаем: добавить ДК в чек:
                finish();
            }
        } else {
            // выкинем Excpetion с первым сообщением об ошибке
            throw new CardsException(errors.get(0));
        }
    }

    /**
     * Активирует карту в внешнем сервисе лояльности.
     * @throws CardsException если при активации карты произошли ошибки.
     */
    public void activateCard() throws CardsException {
        List<String> errors = this.getModel().getHandler().activateCard(getModel().getCard().getHolderId(), getModel().getCard());
        if(errors.isEmpty()) {
            finish();
        } else {
            throw new CardsException(errors.get(0));
        }
    }

    /**
     * [Успешное] завершение техпроцесса добавления позиции ДК в чек.
     */
    private void finish() {
        setOnFinishLine(true);
        try {
            finishInner();
        } finally {
            setOnFinishLine(false);
        }
    }

    private void finishInner() {
        getModel().stepsPassed();
        PositionDiscountCardEntity positionDiscountCard = getPosition();
        getAdapter().doPositionAdd(positionDiscountCard);

        // и добавим карту в чек, если надо - если последним шагом техпроцесса идет APPLY
        if (!DiscountCardPluginState.APPLY_CARD.equals(getModel().getLastProcessStep())) {
            log.trace("it is not required to apply card - according to the handler's tech process settings");
        } else {
            // добавить карту в чек возможно если выставлена настройка у продукта cardApplicable
            if (positionDiscountCard.isInstantApplicable()) {
                try {
                    // добавим только если  еще не применяли ни одну внутреннюю карту
                    addCardIfNoSuchCards();
                } catch (Exception t) {
                    // оповещение на экран кассира какое-нибудь? хотя позиция уже добавлена в чек
                    //   да можно и без оповещений: единственный Exception, что можем здесь словить: CardAlreadyAddedException
                    log.error("failed to apply card!", t);
                }
            }
        }

        // чистим модель
        cleanUp();

        // и завершаем процесс
        getAdapter().dispatchCloseEvent(true);
    }

    /**
     * Добавление продаваемой карты в чек, если в чеке нет карт того же типа
     */
    private void addCardIfNoSuchCards() throws CardAlreadyAddedException {
        if (checkService == null) {
            log.error("Internal error: failed to locate CheckService!");
            return;
        }

        DiscountCardAsGoodVO card = getModel().getCard();
        if (card == null) {
            log.error("Internal error: somehow the model lost card...");
            return;
        }

        CardTypeEntity cardType = getModel().getHandler().getCardType(card);

        if (cardType == null) {
            log.error("the card [[]] not found", getModel().getCard());
            return;
        }

        if (!cardType.isApplicableOnCheck()) {
            // карту на продаже добавили, но так получается что мы не можем добавить ее в чек из за
            // настройки на сервере, связанной с наличием анкеты у карты
            log.trace("the card [{}] will not be applied 'cause setting on server prohibit to apply on check without questionnaire", getModel().getCard());
            return;
        }

        PurchaseEntity pe = checkService.getCheck(checkService.getCurrentCheckNum());
        if (pe != null && !pe.containsCardsByType(cardType.getCardTypeEnumValue()).isEmpty()) {
            // какая-то внутренняя карта уже применена в чеке.
            //  Не следует применять "покупаемую", если уже использовали "существующую" (скорее всего, уже "прокачанную" - с большим процентом скидки)
            log.trace("the card [{}] will not be applied 'cause some other discount card was applied already", getModel().getCard());
            return;
        }

        // непосредственно добавление карты в чек
        checkService.addCard(card.getNumber(), cardType, checkService.getCurrentCheckNum(), null);
        Factory.getInstance().showCardIcon(cardType);

        log.trace("the card [{}] was applied", getModel().getCard());
    }

    /**
     * Вернем модель в исходное состояние - чтоб можно было начать добавлять в чек следующую ДК
     */
    private void cleanUp() {
        // сбросим состояние в начальное - для добавления новой карты
        getModel().moveFirst();

        // очистим этапы техпроцесса:
        getModel().getProcessSteps().clear();

        // и, вообще, обнулим модель:
        getModel().setCard(null);
    }

    /**
     * Реакция на выбор пользователя в диалоге "Будете вводить анкетные данные".
     *
     * @param fillIt
     *            что выбрал кассир: {@code true}, если решили ввести анкетные данные
     */
    public void setFillApplicationInfo(boolean fillIt) {
        boolean nextStepExists = false;
        if (fillIt) {
            // переходим к вводу анкетных данных (это один шаг вперед по техпроцессу)
            nextStepExists = getModel().moveForward();
        } else {
            // ввод анкетных данных пропускаем (2 шага вперед по техпроцессу)
            getModel().moveForward();
            nextStepExists = getModel().moveForward();
        }
        if (nextStepExists && !DiscountCardPluginState.APPLY_CARD.equals(getModel().getStage())) {
            getModel().changed();
        } else {
            // техпроцесс завершаем: добавить ДК в чек:
            finish();
        }
    }

    /**
     * Вернуться на предыдущий этап/окно техпроцесса продажи ДК.
     *
     * @return {@code false}, если были на первом экране (т.е., более "предыдущего" экрана уже нет)
     */
    public boolean back() {
        boolean result = getModel().moveBackward();

        // на экране ввода ID клиента не задержимся, если ID не вводили
        if (DiscountCardPluginState.ENTER_HOLDER_ID.equals(getModel().getStage()) &&
                (getModel().getCard() == null || StringUtils.isEmpty(getModel().getCard().getHolderId()))) {
            getModel().moveBackward();
        }

        // на экране диалога "будете вводить клиентские данные?" тоже не задерживаемся:
        if (DiscountCardPluginState.FILL_HOLDER_APPLICATION_DLG.equals(getModel().getStage())) {
            getModel().moveBackward();
        }

        if(DiscountCardPluginState.ACTIVATE_CARD.equals(getModel().getStage())) {
            getModel().moveBackward();
        }

        // при движении назад - в любом случае чистим ID картоносца
        clearHolderId();

        getModel().changed();

        return result;
    }

    /**
     * Обнулит ID картоносца в Модели - если позиция в модели уже сформирована
     */
    private void clearHolderId() {
        getModel().getCard().setHolderId(null);
    }

    /**
     * Вернет позицию, что сформировалась в результате диалога при продаже ДК - на текущий момнет времени.
     *
     * @return не {@code null}
     */
    public PositionDiscountCardEntity getPosition() {
        PositionDiscountCardEntity position = new PositionDiscountCardEntity();

        // TODO цену (2й аргумент) надо по другому определять: эта цена должна быть в модели - с формы ввода
        ProductEntity product = getModel().getProduct();
        fillDefaultPosition(BigDecimal.ONE, product.getPrice().getPriceBigDecimal(), product, position);
        position.setCardNumber(getModel().getCard().getNumber());
        position.setHolderId(getModel().getCard().getHolderId());
        position.setInstantApplicable(product.getProductConfig().getIsCardApplicable() && !product.isGift());

        return position;
    }

    /**
     * Вернет реестр.
     *
     * @return не {@code null}
     * @throws CardsException
     *             если реестр не найдет - в описании ошибки будет написано, что реестра нет
     */
    private DiscountCardAsGoodHandlersRegistry getRegistry() throws CardsException {
        if (registry == null) {
            registry = Factory.getInstance().getCardAsGoodHandlersRegistry();
        }
        if (registry == null) {
            CardsException e = new CardsException(ResBundleGoodsDiscountCard.getString("IC_AS_GOOD_HANDLERS_REGISTRY_ABSENT"));
            throw e;
        }

        return registry;
    }

    private boolean cardAlreadyAddedInThisCheck(String cardNumber) {
        return cardNumber != null && Optional.ofNullable(checkService.getCheck(checkService.getCurrentCheckNum()))
                .map(PurchaseEntity::getPositions)
                .map(List::stream)
                .orElseGet(Stream::empty)
                .filter(PositionDiscountCardEntity.class::isInstance)
                .map(PositionDiscountCardEntity.class::cast)
                .map(PositionDiscountCardEntity::getCardNumber)
                .anyMatch(cardNumber::equals);
    }

    private void checkAbilityToAddThisPosition() throws CardsException {
        try {
            PurchaseEntity purchase = checkService.getCheck(checkService.getCurrentCheckNum());
            if (purchase != null) {
                checkService.isPossibleToAddPosition(getPosition(), purchase);
            }
        } catch (PositionAddingException e) {
            throw new CardsException(e.getMessage(), e);
        }
    }

    public boolean isOnFinishLine() {
        return onFinishLine;
    }

    public void setOnFinishLine(boolean onFinishLine) {
        this.onFinishLine = onFinishLine;
    }

}
