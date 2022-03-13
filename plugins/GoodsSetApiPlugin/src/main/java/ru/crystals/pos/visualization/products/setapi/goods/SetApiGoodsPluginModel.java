package ru.crystals.pos.visualization.products.setapi.goods;

import ru.crystals.pos.api.plugin.goods.NewLineItem;
import ru.crystals.pos.visualization.commonplugin.model.CommonProductPluginModel;

import java.util.HashMap;
import java.util.Map;

/**
 * По факту содержит состояние автомата "Продажа плагинного товара": собственно описание самого продаваемого объекта,
 * а также этап техпроцесса продажи (добавления этого товара в чек: проверка возраста (при необходимости),
 * диалоги в плагинном модуле, добавление в чек).
 *
 * @author aperevozchikov
 */
public class SetApiGoodsPluginModel extends CommonProductPluginModel {

    /**
     * Текущее состояние техпроцесса
     */
    private SetApiGoodsPluginState addState = SetApiGoodsPluginState.START;

    /**
     * Идентификатор товарного плагина, чью позицию пытаемся добавить в чек.
     * На самом деле - пожалуй, только чисто ради логов. Наверно, в логике не понадобится.
     */
    private String pluginId;

    /**
     * [уже сформированная в плагине] Позиция, что собираемся добавить в чек
     */
    private NewLineItem lineItem;

    /**
     * Минимальный возраст, которого надо достичь для того, чтобы иметь право приобрести этот товар. В годах
     */
    private int minAge;

    /**
     * Сообщение об ошибке, что надо показать при негативном завершении сценария "продажа товара"
     */
    private String errorMessage;

    /**
     * Расширенные атрибуты для сохранения в чеке.
     */
    private Map<String, String> extendedReceiptAttributes;

    @Override
    public String toString() {
        return String.format("set-api-goods-plugin-model [plugin-id: \"%s\"; current state: %s; line-item: %s; min-age: %s]",
                getPluginId(), getAddState(), getLineItem(), getMinAge());
    }

    public SetApiGoodsPluginState getAddState() {
        return addState;
    }

    public void setAddState(SetApiGoodsPluginState addState) {
        this.addState = addState;
    }

    public String getPluginId() {
        return pluginId;
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public NewLineItem getLineItem() {
        return lineItem;
    }

    public void setLineItem(NewLineItem lineItem) {
        this.lineItem = lineItem;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getExtendedReceiptAttributes() {
        if (extendedReceiptAttributes == null) {
            extendedReceiptAttributes = new HashMap<>();
        }
        return extendedReceiptAttributes;
    }

    public void setExtendedReceiptAttributes(Map<String, String> extendedReceiptAttributes) {
        this.extendedReceiptAttributes = extendedReceiptAttributes;
    }
}
