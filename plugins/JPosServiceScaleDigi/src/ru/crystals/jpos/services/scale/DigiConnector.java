package ru.crystals.jpos.services.scale;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import jpos.JposConst;
import jpos.JposException;

public class DigiConnector {

	private final String ENCODING = "cp866";
	private final String ENQ = "\u0005";
	private final String STX = "\u0002";
		
//	private final long TIME_OUT = 1000;

	private InputStream is = null;
	private BufferedInputStream bis = null;
	private InputStreamReader in = null;

	private OutputStream os = null;
	private BufferedOutputStream bos = null;
	private OutputStreamWriter out = null;
	
	private SerialPort serialPort = null;
	
	public void open(String portName, String baudRate, String dataBits, String stopBits, String parity) throws Exception {

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
		in = new InputStreamReader(bis, ENCODING);

		os = serialPort.getOutputStream();
		bos = new BufferedOutputStream(os);
		out = new OutputStreamWriter(bos, ENCODING);
	}
	
	public void close()
	{
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

	private void SendData(String data) throws IOException {

		out.write(data);
		out.flush();
		bos.flush();
		os.flush();
	}
	
	public int getWeight(int timeOut) throws Exception {
		
		long startTime = System.currentTimeMillis();
		StringBuilder packet = new StringBuilder();
		this.SendData(ENQ);
		
		while (true) {
			if (in.ready()) {
				int c = in.read();
				packet.append((char) c);
					
				if (packet.indexOf(STX) == -1) {
					packet = new StringBuilder();
					continue;
				} else if (packet.length() >= 10) {
					break;
				}
			}
			
			if ((System.currentTimeMillis() - startTime) > timeOut) {
				throw new JposException(JposConst.JPOS_E_TIMEOUT, "Scale is not connected");
			}
		}
		byte readCRC = packet.toString().getBytes()[9];

		byte[] arr = packet.toString().getBytes(ENCODING);
		
		byte calcCRC = 0;
	
		for (int i = 0; i < arr.length - 1; i++) {
			calcCRC = (byte) (calcCRC ^ arr[i]);
		}
		
		if (calcCRC != readCRC) {
			throw new Exception("Error CRC: readCRC=" + readCRC + " calcCRC=" + calcCRC);
		}
		else if(arr[0] != 2) throw new JposException(JposConst.JPOSERR, "Error format data");
		else if(arr[1] != 0x53) return 0;//throw new JposException(JposConst.JPOSERR, "Scale is unstable");
		else if(arr[2] == 0x2D) return 0;
		else if(arr[2] == 0x46) throw new JposException(JposConst.JPOSERR, "Over load");		
		
		return Integer.parseInt(packet.substring(3, packet.length() - 1));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try
		{
			DigiConnector dg = new DigiConnector();
			dg.open("COM1", "9600", "8", "1", "0");
//			System.out.println(dg.getWeight());
			dg.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally{
			System.exit(0);
		}

	}

}
