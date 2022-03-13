package ru.crystals.pos.bank.emulator;

import ru.crystals.pos.bank.BankQRPlugin;
import ru.crystals.pos.bank.BankQRProcessingCallback;
import ru.crystals.pos.bank.ResBundleBank;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.exception.BankException;
import ru.crystals.pos.bank.exception.BankInterruptedException;
import ru.crystals.pos.checkdisplay.PictureId;
import ru.crystals.utils.time.Timer;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

public class BankQREmulatorServiceImpl extends BankEmulatorServiceImpl implements BankQRPlugin {

    @Override
    public AuthorizationData saleByQR(SaleData saleData, BankQRProcessingCallback callback) throws BankException {
        callback.eventShowQRCode(readProperties().getQRPayload(),
                getPaymentSystemLogoId(),
                saleData.getAmount(),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(5),
                "");

        final Timer totalTimer = Timer.of(readProperties().getOperationDuration());
        final Timer canBeInterrupted = Timer.of(readProperties().getCanInterruptedFor());

        while (canBeInterrupted.isNotExpired()) {
            if (callback.isStopped()) {
                throw new BankInterruptedException();
            }
            sleep(500);
        }
        callback.eventHideQRCode();
        while (totalTimer.isNotExpired()) {
            sleep(500);
        }
        return createAuthorizationDataForSale(saleData);
    }

    @Override
    public boolean canPayByCustomerQR() {
        return readProperties().canPayByCustomerQR();
    }

    @Override
    public AuthorizationData saleByCustomerQR(SaleData saleData) {
        sleep(readProperties().getOperationDuration().toMillis());
        return createAuthorizationDataForSale(saleData);
    }

    private void sleep(long sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public AuthorizationData refundByQR(RefundData refundData) throws BankException {
        if (refundData.getRefNumber() == null) {
            throw new BankException(ResBundleBank.getString("ARBITRARY_REFUND_DENIED"));
        }
        return createAuthorizationDataForRefund(refundData);
    }

    @Override
    public PictureId getPaymentSystemLogoId() {
        return readProperties().getPaymentSystemLogoId();
    }

    @Override
    public boolean isDailyLog() {
        return false;
    }

    @Override
    public DailyLogData dailyReport(Long cashTransId) {
        return null;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData sale(SaleData saleData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorizationData refund(RefundData refundData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        Set<BankPaymentType> types = readProperties().getSupportedPaymentTypes();
        if (types.isEmpty()) {
            return EnumSet.of(BankPaymentType.QR, BankPaymentType.CARD_INSTALLMENT);
        } else {
            return types;
        }
    }
}
