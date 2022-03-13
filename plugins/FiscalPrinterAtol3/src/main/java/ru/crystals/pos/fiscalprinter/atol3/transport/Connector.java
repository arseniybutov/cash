package ru.crystals.pos.fiscalprinter.atol3.transport;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.Response;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.AbortAction;
import ru.crystals.pos.fiscalprinter.atol3.taskbuffer.actions.Action;

public class Connector {
    private static final Logger logger = LoggerFactory.getLogger(Connector.class);
    private static final int DATA_BITS = 8;
    private static final int STOP_BITS = 1;
    private static final int PARITY = 0;
    private static final int MIN_ID = 0;
    private static final int MAX_ID = 0xDF;
    private static final long RESULT_TIMEOUT = 5_000_000_000L; // 5 sec

    private SerialPort serialPort;
    private Transmitter transmitter;
    private Receiver receiver;
    private int id = MIN_ID;

    public synchronized void open(String portName, int baudRate, boolean useFlowControl) throws IOException {
        try {
            CommPortIdentifier portIdentifier = getPortIdentifier(portName);

            if (portIdentifier.isCurrentlyOwned()) {
                throw new RuntimeException("Port " + portName + " is busy");
            }

            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
            serialPort.setSerialPortParams(baudRate, DATA_BITS, STOP_BITS, PARITY);

            receiver = new Receiver(serialPort.getInputStream());
            receiver.start();

            transmitter = new Transmitter(serialPort.getOutputStream());
        } catch (UnsupportedCommOperationException | NoSuchPortException | PortInUseException e) {
            throw new IOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static CommPortIdentifier getPortIdentifier(String portName) throws NoSuchPortException {
        // get actual enumaration of port identifiers
        Enumeration<CommPortIdentifier> enumeration = (Enumeration<CommPortIdentifier>) CommPortIdentifier.getPortIdentifiers();
        while (enumeration.hasMoreElements()) {
            CommPortIdentifier commPortIdentifier = enumeration.nextElement();
            if (commPortIdentifier.getName().equals(portName)) {
                return commPortIdentifier;
            }
        }

        throw new NoSuchPortException();
    }

    public synchronized void close() throws IOException {
        if (receiver != null) {
            receiver.stop();

            try {
                receiver.close();
            } catch (IOException e) {
                logger.warn("Exception while close receiver: {}", e);
            } finally {
                receiver = null;
            }
        }

        try {
            // trick to allow run() complete
            cast(id, new AbortAction());
        } catch (Exception e) {
            logger.warn("Exception while send last command: {}", e);
        }

        if (transmitter != null) {
            try {
                transmitter.close();
            } catch (IOException e) {
                logger.warn("Exception while close transmitter: {}", e);
            } finally {
                transmitter = null;
            }
        }

        if (serialPort != null) {
            try {
                serialPort.removeEventListener();
                serialPort.close();
            } catch (Exception e) {
                logger.warn("Exception while close serialPort: {}", e);
            } finally {
                serialPort = null;
            }
        }
    }

    // it is always sync
    public synchronized Response call(Action action) throws IOException, InterruptedException {
        cast(id, action);
        Response response = receiver.receive(r -> r.isSyncResultFor(id), RESULT_TIMEOUT);
        incrementId();
        return response;
    }

    public Response waitForAsyncResult(Predicate<Response> predicate) throws IOException, InterruptedException {
        return receiver.receive(predicate, RESULT_TIMEOUT);
    }

    public void clearPendingAsyncError() {
        receiver.clearPendingAsyncError();
    }

    private void cast(int id, Action action) throws IOException {
        Packet packet = Packet.create(id, action);
        transmitter.transmit(packet);
    }

    private void incrementId() {
        if (id < MAX_ID) {
            id += 1;
        } else {
            id = MIN_ID;
        }
    }
}
