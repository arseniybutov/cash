package ru.crystals.jpos.services.scale;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import jpos.JposConst;
import jpos.JposException;

public class DivaConnector {

	private final int W = 0x57;
	private final int CR = 0x0D;
	private final int ETX = 0x03;

	private InputStream is = null;
	private BufferedInputStream bis = null;
	private InputStreamReader in = null;

	private OutputStream os = null;
	private BufferedOutputStream bos = null;
	private OutputStreamWriter out = null;

	private SerialPort serialPort = null;

	public void open(String portName, String baudRate, String dataBits, String stopBits, String parity) throws Exception {
		//System.out.println("	***Connector - open");
		
		int _stopBits = SerialPort.STOPBITS_1;
		int _parity = SerialPort.PARITY_NONE;

		if (stopBits.equals("1.5")) {
			_stopBits = SerialPort.STOPBITS_1_5;
		} else if (stopBits.equals("2")) {
			_stopBits = SerialPort.STOPBITS_2;
		}
		
		parity = parity.toUpperCase();

		if (parity.equals("ODD")) {
			_parity = SerialPort.PARITY_ODD;
		} else if (parity.equals("EVEN")) {
			_parity = SerialPort.PARITY_EVEN;
		} else if (parity.equals("MARK")) {
			_parity = SerialPort.PARITY_MARK;
		} else if (parity.equals("SPACE")) {
			_parity = SerialPort.PARITY_SPACE;
		}
		
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		if (portIdentifier.isCurrentlyOwned()) {
			throw new JposException(JposConst.JPOS_E_CLAIMED, "port is busy");
		}

		serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 50);
		serialPort.setSerialPortParams(Integer.parseInt(baudRate), Integer.parseInt(dataBits), _stopBits, _parity);

		is = serialPort.getInputStream();
		bis = new BufferedInputStream(is);
		in = new InputStreamReader(bis);

		os = serialPort.getOutputStream();
		bos = new BufferedOutputStream(os);
		out = new OutputStreamWriter(bos);

	}

	public void close() {
		//System.out.println("	***Connector - close");
		try {
			serialPort.close();
			
			is.close();
			bis.close();
			in.close();

			os.close();
			bos.close();
			out.close();

		} catch (Exception e) {

		}
	}

	private void sendData(int data) throws IOException {
		//System.out.println("	***Connector - sendDataInt");
		out.write(data);
		out.flush();
		bos.flush();
		os.flush();
	}

	public int getWeight(int timeOut) throws Exception {
		//System.out.println("	***Connector - getWeight");
		
		// TODO create initial variables
		long startTime = System.currentTimeMillis();
		byte[] response = null;
		byte[] errors = new byte[4];		

		// TODO make request
		this.sendData(W);
		this.sendData(CR);

		// TODO get response
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();		
		while (true) {
			if (in.ready()) {
				int b = bis.read();
				buffer.write(b);
				if (b == ETX)
					break;
			} else {
				if ( (System.currentTimeMillis()- startTime) > timeOut) throw new JposException(JposConst.JPOS_E_TIMEOUT, "Error: Scale is not connected");
			}
		}
		response = buffer.toByteArray();

		// TODO get array with status bytes
		for (int i = 0; i < response.length; i++) {
			if (response[i] == 0x53) {
				errors[0] = response[i + 1];
				errors[1] = response[i + 2];
				errors[2] = response[i + 3];
				errors[3] = response[i + 4];
			}
		}

		// TODO throw Exceptions from status array
		if ((errors[0] & 4) != 0) throw new JposException(JposConst.JPOS_E_NOHARDWARE, "Error: RAM error");
		if ((errors[0] & 8) != 0) throw new JposException(JposConst.JPOS_E_NOHARDWARE,	"Error: EEPROM error");
		if ((errors[1] & 4) != 0) throw new JposException(JposConst.JPOS_E_NOHARDWARE, "Error: ROM error");
		if ((errors[1] & 8) != 0) throw new JposException(JposConst.JPOS_E_NOHARDWARE,	"Error: Faulty calibration");
		if ((errors[0] & 1) != 0) return 0;//throw new JposException(JposConst.JPOSERR,	"Error: Scale in motion");
		if ((errors[0] & 2) != 0) return 0;
		if ((errors[1] & 1) != 0) return 0;
		if ((errors[1] & 2) != 0) throw new JposException(JposConst.JPOSERR,	"Error: Over capacity");

		// TODO get array with bytes, witch will get us weight
		byte[] weight = { response[1], response[2], response[3], response[4], response[5], response[6] };

		// TODO get result
		String tmp = new String(weight);
		double tmp2 = new Double(tmp).doubleValue();
		double result = tmp2 * 1000;		
		return (int) result;
	}
	
	public static void main(String[] args) {
		
		try {
			DivaConnector dConnector = new DivaConnector();
			dConnector.open("COM1", "9600", "7", "1", "2");
			System.out.println(dConnector.getWeight(1000));
			dConnector.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}

	}

}
