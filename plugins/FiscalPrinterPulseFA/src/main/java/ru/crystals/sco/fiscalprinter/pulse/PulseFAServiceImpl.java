package ru.crystals.sco.fiscalprinter.pulse;

import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.fiscalprinter.RequisiteType;
import ru.crystals.pos.fiscalprinter.pirit.PiritFN100;

import java.util.List;
import java.util.Map;

/**
 * Плагин для фискальника Пульс ФА, который делает Dream cass.
 * Система кооманд практически идентична системе комманд Pirit.
 * Связь с фискальникам по Ethernet
 *
 * @author s.sergeev
 */
@PrototypedComponent
public class PulseFAServiceImpl extends PiritFN100 {

    @Override
    protected void initPiritConnector() {
        pc = new PulseFAConnector(config.getIpAddress(), config.getTcpPort());
    }

    @Override
    public void setRequisites(Map<RequisiteType, List<String>> requisites) {
        //по ОФД 1.0.5 установка этих параметра нужна только при регистрации фискальника (пульс ломается, если ставить их когда попало)
    }

    @Override
    protected boolean useComProxy() {
        // Запуск comproxy не нужен.
        return false;
    }
}
