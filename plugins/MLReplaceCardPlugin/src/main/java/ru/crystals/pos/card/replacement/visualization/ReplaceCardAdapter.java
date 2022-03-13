package ru.crystals.pos.card.replacement.visualization;

import ru.crystals.pos.card.replacement.MLReplaceCardResult;
import ru.crystals.pos.card.replacement.visualization.listener.MLReplaceStatusListener;
import ru.crystals.pos.card.replacement.visualization.model.ReplaceCardModel;
import ru.crystals.pos.card.replacement.visualization.view.ReplaceCardView;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.ml.MLService;
import ru.crystals.pos.visualization.components.VisualPanel;

/**
 * Created by agaydenger on 08.08.16.
 */
public class ReplaceCardAdapter {
    private ReplaceCardView view;
    private ReplaceCardModel model;
    private ReplaceCardController controller;

    public ReplaceCardAdapter(String oldCardNumber, ProductEntity cardProduct, MLReplaceStatusListener listener, MLService mlService) {
        model = new ReplaceCardModel(cardProduct, oldCardNumber);
        controller = new ReplaceCardController(listener, mlService);
        controller.setModel(model);
        view = new ReplaceCardView();
        view.setController(controller);
        model.addListener(view);
        model.fireModelChanged();
    }

    public VisualPanel getVisualPanel() {
        return view;
    }


    public MLReplaceCardResult getResult() {
        return model.getResult();
    }
}
