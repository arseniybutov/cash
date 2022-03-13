package ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.view;

import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bl.keylisteners.EscKeyListener;
import ru.crystals.pos.listeners.XKeyListener;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonErrorPanel;
import ru.crystals.pos.visualization.components.MainWindow;
import ru.crystals.pos.visualization.components.WaitComponent;
import ru.crystals.pos.visualization.menu.commands.cards.CardInfoFormListener;
import ru.crystals.pos.visualization.menu.commands.cards.CardNumberInputForm;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.controller.GiftCardInfoController;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.model.GiftCardInfoModel;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.model.GiftCardInfoModelListener;

/**
 * Представление для сценария "Информация по подарочной карте".
 */
public class GiftCardInfoView implements GiftCardInfoModelListener {
    private static final String GIFT_CARD_INFO = ResBundleGoodsGiftCard.getString("GIFT_CARD_INFO");
    private final CommonErrorPanel connectionErrorForm = new CommonErrorPanel(GIFT_CARD_INFO);
    private final CardNumberInputForm enterCardNumberForm;
    private final WaitComponent loadingDataFromCentrumForm;
    private final ShowCardDataForm showCardDataForm = new ShowCardDataForm();
    private final WaitComponent printingCardDataForm;
    private final JPanel showCentrumErrorForm = null;

    public GiftCardInfoView(final GiftCardInfoController controller, final TechProcessInterface techProcess) {
        CardInfoFormListener listener = new CardInfoFormListener() {
            @Override
            public void enterCardEvent(String number) {
                if (StringUtils.isNotEmpty(number)) {
                    controller.enterGiftCardNumber(number, false);
                } else {
                    techProcess.error("Empty card number");
                }
            }

            @Override
            public void enterCardEvent(String track1, String track2, String track3, String track4) {
                if (StringUtils.isNotEmpty(track2)) {
                    controller.enterGiftCardNumber(track2, true);
                } else {
                    techProcess.error("Empty card number");
                }
            }

            @Override
            public void close() {
                controller.exit();
            }

            @Override
            public void printInfo() {
            }
        };
        enterCardNumberForm = new CardNumberInputForm(listener, GIFT_CARD_INFO, ResBundleVisualization.getString("ENTER_CARD_NUMBER"), true);

        loadingDataFromCentrumForm = new WaitComponent(ResBundleGoodsGiftCard.getString("DATA_REQUEST"), new EscKeyListener() {
            @Override
            public void esc() {
                controller.exit();
            }
        });
        loadingDataFromCentrumForm.setHeaderText(GIFT_CARD_INFO);

        printingCardDataForm = new WaitComponent(ResBundleGoodsGiftCard.getString("PRINT"));
        printingCardDataForm.setHeaderText(GIFT_CARD_INFO);

        connectionErrorForm.setListener(e -> controller.exit());

        showCardDataForm.setController(controller);
    }

    @Override
    public void changedEvent(GiftCardInfoModel model) {
        MainWindow mainWindow = Factory.getInstance().getMainWindow();

        switch (model.getState()) {
            case CARD_NUMBER_ENTER: {
                enterCardNumberForm.reset();
                mainWindow.setCurrentContainer(enterCardNumberForm);
                break;
            }
            case LOADING_CARD_DATA_FROM_CENTRUM: {
                mainWindow.setCurrentContainer(loadingDataFromCentrumForm);
                break;
            }
            case LOADING_DATA_ERROR: {
                connectionErrorForm.setMessageText(model.getErrorMessage());
                mainWindow.setCurrentContainer(connectionErrorForm);
                break;
            }
            case SHOW_CARD_DATA: {
                showCardDataForm.setCardInformation(model.getCardInformation());
                showCardDataForm.setShopIndex(model.getShopIndex());
                showCardDataForm.setCashNum(model.getCashNum());
                mainWindow.setCurrentContainer(showCardDataForm);
                break;
            }
            case PRINTING_CARD_DATA: {
                mainWindow.setCurrentContainer(printingCardDataForm);
                break;
            }
            case SHOW_SERVER_ERROR: {
                mainWindow.setCurrentContainer(showCentrumErrorForm);
                break;
            }
        }
    }
}