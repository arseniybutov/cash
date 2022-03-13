package ru.crystals.pos.services.cyberplat;

public class Request {

	/**
	 * Код дилера
	 */
	private String dealerCode;

	/**
	 * Код точки приёма
	 */
	private String acquiringPointCode;

	/**
	 * Код точки оператора
	 */
	private String operatorCode;

	/**
	 * Уникальный идентификатор сессии
	 */
	private String sessionId;

	/**
	 * Номер телефона (или номер счёта) абонента
	 */
	private String accountNumber;

	/**
	 * Сумма к зачислению (разделитель – точка)
	 */
	private String amount;

	/**
	 * Полная сумма, полученная от плательщика (разделитель – точка)
	 */
	private String amountAll;

	@Override
	public String toString() {
		return "SD=" + dealerCode + "\r\n" + "AP=" + acquiringPointCode + "\r\n" + "OP=" + operatorCode + "\r\n" + "SESSION=" + sessionId + "\r\n" + "NUMBER="
				+ accountNumber + "\r\n" + "AMOUNT=" + amount + "\r\n" + "AMOUNT_ALL=" + amountAll;
	}

	/* Getters & setters */

	public String getDealerCode() {
		return dealerCode;
	}

	public void setDealerCode(String dealerCode) {
		this.dealerCode = dealerCode;
	}

	public String getAcquiringPointCode() {
		return acquiringPointCode;
	}

	public void setAcquiringPointCode(String acquiringPointCode) {
		this.acquiringPointCode = acquiringPointCode;
	}

	public String getOperatorCode() {
		return operatorCode;
	}

	public void setOperatorCode(String operatorCode) {
		this.operatorCode = operatorCode;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAmountAll() {
		return amountAll;
	}

	public void setAmountAll(String amountAll) {
		this.amountAll = amountAll;
	}

}
