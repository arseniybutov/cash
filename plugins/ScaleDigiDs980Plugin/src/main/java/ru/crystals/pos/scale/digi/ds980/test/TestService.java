package ru.crystals.pos.scale.digi.ds980.test;

import ru.crystals.pos.CashException;
import ru.crystals.pos.HardwareCOMPortConfig;
import ru.crystals.pos.scale.digi.ds980.DigiDs980ScalesServiceImpl;

public class TestService {

    public static void main(String[] args) {

        HardwareCOMPortConfig config = new HardwareCOMPortConfig();

        DigiDs980ScalesServiceImpl service = new DigiDs980ScalesServiceImpl();

        config.setPort("COM1");
        config.setBaudRate(9600);
        config.setDataBits(8);
        config.setStopBits(1);
        config.setParity(0);

        service.setConfig(config);

        try {
            service.start();
            System.out.println("state = " + service.moduleCheckState());
            System.out.println("weight = " + service.getWeight() + " g");
            service.stop();
        } catch (CashException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

}
