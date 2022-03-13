package ru.crystals.pos.bank.ext;

import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;

import java.time.Instant;
import java.util.Date;

/**
 * На данный момент это класс заглушка для двух банковских процессингов.
 */
public class BankExternalImpl extends AbstractBankPluginImpl {

    @Override
    public void start() {
        // Метод заглушка запуска банковского процессинга - якобы все работает
    }

    @Override
    public boolean isDailyLog() {
        return false;
    }

    @Override
    public boolean canBeUsedWithOtherBanks() {
        return true;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) {
        return null;
    }

    @Override
    public AuthorizationData sale(SaleData saleData) {
        return createAuthorizationData(saleData);
    }

    @Override
    public AuthorizationData refund(RefundData refundData) {
        return createAuthorizationData(refundData);
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) {
        return createAuthorizationData(reversalData);
    }

    private AuthorizationData createAuthorizationData(SaleData saleData) {
        AuthorizationData ad = new AuthorizationData();
        ad.setAmount(saleData.getAmount());
        ad.setCurrencyCode(saleData.getCurrencyCode());
        ad.setDate(Date.from(Instant.now()));
        ad.setCashTransId(saleData.getCashTransId());
        ad.setStatus(true);
        return ad;
    }

}
