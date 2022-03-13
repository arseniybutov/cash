package ru.crystalservice;

import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.ReversalData;
import ru.crystals.pos.bank.datastruct.SaleData;
import ru.crystals.pos.bank.softcase.BankSoftCaseServiceImpl;

public class TestSoftCase {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BankSoftCaseServiceImpl service = new BankSoftCaseServiceImpl();
        service.setTerminalIp("172.16.130.20");
        service.setTerminalTcpPort(3232);
        service.start();
        SaleData sd = new SaleData();
        sd.setAmount(10L);
        AuthorizationData ad = service.sale(sd);
        Thread.sleep(10000L);
        ReversalData rd = new ReversalData();
        rd.setOriginalSaleTransactionAmount(10L);
        rd.setAmount(10L);
        rd.setRefNumber(ad.getRefNumber());
        service.reversal(rd);
    }
}
