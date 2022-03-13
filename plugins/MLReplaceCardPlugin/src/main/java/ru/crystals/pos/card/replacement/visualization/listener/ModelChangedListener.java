package ru.crystals.pos.card.replacement.visualization.listener;

import ru.crystals.pos.card.replacement.visualization.model.ReplaceCardModelChangedEvent;

/**
 * Created by agaydenger on 08.08.16.
 */
public interface ModelChangedListener {
    void modelChanged(ReplaceCardModelChangedEvent event);
}
