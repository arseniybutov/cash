package ru.crystals.pos.keyboard.geg.kraftwaykb78m12.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.crystals.pos.barcodescanner.BarcodeScannerEvent;
import ru.crystals.pos.keyboard.KeyboardEvent;
import ru.crystals.pos.keyboard.KeyboardImpl;
import ru.crystals.pos.keyboard.datastruct.AlphaNumericKey;
import ru.crystals.pos.keyboard.datastruct.ControlKey;
import ru.crystals.pos.keyboard.datastruct.FunctionKey;
import ru.crystals.pos.keyboard.datastruct.GoodsKey;
import ru.crystals.pos.keyboard.datastruct.ManualAdvActionKey;
import ru.crystals.pos.keyboard.datastruct.PaymentKey;
import ru.crystals.pos.keyboard.datastruct.SaleGroupKey;
import ru.crystals.pos.keyboard.datastruct.UnmappedKey;
import ru.crystals.pos.keyboard.geg.kraftwaykb78m12.KraftwayKB78ServiceImpl;
import ru.crystals.pos.keylock.KeyLockEvent;
import ru.crystals.pos.msr.MSREvent;

public class TestService implements KeyLockEvent, KeyboardEvent, MSREvent, BarcodeScannerEvent {
	
	private static KeyboardImpl module = new KeyboardImpl();
	private static KraftwayKB78ServiceImpl service = new KraftwayKB78ServiceImpl();
	
	public static void main(String[] args) {
		setUp();	
		try {
			service.start();
			while(true) {				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setUp() {
		TestService testService = new TestService();
		module.setLayout("kbd-ncr.xml");		
		
		List<Integer> cardPrefix = new ArrayList<Integer>();
		cardPrefix.add(61);
		service.setCardPrefix(cardPrefix );
		
		List<Integer> cardSufix = new ArrayList<Integer>();
		cardSufix.add(10);
		service.setCardSufix(cardSufix);
		
		List<Integer> cardPrefix2 = new ArrayList<Integer>();
		cardPrefix2.add(45);
		service.setCardPrefix2(cardPrefix2 );		
		
		List<Integer> cardSufix2 = new ArrayList<Integer>();
		cardSufix2.add(10);
		service.setCardSufix2(cardSufix2);
		
		Map<String, Integer> keyLockMap = new HashMap<String, Integer>();
		keyLockMap.put("l", 0);
		keyLockMap.put("r", 1);
		keyLockMap.put("s", 2);
//		keyLockMap.put("D", 3);
//		keyLockMap.put("E", 4);
//		keyLockMap.put("F", 5);
		
		service.setKeyboardModule(module);
		service.setKeyLockListener(testService);
		service.setMsrListener(testService);
		service.setKeyboardTimeOut(20L);
		service.setOtherTimeOut(17L);
		service.setUseKeyLockMap(true);
		service.setKeyLockMap(keyLockMap);
	}

	@Override
	public void eventKeyLock(int position) {
		System.err.println("eventKeyLock =" + position);
		
	}

	@Override
	public void eventAlphaNumericKey(AlphaNumericKey key) {
		System.err.println("eventAlphaNumericKey =" + key.getScanCode());
		
	}

	@Override
	public void eventControlKey(ControlKey key) {
		System.err.println("eventControlKey =" + key.getScanCode());
	}

	@Override
	public void eventFunctionKey(FunctionKey key) {
		System.err.println("eventFunctionKey =" + key.getScanCode());
	}

	@Override
	public void eventGoodsKey(GoodsKey key) {
		System.err.println("eventGoodsKey =" + key.getScanCode());
	}

    @Override public void eventPaymentKey(PaymentKey key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void eventManualAdvActionKey(ManualAdvActionKey key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

	@Override
	public void eventSaleGroupKey(SaleGroupKey key) {
		// do nothing
	}

	@Override
	public void eventUnmappedKey(UnmappedKey key) {
		System.err.println("eventUnmappedKey =" + key.getScanCode());
	}

	@Override
	public void eventMSR(String Track1, String Track2, String Track3, String Track4) {
		System.err.println("Track1 =" + Track1);
		System.err.println("Track2 =" + Track2);
		System.err.println("Track3 =" + Track3);
		System.err.println("Track4 =" + Track4);		
	}

	@Override
	public void eventBarcodeScanner(String barcode) {
		System.err.println("eventBarcodeScanner = " + barcode);		
	}
	
	

}
