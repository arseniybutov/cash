package ru.crystals.pos.plugins.converters;

import java.util.List;

import ru.crystals.plugins.interfaces.converters.IVersionGetter;
import ru.crystals.pos.check.PurchaseEntity;

/**
 * Created by myaichnikov on 16.03.2015.
 */

/**
 * Реализация аналогична той что мы видим в ERPI-Integration
 */
public interface IPurchasesConverter extends IVersionGetter {
    /**
     * Упаковка данных при быстрой выгрузке с кассы
     * @param entityList список чеков на выгрузку
     * @return
     */
    public byte[] convertData(List<PurchaseEntity> entityList);
}
