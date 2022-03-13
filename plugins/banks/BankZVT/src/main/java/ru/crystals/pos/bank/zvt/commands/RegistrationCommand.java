package ru.crystals.pos.bank.zvt.commands;

import ru.crystals.pos.bank.zvt.utils.EncodingUtils;

/**
 * Команда регистрации (она же Login в некоторых старых ссылках)
 */
public class RegistrationCommand implements Command {

    private String password;

    public RegistrationCommand(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "0600" + "04" + EncodingUtils.encodeToBCD(password, 6) + "00";
    }

}
