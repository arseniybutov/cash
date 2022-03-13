package ru.crystals.pos.loyal.cash.maintenance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.loyal.cash.persistence.LoyTxDao;
import ru.crystals.pos.property.Properties;

/**
 * Поток, удаляющий устаревшие TX лояльности.
 *
 * @author aperevozchikov
 */
public class LoyTxCleanerWorkhorse implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LoyTxCleanerWorkhorse.class);

    /**
     * Максимальное количество TX лояльности, что можно удалить за одну итерацию
     */
    private int maxRecordsToRemoveAtOnce;

    /**
     * Номер магазина. которой принадлежит данная касса
     */
    private Long shopNo = null;

    /**
     * номер данной кассы
     */
    private Long cashNo = null;

    /**
     * Количество последних смен, документы котрых должны оставаться в оперативном справочнике (не удалять с кассы).
     */
    private Long shiftsToKeep = null;

    /**
     * INN основного юрлица, которому принадлежит "первый" фискальник данной кассы
     */
    private String inn;

    /**
     * DAO, через который и будем вызывать удаление старых TX лояльности
     */
    private LoyTxDao dao;

    /**
     * Настройки кассы
     */
    private Properties prop;

    LoyTxCleanerWorkhorse(LoyTxDao dao, Properties prop, int maxRecordsToRemoveAtOnce) {
        this.dao = dao;
        this.prop = prop;
        this.maxRecordsToRemoveAtOnce = maxRecordsToRemoveAtOnce;
    }

    @Override
    public void run() {
        log.trace("entering run()");

        Long shop = getShopNo();
        Long cash = getCashNo();
        Long keep = getShiftsToKeep();
        String primaryInn = getInn();

        if (shop == null || cash == null) {
            log.error("leaving run(): the cash location is unknown [shop-no: {}; cash-no: {}]", shop, cash);
            return;
        }

        if (keep == null || keep <= 0) {
            log.trace("leaving run(): seems the feature (trim-off superfluous docs) is disabled. shifts-to-keep: {}", keep);
            return;
        }

        LoyTxDao ltd = getDao();
        if (ltd == null) {
            log.error("leaving run(): failed to locate <LoyTxDao>");
            return;
        }

        ltd.removeStaleLoyTxes(keep, getMaxRecordsToRemoveAtOnce(), cash, shop, primaryInn);

        log.trace("leaving run()");
    }

    public int getMaxRecordsToRemoveAtOnce() {
        return maxRecordsToRemoveAtOnce;
    }

    public Long getShopNo() {
        if (shopNo == null) {
            shopNo = prop == null ? null : prop.getShopIndex() == null ? null : prop.getShopIndex();
            log.info("shopNo: {}", shopNo);
        }

        return shopNo;
    }

    public Long getCashNo() {
        if (cashNo == null) {
            cashNo = prop == null ? null : prop.getCashNumber() == null ? null : prop.getCashNumber();
            log.info("cashNo: {}", cashNo);
        }

        return cashNo;
    }

    public Long getShiftsToKeep() {
        if (shiftsToKeep == null) {
            shiftsToKeep = prop == null ? null : prop.getOperDaysStore() == null ? null : prop.getOperDaysStore().longValue();
            log.info("shiftsToKeep: {}", shiftsToKeep);
        }

        return shiftsToKeep;
    }

    public String getInn() {
        if (inn == null) {
            inn = prop == null ? null : prop.getShopINN();
            log.info("inn: {}", inn);
        }

        return inn;
    }

    public LoyTxDao getDao() {
        return dao;
    }
}
