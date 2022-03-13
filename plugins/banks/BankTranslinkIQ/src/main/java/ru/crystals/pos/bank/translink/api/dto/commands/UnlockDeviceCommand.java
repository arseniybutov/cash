package ru.crystals.pos.bank.translink.api.dto.commands;

import com.fasterxml.jackson.annotation.JsonGetter;
import ru.crystals.pos.bank.translink.api.dto.PosOperation;

import java.util.Objects;

/**
 * Команда выполняет разблокировку POS, для выполнения последующих операций.
 * Данная команда должна выполняться перед началом любой операцией расчёта, инициированной с ECR.
 */
public class UnlockDeviceCommand implements CommandParams {

    /**
     * Тип платежной операции, которая будет инициирована на POS
     */
    private final PosOperation posOperation;

    /**
     * Общая сумма транзакции (включая CashBack)
     */
    private final long amount;

    /**
     * Сумма Cashback
     */
    private final long cashBackAmount;

    /**
     * Код валюты
     */
    private final String currencyCode;

    /**
     * Сообщение, которое будет отображаться на экране POS
     */
    private final String idleText;

    /**
     * Язык интерфейса PUI
     */
    private final String language;

    /**
     * Версия программного обеспечения ECR.
     * Значение данного параметра необходимо для отладки, сбора статистики. Используется для отображения в логах TransLink.iQ. В значении параметра Version необходимо
     * указывать идентификатор реализации интеграции. Формат параметра:
     * ZZZZZZZZ-YYYYYYY-xxxxxxxxx
     * <ul>
     * <li>ZZZZZZZZ – краткое название копании, поставщика ПО кассового оборудования</li>
     * <li>YYYYYYY - краткое название банка эквайера</li>
     * <li>xxxxxxxx - номер версии ПО, отвечающего за работу с модулем xConnect (модератором номер является разработчик ПО кассового оборудования).</li>
     * </ul>
     */
    private final String ecrVersion;

    /**
     * Идентификатор кассира
     */
    private final String operatorId;

    /**
     * Имя, фамилия кассира
     */
    private final String operatorName;

    public UnlockDeviceCommand(PosOperation posOperation, long amount, long cashBackAmount, String currencyCode, String idleText, String language, String ecrVersion,
                               String operatorId, String operatorName) {
        this.posOperation = Objects.requireNonNull(posOperation);
        this.amount = amount;
        this.cashBackAmount = cashBackAmount;
        this.currencyCode = Objects.requireNonNull(currencyCode);
        this.idleText = Objects.requireNonNull(idleText);
        this.language = Objects.requireNonNull(language);
        this.ecrVersion = Objects.requireNonNull(ecrVersion);
        this.operatorId = operatorId;
        this.operatorName = operatorName;
    }

    public static UnlockDeviceCommandBuilder builder() {
        return new UnlockDeviceCommandBuilder();
    }

    @JsonGetter("posOperation")
    public PosOperation getPosOperation() {
        return posOperation;
    }

    @JsonGetter("amount")
    public long getAmount() {
        return amount;
    }

    @JsonGetter("cashBackAmount")
    public long getCashBackAmount() {
        return cashBackAmount;
    }

    @JsonGetter("currencyCode")
    public String getCurrencyCode() {
        return currencyCode;
    }

    @JsonGetter("idleText")
    public String getIdleText() {
        return idleText;
    }

    @JsonGetter("language")
    public String getLanguage() {
        return language;
    }

    @JsonGetter("ecrVersion")
    public String getEcrVersion() {
        return ecrVersion;
    }

    @JsonGetter("operatorId")
    public String getOperatorId() {
        return operatorId;
    }

    @JsonGetter("operatorName")
    public String getOperatorName() {
        return operatorName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnlockDeviceCommand that = (UnlockDeviceCommand) o;
        return amount == that.amount &&
                cashBackAmount == that.cashBackAmount &&
                posOperation == that.posOperation &&
                Objects.equals(currencyCode, that.currencyCode) &&
                Objects.equals(idleText, that.idleText) &&
                Objects.equals(language, that.language) &&
                Objects.equals(ecrVersion, that.ecrVersion) &&
                Objects.equals(operatorId, that.operatorId) &&
                Objects.equals(operatorName, that.operatorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(posOperation, amount, cashBackAmount, currencyCode, idleText, language, ecrVersion, operatorId, operatorName);
    }

    @Override
    public String toString() {
        return "UnlockDeviceCommand{" +
                "posOperation=" + posOperation +
                ", amount=" + amount +
                ", cashBackAmount=" + cashBackAmount +
                ", currencyCode='" + currencyCode + '\'' +
                ", idleText='" + idleText + '\'' +
                ", language='" + language + '\'' +
                ", ecrVersion='" + ecrVersion + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", operatorName='" + operatorName + '\'' +
                '}';
    }

    public static class UnlockDeviceCommandBuilder {
        private PosOperation posOperation;
        private long amount;
        private long cashBackAmount;
        private String currencyCode;
        private String idleText;
        private String language;
        private String ecrVersion;
        private String operatorId;
        private String operatorName;

        UnlockDeviceCommandBuilder() {
        }

        public UnlockDeviceCommandBuilder posOperation(PosOperation posOperation) {
            this.posOperation = posOperation;
            return this;
        }

        public UnlockDeviceCommandBuilder amount(long amount) {
            this.amount = amount;
            return this;
        }

        public UnlockDeviceCommandBuilder cashBackAmount(long cashBackAmount) {
            this.cashBackAmount = cashBackAmount;
            return this;
        }

        public UnlockDeviceCommandBuilder currencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public UnlockDeviceCommandBuilder idleText(String idleText) {
            this.idleText = idleText;
            return this;
        }

        public UnlockDeviceCommandBuilder language(String language) {
            this.language = language;
            return this;
        }

        public UnlockDeviceCommandBuilder ecrVersion(String ecrVersion) {
            this.ecrVersion = ecrVersion;
            return this;
        }

        public UnlockDeviceCommandBuilder operatorId(String operatorId) {
            this.operatorId = operatorId;
            return this;
        }

        public UnlockDeviceCommandBuilder operatorName(String operatorName) {
            this.operatorName = operatorName;
            return this;
        }

        public UnlockDeviceCommand build() {
            return new UnlockDeviceCommand(posOperation, amount, cashBackAmount, currencyCode, idleText, language, ecrVersion, operatorId, operatorName);
        }
    }
}
