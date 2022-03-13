package ru.crystals.pos.cash_glory.xml_interfaces;

import ru.crystals.pos.cash_glory.GloryEventInterface;
import ru.crystals.pos.cash_glory.GloryEventNotificator;

public abstract class AbstractEvents {
	protected GloryEventInterface notificator = GloryEventNotificator.INSTANCE;
}
