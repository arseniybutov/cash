package ru.crystals.pos.visualization.products.siebelcard.form;

import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import ru.crystals.pos.visualization.products.siebelcard.ResBundleGoodsSiebelGiftCard;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;

public class SiebelGiftCardFixedAmountForm extends CommonForm {

    private static final String HTML_TEMPLATE = "<html><div style='text-align: center; font-style: italic;'>%s</div></html>";

    private JLabel messageLabel;
    private CommonYesNoPanel yesNoPanel;

    public SiebelGiftCardFixedAmountForm(XListener outerListener) {
        super(outerListener);
        this.setLayout(new FlowLayout());

        messageLabel = new JLabel();
        messageLabel.setPreferredSize(new ScaleDimension(600, 185));
        messageLabel.setFont(MyriadFont.getItalic(37F));
        messageLabel.setForeground(Color.secondTitleForeGround);
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setVerticalAlignment(JLabel.CENTER);
        add(messageLabel);

        yesNoPanel = new CommonYesNoPanel();
        yesNoPanel.setYesButtonCaption(ResBundleGoodsSiebelGiftCard.getString("SELL_GIFT_CARD"));
        yesNoPanel.setNoButtonCaption(ResBundleGoodsSiebelGiftCard.getString("CANCEL"));
        add(yesNoPanel);
    }

    @Override
    protected boolean dispatchKeyEvent(XKeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            return false;
        } else {
            yesNoPanel.keyPressedNew(e);
            return true;
        }
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return true;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return true;
    }

    @Override
    public void clear() {
        //
    }

    public boolean isYes() {
        return yesNoPanel.isYes();
    }

    public void setAmount(Long amount) {
        yesNoPanel.selectYes();
        messageLabel.setText(String.format(
                HTML_TEMPLATE,
                String.format(ResBundleGoodsSiebelGiftCard.getString("SELL_FIXED_RATE_GIFT_CARD"), amount / 100)
        ));
    }
}
