package ru.crystals.pos.visualization.products.giftcard.showgiftcardinfo;

import ru.crystals.pos.menu.MenuCommand;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.menu.commands.AbstractCommand;

/**
 * Информация по подарочной карте.
 */
@MenuCommand(name = "command_GiftCardInfo")
public class GiftCardInfoCommand extends AbstractCommand {

    @Override
    public void execute() {
        new GiftCardInfoScenario().execute();
    }

    @Override
    public boolean isCommandAvailable() {
        return Factory.getTechProcessImpl().isShiftOpen();
    }
}
