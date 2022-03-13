package ru.crystals.pos.cash_glory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Executors;

import org.mockito.Mockito;

import ru.crystals.pos.cash_machine.CashEventsNotificator;
import ru.crystals.pos.cash_machine.Constants;
import ru.crystals.pos.cash_machine.StatusChange;
import ru.crystals.pos.cash_machine.callbacks.CashRequestCallback;

public class GloryEventsTest {

    public static final int TEST_EVENT_PORT = 63999;
    private static CashRequestCallback cashEventsMock;
    private static TCPEventsClient client;

    //	@BeforeClass
    public static void before() throws InterruptedException {
        cashEventsMock = Mockito.mock(CashRequestCallback.class);
        CashEventsNotificator notificator = new CashEventsNotificator(Constants.LOG);
        //notificator.add(cashEventsMock);
        GloryEventNotificator.INSTANCE.setListener(new GloryEventsImpl(notificator));
        new TCPEventsServer(TEST_EVENT_PORT, null);
        Thread.sleep(1000);
        client = new TCPEventsClient();
    }

    //	@Test
    public void testStatusChangeWaitInsertCashEvent() throws IOException, InterruptedException {
        client.sendStatusChangeWaitInsertCashEvent();
        Thread.sleep(500);
        //Mockito.verify(cashEventsMock).eventCashMachineWaitInsertCash(client.getStatusChangeWaitInsertCashEvent());
        Mockito.reset(cashEventsMock);
    }

    //	@Test
    public void testStatusChangeEventCashMachineIdle() throws IOException, InterruptedException {
        client.sendStatusChangeEventCashMachineIdle();
        Thread.sleep(500);
        // Mockito.verify(cashEventsMock).eventCashMachineIdle(client.getStatusChangeEventCashMachineIdle());
        Mockito.reset(cashEventsMock);
    }

    //	@Test
    public void testStatusChangeEventCashMachineCounting() throws IOException, InterruptedException {
        client.sendStatusChangeEventCashMachineCounting();
        Thread.sleep(500);
        //Mockito.verify(cashEventsMock).eventCashMachineCounting(client.getStatusChangeEventCashMachineCounting());
        Mockito.reset(cashEventsMock);
    }

    //	@Test
    public void testStatusChangeEventCashMachineStartChange() throws IOException, InterruptedException {
        client.sendStatusChangeEventCashMachineStartChange();
        Thread.sleep(500);
        //Mockito.verify(cashEventsMock).eventCashMachineStartChange(client.getStatusChangeEventCashMachineStartChange());
        Mockito.reset(cashEventsMock);
    }

    //	@Test
    public void testStatusChangeEventCashMachineChangeOk() throws IOException, InterruptedException {
        client.sendStatusChangeEventCashMachineChangeOk();
        Thread.sleep(500);
        // Mockito.verify(cashEventsMock).eventCashMachineChangeExists();
        Mockito.reset(cashEventsMock);
    }

}

/****************/

class TCPEventsClient {

    private String[] statusChangeWaitInsertCashEvent = new String[] {"</BbxEventRequest> <BbxEventRequest><StatusChangeEvent>\n",
    "<Status>3</Status>\n", "<Amount>10055</Amount>\n", "<Error>0</Error>\n", "<RecoveryURL/>\n", "<User>JOHN.A</User>\n", "<SeqNo/>\n",
    "</StatusChangeEvent>\n"};

    private String[] statusChangeEventCashMachineIdle = new String[] {"</BbxEventRequest> <BbxEventRequest><StatusChangeEvent>\n",
    "<Status>1</Status>\n", "<Amount>10055</Amount>\n", "<Error>0</Error>\n", "<RecoveryURL/>\n", "<User>JOHN.A</User>\n", "<SeqNo/>\n",
    "</StatusChangeEvent>\n"};

    private String[] statusChangeEventCashMachineCounting = new String[] {"</BbxEventRequest> <BbxEventRequest><StatusChangeEvent>\n",
    "<Status>4</Status>\n", "<Amount>10055</Amount>\n", "<Error>0</Error>\n", "<RecoveryURL/>\n", "<User>JOHN.A</User>\n", "<SeqNo/>\n",
    "</StatusChangeEvent>\n"};

    private String[] statusChangeEventCashMachineStartChange = new String[] {"</BbxEventRequest> <BbxEventRequest><StatusChangeEvent>\n",
    "<Status>2</Status>\n", "<Amount>10055</Amount>\n", "<Error>0</Error>\n", "<RecoveryURL/>\n", "<User>JOHN.A</User>\n", "<SeqNo/>\n",
    "</StatusChangeEvent>\n"};

    private String[] statusChangeEventCashMachineChangeOk = new String[] {"</BbxEventRequest> <BbxEventRequest><StatusChangeEvent>\n",
    "<Status>21</Status>\n", "<Amount>10055</Amount>\n", "<Error>0</Error>\n", "<RecoveryURL/>\n", "<User>JOHN.A</User>\n", "<SeqNo/>\n",
    "</StatusChangeEvent>\n"};

    private Socket socket;
    private DataOutputStream out;

    public TCPEventsClient() {
        init();
    }

    private void init() {
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("127.0.0.1", GloryEventsTest.TEST_EVENT_PORT);
                    out = new DataOutputStream(socket.getOutputStream());
                    if (out == null) {
                        throw new Exception("DataOutputStream not created");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        while (out == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendStatusChangeEventCashMachineChangeOk() throws IOException {
        for (String s : statusChangeEventCashMachineChangeOk) {
            out.writeBytes(s);
        }
    }

    public void sendStatusChangeEventCashMachineStartChange() throws IOException {
        for (String s : statusChangeEventCashMachineStartChange) {
            out.writeBytes(s);
        }
    }

    public void sendStatusChangeEventCashMachineCounting() throws IOException {
        for (String s : statusChangeEventCashMachineCounting) {
            out.writeBytes(s);
        }
    }

    public void sendStatusChangeWaitInsertCashEvent() throws IOException {
        for (String s : statusChangeWaitInsertCashEvent) {
            out.writeBytes(s);
        }
    }

    public void sendStatusChangeEventCashMachineIdle() throws IOException {
        for (String s : statusChangeEventCashMachineIdle) {
            out.writeBytes(s);
        }
    }

    public StatusChange getStatusChangeEventCashMachineChangeOk() {
        return new StatusChange(Integer.valueOf(21), Long.valueOf(10055), Integer.valueOf(0), "");
    }

    public StatusChange getStatusChangeEventCashMachineStartChange() {
        return new StatusChange(Integer.valueOf(2), Long.valueOf(10055), Integer.valueOf(0), "");
    }

    public StatusChange getStatusChangeEventCashMachineCounting() {
        return new StatusChange(Integer.valueOf(4), Long.valueOf(10055), Integer.valueOf(0), "");
    }

    public StatusChange getStatusChangeWaitInsertCashEvent() {
        return new StatusChange(Integer.valueOf(3), Long.valueOf(10055), Integer.valueOf(0), "");
    }

    public StatusChange getStatusChangeEventCashMachineIdle() {
        return new StatusChange(Integer.valueOf(1), Long.valueOf(10055), Integer.valueOf(0), "");
    }

}
