package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import java.util.Arrays;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihModelParams;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;

/**
 * Команда: запрос параметров модели.
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link ShtrihModelParams описание параметров модели}.
 * 
 * @author aperevozchikov
 *
 */
public class GetDeviceParamsCommand extends BaseCommand<ShtrihModelParams> {
    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetDeviceParamsCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-device-params-cmd");
    }

    @Override
    public byte getCommandCode() {
        return (byte) 0xF7;
    }

    @Override
    public byte[] getArguments() {
        // передаем только один аргумент: тип запроса == 1
        return new byte[] {(byte) 1};
    }

    @Override
    public ShtrihModelParams decodeResponse(byte[] response) {
        ShtrihModelParams result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new ShtrihModelParams();
        
        // Параметры модели (8 байт): 4-12:
        result.setParams(ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 4, 12))));
        
        // Ширина печати шрифтом 1 (1 байт):
        result.setFirstFontWidth(response[12]);
        
        // Ширина печати шрифтом 2 (1 байт):
        result.setSecondFontWidth(response[13]);
        
        // Номер первой печатаемой линии в графике (1 байт)
        result.setFirstImageLineNo(response[14]);
        
        // Количество цифр в ИНН (1 байт)
        result.setTinWidth(response[15]);
        
        // Количество цифр в РНМ (1 байт)
        result.setRegNoWidth(response[16]);
        
        // Количество цифр в длинном РНМ (1 байт)
        result.setLongRegNoWidth(response[17]);
        
        // Количество цифр в длинном заводском номере (1 байт)
        result.setLongDeviceNoWidth(response[18]);
        
        // Пароль налогового инспектора по умолчанию (4 байта)
        result.setDefaultTaxCollectorPassword((int) ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 19, 23))));
        
        // Пароль сист.админа по умолчанию (4 байта)
        result.setDefaultAdminPassword((int) ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 23, 27))));
        
        // Номер таблицы "BLUETOOTH БЕСПРОВОДНОЙ МОДУЛЬ" настроек Bluetooth (1 байт)
        result.setBluetoothSettingsTableNo(response[27]);
        
        // Номер поля "НАЧИСЛЕНИЕ НАЛОГОВ" (1 байт)
        result.setChargeTaxesFieldNo(response[28]);
        
        // Максимальная длина команды (N/LEN16) (2 байта)
        result.setMaxCmdLength((int) ShtrihUtils.getLong(ShtrihUtils.inverse(Arrays.copyOfRange(response, 29, 31))));
        
        // Ширина произвольной графической линии в байтах для печати одномерного штрих-кода (1 байт)
        result.setGraphicLineWidthInBytes(response[31]);
        
        // Количество цифр в длинном ИНН (1 байт)
        result.setLongTinWidth(response[32]);
        
        // остальные данные:
        if (response.length > 33) {
            result.setOther(Arrays.copyOfRange(response, 33, response.length));
        }
        
        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть как минимум 33 символа: STX, len, cmd, err, как минимум 28 байт данных, LRC
        if (response.length < 33) {
            return false;
        }

        return true;
    }
}