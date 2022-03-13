package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import ru.crystals.pos.spi.ui.UIForms;

public abstract class ShowcaseScenario {

    protected UIForms formManager;

    public ShowcaseScenario(UIForms formManager) {
        this.formManager = formManager;
    }

    public abstract void run();
}
