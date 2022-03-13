package ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo;

import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.controller.GiftCardInfoController;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.model.GiftCardInfoModel;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.view.GiftCardInfoView;

/**
 * Сценарий "Информация по подарочной карте".
 */
public class GiftCardInfoScenario {
    private final GiftCardInfoModel model = new GiftCardInfoModel();
    private final GiftCardInfoController controller = new GiftCardInfoController();

    public GiftCardInfoScenario() {
        controller.setModel(model);
        GiftCardInfoView view = new GiftCardInfoView(controller, Factory.getTechProcessImpl());
        model.addListener(view);
    }

    public void execute() {
        model.setShopIndex(Factory.getTechProcessImpl().getShift().getShopIndex());
        model.setCashNum(Factory.getTechProcessImpl().getShift().getCashNum());
        controller.askGiftCardNumber();
    }

}
