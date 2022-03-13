package ru.crystals.pos.cash_glory.xml_interfaces;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The device status is notified to have changed.
 * value description
 * 0 Initializing
 * 1 Idle
 * 2 At Starting change
 * 3 Waiting insertion of cash
 * 4 Counting
 * 5 Dispensing
 * 6 Waiting removal of cash in reject
 * 7 Waiting removal of cash out reject
 * 8 Resetting
 * 9 Canceling of Change operation
 * 10 Calculating Change amount
 * 11 Canceling Deposit
 * 12 Collecting
 * 13 Error
 * 14 Upload firmware
 * 15 Reading log
 * 16 Waiting Replenishment
 * 17 Counting Replenishment
 * 18 Unlocking
 * 19 Waiting inventory
 * 20 Fiexd deposit amount
 * 21 Fixed dispense amount
 * 30 Waiting for Error recovery
 * 
 * @author p.tykvin
 * 
 */

@XmlType
public class EventStatusChange {

	@XmlElement(name = "Status")
	private Integer status;

	@XmlElement(name = "Amount")
	private Long amount;

	@XmlElement(name = "Error")
	private Integer error;

	@XmlElement(name = "RecoveryURL")
	private String recoveryURL;

	@XmlElement(name = "User")
	private String user;

	@XmlElement(name = "SeqNo")
	private String seqNo;

	public Integer getStatus() {
		return status;
	}

	public Long getAmount() {
		return amount;
	}

	public Integer getError() {
		return error;
	}

	public String getRecoveryURL() {
		return recoveryURL;
	}

	public String getUser() {
		return user;
	}

	public String getSeqNo() {
		return seqNo;
	}
}
