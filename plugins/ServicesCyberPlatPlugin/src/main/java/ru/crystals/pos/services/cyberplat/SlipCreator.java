package ru.crystals.pos.services.cyberplat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator;
import ru.crystals.cash.services.cyberplat.operators.OperatorsDocument.Operators.Operator.Receipts.Parameter;
import ru.crystals.pos.services.cyberplat.xml.Constants;

public class SlipCreator {

	private String maintenancePhoneNumber;

	public List<String> createSlip(Operator operator, Request request, String sessionId) {
		List<String> slip = new ArrayList<String>();
		Map<String, String> receiptData = getReceiptData(operator);
		slip.add("Оплата услуг: мобильная связь");
		slip.add("Поставщик услуг:");
		slip.add(receiptData.get(Constants.OPERATOR_NAME));
		slip.add("Телефон поддержки: " + receiptData.get(Constants.OPERATOR_PHONE));
		slip.add("---------------------------------");
		slip.add("Внесено: " + request.getAmountAll() + " Руб");
		slip.add("К зачислению: " + request.getAmount() + " Руб");
		slip.add("Номер телефона: 8" + request.getAccountNumber());
		slip.add("Номер сессии: " + sessionId);
		slip.add("---------------------------------");
		slip.add("");
		slip.add("Сохраняйте чек до зачисления средств");
		slip.add("ОАО \"КИБЕРПЛАТ\"" + getMaintenancePhoneNumber());
		return slip;
	}

	private Map<String, String> getReceiptData(Operator operator) {
		Map<String, String> receiptData = new HashMap<String, String>();
		for (Parameter parameter : operator.getReceipts().getParameterArray()) {
			receiptData.put(parameter.getName(), parameter.getValue());
		}
		return receiptData;
	}

	public void setMaintenancePhoneNumber(String maintenancePhoneNumber) {
		this.maintenancePhoneNumber = maintenancePhoneNumber;
	}

	public String getMaintenancePhoneNumber() {
		return (maintenancePhoneNumber != null && !maintenancePhoneNumber.isEmpty()) ? (" тел. " + maintenancePhoneNumber) : "";
	}

}
