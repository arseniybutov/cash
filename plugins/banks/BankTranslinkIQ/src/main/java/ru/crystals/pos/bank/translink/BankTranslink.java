package ru.crystals.pos.bank.translink;

import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.BankInstallmentPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.datastruct.ServiceBankOperation;
import ru.crystals.pos.bank.exception.BankException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class BankTranslink extends AbstractBankPluginImpl implements BankInstallmentPlugin {

    private static final Set<BankPaymentType> SUPPORTED_PAYMENT_TYPES = Collections.unmodifiableSet(EnumSet.of(BankPaymentType.CARD,
            BankPaymentType.CARD_INSTALLMENT));
    private final TranslinkConnector connector;
    private final TranslinkConfig config;
    private final List<PrintReportOperation> operations = Collections.singletonList(new PrintReportOperation());
    private boolean canRefundInstallment;

    public BankTranslink() {
        this(new TranslinkConnector());
    }

    public BankTranslink(TranslinkConnector connector) {
        this.config = new TranslinkConfig();
        this.connector = connector;
    }

    @Override
    public void start() {
        config.setBaseConfig(getTerminalConfiguration());
        connector.configure(config, getListeners());
    }

    @Override
    public boolean requestTerminalStateIfOnline() {
        return connector.isOnline();
    }

    @Override
    public boolean requestTerminalStateIfOffline() {
        return connector.isOnline();
    }

    @Override
    public List<? extends ServiceBankOperation> getAvailableServiceOperations() {
        return operations;
    }

    @Override
    public List<List<String>> processServiceOperation(ServiceBankOperation operation) throws BankException {
        if (operation instanceof PrintReportOperation) {
            return Collections.singletonList(connector.printReport());
        }
        return Collections.emptyList();
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) throws BankException {
        final List<String> slip = connector.closeDay();
        final DailyLogData dailyLogData = new DailyLogData();
        dailyLogData.setSlip(slip);
        return dailyLogData;
    }

    @Override
    public AuthorizationData sale(SaleData saleData) throws BankException {
        try {
            final AuthorizationData result = connector.sale(saleData);
            result.setPrintNegativeSlip(isPrintNegativeSlip());
            return result;
        } catch (BankException e) {
            if (e.getAuthorizationData() != null) {
                e.getAuthorizationData().setPrintNegativeSlip(isPrintNegativeSlip());
            }
            throw e;
        }
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) throws BankException {
        try {
            final AuthorizationData result = connector.reversal(reversalData);
            result.setPrintNegativeSlip(isPrintNegativeSlip());
            return result;
        } catch (BankException e) {
            if (e.getAuthorizationData() != null) {
                e.getAuthorizationData().setPrintNegativeSlip(isPrintNegativeSlip());
            }
            throw e;
        }
    }

    @Override
    public AuthorizationData refund(RefundData refundData) throws BankException {
        try {
            final AuthorizationData result = connector.refund(refundData);
            result.setPrintNegativeSlip(isPrintNegativeSlip());
            return result;
        } catch (BankException e) {
            if (e.getAuthorizationData() != null) {
                e.getAuthorizationData().setPrintNegativeSlip(isPrintNegativeSlip());
            }
            throw e;
        }
    }


    @Override
    public void sendDialogResponse(BankDialogType dialogType, String response) {
        if (dialogType == BankDialogType.EXTENDED_LIST_SELECTION) {
            connector.onSelectInstallmentOption(response);
        }
    }

    @Override
    public void closeDialog() {
        connector.onSelectInstallmentOption(null);
    }

    public void setLicenseToken(String licenseToken) {
        config.setLicenseToken(StringUtils.trimToNull(licenseToken));
    }

    public void setCardReadTimeout(long cardReadTimeout) {
        config.setCardReadTimeout(cardReadTimeout);
    }

    public void setCloseDayTimeout(long closeDayTimeout) {
        config.setCloseDayTimeout(closeDayTimeout);
    }

    @Override
    public AuthorizationData saleInstallment(SaleData saleData) throws BankException {
        try {
            final AuthorizationData result = connector.installment(saleData);
            result.setPrintNegativeSlip(isPrintNegativeSlip());
            return result;
        } catch (BankException e) {
            if (e.getAuthorizationData() != null) {
                e.getAuthorizationData().setPrintNegativeSlip(isPrintNegativeSlip());
            }
            throw e;
        }
    }

    @Override
    public AuthorizationData refundInstallment(RefundData refundData) throws BankException {
        return refund(refundData);
    }

    @Override
    public AuthorizationData reversalInstallment(ReversalData reversalData) throws BankException {
        return reversal(reversalData);
    }

    @Override
    public boolean canRefundInstallment() {
        return canRefundInstallment;
    }

    public void setCanRefundInstallment(boolean canRefundInstallment) {
        this.canRefundInstallment = canRefundInstallment;
    }

    @Override
    public Set<BankPaymentType> getSupportedPaymentTypes() {
        return SUPPORTED_PAYMENT_TYPES;
    }
}
