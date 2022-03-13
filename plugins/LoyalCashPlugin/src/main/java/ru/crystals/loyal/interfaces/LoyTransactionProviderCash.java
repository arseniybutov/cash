package ru.crystals.loyal.interfaces;

import ru.crystals.cards.CardBonusBalance;
import ru.crystals.discount.processing.entity.LoySetApiPluginTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;

public interface LoyTransactionProviderCash extends LoyTransactionProvider {

    /**
     * Поиск транзакции, соответствующей чеку {@code purchase}.
     *
     * @param purchase Чек, для которого ищется транзакция
     * @return транзакция, если она есть, null в противном случае
     */
    LoyTransactionEntity findLoyTransaction(PurchaseEntity purchase);

    /**
     * Создает транзакцию списания бонусов SetAPI
     *
     * @param purchaseCard карта, с которой списаны бонусы
     * @param balance      баланс карты
     * @param amount       сумма списания в бонусокопейках
     * @param txId         id транзакции списания
     * @return транзакция SetAPI
     */
    LoySetApiPluginTransactionEntity createSetApiTransaction(PurchaseCardsEntity purchaseCard, CardBonusBalance balance,
                                                             long amount, String txId);

}
