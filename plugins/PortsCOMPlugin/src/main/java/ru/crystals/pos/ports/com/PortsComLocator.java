package ru.crystals.pos.ports.com;

import gnu.io.CommPortIdentifier;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import ru.crystals.pos.ports.PortInterface;

public class PortsComLocator {
	
	private static final String linuxCOMPortNamePrefix = "/dev/ttyS";
	private static final String linuxUSBPortNamePrefix = "/dev/ttyUSB";
	private static final String linuxSpecificPortNamePrefix = "/dev/usb";			
	
	public static List<PortInterface> getPorts() {
		List<PortInterface> portList = new ArrayList<PortInterface>();
		addOpenPorts(portList);
		if (!isWinOS()) {
			try {
				addClosedPorts(portList);
				addDeviceSpecificAliases(portList);
				
				Collections.sort(portList);
				int i = 1;
				for (PortInterface p: portList){
					if (p.getPortType() == PortInterface.SERIAL)
						p.setUniversalName("COM" + i++);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return portList;
	}
	
	private static void addOpenPorts(List<PortInterface> portList) {
		Enumeration<?> portIdentifierEnum = CommPortIdentifier.getPortIdentifiers();
		
		while (portIdentifierEnum.hasMoreElements()) {
			CommPortIdentifier port = (CommPortIdentifier) portIdentifierEnum.nextElement();
			if (port.getPortType() == CommPortIdentifier.PORT_SERIAL){
				if (isWinOS()){
					portList.add(new PortInterface(port.getName(), port.getName(), PortInterface.SERIAL));
				} else {
					String portName = port.getName();
					if (portName.startsWith(linuxCOMPortNamePrefix)){
						portList.add(new PortInterface(port.getName(), port.getName(), PortInterface.SERIAL));
					} else if ((portName.startsWith(linuxUSBPortNamePrefix))) {
						String uniPortName = "USB" + portName.substring(linuxUSBPortNamePrefix.length());
						portList.add(new PortInterface(uniPortName, port.getName(), PortInterface.USB));
					} if ((portName.startsWith(linuxSpecificPortNamePrefix))) {
						String uniPortName = portName.substring(linuxSpecificPortNamePrefix.length());
						portList.add(new PortInterface(uniPortName, port.getName(), PortInterface.SPECIFIC));
					}
				}
			}
		}
	}

	private static boolean isWinOS() {
		return System.getProperty("os.name").toLowerCase().startsWith("win");
	}

	private static void addClosedPorts(List<PortInterface> portList) throws Exception {
		File lockDir = new File("/var/lock/");
		if (lockDir.exists()) {
			File[] lockFiles = lockDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					boolean accepted = false;
					if (pathname.isFile()) {
						if (pathname.getName().startsWith("LCK..")) {
							accepted = true;
						}
					}
					return accepted;
				}
			});
			for (File lockFile : lockFiles) {
				String portName = "/dev/" + lockFile.getName().substring("LCK..".length());
				if (portName.startsWith(linuxCOMPortNamePrefix)){
					portList.add(new PortInterface(portName, portName, PortInterface.SERIAL));
				} else if ((portName.startsWith(linuxUSBPortNamePrefix))) {
					String uniPortName = "USB" + portName.substring(linuxUSBPortNamePrefix.length());
					portList.add(new PortInterface(uniPortName, portName, PortInterface.USB));
				} if ((portName.startsWith(linuxSpecificPortNamePrefix))) {
					String uniPortName = portName.substring(linuxSpecificPortNamePrefix.length());
					portList.add(new PortInterface(uniPortName, portName, PortInterface.SPECIFIC));
				}
			}
		}
	}

	private static void addDeviceSpecificAliases(List<PortInterface> portList) throws Exception {
		File devDir = new File("/dev/");
		if (devDir.exists()) {
			File[] lockFiles = devDir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					boolean accepted = false;
					if (pathname.getName().startsWith("usb") && !pathname.getName().equalsIgnoreCase("usb"))
						accepted = true;
					return accepted;
				}
			});

			for (File lockFile : lockFiles) {
				String portName = "/dev/" + lockFile.getName();
				if (portName.startsWith(linuxCOMPortNamePrefix)){
					portList.add(new PortInterface(portName, portName, PortInterface.SERIAL));
				} else if ((portName.startsWith(linuxUSBPortNamePrefix))) {
					String uniPortName = "USB" + portName.substring(linuxUSBPortNamePrefix.length());
					portList.add(new PortInterface(uniPortName, portName, PortInterface.USB));
				} if ((portName.startsWith(linuxSpecificPortNamePrefix))) {
					String uniPortName = portName.substring(linuxSpecificPortNamePrefix.length());
					portList.add(new PortInterface(uniPortName, portName, PortInterface.SPECIFIC));
				}
			}
		}
	}

}
