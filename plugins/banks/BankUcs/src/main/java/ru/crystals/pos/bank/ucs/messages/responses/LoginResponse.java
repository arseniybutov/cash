package ru.crystals.pos.bank.ucs.messages.responses;

public class LoginResponse extends Response {
    private String infoCode;
    private TerminalStatus terminalStatus = TerminalStatus.UNKNOWN;

    public LoginResponse(String response) {
        super(response);
        if (getLength() == 2) {
            infoCode = getData().substring(0, 1);
            terminalStatus = TerminalStatus.getType(getData().substring(1, 2));
        }
    }

    /**
     * Код запрашивавшейся информации. Присутствует только в случае, если соответствующий параметр передавался в команде 3-0 Login. Значение равно
     * передававшемуся в соответствующей команде 3-0 Login
     */
    public String getInfoCode() {
        return infoCode;
    }

    /**
     * Cостояния EFTPOS устройства. Присутствует только в случае использования расширенного варианта команды 3-0 Login.
     */
    public TerminalStatus getTerminalStatus() {
        return terminalStatus;
    }

    public enum TerminalStatus {
        /**
         * EFTPOS устройство полностью готово к обработке транзакций по картам.
         */
        READY("0"),
        /**
         * EFTPOS устройству требуется инкассация.
         */
        NEED_FINALIZE_DAY("1"),
        /**
         * В EFTPOS устройстве закончилась бумага для печати
         */
        NO_PAPER("2"),
        /**
         * Неизвестное состояние
         */
        UNKNOWN("-1");
        private String value;

        TerminalStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TerminalStatus getType(String value) {
            for (TerminalStatus type : TerminalStatus.values()) {
                if (value.equalsIgnoreCase(type.getValue())) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }

    @Override
    public void setLoggableFields() {
        getLoggerUtil().add("infoCode", getInfoCode());
        getLoggerUtil().add("terminalStatus", getTerminalStatus() + " (" + getTerminalStatus().getValue() + ")");
    }
}
