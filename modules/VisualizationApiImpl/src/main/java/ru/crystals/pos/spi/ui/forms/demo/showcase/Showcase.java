package ru.crystals.pos.spi.ui.forms.demo.showcase;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import ru.crystals.pos.spi.ui.UIForms;
import ru.crystals.pos.spi.ui.utils.Resources;
import ru.crystals.pos.visualization.components.ColorSchema;
import ru.crystals.pos.visualization.styles.Color;

public class Showcase {
    private static final String WINDOW_TITLE = "Component Showcase";
    private static final int WINDOW_WIDTH = 640;
    private static final int WINDOW_HEIGHT = 480;

    private JPanel containerPanel;

    private ShowcaseFormManager formManager;
    private JFrame frame;
    private JPanel headerPanel;
    private ComponentMover componentMover;

    public Showcase() {
        Color.setColorScheme(Color.COLOR_SCHEME_CRYSTALS);
        ColorSchema.updateColors(Color.SHEMA);
        Locale.setDefault(Locale.forLanguageTag("ru-RU"));
        frame = new JFrame();
        frame.setTitle(WINDOW_TITLE);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        containerPanel = new JPanel();
        containerPanel.setLayout(new BorderLayout(0, 0));
        containerPanel.setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));

        headerPanel= new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        headerPanel.add(new JLabel(Resources.getImageIcon("images/head.png")));
        containerPanel.add(headerPanel, BorderLayout.PAGE_START);
        // Placeholder
        containerPanel.add(new JPanel(), BorderLayout.CENTER);

        JPanel tail = new JPanel();
        tail.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tail.add(new JLabel(Resources.getImageIcon("images/tail.png")));
        containerPanel.add(tail, BorderLayout.PAGE_END);

        frame.add(containerPanel);

        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                for(KeyListener k: containerPanel.getKeyListeners()) {
                    k.keyPressed(e);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        formManager = new ShowcaseFormManager(containerPanel);
        frame.pack();
        frame.setVisible(true);

        componentMover = new ComponentMover(frame);
        frame.addMouseMotionListener(componentMover);
        frame.addMouseListener(componentMover);

        SwingUtilities.invokeLater(() -> new ScenarioSelectWindow(Showcase.this));

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Showcase::new);
    }

    public JFrame getFrame() {
        return frame;
    }

    public UIForms getFormManager() {
        return formManager;
    }
}
