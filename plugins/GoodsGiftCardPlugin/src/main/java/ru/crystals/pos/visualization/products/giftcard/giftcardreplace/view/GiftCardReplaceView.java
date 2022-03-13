package ru.crystals.pos.visualization.products.giftcard.giftcardreplace.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bl.keylisteners.EscKeyListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonErrorPanel;
import ru.crystals.pos.visualization.components.MainWindow;
import ru.crystals.pos.visualization.components.WaitComponent;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.controller.GiftCardReplaceController;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.model.GiftCardReplaceModel;
import ru.crystals.pos.visualization.products.giftcard.giftcardreplace.model.GiftCardReplaceModelListener;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.view.CardNumberEnterForm;

/**
 * Представление для сценария "Замена подарочной карты на кассе".
 */
public class GiftCardReplaceView implements GiftCardReplaceModelListener {
    private static final Logger LOG = LoggerFactory.getLogger(GiftCardReplaceView.class);
    private static final String GIFT_CARD_REPLACE = ResBundleGoodsGiftCard.getString("GIFT_CARD_REPLACE");

    private final CardNumberEnterForm cardNumberEnterForm = new CardNumberEnterForm(GIFT_CARD_REPLACE);
    private final WaitComponent waitForm = new WaitComponent(GIFT_CARD_REPLACE);
    private final CommonErrorPanel errorForm = new CommonErrorPanel(GIFT_CARD_REPLACE);
    private final ExitEscListener exitEscListener = new ExitEscListener();

    public GiftCardReplaceView(final GiftCardReplaceController controller) {
        exitEscListener.setController(controller);
        cardNumberEnterForm.setEscListener(exitEscListener);
    }

    @Override
    public void modelChanged(GiftCardReplaceModel model) {
        LOG.debug("Получено событие \"Модель изменилась\"");
        MainWindow mainWindow = Factory.getInstance().getMainWindow();
        switch (model.getState()) {
            case CARD_NUMBER_ENTER: {
                cardNumberEnterForm.setCardNumber(model.getCurrentCardNumber());
                cardNumberEnterForm.setEnterListener(model.getEnterListener());
                cardNumberEnterForm.setHintText(model.getMessage());
                cardNumberEnterForm.setHandEnterEnable(model.isHandEnterEnabled());
                mainWindow.setCurrentContainer(cardNumberEnterForm);
                break;
            }
            case WAIT: {
                waitForm.setEscListener(exitEscListener);
                waitForm.setActionStatusText(model.getMessage());
                mainWindow.setCurrentContainer(waitForm);
                break;
            }
            case SHOW_ERROR: {
                errorForm.setMessageText(model.getMessage());
                errorForm.setListener(model.getErrorListener());
                mainWindow.setCurrentContainer(errorForm);
                break;
            }
            default: {
                throw new IllegalArgumentException(String.format("Unsupported model state: %s", model.getState()));
            }
        }
    }

    class ExitEscListener implements EscKeyListener {
        private GiftCardReplaceController controller;

        public void setController(GiftCardReplaceController controller) {
            this.controller = controller;
        }

        @Override
        public void esc() {
            controller.exit();
        }
    }
}
