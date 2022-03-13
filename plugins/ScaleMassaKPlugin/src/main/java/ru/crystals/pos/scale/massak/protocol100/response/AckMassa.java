package ru.crystals.pos.scale.massak.protocol100.response;

import ru.crystals.pos.scale.massak.protocol100.Protocol100Exception;

/**
 * Data = [ Weight, Division, Stable, Net, Zero, Tare ] <br>
 * <p>
 * Cmd : 0x24 - код ответа на {@link ru.crystals.pos.scale.massak.protocol100.request.GetMassa GetMassa}. <br>
 * <b>Блок Data:</b> <br>
 * Weight : int, 4 байта - текущая масса нетто со знаком. <br>
 * Division : byte - Цена деления. 0=100мг, 1=1г, 2=10г, 3=100г, 4=1кг. <br>
 * Stable : byte - признак стабильности 0, 1. <br>
 * Net : byte - признак индикации на весах знака 'NET'. <br>
 * Zero : byte - признак индикации на весах знака '>0<'. <br>
 * Tare : int, 4 байта - текущая масса тары со знаком. <br>
 */
public class AckMassa extends Response {

    private final int weight = 6;
    private final int division = 10;
    private final int stable = 11;
    private final int net = 12;
    private final int zero = 13;
    private final int tare = 14;

    public AckMassa(byte[] answer) throws Protocol100Exception {
        super(0x24, answer);
    }

    public int getWeight() {
        return parseInt(weight);
    }

    public Division getDivision() {
        return Division.valueOf(answer[division]);
    }

    public boolean isStable() {
        return parseBoolean(stable);
    }

    public boolean isNet() {
        return parseBoolean(net);
    }

    public boolean isZero() {
        return parseBoolean(zero);
    }

    public int getTare() {
        return parseInt(tare);
    }

}
