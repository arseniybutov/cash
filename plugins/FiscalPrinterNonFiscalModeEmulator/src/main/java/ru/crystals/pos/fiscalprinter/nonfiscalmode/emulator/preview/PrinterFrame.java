package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview;

import ru.crystals.bundles.BundleManager;
import ru.crystals.pos.CashEventSource;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableBarCode;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.serilizable.SerializableFontLine;
import ru.crystals.pos.keylock.KeyLockEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class PrinterFrame extends JFrame implements IPrinterView {

    private static final long serialVersionUID = 4821189674606611255L;
    private static PrinterFrame printerFrame;
    private JPanel contentPane;
    private PrinterPanel pnlCheck;
    private JScrollPane scrollPane;
    private Registry registry;
    private Rectangle location;
    private Properties properties;
    private int port = 8889;
    private JCheckBox chkAlwaysOnTop;
    private JCheckBox drawerEmulator;
    private JCheckBox ofdMode;
    private JComboBox fpIsBroken;
    private JLabel innLabel = new JLabel();
    private int index = 0;

    private JComboBox barcodeComboBox = new JComboBox(new FileStringComboBoxModel("fiscal_printer_emulator_barcode_history.txt"));
    private JSlider keyboardKeySlider = null;
    private JLabel critiaclError = new JLabel();

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                PrinterFrame frame = PrinterFrame.getInstance();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static PrinterFrame getInstance() {
        if (printerFrame == null) {
            printerFrame = new PrinterFrame(true, 0);
        }
        return printerFrame;
    }

    public PrinterFrame(boolean useRMI, final long index) {
        this.index = (int) index;
        printerFrame = this;
        setTitle("Эмулятор фискального принтера");
        properties = new Properties();
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (index == 0) {
                    saveParameters();
                }
                if (registry != null) {
                    try {
                        registry.unbind("PrinterView");
                        registry = null;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
                System.exit(0);
            }
        });

        if (useRMI) {
            createRMIListener();
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel toolsPanel = new JPanel(new BorderLayout());
        toolsPanel.setPreferredSize(new Dimension(410, 114));

        toolsPanel.add(createCheckBoxesPanel(), BorderLayout.NORTH);
        toolsPanel.add(createBarcodeEmulatorPanel(), BorderLayout.CENTER);
        toolsPanel.add(createKeyboardKeyPanel(), BorderLayout.SOUTH);

        contentPane.add(toolsPanel, BorderLayout.NORTH);

        pnlCheck = new PrinterPanel();
        scrollPane = new JScrollPane(pnlCheck);
        scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
        contentPane.add(scrollPane, BorderLayout.CENTER);

        SwingUtilities.invokeLater(() -> {
            loadParameters();
            if (location == null) {
                setLocationRelativeTo(null);
            } else {
                setBounds(location);
            }
        });

    }

    /**
     * Панель с чекбоксами и радиобаттонами
     *
     * @return
     */
    private JPanel createCheckBoxesPanel() {
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(null);
        pnlButtons.setPreferredSize(new Dimension(200, 44));

        addPanelTo(pnlButtons, createAlwaysOnTopCheckBox(), 0, 0, 130, 22);
        drawerEmulator = new JCheckBox("Открыт ящик");
        addPanelTo(pnlButtons, drawerEmulator, 132, 0, 130, 22);
        ofdMode = new JCheckBox("ОФД/СКНО");
        addPanelTo(pnlButtons, ofdMode, 264, 0, 130, 22);
        addPanelTo(pnlButtons, createFPisBrokenCheckBox(), 0, 22, 260, 22);
        addPanelTo(pnlButtons, innLabel, 264, 22, 150, 22);
        return pnlButtons;
    }

    private void addPanelTo(JPanel panel, JComponent component, int x, int y, int width, int height) {
        component.setBounds(new Rectangle(x, y, width, height));
        panel.add(component);
    }

    private JComponent createFPisBrokenCheckBox() {
        JPanel errorsPanel = new JPanel(null);
        fpIsBroken = new JComboBox(FRError.values());
        addPanelTo(errorsPanel, new JLabel("Ошибка: "), 0, 0, 70, 22);
        addPanelTo(errorsPanel, fpIsBroken, 72, 0, 170, 22);
        return errorsPanel;
    }

    private JCheckBox createAlwaysOnTopCheckBox() {
        chkAlwaysOnTop = new JCheckBox("Всегда сверху");
        chkAlwaysOnTop.addActionListener(e -> printerFrame.setAlwaysOnTop(chkAlwaysOnTop.isSelected()));
        return chkAlwaysOnTop;
    }

    private JPanel createBarcodeEmulatorPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton barcodeButton = new JButton("barcode");
        barcodeButton.addActionListener(e -> {
            String barcode = (String) barcodeComboBox.getSelectedItem();
            barcodeComboBox.addItem(barcode);
            CashEventSource.getInstance().barcodeScanned(barcode);
        });

        JButton msrButton = new JButton("msr");
        msrButton.addActionListener(e -> {
            String barcode = (String) barcodeComboBox.getSelectedItem();
            barcodeComboBox.addItem(barcode);
            CashEventSource.getInstance().scannedMSR(null, barcode, null, null);
        });

        barcodeComboBox.setPreferredSize(new Dimension(180, 24));
        barcodeComboBox.setEditable(true);
        barcodeComboBox.setMaximumRowCount(((StringComboBoxModel) barcodeComboBox.getModel()).getSizeLimit());
        barcodeButton.setPreferredSize(new Dimension(100, 24));
        msrButton.setPreferredSize(new Dimension(100, 24));
        p.add(barcodeComboBox);
        p.add(barcodeButton);
        p.add(msrButton);
        return p;
    }

    private JPanel createKeyboardKeyPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p.setPreferredSize(new Dimension(100, 40));
        p.add(new JLabel("Положение ключа"));
        keyboardKeySlider = new JSlider(JSlider.VERTICAL, 1, 2, 1);
        keyboardKeySlider.setPreferredSize(new Dimension(80, 32));
        keyboardKeySlider.setPaintTicks(true);
        keyboardKeySlider.setMinorTickSpacing(1);
        java.awt.Font font = new java.awt.Font("Serif", java.awt.Font.PLAIN, 15);
        keyboardKeySlider.setFont(font);
        keyboardKeySlider.addChangeListener(e -> BundleManager.get(KeyLockEvent.class).eventKeyLock(keyboardKeySlider.getValue()));

        p.add(keyboardKeySlider);

        critiaclError.setOpaque(true);
        p.add(critiaclError);

        return p;
    }

    public boolean isDrawerOpened() {
        return drawerEmulator.isSelected();
    }

    public void openMoneyDrawer() {
        drawerEmulator.setSelected(true);
        drawerEmulator.repaint();
    }

    public boolean isOFDMode() {
        return ofdMode.isSelected();
    }

    public void setOFDMode(boolean mode) {
        ofdMode.setSelected(mode);
    }

    public FRError getPrinterBroken() {
        return (FRError) fpIsBroken.getSelectedItem();
    }

    protected void saveParameters() {
        try (OutputStream out = new FileOutputStream("PrinterView.conf")) {
            location = getBounds();
            properties.setProperty("locationX", Integer.toString(location.x));
            properties.setProperty("locationY", Integer.toString(location.y));
            properties.setProperty("locationW", Integer.toString(location.width));
            properties.setProperty("locationH", Integer.toString(location.height));
            properties.setProperty("port", Integer.toString(port));
            properties.setProperty("alwaysOnTop", Boolean.toString(chkAlwaysOnTop.isSelected()));

            properties.store(out, "Fiscal printer state");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadParameters() {
        File file = new File("PrinterView.conf");
        if (file.exists()) {
            try (InputStream is = new FileInputStream(file)) {

                properties.load(is);
                if (properties.containsKey("locationX")) {
                    location = new Rectangle();
                    location.x = Integer.valueOf(properties.getProperty("locationX", "800")) + index * 500;
                    location.y = Integer.valueOf(properties.getProperty("locationY", "0"));
                    location.width = Integer.valueOf(properties.getProperty("locationW", "410"));
                    location.height = Integer.valueOf(properties.getProperty("locationH", "600"));
                }
                port = Integer.valueOf(properties.getProperty("port", "8889"));
                pnlCheck.setLastDocsCount(Integer.valueOf(properties.getProperty("LastDocsCount", "10")));
                chkAlwaysOnTop.setSelected(Boolean.valueOf(properties.getProperty("alwaysOnTop")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            location = new Rectangle(800 + index * 500, 0, 430, 800);
            chkAlwaysOnTop.setSelected(false);
            pnlCheck.setLastDocsCount(10);
        }
        printerFrame.setAlwaysOnTop(chkAlwaysOnTop.isSelected());
    }

    private void createRMIListener() {
        try {
            try {
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets)) {
                    if (!netint.getDisplayName().toUpperCase().contains("VMWARE")) {
                        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                            if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                                System.out.println("NET Interface Name: " + netint.getDisplayName());
                                System.out.println("Address: " + inetAddress.getHostAddress());
                                System.setProperty("java.rmi.server.hostname", inetAddress.getHostAddress());
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }

            System.out.println("java.rmi.server.hostname: " + System.getProperty("java.rmi.server.hostname"));
            IPrinterView stub = (IPrinterView) UnicastRemoteObject.exportObject(PrinterFrame.this, port);
            registry = LocateRegistry.createRegistry(8889);
            registry.rebind("PrinterView", stub);
            System.out.println("RMI Listening port: " + port);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void updateScroll() {
        SwingUtilities.invokeLater(() -> {
            scrollPane.setViewportView(pnlCheck);
            Rectangle rect = pnlCheck.getLastBounds();
            if (rect != null) {
                pnlCheck.scrollRectToVisible(rect);
            }
        });
    }

    @Override
    public void setMaxCharRow(int maxCharRow) {
        pnlCheck.setRibbonChars(maxCharRow);
    }

    @Override
    public int getMaxRowChars(Font font) {
        return pnlCheck.getRibbonChars(font);
    }

    @Override
    public void ping() throws RemoteException {
        //
    }

    @Override
    public void appendText(SerializableFontLine line) throws RemoteException {
        pnlCheck.appendText(line);
        updateScroll();
    }

    @Override
    public void appendText(List<SerializableFontLine> text) throws RemoteException {
        for (SerializableFontLine line : text) {
            pnlCheck.appendText(line);
            updateScroll();
        }
    }

    @Override
    public void appendFiscal(long shiftNumber, long docNumber, long kpk) throws RemoteException {
        pnlCheck.appendFiscal(shiftNumber, docNumber, kpk);
        updateScroll();
    }

    @Override
    public void appendLogo() throws RemoteException {
        pnlCheck.appendLogo();
        updateScroll();
    }

    @Override
    public void appendBarcode(SerializableBarCode barCode) throws RemoteException {
        pnlCheck.appendBarcode(barCode);
        updateScroll();
    }

    @Override
    public void appendCutter() throws RemoteException {
        pnlCheck.appendCutter();
        updateScroll();
    }

    public void beepCriticalError() {
        final Color back = this.getBackground();
        SwingUtilities.invokeLater(() -> {
            PrinterFrame.this.critiaclError.setText("Critical error");
            PrinterFrame.this.critiaclError.setBackground(new Color(255, 0, 0));
        });
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            //
        }

        SwingUtilities.invokeLater(() -> {
            PrinterFrame.this.critiaclError.setText("");
            PrinterFrame.this.critiaclError.setBackground(back);
        });
    }

    public void setInn(String string) {
        innLabel.setText(string);
    }
}
