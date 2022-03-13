package ru.crystals.pos.visualization.products.giftcard.giftcardreplace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.menu.MenuCommand;
import ru.crystals.pos.visualization.menu.commands.AbstractCommand;

/**
 * Команда "Замена подарочной карты на кассе".
 */
@MenuCommand(name = "command_GiftCardReplace")
public class GiftCardReplaceCommand extends AbstractCommand {
    private static final Logger LOG = LoggerFactory.getLogger(GiftCardReplaceCommand.class);
    private GiftCardReplaceScenary scenary;

    @Override
    public void execute() {
        LOG.debug("Выбран пункт меню \"Замена ПК\"");
        if (scenary == null) {
            scenary = new GiftCardReplaceScenary();
        }
        scenary.execute();
    }

    @Override
    public boolean isCommandAvailable() {
        return true;
    }
}