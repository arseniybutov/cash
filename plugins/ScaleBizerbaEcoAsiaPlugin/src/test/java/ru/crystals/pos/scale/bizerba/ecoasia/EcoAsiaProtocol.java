package ru.crystals.pos.scale.bizerba.ecoasia;

import org.junit.Test;

public class EcoAsiaProtocol {
    private BizebraEcoAsiaScalesServiceImpl service = new BizebraEcoAsiaScalesServiceImpl();
    @Test
    public void testStart() throws Exception {
        service.setBaudRate(9600);
        service.setDataBits(8);
        service.setParity(0);
        service.setStopBits(1);
        service.setPort("COM1");
        service.start();
        System.out.println("1 Weight = "+service.getWeight());
        Thread.sleep(2000);
        System.out.println("2 Weight = "+service.getWeight());
        Thread.sleep(3000);
        System.out.println("3 Weight = "+service.getWeight());
        service.stop();
    }
}
