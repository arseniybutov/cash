package ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.view;

import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.presentcards.PresentCardInformationVO;
import ru.crystals.pos.cards.PresentCardToString;
import ru.crystals.pos.listeners.XKeyListener;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonHeaderPanel;
import ru.crystals.pos.visualization.products.giftcard.GiftCardHelper;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo.controller.GiftCardInfoController;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualization.utils.ScaleFont;
import ru.crystals.pos.visualizationtouch.components.buttons.ButtonSimple;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;

/**
 * Отображает информацию о подарочной карте.
 */
public class ShowCardDataForm extends JPanel implements XKeyListener {
    private final JLabel cardNumberValue = new JLabel();
    private final JLabel amountValue = new JLabel();
    private final JLabel balanceValue = new JLabel();
    private final JLabel statusValue = new JLabel();
    private final JLabel activateDateValue = new JLabel();
    private final JLabel expirationDateValue = new JLabel();
    private final JLabel shopIndexValue = new JLabel();
    private final JLabel cashValue = new JLabel();
    private final ButtonSimple closeButton = new ButtonSimple(ResBundleGoodsGiftCard.getString("CLOSE"));
    private final ButtonSimple printButton = new ButtonSimple(ResBundleGoodsGiftCard.getString("PRINT_UPPER_CASE"));
    private GiftCardInfoController controller;

    public ShowCardDataForm() {
        this.setLayout(new BorderLayout(Scale.getX(16), Scale.getY(16)));
        this.setBackground(Color.greyBackground);
        CommonHeaderPanel headerPanel = new CommonHeaderPanel(ResBundleGoodsGiftCard.getString("GIFT_CARD_INFO"));
        add(headerPanel, BorderLayout.NORTH);
        add(initGridLayoutPanel(), BorderLayout.CENTER);
    }

    private JPanel initGridLayoutPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(Scale.getX(16), Scale.getY(16), Scale.getX(16), Scale.getY(16)));
        panel.setLayout(new GridLayout(9, 2, Scale.getX(8), Scale.getX(8)));
        panel.setBackground(Color.greyBackground);

        JLabel cardNumber = new JLabel(ResBundleGoodsGiftCard.getString("CARD_NUMBER") + ":");
        Style.setLabelNameStyle(cardNumber);
        Style.setLabelValueStyle(cardNumberValue);
        panel.add(cardNumber);
        panel.add(cardNumberValue);

        JLabel amount = new JLabel(ResBundleGoodsGiftCard.getString("CARD_AMOUNT") + ":");
        Style.setLabelNameStyle(amount);
        Style.setLabelValueStyle(amountValue);
        panel.add(amount);
        panel.add(amountValue);

        JLabel balance = new JLabel(ResBundleGoodsGiftCard.getString("CARD_BALANCE") + ":");
        Style.setLabelNameStyle(balance);
        Style.setLabelValueStyle(balanceValue);
        panel.add(balance);
        panel.add(balanceValue);

        JLabel status = new JLabel(ResBundleGoodsGiftCard.getString("STATUS") + ":");
        Style.setLabelNameStyle(status);
        Style.setLabelValueStyle(statusValue);
        panel.add(status);
        panel.add(statusValue);

        JLabel activateDate = new JLabel(ResBundleGoodsGiftCard.getString("ACTIVATE_DATE") + ":");
        Style.setLabelNameStyle(activateDate);
        Style.setLabelValueStyle(activateDateValue);
        panel.add(activateDate);
        panel.add(activateDateValue);

        JLabel expirationDate = new JLabel(ResBundleGoodsGiftCard.getString("EXPIRATION_DATE") + ":");
        Style.setLabelNameStyle(expirationDate);
        Style.setLabelValueStyle(expirationDateValue);
        panel.add(expirationDate);
        panel.add(expirationDateValue);

        JLabel shopIndex = new JLabel(ResBundleGoodsGiftCard.getString("SHOP") + ":");
        Style.setLabelNameStyle(shopIndex);
        Style.setLabelValueStyle(shopIndexValue);
        panel.add(shopIndex);
        panel.add(shopIndexValue);

        JLabel cash = new JLabel(ResBundleGoodsGiftCard.getString("CASH") + ":");
        Style.setLabelNameStyle(cash);
        Style.setLabelValueStyle(cashValue);
        panel.add(cash);
        panel.add(cashValue);

        selectPrintButton();//оформление
        selectCloseButton();


        Font font;
        this.closeButton.setPreferredSize(new ScaleDimension(180, 30));
        font = this.closeButton.getFont();
        closeButton.setFont(ScaleFont.decorate(font));
        this.printButton.setPreferredSize(new ScaleDimension(180, 30));
        font = this.printButton.getFont();
        printButton.setFont(ScaleFont.decorate(font));

        panel.add(closeButton);
        panel.add(printButton);

        new XListenerAdapter(this);

        return panel;
    }

    public void setCardInformation(PresentCardInformationVO cardInformation) {
        cardNumberValue.setText(cardInformation.getCardNumber());
        amountValue.setText(PresentCardToString.currencyToString(cardInformation.getAmount()));
        balanceValue.setText(PresentCardToString.currencyToString(cardInformation.getBalance()));
        statusValue.setText(GiftCardHelper.statusToString(cardInformation));
        if (cardInformation.getStatus() == CardStatus.Create) {
            activateDateValue.setText("");
            expirationDateValue.setText("");
        } else {
            activateDateValue.setText(PresentCardToString.dateToString(cardInformation.getActivationDate()));
            expirationDateValue.setText(PresentCardToString.dateToString(cardInformation.getExpirationDate()));
        }
    }

    public void setShopIndex(Long shopIndex) {
        shopIndexValue.setText(String.valueOf(shopIndex));
    }

    public void setCashNum(Long cash) {
        cashValue.setText(String.valueOf(cash));
    }

    private void selectPrintButton() {
        printButton.setSelected(true);
        closeButton.setSelected(false);
        printButton.repaint();
        closeButton.repaint();
    }

    private void selectCloseButton() {
        printButton.setSelected(false);
        closeButton.setSelected(true);
        printButton.repaint();
        closeButton.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                selectCloseButton();
                break;
            }
            case KeyEvent.VK_RIGHT: {
                selectPrintButton();
                break;
            }
            case KeyEvent.VK_ENTER: {
                if (closeButton.isSelected()) {
                    controller.exit();
                } else {
                    selectCloseButton();
                    controller.printGiftCard();
                }
                break;
            }
            case KeyEvent.VK_ESCAPE: {
                selectCloseButton();
                controller.exit();
                break;
            }
        }
    }

    public void setController(GiftCardInfoController controller) {
        this.controller = controller;
    }
}
