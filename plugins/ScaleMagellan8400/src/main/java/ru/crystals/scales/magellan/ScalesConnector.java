package ru.crystals.scales.magellan;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ScalesConnector {

	private static final int timeOut = 2000;
	private SerialPort serialPort;
	private InputStream in = null;
	private OutputStream out = null;

	public void open(String port, int baudRate, int dataBits, int stopBits, int parity, int flowControl) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(port);
		if (portIdentifier.isCurrentlyOwned())
			throw new Exception("Port " + port + " is busy");

		serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), timeOut);
		serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
		serialPort.setFlowControlMode(flowControl);
		serialPort.enableReceiveTimeout(timeOut);

		in = serialPort.getInputStream();
		out = serialPort.getOutputStream();
	}

	public int available() throws IOException {
		return in.available();
	}

	public int read() throws IOException {
		return in.read();
	}

	public byte[] readAll() {
		try {
			byte[] buff = new byte[in.available()];
			in.read(buff);
			return buff;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void close() {
		try {
			in.close();
			serialPort.setDTR(false);
			serialPort.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized void write(final byte[] command) throws IOException {
		out.write(command);
	}

	public boolean isActive() {
		return in != null && out != null;
	}

}
