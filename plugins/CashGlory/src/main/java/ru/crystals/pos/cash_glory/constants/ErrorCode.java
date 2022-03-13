package ru.crystals.pos.cash_glory.constants;

import java.math.BigInteger;

import ru.crystals.pos.cash_machine.ResBundleCashMachine;

public enum ErrorCode {

	SUCCESS(0, "SUCCESS"),
	CANCEL(1, "CANCEL"),
	RESET(2, "RESET"),
	OCCUPATION_NOT_AVALIABLE(4, "OCCUPATION_NOT_AVALIABLE"),
	CHANGE_SHORTAGE(10, "CHANGE_SHORTAGE"),
	EXCLUSIVE_ERROR(11, "EXCLUSIVE_ERROR"),
	OCCUPIED_BY_ITSELF(17, "OCCUPIED_BY_ITSELF"),
	SESSION_NOT_AVAILABLE(20, "SESSION_NOT_AVAILABLE"),
	INVALID_SESSION(21, "INVALID_SESSION"),
	PROGRAM_INNER_ERROR(99, "PROGRAM_INNER_ERROR");

	private String name;
	private int id;

	private ErrorCode(int id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String toString() {
		return ResBundleCashMachine.getString(name);
	}

	public boolean isEquals(ErrorCode errorCode) {
		return id == errorCode.id;
	}

	public static ErrorCode valueOf(BigInteger code) {
		for (ErrorCode c : ErrorCode.values()) {
			if (c.id == code.intValue()) {
				return c;
			}
		}
		System.err.println("Получен не верный код " + code);
		return null;
	}

	public boolean equals(BigInteger i) {
		return id == i.intValue();
	}

}
