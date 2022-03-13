package ru.crystals.pos.loyal.cash.service;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.calculation.AdvertisingActionUtils;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.service.AdvResultPersistenceManager;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;

/**
 * @author Anton Martynov amartynov@crystals.ru
 */
public class CashAdvResultPersistenceManager extends AdvResultPersistenceManager {
    
    /**
     * черный ящик
     */
    private Logger log = LoggerFactory.getLogger(CashAdvResultPersistenceManager.class);
    
    /**
     * Через эту штуку будем сохранять/читать {@link LoyTransactionEntity TX лояльности} в/из БД.
     */
    private LoyTxDao loyTxDao;

    /**
     * Список активных рекламных акций, которые могут применяться в чеке
     */
    private AdvertisingActionEntity[] actions;

    /**
     * Конструктор менеджера сохранения результатов РА
     * 
     * @param loyTxDao
     *            инструмент для сохранения TX лояльности в БД
     * @param actions
     *            список активных рекламных акций, которые могут применяться в чеке
     */
    public CashAdvResultPersistenceManager(LoyTxDao loyTxDao, AdvertisingActionEntity[] actions) {
        this.loyTxDao = loyTxDao;
        this.actions = actions;
    }

    @Override
    public LoyTransactionEntity saveOrUpdate(LoyTransactionEntity loyTransaction, boolean sendToCentrum) {
        LoyTransactionEntity result;
        long stopWatch = System.currentTimeMillis();
        
        log.trace("entering saveOrUpdate(LoyTransactionEntity). The arguments are: loyTransaction [{}]", loyTransaction);
        
        result = loyTxDao.saveLoyTx(loyTransaction);
        
        log.trace("leaving saveOrUpdate(LoyTransactionEntity). The result is: {}; it took {} [ms]", 
            result, System.currentTimeMillis() - stopWatch);
        
        return result;
    }

    @Override
    protected LoyAdvActionInPurchaseEntity findOrCreateAdvActionByGuid(Long actionGuid) {
        if (actionGuid == null) {
            return null;
        }

        LoyAdvActionInPurchaseEntity advAction = loyTxDao.getLoyAdvActionByGuid(actionGuid);
        if (advAction == null) {
            for (AdvertisingActionEntity action : actions) {
                if (actionGuid.equals(action.getGuid())) {
                    advAction = new LoyAdvActionInPurchaseEntity();
                    advAction.setGuid(action.getGuid());
                    advAction.setActionName(action.getName());                
                    advAction.setActionType(AdvertisingActionUtils.getType(action));
                    advAction.setApplyMode(action.getMode());
                    advAction.setExternalCode(action.getExternalCode());
                    advAction.setDiscountType(action.getDiscountType());

                    advAction = loyTxDao.saveLoyAdvAction(advAction);

                    break;
                }
            }
        }

        return advAction;
    }

    @Override
    protected boolean checkTransaction(Purchase purchase) {
        return true;
    }
}
