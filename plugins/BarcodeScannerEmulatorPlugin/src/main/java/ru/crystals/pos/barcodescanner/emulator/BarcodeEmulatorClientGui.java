package ru.crystals.pos.barcodescanner.emulator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.PrintStream;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Гуёвая обёртка над {@link BarcodeEmulatorClient}
 */
public class BarcodeEmulatorClientGui {
    private static final String WINDOW_TITLE = "Barcode Emulator Gui";

    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel();

    private JTextField textFieldIp;
    private JTextField textFieldPort;
    private JButton buttonScan;
    private JTextField textFieldBarcode;
    private JTextArea textAreaLogger;

    private JTextAreaOutputStream stdoutInterceptor;

    private BarcodeEmulatorClient client;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link BarcodeEmulatorClientGui}.
     * Окно программы отображается немедленно после отработки конструктора.
     * @param client экземпляр {@link BarcodeEmulatorClient}, гуёвой оберткой для которого является этот класс.
     */
    public BarcodeEmulatorClientGui(BarcodeEmulatorClient client) {
        this.client = client;
        createGui();
        stdoutInterceptor = new JTextAreaOutputStream(textAreaLogger);
        System.setOut(new PrintStream(stdoutInterceptor));
        System.setErr(new PrintStream(stdoutInterceptor));
    }

    private void createGui() {
        frame.setTitle(WINDOW_TITLE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        panel.setLayout(new BorderLayout());
        createTopPanel(panel);
        createTrackPanel(panel);

        textAreaLogger = new JTextArea();
        textAreaLogger.setColumns(20);
        textAreaLogger.setRows(20);
        textAreaLogger.setLineWrap(true);
        textAreaLogger.setAutoscrolls(true);
        JScrollPane scrollPane = new JScrollPane(textAreaLogger);

        panel.add(scrollPane, BorderLayout.PAGE_END);

        frame.setContentPane(panel);
        frame.pack();
        frame.setVisible(true);
    }

    private void createTopPanel(JPanel parent) {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(new JLabel("IP: "));
        textFieldIp = new JTextField();
        textFieldIp.setColumns(15);

        topPanel.add(textFieldIp);

        topPanel.add(new JLabel("Port: "));

        textFieldPort = new JTextField();
        textFieldPort.setColumns(5);

        topPanel.add(textFieldPort);

        buttonScan = new JButton("Scan Barcode");

        buttonScan.addActionListener(e -> {
            try {
                String host = textFieldIp.getText();
                String port = textFieldPort.getText();
                String barcode = textFieldBarcode.getText() ;
                if(validateInput(host, port, barcode)) {
                    client.scanBarcode(host, Integer.parseInt(port), barcode);
                    System.out.println("Sent.");
                } else {
                    System.err.println("Invalid input.");
                }
            } catch(Exception ex) {
                ex.printStackTrace(System.err);
            }
        });

        topPanel.add(buttonScan);

        parent.add(topPanel, BorderLayout.PAGE_START);
    }

    private boolean validateInput(String host, String port, String barcode) {
        if(host == null || "".equals(host.trim())) {
            System.err.println("Host could not be empty.");
            return false;
        }
        if(port == null || "".equals(port.trim())) {
            System.err.println("Port cannot be empty.");
            return false;
        }
        try {
            int p = Integer.parseInt(port);
            if( p <= 0 || p > 65535) {
                System.err.println("Invalid port number");
                return false;
            }
        } catch(NumberFormatException nfe) {
            nfe.printStackTrace(System.err);
            return false;
        }
        if(barcode == null || "".equals(barcode.trim())) {
            System.err.println("Barcode cannot be empty.");
            return false;
        }
        return true;
    }

    private void createTrackPanel(JPanel parent) {
        JPanel trackPanel = new JPanel();
        trackPanel.setLayout(new BoxLayout(trackPanel, BoxLayout.Y_AXIS));

        trackPanel.add(new JLabel("Barcode:"));
        textFieldBarcode = createTrackField();
        trackPanel.add(textFieldBarcode);

        parent.add(trackPanel, BorderLayout.CENTER);
    }

    private JTextFieldLimited createTrackField() {
        JTextFieldLimited track = new JTextFieldLimited(24);
        track.setColumns(24);
        return track;
    }

    public void setHost(String host) {
        this.textFieldIp.setText(host);
    }

    public void setPort(int port) {
        this.textFieldPort.setText(String.valueOf(port));
    }

    public void setBarcode(String value) {
        this.textFieldBarcode.setText(value);
    }

}
