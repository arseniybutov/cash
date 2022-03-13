package ru.crystals.pos.bank.emulator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.AbstractBankPluginImpl;
import ru.crystals.pos.bank.ChildCardBankPlugin;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.datastruct.DailyLogData;
import ru.crystals.pos.bank.datastruct.RefundData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ChildCardBankEmulatorServiceImpl extends AbstractBankPluginImpl implements ChildCardBankPlugin {
    private static final Logger logger = LoggerFactory.getLogger(ChildCardBankEmulatorServiceImpl.class);
    private int slipsCount = 2;
    private String innerId = "Emulator2";

    @Override
    public void start() {
        logger.info("{} started", this.getClass().getSimpleName());
    }

    @Override
    public boolean isDailyLog() {
        return true;
    }

    @Override
    public DailyLogData dailyLog(Long cashTransId) {

        DailyLogData result = new DailyLogData();
        result.setSlip(Arrays.asList("Data stub", "data", innerId));
        return result;
    }

    @Override
    public DailyLogData dailyReport(Long cashTransId) {
        DailyLogData result = new DailyLogData();
        result.setSlip(Arrays.asList("Итоги дня", "data", innerId));
        return result;
    }

    public int getSlipsCount() {
        return slipsCount;
    }

    public void setSlipsCount(int slipsCount) {
        this.slipsCount = slipsCount;
    }

    @Override
    public AuthorizationData reversal(ReversalData reversalData) {
        return createAuthorizationDataForReversal(reversalData);
    }

    @Override
    public AuthorizationData sale(SaleData saleData) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            logger.error("Suddenly awoken", e);
        }
        return createAuthorizationDataForSale(saleData);
    }

    @Override
    public AuthorizationData refund(RefundData refundData) {
        return createAuthorizationDataForRefund(refundData);
    }

    private AuthorizationData createAuthorizationDataForSale(SaleData saleData) {
        List<List<String>> slips = new ArrayList<>();
        List<String> list = new ArrayList<>();
        list.add("    ОАО \"Банк  ЗООПАРК\"");
        list.add("     ул. Макакина, д.27");
        list.add("Терминал             " + innerId);
        list.add(new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(new Date()));
        if (saleData instanceof ReversalData) {
            list.add("        ОТМЕНА ПОКУПКИ");
        } else if (saleData instanceof RefundData) {
            list.add("        ВОЗВРАТ ПОКУПКИ");
        } else {
            list.add("        ОПЛАТА ПОКУПКИ");
        }
        if (saleData != null) {
            list.add(BigDecimal.valueOf(saleData.getAmount() / 100.0).setScale(2, RoundingMode.HALF_EVEN) + " " + saleData.getCurrencyCode());
        }
        list.add("Код ответа               001");
        list.add("Код авторизации        98765");
        list.add("Карта        ***********4421");
        list.add("MasterCard             01/17");
        list.add("");
        list.add("");
        list.add("____________________________");
        list.add("           (Подпись клиента)");

        for (int i = 0; i < slipsCount; i++) {
            slips.add(list);
        }

        BankCard bc = new BankCard();
        bc.setCardNumber("************4421");
        bc.setCardType("MasterCard");
        bc.setExpiryDate(new Date());

        AuthorizationData ad = new AuthorizationData();
        if (saleData != null) {
            ad.setAmount(saleData.getAmount());
        }
        if (saleData != null) {
            ad.setCurrencyCode(saleData.getCurrencyCode());
        }
        ad.setDate(new Date());
        ad.setAuthCode("1111111");
        ad.setRefNumber("222");
        ad.setHostTransId(444L);
        if (saleData != null) {
            ad.setCashTransId(saleData.getCashTransId());
        }
        ad.setCard(bc);
        ad.setOperationCode(1L);
        ad.setTerminalId("666");
        ad.setMerchantId("777");
        ad.setResponseCode("555");
        ad.setResultCode(333L);
        ad.setStatus(true);
        ad.setMessage("УСПЕШНО");
        ad.setSlips(slips);

        return ad;
    }

    private AuthorizationData createAuthorizationDataForRefund(RefundData refundData) {
        AuthorizationData ad = createAuthorizationDataForSale(refundData);
        ad.setOperationCode(3L);
        return ad;
    }

    private AuthorizationData createAuthorizationDataForReversal(ReversalData reversalData) {
        AuthorizationData ad = createAuthorizationDataForSale(reversalData);
        ad.setOperationCode(3L);
        return ad;
    }

    @Override
    public AuthorizationData getBankCardBalance() {
        AuthorizationData ad = new AuthorizationData();
        ad.setOperationCode(13L);
        ad.setDate(new Date());
        ad.setAmount(25500L);
        ad.setStatus(true);
        ad.setMessage("УСПЕШНО");
        return ad;
    }
}
