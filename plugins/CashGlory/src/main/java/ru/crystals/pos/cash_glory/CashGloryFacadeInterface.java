package ru.crystals.pos.cash_glory;

import jp.co.glory.bruebox.CashType;
import jp.co.glory.bruebox.DenominationType;
import jp.co.glory.bruebox.InventoryResponseType;
import jp.co.glory.bruebox.StatusResponseType;
import ru.crystals.pos.cash_glory.constants.DeviceType;
import ru.crystals.pos.cash_machine.Response;
import ru.crystals.pos.cash_machine.entities.interfaces.DenominationInterface;
import ru.crystals.pos.cash_machine.exceptions.CashMachineException;

public interface CashGloryFacadeInterface {

	public boolean isAlive();

	/**
	 * Отменить запрос конкретной суммы
	 * 
	 */
	public Response cancelCashRequest();

	/**
	 * Выдать деньги (номиналы и количество передать через cashForOut)
	 * 
	 */
	public Response cashOut(CashType cashForOut);

    public Response cashOut(long amount);

	/**
	 * Получить статус оборудования
	 * 
	 * @param type
	 *            :: 0 - без информации по деньгам; 1 - с информацией по деньгам
	 */
	public StatusResponseType getStatus();

	/**
	 * Requests “CI-10(ISP-K05)” to start change transaction.<br>
	 * Respond cash in information and cash out information after the normal finish of change<br>
	 * transaction.<br>
	 * When change transaction was cancelled, cash in information and amount of the refund<br>
	 * will be responded.<br>
	 * 
	 * @param manualAmount
	 */
	public Response cashRequest(long amount);

	/**
	 * To control “CI-10(ISP-K05)” power status（Shutdown/ Reboot）
	 * 
	 * @param type
	 *            0 - выключить 1 - перезагрузить
	 * @param callback
	 *            :: передать null для синхронного вызова
	 * @return вернет null если передан callback
	 */
	public Response powerControl(int type);

	/**
	 * Requests canceling cash-in transaction to the device.<br>
	 * Except “CI-10(ISP-K05)” is in the middle of cash-in transaction, returns result=11<br>
	 * exclusive error.<br>
	 * 
	 */
	public Response cancelCashIn();

	/**
	 * Requests start transaction of cash in to the device.<br>
	 * The device enters cash in status by “StartCashinRequest”.<br>
	 * Cash in status device ends this status by endCashIn command.<br>
	 * 
	 */
	public Response cashIn();

	/**
	 * Requests ending cash-in transaction to the device.<br>
	 * The device enters the status of end of cash in transaction by “EndCashinRequest”.<br>
	 * Except the device is in the middle of the cash in transaction,<br>
	 * returns exclusive error (result=11).<br>
	 * 
	 */
	public Response cashEnd();

	/**
	 * To get the device inventory.<br>
	 * To get the information of device inventory and payable number of the moneys.<br>
	 * 
	 */
    public InventoryResponseType inventory();

	/**
	 * Вернуть монеты и купюры, положенные в приемник, пока оборудование было неактивно
	 * 
	 */
	public Response returnCash();

	public Response open();

	public Response close();

	public Response collect(CashType cash, boolean collectMix, boolean toExit);

	public Response reset();

	public Response lock(DeviceType type);

	public Response unlock(DeviceType type);

	public Response cashManualDeposit(long manualAmount);

    public CashType getDenominationsByAmount(long amount) throws CashMachineException;

    public DenominationType convToDenomintionType(DenominationInterface denomination);

    /**
     * Признак доступности терминала по его сетевому адресу
     * @param timeout время ожидания ответа от терминала (мс)
     */
    boolean isAccessibleByIP(int timeout);
}