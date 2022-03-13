package ru.crystals.pos.bank.emulator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.DurationDeserializer;
import ru.crystals.pos.bank.BankDialogType;
import ru.crystals.pos.bank.datastruct.BankPaymentType;
import ru.crystals.pos.checkdisplay.PictureId;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class BankEmulatorProperties {

    /**
     * Данные QR-кода для отображения на кассе
     */
    private String qrPayload = "https://qr.nspk.ru/AD100004BAL7227F9BNP6KNE007J9B3K?type=02&bank=100000000007&sum=12345&cur=RUB&crc=AB75";

    /**
     * Статус доступности терминала
     */
    @JsonProperty("status")
    private boolean status = true;

    /**
     * Время выполнения транзакции (оплата, возврат, отмена)
     */
    @JsonProperty("operationDuration")
    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration operationDuration = Duration.ofSeconds(5);
    /**
     * Время, в течение которого операция может быть прервана кассиром (входит в общее время выполнения {@link #operationDuration})
     */
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonProperty("canInterruptedFor")
    private Duration canInterruptedFor = Duration.ofSeconds(5);

    /**
     * Можно ли выполнить оплату по QR покупателя
     */
    @JsonProperty("canPayByCustomerQR")
    private boolean canPayByCustomerQR;

    @JsonProperty("extendedData")
    private Map<String, String> extendedData;

    /**
     * Интервал, с которым проверяется доступность терминала, если в прошлый раз он был online
     */
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonProperty("checkIntervalWhenOnline")
    private Duration checkIntervalWhenOnline = Duration.ofSeconds(30);

    /**
     * Интервал, с которым проверяется доступность терминала, если в прошлый раз он был offline
     */
    @JsonDeserialize(using = DurationDeserializer.class)
    @JsonProperty("checkIntervalWhenOffline")
    private Duration checkIntervalWhenOffline = Duration.ofSeconds(10);

    @JsonProperty("canRefundInstallment")
    private boolean canRefundInstallment;

    @JsonProperty("canBeUsedWithOtherBanks")
    private boolean canBeUsedWithOtherBanks;

    /**
     * Признак, что оплату можно прервать для расчета скидок
     */
    @JsonProperty("canBeSuspended")
    private boolean canBeSuspended;

    @JsonProperty("authorizationData")
    private AuthorizationDataProperties authorizationData = new AuthorizationDataProperties();

    /**
     * Лого платежной системы для экрана оплаты QR
     */
    @JsonProperty("paymentSystemLogoId")
    private String paymentSystemLogoId;

    @JsonProperty("supportedPaymentTypes")
    private Set<BankPaymentType> supportedPaymentTypes = Collections.emptySet();

    @JsonProperty("needGenerateRequestId")
    private boolean needGenerateRequestId;

    @JsonProperty("bankDialogType")
    private String bankDialogType;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Duration getOperationDuration() {
        return operationDuration;
    }

    public void setOperationDuration(Duration operationDuration) {
        this.operationDuration = operationDuration;
    }

    public Duration getCanInterruptedFor() {
        return canInterruptedFor;
    }

    public void setCanInterruptedFor(Duration canInterruptedFor) {
        this.canInterruptedFor = canInterruptedFor;
    }

    public String getQRPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public boolean canPayByCustomerQR() {
        return canPayByCustomerQR;
    }

    public void setCanPayByCustomerQR(boolean canPayByCustomerQR) {
        this.canPayByCustomerQR = canPayByCustomerQR;
    }

    public Map<String, String> getExtendedData() {
        return extendedData;
    }

    public void setExtendedData(Map<String, String> extendedData) {
        this.extendedData = extendedData;
    }

    public Duration getCheckIntervalWhenOnline() {
        return checkIntervalWhenOnline;
    }

    public void setCheckIntervalWhenOnline(Duration checkIntervalWhenOnline) {
        this.checkIntervalWhenOnline = checkIntervalWhenOnline;
    }

    public Duration getCheckIntervalWhenOffline() {
        return checkIntervalWhenOffline;
    }

    public void setCheckIntervalWhenOffline(Duration checkIntervalWhenOffline) {
        this.checkIntervalWhenOffline = checkIntervalWhenOffline;
    }

    public boolean canRefundInstallment() {
        return canRefundInstallment;
    }

    public void setCanRefundInstallment(boolean canRefundInstallment) {
        this.canRefundInstallment = canRefundInstallment;
    }

    public boolean canBeUsedWithOtherBanks() {
        return canBeUsedWithOtherBanks;
    }

    public void setCanBeUsedWithOtherBanks(boolean canBeUsedWithOtherBanks) {
        this.canBeUsedWithOtherBanks = canBeUsedWithOtherBanks;
    }

    public void setAuthorizationData(AuthorizationDataProperties authorizationData) {
        this.authorizationData = authorizationData;
    }

    public AuthorizationDataProperties getAuthorizationData() {
        return authorizationData;
    }

    public PictureId getPaymentSystemLogoId() {
        if (paymentSystemLogoId == null) {
            return null;
        }
        try {
            return PictureId.valueOf(paymentSystemLogoId);
        } catch (Exception e) {
            return null;
        }
    }

    public void setPaymentSystemLogoId(String paymentSystemLogoId) {
        this.paymentSystemLogoId = paymentSystemLogoId;
    }

    public boolean canBeSuspended() {
        return canBeSuspended;
    }

    public void setCanBeSuspended(boolean canBeSuspended) {
        this.canBeSuspended = canBeSuspended;
    }

    public Set<BankPaymentType> getSupportedPaymentTypes() {
        return supportedPaymentTypes;
    }

    public void setSupportedPaymentTypes(Set<BankPaymentType> types) {
        supportedPaymentTypes = types;
    }

    public void setNeedGenerateRequestId(boolean needGenerateRequestId) {
        this.needGenerateRequestId = needGenerateRequestId;
    }

    public boolean getNeedGenerateRequestId() {
        return needGenerateRequestId;
    }

    public void setBankDialogType(String bankDialogType) {
        this.bankDialogType = bankDialogType;
    }

    public BankDialogType getBankDialogType() {
        BankDialogType bankDialogType;
        try {
            bankDialogType = BankDialogType.valueOf(this.bankDialogType);
        }
        catch (Exception e) {
            bankDialogType = null;
        }
        return bankDialogType;
    }
}
