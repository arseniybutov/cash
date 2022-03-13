package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;


import ru.crystals.pos.spi.ui.UIForms;

public class MessageFormShowcaseScenario extends ShowcaseScenario {
    public MessageFormShowcaseScenario(UIForms f) {
        super(f);
    }

    @Override
    public void run() {
        formManager.showMessageForm(
                "A long time ago, in a galaxy far, far away... It is a period of civil war. " +
                        "Rebel spaceships, striking from a hidden base, have won their first victory " +
                        "against the evil Galactic Empire.",
                null
        );
    }
}
