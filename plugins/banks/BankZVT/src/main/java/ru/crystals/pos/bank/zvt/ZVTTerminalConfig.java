package ru.crystals.pos.bank.zvt;

import ru.crystals.pos.bank.TerminalConfiguration;

public class ZVTTerminalConfig {

    private TerminalConfiguration baseConfiguration;
    /**
     * Таймаут между отправкой команды и получением первичного овтета (80-00/84-XX), секунды
     */
    private int timeoutT3;
    /**
     * Таймаут между получением первичного ответа (80-00) и итоговым ответом по команде (может продлеваться в процессе обмена), секунды
     */
    private int timeoutT4;

    /**
     * Пароль для некоторых команд
     */
    private String password;

    public TerminalConfiguration getBaseConfiguration() {
        return baseConfiguration;
    }

    public void setBaseConfiguration(TerminalConfiguration baseConfiguration) {
        this.baseConfiguration = baseConfiguration;
    }

    public int getTimeoutT3() {
        return timeoutT3;
    }

    public void setTimeoutT3(int timeoutT3) {
        this.timeoutT3 = timeoutT3;
    }

    public int getTimeoutT4() {
        return timeoutT4;
    }

    public void setTimeoutT4(int timeoutT4) {
        this.timeoutT4 = timeoutT4;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
