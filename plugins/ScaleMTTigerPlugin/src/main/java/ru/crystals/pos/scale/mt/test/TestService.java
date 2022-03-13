package ru.crystals.pos.scale.mt.test;

import ru.crystals.pos.scale.mt.ToledoTigerImpl;

public class TestService {

    public static void main(String[] args) {
        ToledoTigerImpl serialPortAdapter = new ToledoTigerImpl();
        try {
            serialPortAdapter.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                int weight = serialPortAdapter.getWeight();
                System.out.println("weight = " + weight);
                Thread.sleep(1000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                serialPortAdapter.stop();
            } catch (Exception ignore) {
            }
            System.exit(-1);
        }
    }
}
