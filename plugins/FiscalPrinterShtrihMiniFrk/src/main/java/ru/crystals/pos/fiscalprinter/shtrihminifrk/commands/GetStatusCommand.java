package ru.crystals.pos.fiscalprinter.shtrihminifrk.commands;

import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihBoardFlags;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihFlags;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihMode;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihStateDescription;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation.ShtrihSubState;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.utils.ShtrihUtils;
import ru.crystals.pos.utils.PortAdapterUtils;

/**
 * Команда: "Запрос состояния ФР".
 * <p/>
 * {@link #decodeResponse(byte[]) Ответом} данной команды является {@link ShtrihStateDescription состояние ФР}.
 * 
 * @author aperevozchikov
 */
public class GetStatusCommand extends BaseCommand<ShtrihStateDescription> {
    /**
     * Единственно правильный конструктор.
     * 
     * @param password
     *            пароль оператора, от имени которого собираемся выполнить эту команду
     */
    public GetStatusCommand(int password) {
        super(password);
    }

    @Override
    public String toString() {
        return String.format("get-status-cmd [password: %s]", PortAdapterUtils.arrayToString(password));
    }

    @Override
    public byte getCommandCode() {
        return 0x11;
    }

    @Override
    public byte[] getArguments() {
        // "длина" аргументов == 4 байт
        byte[] result = new byte[4];

        // Пароль оператора (4 байта)
        System.arraycopy(password, 0, result, 0, 4);

        return result;
    }

    @Override
    public ShtrihStateDescription decodeResponse(byte[] response) {
        ShtrihStateDescription result;

        if (!validateResponse(response)) {
            return null;
        }
        // ответ полностью валиден и соответсвует протоколу

        result = new ShtrihStateDescription();

        // Версия ПО ФР: 6й и 7й байты:
        result.setSoftwareVersion(getSoftwareVersion(new byte[] {response[5], response[6]}));

        // Сборка ПО ФР: 8й (мл.) и 9й (ст.) байты:
        result.setSoftwareBuild(ShtrihUtils.getInt(response[7], response[8]));

        // Дата ПО ФР: 10й (ДД), 11й (ММ), и 12й (ГГ):
        result.setSoftwareReleaseDate(ShtrihUtils.getDate(response[9], response[10], response[11]));

        // Номер в зале: 13й байт:
        result.setNumber(response[12]);

        // сквозной номер текущего документа: 14й (мл) и 15й (ст):
        result.setCurrentDocNo(ShtrihUtils.getInt(response[13], response[14]));

        // флаги ФР (16й (мл. байт) и 17й (ст. байт) байты в ответе):
        result.setFlags(new ShtrihFlags((short) ShtrihUtils.getInt(response[15], response[16])));

        // режим ФР: 18й байт:
        result.setMode(new ShtrihMode(response[17]));

        // подрежим ФР (19й байт):
        ShtrihSubState subState = ShtrihSubState.getByCode(response[18]);
        result.setSubState(subState);

        // порт ФР: 20й байт:
        result.setPort(response[19]);

        // Версия ПО ФП: 21й и 22й байты:
        result.setFiscalBoardSoftwareVersion(getSoftwareVersion(new byte[] {response[20], response[21]}));

        // Сборка ПО ФП: 23й (мл.) и 24й (ст.) байты:
        result.setFiscalBoardSoftwareBuild(ShtrihUtils.getInt(response[22], response[23]));

        // Дата ПО ФП: 25й (ДД), 26й (ММ), и 27й (ГГ):
        result.setFiscalBoardSoftwareReleaseDate(ShtrihUtils.getDate(response[24], response[25], response[26]));

        // текущее время ККМ: 28й (ДД), 29й (ММ), 30й (ГГ), 31й (ЧЧ), 32й (ММ), 33й (СС):
        result.setCurrentTime(ShtrihUtils.getDate(response[27], response[28], response[29], response[30], response[31], response[32]));

        // флаги ФП: 34й байт:
        result.setBoardFlags(new ShtrihBoardFlags(response[33]));

        // заводской номер: байты от младшего к старшему: 35й - 38й:
        result.setDeviceNo(ShtrihUtils.getLong(new byte[] {response[37], response[36], response[35], response[34]}));
        if (result.getDeviceNo() == 0xFFFFFFFFL) {
            // заводской номер отсутствует!
            result.setDeviceNo(-1L);
        }

        // номер последней закрытой смены: 39й (мл) и 40й (ст):
        result.setLastClosedShiftNo(ShtrihUtils.getInt(response[38], response[39]));

        // количество свободных записей в ФП: 41й (мл) и 42й (ст):
        result.setFreeFiscalRecords(ShtrihUtils.getInt(response[40], response[41]));

        // количество перерегистраций (фискализаций): 43й:
        result.setFiscalizedCount(response[42]);

        // количество оставшихся перерегистраций (фискализаций): 44й:
        result.setFiscalizeCountRemaining(response[43]);

        // ИНН: бйты от младшего к старшему: 45й - 50й:
        result.setTin(ShtrihUtils.getLong(new byte[] {response[49], response[48], response[47], response[46], response[45], response[44]}));
        if (result.getTin() == 0xFFFFFFFFFFFFL) {
            // ИНН не введен
            result.setTin(-1L);
        }

        return result;
    }

    @Override
    public boolean validateResponse(byte[] response) {
        if (!super.validateResponse(response)) {
            return false;
        }

        // длина ответа должна быть 51 байт: 3 служебных байта и 48 байтов "полезной нагрузки"
        if (response.length != 51) {
            return false;
        }

        // 19й байт должен быть в диапазоне 0..5:
        ShtrihSubState subState = ShtrihSubState.getByCode(response[18]);
        if (subState == null) {
            // ответ все-таки не валиден!
            return false;
        }

        return true;
    }

    /**
     * Вернет версию ПО, "зашифрованную" в аргументе.
     * 
     * @param data
     *            массив байт, что содержит информацию о версии ПО
     * @return не <code>null</code>; значение вида "9.9"
     */
    private String getSoftwareVersion(byte[] data) {
        // 1. сначала преобразуем аргемен в строку
        String ver = getString(data);

        // 2. а потом поставим ".":
        String result;
        if (ver.length() > 1) {
            result = ver.substring(0, ver.length() - 1) + "." + ver.substring(ver.length() - 1, ver.length());
        } else {
            result = ver;
        }

        return result;
    }
    
    @Override
    public long getMaxResponseTime() {
        return 20_000L;
    }
}
