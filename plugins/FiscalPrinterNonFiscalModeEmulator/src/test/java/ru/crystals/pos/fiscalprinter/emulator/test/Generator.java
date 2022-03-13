package ru.crystals.pos.fiscalprinter.emulator.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import ru.crystals.pos.InventoryOperationType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CheckType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Disc;
import ru.crystals.pos.fiscalprinter.datastruct.documents.DiscType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Margin;
import ru.crystals.pos.fiscalprinter.datastruct.documents.MarginType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Money;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Tax;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

public abstract class Generator {
	protected static Random random = new Random();

	public static Money generateMoneyDocumnet(InventoryOperationType type, int maxValue) throws FiscalPrinterException {
		int maxSum = random.nextInt(maxValue);
		Money result = new Money();
		Cashier cashier = new Cashier();
		cashier.setTabNum("1");
		cashier.setName("Кассирова А.Л.");
		result.setCashier(cashier);
		result.setCurrency("RUB");
		result.setOperationType(type);
		result.setSumCoins((long) (random.nextInt(maxSum) + 1));
		return result;
	}

	public static Check generateCheque(CheckType type, List<PaymentType> availablePaymentTypes, int maxPositionCount) throws FiscalPrinterException {
		Check result = new Check();
		result.setAnnul(false);
		result.setCopy(false);
		result.setType(type);
		result.setDate(Calendar.getInstance().getTime());
		result.setShiftId(0);
		result.setShiftNum(0L);
		result.setDepart(1L);
		result.setCheckNumber(0L);

		Cashier cashier = new Cashier();
		cashier.setName("Кассирова А.Л.");
		cashier.setTabNum("1");
		result.setCashier(cashier);

		Goods pos;
		long checkSum = 0;
		long checkSumWithoutDisc = 0;
		for (int i = 0; i < random.nextInt(maxPositionCount) + 1; i++) {
			pos = new Goods();
			pos.setPositionNum(i+1L);
			pos.setDepartNumber(1L);
			pos.setItem(String.valueOf(random.nextInt(999)));
			pos.setName("ТОВАР " + (i + 1));
			long posPrice = random.nextInt(10000) + 100L;
			long quant = (random.nextInt(10) + 1L);
			pos.setStartPricePerUnit(posPrice);
			pos.setEndPricePerUnit(posPrice);
			pos.setQuant(quant * 1000L);

			checkSumWithoutDisc += posPrice * quant / 1000;

			if (quant == 1) {
				if (random.nextBoolean()) {
					long posDisc = random.nextInt((int) posPrice)+1;
					Disc discount = new Disc(DiscType.SUMMA, "Скидка на позицию", posDisc);
					pos.getDiscs().add(discount);
					pos.setEndPricePerUnit(posPrice - posDisc);
				} else {
					long posSurcharge = random.nextInt((int) posPrice)+1;
					Margin surcharge = new Margin(MarginType.SUMMA, "Наценка на позицию", posSurcharge);
					pos.getMargins().add(surcharge);
					pos.setEndPricePerUnit(posPrice + posSurcharge);
				}
			}

			result.getGoods().add(pos);

			//checkSum += (pos.getQuant() / 1000D * pos.getEndPricePerUnit() / 100D) * 100L;
			checkSum += pos.getEndPricePerUnit() * quant;
		}

		if (random.nextBoolean()) {
			long chequeDisc = random.nextInt((int) checkSum)+1;
			Disc discount = new Disc(DiscType.SUMMA, "Скидка на чек", chequeDisc);
			checkSum -= chequeDisc;
			result.getDiscs().add(discount);
			result.setDiscountValue(chequeDisc);
		} else {
			long chequeSurcharge = random.nextInt((int) checkSum)+1;
			Margin surcharge = new Margin(MarginType.SUMMA, "Наценка на чек", chequeSurcharge);
			checkSum += chequeSurcharge;
			surcharge.setValue(chequeSurcharge);
			result.getMargins().add(surcharge);
			result.setDiscountValue(-chequeSurcharge);
		}
		//result.setDiscountValueTotal(checkSumWithoutDisc - checkSum);
		result.setCheckSumStart(checkSumWithoutDisc);
		result.setCheckSumEnd(checkSum);

		List<Long> pays = new ArrayList<Long>();
		for (PaymentType pay: availablePaymentTypes) {
			System.out.println(pay.getIndexPayment()+" "+pay.getName());
			if (pay.getName() != null && !pay.getName().isEmpty()) {
				pays.add(pay.getIndexPayment());
			}
		}

		Tax tax = new Tax();
		tax.setTax(18f);
		tax.setTaxClass("NDS");
		tax.setTaxSum(Math.round(checkSum - checkSum / 1.18f));
		result.getTaxes().add(tax);

		Payment payment;
		long paySum = 0;
		while (paySum < checkSum) {
			Long restPaySum = checkSum - paySum;

			if (restPaySum < 0)
				break;
			long payIndex = pays.get(random.nextInt(pays.size()));
			long sum = random.nextInt(restPaySum.intValue()) + 1;
			if (payIndex == 0 && type == CheckType.SALE) {
				sum += random.nextInt(10000)+1;
			}

			payment = new Payment();
			payment.setIndexPayment(payIndex);
			payment.setSum(sum);

			result.getPayments().add(payment);

			paySum += payment.getSum();
		}

		return result;
	}

}
