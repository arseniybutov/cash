package ru.crystals.pos.cashdrawer.wincor.beetle20;

public class CashDrawerConnector {

	static {
		System.loadLibrary("CashDrawerWincorBeetle20");
	}

	native int getDrawerOpened(int number);

	native int openDrawer(int number);

}
