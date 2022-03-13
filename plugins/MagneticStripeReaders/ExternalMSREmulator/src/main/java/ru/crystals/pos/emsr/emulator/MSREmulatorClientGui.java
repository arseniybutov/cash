package ru.crystals.pos.emsr.emulator;

import org.apache.commons.lang.StringUtils;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.PrintStream;

/**
 * Гуёвая обёртка над {@link MSREmulatorClient}
 */
public class MSREmulatorClientGui {
    private static final String WINDOW_TITLE = "MSR Emulator Gui";

    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel();

    private JTextField textFieldIp;
    private JTextField textFieldPort;
    private JButton buttonScan;
    private JTextField textFieldTrack1;
    private JTextField textFieldTrack2;
    private JTextField textFieldTrack3;
    private JTextField textFieldTrack4;
    private JTextArea textAreaLogger;

    private JTextAreaOutputStream stdoutInterceptor;

    private MSREmulatorClient client;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link MSREmulatorClientGui}.
     * Окно программы отображается немедленно после отработки конструктора.
     *
     * @param client экземпляр {@link MSREmulatorClient}, гуёвой оберткой для которого является этот класс.
     */
    public MSREmulatorClientGui(MSREmulatorClient client) {
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

        buttonScan = new JButton("Scan MSR");

        buttonScan.addActionListener(e -> {
            try {
                String host = textFieldIp.getText();
                String port = textFieldPort.getText();
                String track1 = StringUtils.isBlank(textFieldTrack1.getText()) ? null : textFieldTrack1.getText();
                String track2 = StringUtils.isBlank(textFieldTrack2.getText()) ? null : textFieldTrack2.getText();
                String track3 = StringUtils.isBlank(textFieldTrack3.getText()) ? null : textFieldTrack3.getText();
                String track4 = StringUtils.isBlank(textFieldTrack4.getText()) ? null : textFieldTrack4.getText();
                if (validateInput(host, port, track1, track2, track3, track4)) {
                    client.sendMsr(host, Integer.parseInt(port), track1, track2, track3, track4);
                    System.out.println("Sent.");
                }
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        });

        topPanel.add(buttonScan);

        parent.add(topPanel, BorderLayout.PAGE_START);
    }

    private boolean validateInput(String host, String port, String track1, String track2, String track3, String track4) {
        if (StringUtils.isBlank(host)) {
            System.err.println("Host could not be empty.");
            return false;
        }
        if (StringUtils.isBlank(port)) {
            System.err.println("Port cannot be empty.");
            return false;
        }
        try {
            int p = Integer.parseInt(port);
            if (p <= 0 || p > 65535) {
                System.err.println("Invalid port number");
                return false;
            }
        } catch (NumberFormatException nfe) {
            nfe.printStackTrace(System.err);
            return false;
        }
        if (StringUtils.isBlank(track1) && StringUtils.isBlank(track2) && StringUtils.isBlank(track3) && StringUtils.isBlank(track4)) {
            System.err.println("All tracks cannot be empty.");
            return false;
        }
        return true;
    }

    private void createTrackPanel(JPanel parent) {
        JPanel trackPanel = new JPanel();
        trackPanel.setLayout(new BoxLayout(trackPanel, BoxLayout.Y_AXIS));

        trackPanel.add(new JLabel("Track 1:"));
        textFieldTrack1 = createTrackField();
        trackPanel.add(textFieldTrack1);

        trackPanel.add(new JLabel("Track 2:"));
        textFieldTrack2 = createTrackField();
        trackPanel.add(textFieldTrack2);

        trackPanel.add(new JLabel("Track 3:"));
        textFieldTrack3 = createTrackField();
        trackPanel.add(textFieldTrack3);

        trackPanel.add(new JLabel("Track 4:"));
        textFieldTrack4 = createTrackField();
        trackPanel.add(textFieldTrack4);

        parent.add(trackPanel, BorderLayout.CENTER);
    }

    private JTextFieldLimited createTrackField() {
        JTextFieldLimited track = new JTextFieldLimited(MSREmulatorService.MAX_TRACK_LENGTH);
        track.setColumns(24);
        return track;
    }

    public void setHost(String host) {
        this.textFieldIp.setText(host);
    }

    public void setPort(int port) {
        this.textFieldPort.setText(String.valueOf(port));
    }

    public void setTrack1(String value) {
        this.textFieldTrack1.setText(value);
    }

    public void setTrack2(String value) {
        this.textFieldTrack2.setText(value);
    }

    public void setTrack3(String value) {
        this.textFieldTrack3.setText(value);
    }

    public void setTrack4(String value) {
        this.textFieldTrack4.setText(value);
    }
}
