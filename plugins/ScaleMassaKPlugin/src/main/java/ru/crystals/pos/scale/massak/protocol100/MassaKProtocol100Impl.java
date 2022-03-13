package ru.crystals.pos.scale.massak.protocol100;

import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashException;
import ru.crystals.pos.scale.AbstractScalePluginImpl;
import ru.crystals.pos.scale.exception.ScaleException;
import ru.crystals.pos.scale.massak.protocol100.request.GetMassa;
import ru.crystals.pos.scale.massak.protocol100.request.Request;
import ru.crystals.pos.scale.massak.protocol100.request.SetTare;
import ru.crystals.pos.scale.massak.protocol100.request.SetZero;
import ru.crystals.pos.scale.massak.protocol100.response.AckMassa;
import ru.crystals.pos.scale.massak.protocol100.response.AckSet;
import ru.crystals.pos.scale.massak.protocol100.response.AckSetTare;
import ru.crystals.pos.utils.PortAdapterNoConnectionException;
import ru.crystals.pos.utils.SerialPortAdapter;

import java.io.IOException;

class MassaKProtocol100Impl extends AbstractScalePluginImpl {

    public static final Logger LOG = LoggerFactory.getLogger(MassaKProtocol100Impl.class);
    private final SerialPortAdapter portAdapter;

    public MassaKProtocol100Impl() {
        this.portAdapter = new SerialPortAdapter();
    }

    public MassaKProtocol100Impl(SerialPortAdapter portAdapter) {
        this.portAdapter = portAdapter;
    }

    public MassaKProtocol100Impl(String portName) {
        this();
        portAdapter.setPort(portName);
    }

    @Override
    public void start() throws CashException {
        try {
            portAdapter.setLogger(LOG)
                    .setBaudRate(19200)
                    .setStopBits(SerialPort.STOPBITS_1)
                    .setParity(SerialPort.PARITY_EVEN)
                    .openPort();
        } catch (Exception e) {
            LOG.error("Failed to open port", e);
            throw new CashException(e);
        }
    }

    @Override
    public void stop() throws CashException {
        portAdapter.close();
    }

    @Override
    public int getWeight() throws ScaleException {
        try {
            AckMassa ackMassa = new AckMassa(send(new GetMassa()));
            return ackMassa.isStable() ? ackMassa.getWeight() : 0;
        } catch (Exception e) {
            throw new ScaleException(e);
        }
    }

    @Override
    public void setZero() throws ScaleException {
        try {
            AckSet response = new AckSet(send(new SetZero()));
        } catch (Exception e) {
            throw new ScaleException(e);
        }
    }

    @Override
    public void setTare(int tareWeight) throws ScaleException {
        try {
            AckSetTare response = new AckSetTare(send(new SetTare(tareWeight)));
        } catch (Exception e) {
            throw new ScaleException(e);
        }
    }

    @Override
    public Boolean moduleCheckState() {
        return portAdapter.isConnected();
    }

    private byte[] send(Request cmd) throws Exception {
        return send(cmd.constructBytes());
    }

    private byte[] send(byte[] data) throws PortAdapterNoConnectionException, IOException {
        portAdapter.write(data);
        return portAdapter.readBytes(2000L, 220L);
    }

    public void setPort(String portName) {
        this.portAdapter.setPort(portName);
    }
}