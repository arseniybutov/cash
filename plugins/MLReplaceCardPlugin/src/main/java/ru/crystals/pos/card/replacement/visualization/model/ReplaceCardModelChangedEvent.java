package ru.crystals.pos.card.replacement.visualization.model;

/**
 * Created by agaydenger on 08.08.16.
 */
public class ReplaceCardModelChangedEvent {
    private ReplaceCardModelInfo model;
    private ReplaceCardState state;
    private String messageText;

    public ReplaceCardModelChangedEvent(ReplaceCardModelInfo model, ReplaceCardState state, String messageText) {
        this.model = model;
        this.state = state;
        this.messageText = messageText;
    }

    public ReplaceCardModelInfo getModel() {
        return model;
    }

    public ReplaceCardState getState() {
        return state;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
