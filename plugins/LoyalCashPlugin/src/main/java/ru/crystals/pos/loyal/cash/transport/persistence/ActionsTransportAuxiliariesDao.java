package ru.crystals.pos.loyal.cash.transport.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Queue;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.pos.loyal.DiscountGUIDEntity;
import ru.crystals.pos.loyal.LastDiscountIDEntity;

/**
 * Через реализации этого интерфейса будем CRUD'ить (Create-Read-Update-Delete) "вспомогательные" сущности при импорте РА (рекламных Акций):
 * сущностей, что хранят некую информацию о ходе процесса - например: {@link LastDiscountIDEntity идентификатор последнего импортированного файла с
 * объектами лояльности} и {@link DiscountGUIDEntity GUID РА, об импорте которой надо отчитаться перед сервером}.
 * 
 * @author aperevozchikov
 */
public interface ActionsTransportAuxiliariesDao {

    /**
     * вернет {@link LastDiscountIDEntity инфу о последнем успешно импортированном файле с объектами (РА + настройки) лояльности}.
     * 
     * @return <code>null</code>, если нет такой информации в БД: если на этой кассе еще ни разу не получали объекты лояльности от сервера, например
     */
    LastDiscountIDEntity getLastDiscountId();

    /**
     * Сохранит указанную {@link LastDiscountIDEntity инфу о последнем успешно импортированном файле с объектами (РА + настройки) лояльности} в БД и
     * вернет результат сохранения.
     * <p/>
     * Implementation Note: перед сохранением из БД будут удалены ВСЕ {@link LastDiscountIDEntity}!
     * 
     * @param lDiscountId
     *            инфа, что надо сохранить в БД
     * @return <code>null</code>, если операция не удалась по любой причине (например, аргумент невалиден)
     */
    LastDiscountIDEntity saveLastDiscountId(LastDiscountIDEntity lDiscountId);

    /**
     * Вернет список {@link AdvertisingActionEntity#getGuid() GUID'ов} РА, что были успешно импротированы и сохранены в БД, но об этом успехе не
     * удалось доложить серверу (для фидбэка).
     * <p/>
     * Implementation Note: фактически делает что-то похожее на {@link Queue#poll() извлечь и удалить}: извлеченная информация из БД будет удалена!
     * (FIXME: х.з. почему так было реализовано)
     * 
     * @return никогда не вернет <code>null</code> - в крайнем случае вернет пустую коллекцию
     */
    List<Long> getProcessedActionsGuids();

    /**
     * Сохранит в БД информацию о том, что {@link AdvertisingActionEntity РА} с указанными {@link AdvertisingActionEntity#getGuid() GUID'ами} были
     * успешно импортированы и сохраннены в БД.
     * 
     * @param actionsGuids
     *            {@link AdvertisingActionEntity#getGuid() GUID'ы} РА, об успешном импорте которых надо сделать запись в БД
     * @return количество сохраненных в БД записей - для фидбэка: если отличается от {@link Collection#size() размерности} аргумента - значит в
     *         аргументе были либо повторяющиеся, либо невалидные значения.
     */
    int saveDiscounts(Collection<Long> actionsGuids);
}
