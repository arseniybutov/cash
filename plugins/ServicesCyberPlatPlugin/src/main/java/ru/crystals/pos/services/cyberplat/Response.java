package ru.crystals.pos.services.cyberplat;

public class Response {

	/**
	 * Дата и время получения запроса на проверку номера (местное время на
	 * сервере Киберплат)
	 */
	private String date;

	/**
	 * Уникальный идентификатор сессии
	 */
	private String sessionId;

	/**
	 * true – успех, false – ошибка
	 */
	private boolean result;

	/**
	 * Код ошибки
	 */
	private int errorCode;

	/**
	 * Сообщение об ошибке
	 */
	private String errorMessage;

	/* Getters & setters */

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}
