package ru.crystals.pos.barcodescanner.serial;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;


public class BarcodeScannerConnector {

	public static final Logger LOG = LoggerFactory.getLogger(BarcodeScannerConnector.class);

	private SerialPort serialPort;
	private InputStream in = null;

	public void open(BarcodeScannerConfig config) throws Exception {
		log("---{ open }---");

		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(config.getPort());
		if (portIdentifier.isCurrentlyOwned())
			throw new Exception("Port " + config.getPort() + " is busy");

		serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
		serialPort.setSerialPortParams(config.getBaudRate(), config.getDataBits(), config.getStopBits(), config.getParity());
		serialPort.setDTR(true);

		in = serialPort.getInputStream();
	}

	public void close() {
		log("---{ close }---");
		try {
			in.close();
			serialPort.setDTR(false);
			serialPort.close();
		} catch (Exception e) {
			LOG.error("", e);
		}
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
			/* int readed = */in.read(buff);
			// byte ret[]=new byte[readed];
			// System.arraycopy(buff, 0, ret, 0, readed);
			return buff;
		} catch (IOException e) {
			LOG.error("", e);
		}
		return null;
	}

	private void log(String text) {
		LOG.info(text);
		// System.out.println(text);
	}

	public void write(int i) throws IOException {
		serialPort.getOutputStream().write(i);
	}

}
