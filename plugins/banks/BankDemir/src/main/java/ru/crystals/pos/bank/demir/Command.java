package ru.crystals.pos.bank.demir;

public class Command {

    private static final String START = "BS2";
    private static final String MESSAGE_TYPE = "01";

    private long cashNumber;
    private ProcessType processType;
    private Long amount;

    public byte[] toByteArray() {
        StringBuilder sb = new StringBuilder();
        sb.append(START)
                .append(MESSAGE_TYPE)
                .append(String.format("%04d", cashNumber))
                // operation sequence No.
                .append("0001")
                .append(processType.getCode());
        if (amount != null) {
            sb.append(String.format("%012d", amount));
        }

        return sb.toString().getBytes();
    }

    public Command setCashNumber(long cashNumber) {
        this.cashNumber = cashNumber;
        return this;
    }

    public Command setProcessType(ProcessType processType) {
        this.processType = processType;
        return this;
    }

    public Command setAmount(long amount) {
        this.amount = amount;
        return this;
    }
}
