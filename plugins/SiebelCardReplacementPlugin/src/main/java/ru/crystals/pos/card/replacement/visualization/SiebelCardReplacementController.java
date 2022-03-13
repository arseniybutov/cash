package ru.crystals.pos.card.replacement.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.card.replacement.SiebelCardReplacementListener;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.cards.siebel.exception.SiebelServiceException;
import ru.crystals.pos.cards.siebel.results.SiebelCardResult;
import ru.crystals.siebel.SiebelCardController;

/**
 * Контроллер процесса замены карты Siebel. Управляет изменением представления на гуе и выполняет обращения к процессингу за заменой карты.
 *
 * @since 10.2.83.0
 */
public class SiebelCardReplacementController implements SiebelCardController {
    private static final Logger logger = LoggerFactory.getLogger(SiebelCardReplacementController.class);
    private SiebelCardReplacementListener listener;
    private SiebelService siebelService;
    private SiebelCardReplacementModel model;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SiebelCardReplacementController}.
     *
     * @param service  доступ к процессингу Siebel.
     * @param listener слушатель процесса замены карты.
     */
    public SiebelCardReplacementController(SiebelService service, SiebelCardReplacementListener listener) {
        this.listener = listener;
        this.siebelService = service;
    }

    /**
     * Возвращает модель данных, которой данный контроллер управляет.
     *
     * @return модель данных, которой данный контроллер управляет.
     */
    public SiebelCardReplacementModel getModel() {
        return model;
    }

    /**
     * Устанавливает модель данных, которой данный контроллер управляет.
     *
     * @param model модель данных, которой данный контроллер управляет.
     */
    public void setModel(SiebelCardReplacementModel model) {
        this.model = model;
    }

    /**
     * Вызывается при завершении процесса замены карты (успешном или неуспешном).
     */
    @Override
    public void finish() {
        listener.onReplacementComplete();
    }

    /**
     * Вызывается при завершении сканирования новой карты Siebel и готовности совершить замену.
     *
     * @param cardNumber номер новой карты.
     */
    @Override
    public void onCardNumberEntered(String cardNumber) {
        if (!siebelService.isSiebelLoyaltyCard(cardNumber)) {
            model.setState(SiebelCardReplacementModelState.INVALID_CARD_SCANNED_MESSAGE);
            return;
        }
        SiebelCardResult result;
        model.setState(SiebelCardReplacementModelState.CARD_REPLACEMENT_IN_PROGRESS);
        try {
            result = siebelService.replaceCard(model.getOldCardNumber(), cardNumber, model.getPurchase());
        } catch (SiebelServiceException ssex) {
            logger.error("Failed to replace card \"{}\" to \"{}\" in Siebel", model.getOldCardNumber(), cardNumber, ssex);
            model.setErrorText(ssex.getMessage());
            model.setState(SiebelCardReplacementModelState.ERROR_RAISED);
            return;
        }
        model.setResult(result);
        finish();
    }
}
