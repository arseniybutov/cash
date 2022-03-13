package ru.crystals.pos.cash_glory;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jp.co.glory.bruebox.CashType;
import jp.co.glory.bruebox.DenominationType;

import org.apache.commons.lang.StringUtils;

import ru.crystals.pos.cash_glory.constants.DeviceType;
import ru.crystals.pos.cash_machine.CashEventsNotificator;
import ru.crystals.pos.cash_machine.Constants;

public class CiManager {

    private CashGloryFacadeInterface facade;

    private CashEventsNotificator notificator = new CashEventsNotificator(Constants.LOG);

    public static void main(String[] args) {
        String ip = "172.16.1.65";
        int port = 55564;
        if (args != null) {
            ip = args[0];
        }
        System.out.println("Try to connect to Glory on " + ip + ":" + port);
        new CiManager(ip);
    }

    public CiManager(String ip) {
        try {
            facade = new CashGloryFacade(ip, 55564, false);
            new TCPEventsServer(55565, null);
            GloryEventsListener eventsAdapter = new GloryEventsImpl(notificator);
            GloryEventNotificator.INSTANCE.setListener(eventsAdapter);
            initFrame();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    JFrame frame = null;

    private void initFrame() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Glory Cash Infinity Manager");
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(CiManager.class.getResource("/ru/crystals/pos/cash_glory/main.png")));
        frame.setSize(800, 500);

        Container pane = frame.getContentPane();
        JButton b1 = new JButton(new AbstractAction("<HTML><center>Начать прием денег</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    startCashIn();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        JButton b2 = new JButton(new AbstractAction("<HTML><center>Закончить прием денег</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    endCashIn();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        JButton b3 = new JButton(new AbstractAction("<HTML><center>Изъять все деньги</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Object[] options = {"Все", "Все купюры", "Все банкноты"};
                    int n =
                            JOptionPane.showOptionDialog(frame, "Какие типы наличных извлечь?", "Изъятие", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                                options, options[2]);
                    if (n > -1) {
                        fullCashOut(n, null);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        JButton b5 = new JButton(new AbstractAction("<HTML><center>Перезагрузить</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    reboot();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });
        JButton b6 = new JButton(new AbstractAction("<HTML><center>Отменить прием денег</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cashInCancel();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });
        JButton b7 = new JButton(new AbstractAction("<HTML><center>Запросить деньги</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) JOptionPane.showInputDialog(frame, "Введите сумму в копейках", "Запрос денег", JOptionPane.QUESTION_MESSAGE, null, null, "");
                if (!StringUtils.isEmpty(s) && StringUtils.isNumeric(s)) {
                    final long amount = Long.valueOf(s);
                    if (amount > 0) {
                        Executors.newCachedThreadPool().execute(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    requestCash(amount);
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            }

        });

        JButton b9 = new JButton(new AbstractAction("<HTML><center>Отменить запрос денег</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    cancelChangeRequest();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });

        JButton b10 = new JButton(new AbstractAction("<HTML><center>Вернуть деньги</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    returnCash();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });

        JButton b11 = new JButton(new AbstractAction("Сброс") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facade.reset();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });

        JButton b12 = new JButton(new AbstractAction("<HTML><center>Блокировка Разблокировка кассеты</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String[] options = {"Блокировать", "Разблокировать"};
                    int n = JOptionPane.showOptionDialog(frame, "Выберите действие", "Касета", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
                    if (n >= 0)
                        lock(n == 0);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });

        JButton b8 = new JButton(new AbstractAction("<HTML><center>Изъять часть денег</center></HTML>") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String[] options = {"0.05", "0.10", "0.50", "1.00", "2.00", "5.00", "10.00", "50.00", "100.00", "500.00"};
                    int n =
                            JOptionPane.showOptionDialog(frame, "Выберите номинал", "Изъятие", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options,
                                options[9]);
                    if (n >= 0) {
                        fullCashOut(0, BigInteger.valueOf(Math.round(Float.valueOf(options[n]) * 100)));
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }

        });

        JButton b13 = new JButton(new AbstractAction("Подготовка к демо") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    getRandomDenominations();
                } catch (Exception e1) {
                    // TODO: Не забыть прокинуть Exception выше
                    e1.printStackTrace();
                }
            }
        });
        b13.setVisible(false);

        JButton b14 = new JButton(new AbstractAction("Collect MIX") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facade.collect(null, true, true);
                } catch (Exception e1) {
                    // TODO: Не забыть прокинуть Exception выше
                    e1.printStackTrace();
                }
            }
        });
        b14.setVisible(false);
        JButton b17 = new JButton(new AbstractAction("Open") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facade.open();
                } catch (Exception e1) {
                    // TODO: Не забыть прокинуть Exception выше
                    e1.printStackTrace();
                }
            }
        });
        b17.setVisible(false);
        JButton b18 = new JButton(new AbstractAction("Close") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facade.close();
                } catch (Exception e1) {
                    // TODO: Не забыть прокинуть Exception выше
                    e1.printStackTrace();
                }
            }
        });
        b18.setVisible(false);
        JButton b19 = new JButton(new AbstractAction("Manual +10") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    facade.cashManualDeposit(10);
                } catch (Exception e1) {
                    // TODO: Не забыть прокинуть Exception выше
                    e1.printStackTrace();
                }
            }
        });
        b19.setVisible(false);

        pane.add(b1);
        pane.add(b2);
        pane.add(b6);

        JButton btnSumOut = new JButton(new AbstractAction("Изъять сумму") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                final String s = (String) JOptionPane.showInputDialog(frame, "Введите сумму в копейках", "Изъятие", JOptionPane.QUESTION_MESSAGE, null, null, "");
                if (!StringUtils.isEmpty(s) && StringUtils.isNumeric(s)) {
                    long amount = Long.valueOf(s);
                    if (amount > 0) {
                        facade.cashOut(amount);
                    }
                }
            }
        });
        frame.getContentPane().add(btnSumOut);

        pane.add(b3);
        pane.add(b8);
        pane.add(b5);
        pane.add(b7);
        pane.add(b9);
        pane.add(b10);
        pane.add(b11);
        pane.add(b12);
        JButton b4 = new JButton(new AbstractAction("Инвентаризация") {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    inventory();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        b4.setVisible(false);
        pane.add(b4);
        pane.add(b13);
        pane.add(b14);
        pane.add(b17);
        pane.add(b18);
        pane.add(b19);
        pane.setLayout(new GridLayout(4, 5));

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void lock(boolean lock) throws Exception {
        String[] options = {"Купюры", "Монеты"};
        int n = JOptionPane.showOptionDialog(frame, "Выберите оборудование", "Кассета", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (n > -1) {
            if (lock) {
                facade.lock(DeviceType.valueOf(n + 1));
            } else {
                facade.unlock(DeviceType.valueOf(n + 1));
            }
        }
    }

    private void returnCash() throws Exception {
        facade.returnCash();
    }

    public void requestCash(long amount) throws Exception {
        facade.cashRequest(amount);
    }

    public void cancelChangeRequest() throws Exception {
        facade.cancelCashRequest();
    }

    public void getStatus() throws Exception {
        facade.getStatus();
    }

    private void reboot() throws Exception {
        facade.powerControl(1);
    }

    private void cashInCancel() throws Exception {
        Executors.newCachedThreadPool().execute(new Runnable() {

            @Override
            public void run() {
                facade.cancelCashIn();
            }
        });
    }

    private void startCashIn() throws Exception {
        facade.cashIn();
    }

    private void endCashIn() throws Exception {
        facade.cashEnd();
    }

    private void fullCashOut(int n, BigInteger fv) throws Exception {

        CashType cash = new CashType();

        ArrayList<DenominationType> list = new ArrayList<DenominationType>();
        for (CashType cashType : getAllDenomination()) {
            if (cashType.getType().intValue() == 4) {
                for (DenominationType d : cashType.getDenomination()) {
                    if (d.getPiece().intValue() > 0 && d.getCc().equals("RUB") && (d.getDevid().intValue() == n || n == 0) &&
                        (fv == null || (fv != null && d.getFv().intValue() == fv.intValue()))) {
                        final DenominationType outDenom = d;
                        DenominationType denom = new DenominationType() {

                            {
                                setCc(outDenom.getCc());
                                setDevid(outDenom.getDevid());
                                setStatus(BigInteger.valueOf(0));
                                setFv(outDenom.getFv());
                                setPiece(outDenom.getPiece());
                            }
                        };
                        list.add(denom);
                    }
                }
            }
        }

        if (list.size() == 0) {
            System.out.println("Денег нет :(");
            return;
        }

        cash.setDenomination(list);
        facade.cashOut(cash);
    }

    private void getRandomDenominations() throws Exception {
        CashType cash = new CashType();
        Random random = new Random();
        for (CashType c : getAllDenomination()) {
            if (c.getType().intValue() == 4) {
                for (DenominationType d : c.getDenomination()) {
                    if (d.getPiece().intValue() > 0) {
                        int piece = random.nextInt(d.getPiece().intValue());
                        if (piece > 10)
                            piece = 10;
                        if (piece == 0)
                            piece = 5;
                        if (d.getPiece().intValue() < piece)
                            piece = d.getPiece().intValue();
                        d.setPiece(BigInteger.valueOf(piece));
                    }
                }
                cash = c;
            }
        }
        facade.cashOut(cash);
    }

    private List<CashType> getAllDenomination() throws Exception {
        return facade.inventory().getCash();
    }

    private void inventory() throws Exception {
        facade.inventory();
    }
}
