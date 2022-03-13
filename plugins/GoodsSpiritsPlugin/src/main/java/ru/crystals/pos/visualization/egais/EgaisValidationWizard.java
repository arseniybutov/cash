package ru.crystals.pos.visualization.egais;

import org.springframework.stereotype.Component;
import ru.crystals.pos.egais.EgaisNotValidItem;
import ru.crystals.pos.egais.EgaisValidationGUI;
import ru.crystals.pos.egais.EgaisValidationGUIListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonYesNoPanel;
import ru.crystals.pos.visualization.components.Empty;
import ru.crystals.pos.visualization.components.VisualPanel;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.products.spirits.ResBundleGoodsSpirits;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.styles.MyriadFont;
import ru.crystals.pos.visualization.styles.Size;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;
import ru.crystals.pos.visualization.utils.Swing;
import ru.crystals.pos.visualizationtouch.components.labels.Label;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.util.List;


/**
 * Диалог сканирования акцизных бутылок для удаления их из чека.
 * Здесь только ГУИ. Никакой бизнес-логики нет и быть не должно.
 * <p>
 * Created by achibisov on 21.06.16.
 */
@Component
public class EgaisValidationWizard extends VisualPanel implements EgaisValidationGUI {
    private static final String BUTTON_VIEW = "BUTTON_VIEW";
    private static final String MESSAGE_VIEW = "MESSAGE_VIEW";
    private static final String POSITION_LIST_VIEW = "POSITION_LIST_VIEW";
    private static final String YES_NO_PANEL_VIEW = "YES_NO_PANEL_VIEW";
    private final CardLayout bodyLayout = new CardLayout(0, 0);
    private PositionNameAndBarcodeTable positionsTable = new PositionNameAndBarcodeTable();
    private JPanel header;
    private JPanel body = new JPanel();
    private JPanel footer = new JPanel();
    private CardLayout footerLayout = new CardLayout();
    private EgaisValidationGUIListener listener;
    private final Label customerMessage = new Label();
    private final Label scannedLabel = new Label();
    private JLabel headerLabel;
    private JLabel warningMessage;
    private CommonYesNoPanel yesNoPanel;
    private JPanel warningIconPanel;
    private boolean isCancelShowing = false;
    private boolean isServerUnavailableShowing = false;

    public EgaisValidationWizard() {
        this.setPreferredSize(Size.mainPanel);
        this.setLayout(new BorderLayout());

        initHeader();
        initBody();
        initFooter();

        this.add(header, BorderLayout.NORTH);
        this.add(body, BorderLayout.CENTER);
        this.add(new Empty(10, 1), BorderLayout.EAST);
        this.add(new Empty(10, 1), BorderLayout.WEST);
        this.add(footer, BorderLayout.SOUTH);
        createEventListener();

    }

    private void initFooter() {
        footer.setBackground(Color.greyBackground);
        footer.setPreferredSize(new ScaleDimension(640, 70));

        footer.setLayout(footerLayout);

        JLabel nextButton = new JLabel(ResBundleGoodsSpirits.getString("PROCEED"));
        Style.setButtonStyle(nextButton);
        Style.setSelectedButtonStyle(nextButton);
        nextButton.setFont(MyriadFont.getRegular(28f));
        nextButton.setPreferredSize(new ScaleDimension(300, 40));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.greyBackground);
        buttonPanel.add(nextButton);

        footer.add(buttonPanel, BUTTON_VIEW);
        JPanel scanPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        scanPanel.setPreferredSize(new ScaleDimension(640, 60));
        scanPanel.setBackground(Color.greyBackground);
        JPanel alreadyScannedPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        alreadyScannedPanel.setPreferredSize(new ScaleDimension(620, 30));
        alreadyScannedPanel.setBackground(Color.greyBackground);

        scannedLabel.setPreferredSize(new ScaleDimension(275, 30));
        scannedLabel.setBackground(Color.greyBackground);
        scannedLabel.setForeground(Color.darkGreyLabel);
        scannedLabel.setFont(MyriadFont.getItalic(26F));
        alreadyScannedPanel.add(scannedLabel);

        JLabel textLabel = new JLabel(ResBundleGoodsSpirits.getString("SCAN_EXCISE"));
        Style.setDialogLabelStyle(textLabel);
        textLabel.setPreferredSize(new ScaleDimension(330, 30));


        scanPanel.add(alreadyScannedPanel);
        scanPanel.add(textLabel);
        scanPanel.add(new Empty(620, 10));
        footer.add(scanPanel, MESSAGE_VIEW);
        footer.add(getCancelButtonsPanel(), YES_NO_PANEL_VIEW);

    }

    private void initBody() {
        JPanel warningPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        warningPanel.setPreferredSize(new ScaleDimension(620, 200));

        warningPanel.setBackground(Color.greyBackground);
        warningMessage = new JLabel();
        warningMessage.setBackground(Color.greyBackground);
        Style.setWarningLabelStyle(warningMessage);
        warningMessage.setPreferredSize(new ScaleDimension(530, 224));
        warningMessage.setIcon(null);
        warningMessage.setFont(MyriadFont.getItalic(30F));
        JLabel warningIcon = new JLabel();
        Style.setWarningLabelStyle(warningIcon);
        warningIcon.setPreferredSize(new ScaleDimension(60, 70));

        warningIconPanel = new JPanel();
        warningIconPanel.setLayout(new BoxLayout(warningIconPanel, BoxLayout.Y_AXIS));
        warningIconPanel.add(warningIcon);
        warningIconPanel.setBackground(Color.greyBackground);
        warningPanel.add(warningIconPanel);
        warningIconPanel.setPreferredSize(new ScaleDimension(60, 200));

        warningPanel.add(warningMessage);

        body.setLayout(bodyLayout);
        body.setBackground(Color.greyBackground);
        body.add(warningPanel, MESSAGE_VIEW);
        JScrollPane scrollPane = initPositionsTable();
        JPanel centralPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        centralPanel.add(scrollPane);
        centralPanel.add(new Empty(620, 10));
        centralPanel.setBackground(Color.greyBackground);
        customerMessage.setFont(MyriadFont.getItalic(30F));
        customerMessage.setPreferredSize(new ScaleDimension(620, 80));
        customerMessage.setBackground(Color.greyBackground);
        customerMessage.setForeground(Color.darkGreyLabel);

        centralPanel.add(customerMessage);
        body.add(centralPanel, POSITION_LIST_VIEW);
    }

    private JScrollPane initPositionsTable() {
        positionsTable = new PositionNameAndBarcodeTable();
        JScrollPane scroll = new JScrollPane(positionsTable);
        scroll.setPreferredSize(new ScalableDimension(620, 220));
        scroll.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        scroll.setBounds(0, 0, 0, 0);
        scroll.getViewport().setBackground(Color.tableBackGround);

        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        return scroll;
    }

    private void initHeader() {
        header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setBackground(Color.greyBackground);
        header.add(new Empty(620, 15));
        headerLabel = new JLabel(ResBundleGoodsSpirits.getString("EGAIS_VALIDATION_HEADER"));
        Style.setHeaderLabelStyle(headerLabel);
        headerLabel.setPreferredSize(new ScaleDimension(600, 30));
        header.setPreferredSize(new ScaleDimension(640, 45));
        header.add(headerLabel);
    }


    private void createEventListener() {
        new XListenerAdapter(this) {
            @Override
            public boolean keyPressedNew(XKeyEvent e) {
                return onKeyPressed(e);
            }

            @Override
            public boolean barcodeScanned(String barcode) {
                return onBarcodeScanned(barcode);
            }

            @Override
            public boolean eventMSR(String track1, String track2, String track3, String track4) {
                Factory.getTechProcessImpl().error("MSR is not allowed in EgaisValidationWizard");
                return true;
            }
        };
    }

    private boolean onBarcodeScanned(String barcode) {
        listener.barcodeScanned(barcode);
        return true;
    }

    private boolean onKeyPressed(XKeyEvent e) {
        switch (e.getKeyCode()) {
            case XKeyEvent.VK_ENTER:
                return processEnterKey();
            case XKeyEvent.VK_ESCAPE:
                Factory.getTechProcessImpl().stopCriticalErrorBeeping();
                listener.escape();
                break;
            case XKeyEvent.VK_LEFT:
            case XKeyEvent.VK_RIGHT:
                return false;

            default:
                Factory.getTechProcessImpl().error("Not allowed key is pressed in EgaisValidationWizard");
        }

        return true;
    }

    private boolean processEnterKey() {
        Factory.getTechProcessImpl().stopCriticalErrorBeeping();
        if (isCancelShowing && yesNoPanel.isYes()) {
            listener.cancelCheck();
            return true;
        }
        if (isServerUnavailableShowing) {
            if (yesNoPanel.isYes()) {
                listener.breakScenarioAndContinueSubtotal();
            } else {
                listener.breakScenarioAndGoToCheck();
            }
            return true;
        }
        listener.confirmed();
        return true;
    }


    @Override
    public void showStartForm(final int bottlesCount, final boolean isReturn) {
        Swing.invokeAndWait(() -> {
            headerLabel.setText(ResBundleGoodsSpirits.getString("EGAIS_VALIDATION_HEADER"));

            String message;
            switch (bottlesCount) {
                case 0:
                    if (isReturn) {
                        message = ResBundleGoodsSpirits.getString("ALL_BOTTLES_WILL_BE_DELETED_RETURN");
                    } else {
                        message = ResBundleGoodsSpirits.getString("ALL_BOTTLES_WILL_BE_DELETED");
                    }
                    break;
                case 1:
                    message = ResBundleGoodsSpirits.getString("CHECK_CONTAINS_PROHIBITED_EXCISE_ALCOHOL");
                    break;
                default:
                    message = ResBundleGoodsSpirits.getString("CHECK_CONTAINS_PROHIBITED_EXCISE_ALCOHOLS");
            }
            initStartForm(message);
            footerLayout.show(footer, BUTTON_VIEW);
        });
        Factory.getInstance().getMainWindow().showLockComponent(this);
    }

    @Override
    public void showScanExciseForm(final List<EgaisNotValidItem> bottles, final int scannedBottlesCount) {
        positionsTable.setData(bottles);
        Swing.invokeAndWait(() -> {
            positionsTable.clearSelection();
            customerMessage.setText(ResBundleGoodsSpirits.getString("TAKE_AWAY_ALCOHOL"));
            scannedLabel.setText(String.format(ResBundleGoodsSpirits.getString("SCANNED_FROM"), scannedBottlesCount, bottles.size()));
            bodyLayout.show(body, POSITION_LIST_VIEW);
            footerLayout.show(footer, MESSAGE_VIEW);
        });

    }

    @Override
    public void addListener(EgaisValidationGUIListener listener) {
        this.listener = listener;
    }

    @Override
    public void exitFromWizard() {
        Factory.getInstance().getMainWindow().unlockComponent(this);
    }

    @Override
    public void showWaitDialog() {
        Factory.getInstance().getMainWindow().getCheckContainer().showWaitComponent(ResBundleGoodsSpirits.getString("CONNECT_TO_SERVER"));
    }

    @Override
    public void showMessage(final String message) {
        Factory.getTechProcessImpl().startCriticalErrorBeeping(message);
        Swing.invokeAndWait(() -> {
            customerMessage.setText(message);
            footerLayout.show(footer, BUTTON_VIEW);
            footer.setPreferredSize(new ScaleDimension(640, 60));
        });
    }

    @Override
    public void deleteBottle(final EgaisNotValidItem bottle) {
        Swing.invokeAndWait(() -> {
            positionsTable.selectRowByBottle(bottle);
            customerMessage.setText(ResBundleGoodsSpirits.getString("BOTTLE_WILL_REMOVED"));
            footerLayout.show(footer, BUTTON_VIEW);
        });
    }

    @Override
    public void showServerUnavailableDialog() {
        Swing.invokeAndWait(() -> {
            initStartForm(ResBundleGoodsSpirits.getString("SERVER_UNAVAILABLE_CONTINUE"));
            yesNoPanel.selectYes();
            yesNoPanel.setYesButtonCaption(ResBundleGoodsSpirits.getString("CONTINUE"));
            yesNoPanel.setNoButtonCaption(ResBundleGoodsSpirits.getString("RETURN_TO_CHECK"));
            footer.setPreferredSize(new ScaleDimension(640, 80));
            footerLayout.show(footer, YES_NO_PANEL_VIEW);
        });
        isServerUnavailableShowing = true;
        isCancelShowing = false;
        Factory.getInstance().getMainWindow().showLockComponent(this);
    }

    private void initStartForm(String message) {
        Swing.wrapLabelTextUsingSeparators(warningMessage, message);
        warningMessage.setPreferredSize(new ScaleDimension(530, 224));
        warningIconPanel.setPreferredSize(new ScaleDimension(60, 200));
        footer.setPreferredSize(new ScaleDimension(640, 60));
        bodyLayout.show(body, MESSAGE_VIEW);
        isCancelShowing = false;
        isServerUnavailableShowing = false;

    }

    @Override
    public void showCancelCheckDialog() {
        Swing.invokeAndWait(() -> {
            headerLabel.setText(ResBundleGoodsSpirits.getString("CANCEL_CHECK"));
            warningIconPanel.setPreferredSize(new ScaleDimension(60, 49));
            warningMessage.setPreferredSize(new ScaleDimension(530, 46));
            warningMessage.setText(ResBundleGoodsSpirits.getString("CANCEL_CHECK_QUESTION"));
            bodyLayout.show(body, MESSAGE_VIEW);
            footerLayout.show(footer, YES_NO_PANEL_VIEW);
            footer.setPreferredSize(new ScaleDimension(640, 80));
            yesNoPanel.setYesButtonCaption(ResBundleGoodsSpirits.getString("CANCELLATION"));
            yesNoPanel.setNoButtonCaption(ResBundleGoodsSpirits.getString("CANCEL"));
            yesNoPanel.selectYes();
        });
        isCancelShowing = true;
        isServerUnavailableShowing = false;
    }

    public JPanel getCancelButtonsPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.greyBackground);
        panel.setPreferredSize(new ScaleDimension(640, 80));
        yesNoPanel = new CommonYesNoPanel(ResBundleGoodsSpirits.getString("CANCELLATION"), ResBundleGoodsSpirits.getString("CANCEL"));
        yesNoPanel.selectYes();
        panel.add(yesNoPanel);
        return panel;
    }
}
