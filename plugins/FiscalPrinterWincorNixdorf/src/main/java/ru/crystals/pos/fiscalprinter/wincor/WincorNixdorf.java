package ru.crystals.pos.fiscalprinter.wincor;

import org.apache.commons.lang.StringUtils;
import ru.crystals.comportemulator.mstar.MstarCommand;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.mstar.Mstar;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;
import ru.crystals.pos.fiscalprinter.wincor.core.ResBundleFiscalPrinterWincor;

import java.math.BigInteger;
import java.util.List;

/**
 * ФР Wincor Nixdorf TH230+ полностью копирует протокол Mstar, но не хватает некоторых настроек в таблице ККТ,
 * возможно версия прошивки отстает. (19.02.2019)
 */
@PrototypedComponent
public class WincorNixdorf extends Mstar {

    /**
     * конфигурация WincorNixdorf, в ФР отсутствуют настройки COMPRESS_FONT, CHAR_IN_ROW
     *
     * @throws FiscalPrinterException
     */
    @Override
    protected void configureDevice() throws FiscalPrinterException {
        mstarConfig.setAutoWithdrawal(false);
    }

    /**
     * в ФР WincorNixdorf нету ограничения на длинну имени кассира как в Mstar,
     * передаем имя полностью
     *
     * @param cashier кассир
     * @return строка с именем кассира
     */
    @Override
    protected String getCashierName(Cashier cashier) {
        return cashier.getNullSafeName();
    }

    @Override
    public String getDeviceName() {
        return ResBundleFiscalPrinterWincor.getString("DEVICE_NAME_WINCOR");
    }

    @Override
    protected void putGoods(List<Goods> goods, boolean isAsyncMode) throws Exception {
        int posNum = 0;
        for (Goods good : goods) {
            //Добавление признака способа расчета и КТН
            addItemAttributes(good.getCalculationMethod(), getCodeMark(good));
            posNum = putGood(good, posNum, isAsyncMode);
            printProductItem(good);
            printRfidSerialNumber(good);
        }
    }

    private void printProductItem(Goods good) throws Exception {
        if (StringUtils.isNotBlank(good.getItem())) {
            printLine(new FontLine(good.getItem(), Font.NORMAL));
        }
    }

    private void printRfidSerialNumber(Goods good) throws Exception {
        if (StringUtils.isNotBlank(good.getRfidSerialNumber())) {
            String serialNumberText = ResBundleFiscalPrinterWincor.getString("RFID_PRINT_TEXT") + good.getRfidSerialNumber();
            printLine(new FontLine(serialNumberText, Font.NORMAL));
        }
    }

    private void addItemAttributes(Integer calculationMethod, String codeMark) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        dp.putIntValue(calculationMethod);
        //Тег 1212
        dp.putStringValue(StringUtils.EMPTY);
        dp.putStringValue(codeMark);
        //Теги 1191, 1226, 1230, 1231, 1229
        dp.putStringValue(StringUtils.repeat(DataPacket.FS, 4));
        mstarConnector.sendRequest(MstarCommand.SET_ITEM_ATTRIBUTES, dp);
    }

    private String getCodeMark(Goods good) {
        if (StringUtils.isBlank(good.getExcise())) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(good.getMarkCode())
                .append(good.getMarkEanAsHex())
                .append(String.format("%x", new BigInteger(1, good.getSerialNumber().getBytes())).toUpperCase());

        if (good.getMarkMrp() != null) {
            builder.append(String.format("%x", new BigInteger(1, good.getMarkMrp().getBytes())).toUpperCase());
        }
        return builder.toString();
    }
}
