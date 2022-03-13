package ru.crystals.pos.visualization.products.kit.view;

import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonEditOrDeleteForm;

public class KitEditOrDeleteForm extends CommonEditOrDeleteForm {

    public KitEditOrDeleteForm(XListener outerListener) {
        super(outerListener);
        KitEditOrDeleteForm.this.setName("ru.crystals.pos.visualization.products.kit.view.KitEditOrDeleteForm");
    }

}
