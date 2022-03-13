package ru.crystals.pos.fiscalprinter.nonfiscalmode.remote;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions.ExceptionArea;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions.ManualExceptionAppender;
import ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.exceptions.ManualFiscalPrinterException;

public class RemoteControl extends JFrame {

	private class ReadOnlyCheckBox extends JCheckBox {
		private static final long serialVersionUID = 1L;

		public ReadOnlyCheckBox (String text) {
	        super(text);
	        setFocusable(false);
	    }

	    @Override
		protected void processKeyEvent(KeyEvent e) {
	    }

	    @Override
		protected void processMouseEvent(MouseEvent e) {

	    }
	}

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private Registry registry;
	private ManualExceptionAppender hw;
	private JButton btnResetException;
	private JLabel label;
	private JComboBox edtExceptionArea;
	private JTextField edtExceptionMessage;
	private JCheckBox chkFatal;
	private JPanel panel;
	private JLabel lblNewLabel_1;
	private JLabel lblNewLabel_2;
	private JTextField edtHost;
	private JSpinner edtPort;
	private Properties properties;
	private Rectangle location;
	private JPanel panel_1;
	private JToggleButton btnConnect;
	private JToggleButton btnDisconnect;
	private JButton btnThrowException;
	private JPanel panel_2;
	private JButton btnOpenDrawer;
	private JCheckBox chkDrawerState;
	private JButton btnGetDrawerState;

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				RemoteControl frame = new RemoteControl();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public RemoteControl() {
		properties = new Properties();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveParameters();
			}
		});
		setTitle("Управление эмулятором ФР");

		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setBounds(100, 100, 418, 315);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 114, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 20, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Параметры связи", TitledBorder.LEADING,
				TitledBorder.TOP, null, null), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		lblNewLabel_1 = new JLabel("Хост");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);

		edtHost = new JTextField();
		edtHost.setText("127.0.0.1");
		GridBagConstraints gbc_edtHost = new GridBagConstraints();
		gbc_edtHost.insets = new Insets(0, 0, 5, 5);
		gbc_edtHost.fill = GridBagConstraints.HORIZONTAL;
		gbc_edtHost.gridx = 1;
		gbc_edtHost.gridy = 0;
		panel.add(edtHost, gbc_edtHost);
		edtHost.setColumns(10);

		lblNewLabel_2 = new JLabel("Порт");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 0;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);

		edtPort = new JSpinner();
		edtPort.setModel(new SpinnerNumberModel(8888, 1025, 65535, 1));
		GridBagConstraints gbc_edtPort = new GridBagConstraints();
		gbc_edtPort.insets = new Insets(0, 0, 5, 0);
		gbc_edtPort.anchor = GridBagConstraints.WEST;
		gbc_edtPort.gridx = 3;
		gbc_edtPort.gridy = 0;
		panel.add(edtPort, gbc_edtPort);

		panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 4;
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		panel.add(panel_1, gbc_panel_1);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		btnConnect = new JToggleButton("Соединиться");
		btnConnect.addActionListener(e -> connectClick());
		panel_1.add(btnConnect);

		btnDisconnect = new JToggleButton("Разъединиться");
		btnDisconnect.addActionListener(e -> connectClick());
		btnDisconnect.setSelected(true);
		panel_1.add(btnDisconnect);

		ButtonGroup group = new ButtonGroup();
		group.add(btnConnect);
		group.add(btnDisconnect);

		label = new JLabel("Текст ошибки");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 1;
		contentPane.add(label, gbc_label);

		edtExceptionMessage = new JTextField();
		edtExceptionMessage.setEnabled(false);
		GridBagConstraints gbc_edtExceptionMessage = new GridBagConstraints();
		gbc_edtExceptionMessage.gridwidth = 2;
		gbc_edtExceptionMessage.insets = new Insets(0, 0, 5, 0);
		gbc_edtExceptionMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_edtExceptionMessage.gridx = 1;
		gbc_edtExceptionMessage.gridy = 1;
		contentPane.add(edtExceptionMessage, gbc_edtExceptionMessage);
		edtExceptionMessage.setColumns(10);

		final JLabel lblNewLabel = new JLabel("Область");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);

		edtExceptionArea = new JComboBox(ExceptionArea.values());
		edtExceptionArea.setEnabled(false);
		GridBagConstraints gbc_edtExceptionArea = new GridBagConstraints();
		gbc_edtExceptionArea.gridwidth = 2;
		gbc_edtExceptionArea.insets = new Insets(0, 0, 5, 0);
		gbc_edtExceptionArea.fill = GridBagConstraints.HORIZONTAL;
		gbc_edtExceptionArea.gridx = 1;
		gbc_edtExceptionArea.gridy = 2;
		contentPane.add(edtExceptionArea, gbc_edtExceptionArea);

		chkFatal = new JCheckBox("Фатальная");
		chkFatal.setEnabled(false);
		chkFatal.setHorizontalTextPosition(SwingConstants.LEADING);
		GridBagConstraints gbc_chkFatal = new GridBagConstraints();
		gbc_chkFatal.anchor = GridBagConstraints.WEST;
		gbc_chkFatal.gridwidth = 2;
		gbc_chkFatal.insets = new Insets(0, 0, 5, 5);
		gbc_chkFatal.gridx = 0;
		gbc_chkFatal.gridy = 3;
		contentPane.add(chkFatal, gbc_chkFatal);

		chkDrawerState = new ReadOnlyCheckBox("Ящик открыт");
		chkDrawerState.setEnabled(false);
		chkDrawerState.setHorizontalTextPosition(SwingConstants.LEADING);
		GridBagConstraints gbc_chkDrawerState = new GridBagConstraints();
		gbc_chkDrawerState.insets = new Insets(0, 0, 5, 5);
		gbc_chkDrawerState.gridx = 0;
		gbc_chkDrawerState.gridy = 4;
		contentPane.add(chkDrawerState, gbc_chkDrawerState);

		btnGetDrawerState = new JButton("Получить состояние");
		btnGetDrawerState.addActionListener(e -> {
			try {
				hw = (ManualExceptionAppender) registry.lookup("ManualExceptionAppender");
				chkDrawerState.setSelected(hw.isDrawerOpened());
				btnOpenDrawer.setText((chkDrawerState.isSelected() ? "Закрыть" : "Открыть") + " ящик");
				hw = null;
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnGetDrawerState.setEnabled(false);
		GridBagConstraints gbc_btnGetDrawerState = new GridBagConstraints();
		gbc_btnGetDrawerState.anchor = GridBagConstraints.WEST;
		gbc_btnGetDrawerState.insets = new Insets(0, 0, 5, 5);
		gbc_btnGetDrawerState.gridx = 1;
		gbc_btnGetDrawerState.gridy = 4;
		contentPane.add(btnGetDrawerState, gbc_btnGetDrawerState);

		panel_2 = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.anchor = GridBagConstraints.SOUTH;
		gbc_panel_2.gridwidth = 3;
		gbc_panel_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 5;
		contentPane.add(panel_2, gbc_panel_2);

		btnThrowException = new JButton("Добавить ошибку");
		panel_2.add(btnThrowException);
		btnThrowException.setEnabled(false);

		btnResetException = new JButton("Очистить ошибку");
		panel_2.add(btnResetException);
		btnResetException.setEnabled(false);

		btnOpenDrawer = new JButton("Открыть ящик");
		btnOpenDrawer.addActionListener(e -> {
			try {
				hw = (ManualExceptionAppender) registry.lookup("ManualExceptionAppender");
				hw.setCashDrawerOpen(!chkDrawerState.isSelected());
				chkDrawerState.setSelected(hw.isDrawerOpened());
				btnOpenDrawer.setText((chkDrawerState.isSelected() ? "Закрыть" : "Открыть") + " ящик");
				hw = null;
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnOpenDrawer.setEnabled(false);
		panel_2.add(btnOpenDrawer);
		btnResetException.addActionListener(e -> {
			try {
				hw = (ManualExceptionAppender) registry.lookup("ManualExceptionAppender");
				hw.resetException();
				hw = null;
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		});
		btnThrowException.addActionListener(e -> {
			try {
				hw = (ManualExceptionAppender) registry.lookup("ManualExceptionAppender");
				ManualFiscalPrinterException manualEx = new ManualFiscalPrinterException(edtExceptionMessage.getText(), (ExceptionArea) edtExceptionArea
						.getSelectedItem(), chkFatal.isSelected());
				hw.throwException(manualEx);
				hw = null;
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
			}
		});

		loadParameters();
		if (location == null) {
			setLocationRelativeTo(null);
		} else {
			setBounds(location);
		}

		SwingUtilities.invokeLater(() -> edtExceptionMessage.requestFocus());

	}

	protected void connectClick() {
		try {
			if (btnConnect.isSelected()) {
				registry = LocateRegistry.getRegistry(edtHost.getText(), (Integer) edtPort.getValue());
				try {
					hw = (ManualExceptionAppender) registry.lookup("ManualExceptionAppender");
					chkDrawerState.setSelected(hw.isDrawerOpened());
					btnOpenDrawer.setText((chkDrawerState.isSelected() ? "Закрыть" : "Открыть") + " ящик");
					hw = null;
				} catch (Exception e) {
					btnDisconnect.setSelected(true);
					e.printStackTrace();
					JOptionPane.showMessageDialog(null, e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				registry = null;
			}

			edtExceptionArea.setEnabled(btnConnect.isSelected());
			edtExceptionMessage.setEnabled(btnConnect.isSelected());
			chkFatal.setEnabled(btnConnect.isSelected());

			btnGetDrawerState.setEnabled(btnConnect.isSelected());
			chkDrawerState.setEnabled(btnConnect.isSelected());

			btnThrowException.setEnabled(btnConnect.isSelected());
			btnResetException.setEnabled(btnConnect.isSelected());
			btnOpenDrawer.setEnabled(btnConnect.isSelected());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadParameters() {
		File file = new File("RemoteControl.conf");
		if(!file.exists()) {
			return;
		}
		try(InputStream is = new FileInputStream(file)) {

			properties.load(is);
			is.close();

			edtExceptionArea.setSelectedItem(ExceptionArea.valueOf(properties.getProperty("edtExceptionArea", "PRINT_LINE")));
			edtExceptionMessage.setText(properties.getProperty("edtExceptionMessage", ""));
			chkFatal.setSelected(Boolean.valueOf(properties.getProperty("chkFatal", "false")));

			edtHost.setText(properties.getProperty("edtHost", "127.0.0.1"));
			edtPort.setValue(Integer.valueOf(properties.getProperty("edtPort", "8890")));

			if (properties.containsKey("locationX")) {
				location = new Rectangle();
				location.x = Integer.valueOf(properties.getProperty("locationX", "100"));
				location.y = Integer.valueOf(properties.getProperty("locationY", "100"));
				location.width = Integer.valueOf(properties.getProperty("locationW", "420"));
				location.height = Integer.valueOf(properties.getProperty("locationH", "320"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveParameters() {
		try {
			properties.setProperty("edtExceptionArea", edtExceptionArea.getSelectedItem().toString());
			properties.setProperty("edtExceptionMessage", edtExceptionMessage.getText());
			properties.setProperty("chkFatal", Boolean.toString(chkFatal.isSelected()));

			properties.setProperty("edtHost", edtHost.getText());
			properties.setProperty("edtPort", Integer.toString((Integer) edtPort.getValue()));

			location = getBounds();
			properties.setProperty("locationX", Integer.toString(location.x));
			properties.setProperty("locationY", Integer.toString(location.y));
			properties.setProperty("locationW", Integer.toString(location.width));
			properties.setProperty("locationH", Integer.toString(location.height));

			try(OutputStream out = new FileOutputStream("RemoteControl.conf")) {
                properties.store(out, "Fiscal printer state");
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
