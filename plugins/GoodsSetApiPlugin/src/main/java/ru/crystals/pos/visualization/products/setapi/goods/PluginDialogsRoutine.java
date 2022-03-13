package ru.crystals.pos.visualization.products.setapi.goods;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.api.commons.AddForRefundRequestEntity;
import ru.crystals.api.commons.AddForSaleRequestEntity;
import ru.crystals.api.commons.MerchandiseBuilder;
import ru.crystals.api.commons.ReceiptPurchaseEntityWrapper;
import ru.crystals.api.commons.SetApiActionsProcessor;
import ru.crystals.pos.api.ext.loyal.dto.AddForRefundResult;
import ru.crystals.pos.api.ext.loyal.dto.AddForSaleResult;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.CashOperation;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.spi.plugin.goods.AddForRefundCallback;
import ru.crystals.pos.spi.plugin.goods.AddForSaleCallback;
import ru.crystals.pos.spi.plugin.goods.InvalidLineItemException;
import ru.crystals.pos.spi.receipt.Merchandise;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.crystals.pos.spi.receipt.ReceiptType;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.setapi.goods.i18n.ResBundleSetApiGoods;

import java.util.Map;

/**
 * Инкапсулирует механизм манипуляции интерфейсом плагинами товаров.
 */
class PluginDialogsRoutine {
    private static final Logger logger = LoggerFactory.getLogger(PluginDialogsRoutine.class);
    private TechProcessInterface techProcess;

    public PluginDialogsRoutine(TechProcessInterface techProcess) {
        this.techProcess = techProcess;
    }

    /**
     * Инициирует добавление позиции в чек при помощи плагина товаров Set API.<br>
     * На этом этапе управление передаётся плагину и он может манипулировать UI кассы.
     * @param view вьюха, на которой совершаются гуёвые операции
     * @param model модель, описывающая текущее состояние чека и добавления позиции в него
     * @param controller контроллер, который всем этим управялет.
     */
    public void perform(SetApiGoodsPluginView view, SetApiGoodsPluginModel model, SetApiGoodsPluginController controller) {

        Receipt receipt = new ReceiptPurchaseEntityWrapper(getPurchase(controller));

        ProductEntity product = model.getProduct();
        Merchandise me = MerchandiseBuilder.createMerchandise(product);
        view.setupPluginViewPanel(product.getDiscriminator());

        if(receipt.getType() == ReceiptType.REFUND) {
            performOnRefund(view, model, controller, receipt, me);
        } else {
            performOnSale(view, model, controller, receipt, me);
        }
    }

    private void performOnSale(SetApiGoodsPluginView view, SetApiGoodsPluginModel model, SetApiGoodsPluginController controller,
            Receipt receipt, Merchandise merchandise) {
        try {
            view.goodsPluginDescriptor.getPlugin().addForSale(new AddForSaleRequestEntity(receipt, merchandise, new AddForSaleCallback() {
                @Override
                public void completed(AddForSaleResult result) throws InvalidLineItemException {
                    controller.saveExtendedReceiptAttributes(result.getExtendedReceiptAttributes());
                    controller.getModel().setLineItem(result.getNewLineItem());
                    // проверка теоретической возможности добавить эту позицию в чек
                    controller.testLineItemToSell();
                    if (ru.crystals.pos.spi.receipt.CashOperation.EXPENSE.equals(receipt.getCashOperation())) {
                        model.setState(ProductContainer.ProductState.EXPENSE);
                    }
                    controller.getModel().setAddState(SetApiGoodsPluginState.FINISH);
                    controller.getModel().changed();

                    SetApiActionsProcessor.processActions(techProcess, result.getActionsToTake());
                }

                @Override
                public void notCompleted(AddForSaleResult result) {
                    controller.saveExtendedReceiptAttributes(result.getExtendedReceiptAttributes());
                    controller.finishSale(false);
                }
            } ));
        } catch (Exception ex) {
            logger.error("An error has occurred during plugin invocation", ex);
            Factory.getTechProcessImpl().error(ResBundleSetApiGoods.getString("set.api.goods.add.position.error"));
            controller.finishSale(false);
        }
    }


    private void performOnRefund(SetApiGoodsPluginView view, SetApiGoodsPluginModel model, SetApiGoodsPluginController controller, Receipt receipt, Merchandise merchandise) {
        try {
            /*
             * Здесь есть подводный камень: плагин *должен* показать какой-нибудь гуй, реагирующий на клавиатуру, иначе касса не возвращается в режим добавления
             * товаров, а отображает форму-подложку, если она была. Тогда придётся руками нажать "Отмена", чтобы её убрать.
             * Такое происходит в следующем сценарии:
             * - переходим в произвольный возврат;
             * - добавляем товар;
             * - плагин товаров немедленно возвращает управление, не показывая UI;
             * - касса не сменила визуальное состояние обратно на "добавление товаров";
             * - нажимает "Отмена", товар добавляется, касса переходит в добавление товаров;
             * - последующие добавления выполняются успешно.
             * Это справедливо и для режима продажи. Сделать с этим что-нибудь сейчас не хотим^W можем.
             */
            view.goodsPluginDescriptor.getPlugin().addForRefund(new AddForRefundRequestEntity(receipt, merchandise, new AddForRefundCallback() {
                @Override
                public void completed(AddForRefundResult result) throws InvalidLineItemException {
                    controller.saveExtendedReceiptAttributes(result.getExtendedReceiptAttributes());
                    controller.getModel().setLineItem(result.getNewLineItem());
                    // проверка теоретической возможности добавить эту позицию в чек
                    controller.testLineItemToSell();
                    model.setState(ProductContainer.ProductState.REFUND);
                    controller.getModel().setAddState(SetApiGoodsPluginState.FINISH);
                    controller.getModel().changed();
                }

                @Override
                public void notCompleted(AddForRefundResult result) {
                    controller.saveExtendedReceiptAttributes(result.getExtendedReceiptAttributes());
                    controller.finishRefund(false);
                }
            } ));
        } catch (Exception ex) {
            logger.error("An error has occurred during plugin invocation", ex);
            Factory.getTechProcessImpl().error(ResBundleSetApiGoods.getString("set.api.goods.add.position.error"));
            controller.finishRefund(false);
        }
    }

    /**
     * Вернет {@code true}, если текущий чек - это чек возврата (или если чека еще нет, но находимся
     * в окне редактирования чека возврата - тогда наша позиция будет первой в возвратном чеке).
     * @return true если текущий чек является чеком возврата и false в противном случае.
     */
    private boolean isReturnPurchase() {
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        if (purchaseEntity != null) {
            return purchaseEntity.isReturn();
        } else {
            return Factory.getInstance().getMainWindow().getCheckContainer().getState() == CheckState.RETURN_CHECK;
        }
    }

    private boolean isExpenseState(SetApiGoodsPluginController controller) {
        return ProductContainer.ProductState.EXPENSE.equals(controller.getAdapter().getProductState());
    }

    private PurchaseEntity getPurchase(SetApiGoodsPluginController controller) {
        PurchaseEntity purchase = Factory.getTechProcessImpl().getCheckOrNextCheckStub(!isReturnPurchase());

        if (purchase.getPositions().isEmpty()) {
            purchase.setCashOperation(isExpenseState(controller) ? CashOperation.EXPENSE : CashOperation.INCOME);
        }

        if (MapUtils.isNotEmpty(controller.getModel().getExtendedReceiptAttributes())) {
            for (Map.Entry<String, String> attribute : controller.getModel().getExtendedReceiptAttributes().entrySet()) {
                purchase.addExtData(attribute.getKey(), attribute.getValue());
            }
        }

        return purchase;
    }
}
