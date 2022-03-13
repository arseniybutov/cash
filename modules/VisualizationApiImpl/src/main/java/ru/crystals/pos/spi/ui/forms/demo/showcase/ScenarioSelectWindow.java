package ru.crystals.pos.spi.ui.forms.demo.showcase;

import java.awt.Dimension;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.DialogShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.ErrorFormShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.InputFormShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.InputSelectorFormShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.MessageFormShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.PatternFormShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.PaymentFormScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.ScanFormShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.SpinnerShowcaseScenario;
import ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios.TimeoutSpinnerShowcaseScenario;

public class ScenarioSelectWindow {
    private JFrame frame;
    private JList demoBrowser;
    private Showcase showcase;

    public ScenarioSelectWindow(Showcase showcase) {
        this.showcase = showcase;
        frame = new JFrame();
        frame.setTitle("Component Showcase");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(showcase.getFrame());

        demoBrowser = new JList();
        demoBrowser.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        demoBrowser.addListSelectionListener(e -> {
            switch (demoBrowser.getSelectedIndex()) {
                case 0:
                    new InputFormShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 1:
                    new PaymentFormScenario(showcase.getFormManager()).run();
                    break;
                case 2:
                    new DialogShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 3:
                    new ErrorFormShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 4:
                    new SpinnerShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 5:
                    new MessageFormShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 6:
                    new ScanFormShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 7:
                    new TimeoutSpinnerShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 8:
                    new PatternFormShowcaseScenario(showcase.getFormManager()).run();
                    break;
                case 9:
                    new InputSelectorFormShowcaseScenario(showcase.getFormManager()).run();
                    break;
            }

        });
        JScrollPane demoBrowserScrollPane = new JScrollPane(demoBrowser);
        demoBrowserScrollPane.setPreferredSize(new Dimension(200, 320));
        frame.add(demoBrowserScrollPane);
        fillDemoList();
        demoBrowser.setSelectedIndex(1);
        frame.pack();
        frame.setVisible(true);
    }

    private void fillDemoList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        listModel.addElement("Number Input");
        listModel.addElement("Payment");
        listModel.addElement("Dialog");
        listModel.addElement("Error");
        listModel.addElement("Spinner");
        listModel.addElement("Message");
        listModel.addElement("Scan form");
        listModel.addElement("Timeout spinner");
        listModel.addElement("Mobile phone input");
        listModel.addElement("Input selector");
        demoBrowser.setModel(listModel);
    }
}
