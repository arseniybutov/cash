package ru.crystals.pos.services.cyberplat;

import ru.crystals.pos.services.exception.ServicesException;

public class Calculator {

	private double commission;
	private long minCommission;
	private long maxCommission;

	public Calculator(double commission, long minCommission, long maxCommission) {
		this.commission = commission;
		this.minCommission = minCommission;
		this.maxCommission = maxCommission;
	}

	public long calculateComission(long money) throws ServicesException {
		if (money <= minCommission) {
			throw new ServicesException(ResBundleServicesCyberPlat.getString("COMISSION_MORE_THEN_OR_EQUAL_TO_SUM"));
		}
		long commissionAmount = (long) ((commission / 100) * money);
		if (commissionAmount <= minCommission) {
			commissionAmount = minCommission;
		}
		if (maxCommission != -1 && commissionAmount > maxCommission) {
			commissionAmount = maxCommission;
		}
		return commissionAmount;
	}

}
