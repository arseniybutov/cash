package ru.crystals.pos.visualization.products.giftcard.ret;

import javax.swing.JPanel;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.products.giftcard.ResBundleGoodsGiftCard;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.IconStyle;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleRectangle;
import ru.crystals.pos.visualizationtouch.components.ScaleLayout;
import ru.crystals.pos.visualizationtouch.components.XFont;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

/**
 *
 * @author dalex
 */
public class ValidateReturnlGiftCardInfoPanelShort extends JPanel {

    private Label welcomeText = new Label();
    private Label warningLabel = new Label("");

    public ValidateReturnlGiftCardInfoPanelShort() {
        this.setLayout(new ScaleLayout());
        this.setBackground(Color.greyBackground);
        this.setOpaque(true);

        Style.setDialogLabelStyle(welcomeText);
        welcomeText.setFont(new XFont(MyriadFont.getItalic(26F), 1.0f));
        this.add(welcomeText, new ScaleRectangle(16, 10, 620, 60));

        Style.setDialogLabelStyle(warningLabel);
        warningLabel.setFont(new XFont(MyriadFont.getItalic(26F), 1.0f));
        this.add(warningLabel, new ScaleRectangle(16, 78, 620, 60));
    }

    public void setRequestCardNumber(String cardNumber) {
        welcomeText.setText(String.format(ResBundleGoodsGiftCard.getString("PLEACE_SCAN_RETURN_GIFT_CARD"), cardNumber));
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
