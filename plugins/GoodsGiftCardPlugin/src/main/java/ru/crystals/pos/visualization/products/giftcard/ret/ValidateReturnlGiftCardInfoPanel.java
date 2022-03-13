package ru.crystals.pos.visualization.products.giftcard.ret;

import javax.swing.JPanel;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleRectangle;
import ru.crystals.pos.visualizationtouch.components.ElementFactory;
import ru.crystals.pos.visualizationtouch.components.ScaleLayout;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

/**
 *
 * @author dalex
 */
public class ValidateReturnlGiftCardInfoPanel extends JPanel {

    private Label jNameLabel = new Label(ResBundleGoodsGiftCard.getString("RETURN_GIFT_CARD"));

    private Label welcomeText = new Label(String.format(ResBundleGoodsGiftCard.getString("PLEACE_SCAN_RETURN_GIFT_CARD"),""));
    private Label giftCardCount = new Label("");
    private Label warningLabel = new Label("");

    public ValidateReturnlGiftCardInfoPanel() {
        this.setLayout(new ScaleLayout());
        this.setBackground(Color.greyBackground);

        Style.setNameLabelStyle(jNameLabel);
        jNameLabel.setAligmentY(ElementFactory.AligmentY.Y_ALIGMENT_CENTER);
        this.add(jNameLabel, new ScaleRectangle(10, 10, 620, 60));

        Style.setDialogLabelStyle(welcomeText);
        this.add(welcomeText, new ScaleRectangle(10, 120, 620, 30));

        Style.setDialogLabelStyle(giftCardCount);
        this.add(giftCardCount, new ScaleRectangle(10, 150, 620, 30));

        Style.setDialogLabelStyle(warningLabel);
        this.add(warningLabel, new ScaleRectangle(10, 190, 620, 40));
    }

    public void setCardsCount(int count, int ofCount) {
        if (count == -1) {
            giftCardCount.setText("");
        } else {
            giftCardCount.setText(String.format(ResBundleGoodsGiftCard.getString("EXPECTED_CARD_COUNT"), count, ofCount));
        }
    }

    public void showWarning(String text) {
        if (text != null && text.length() > 0) {
            Factory.getTechProcessImpl().error(text);
            warningLabel.setIcon(IconStyle.getImageIcon(IconStyle.WARNING));
            warningLabel.setText(text);
        } else {
            warningLabel.setIcon(null);
            warningLabel.setText("");
        }
    }
}
