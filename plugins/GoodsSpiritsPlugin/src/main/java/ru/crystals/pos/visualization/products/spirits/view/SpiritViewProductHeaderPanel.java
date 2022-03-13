package ru.crystals.pos.visualization.products.spirits.view;

import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductSpiritsEntity;
import ru.crystals.pos.visualization.ResBundleVisualization;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonViewProductHeaderPanel;
import ru.crystals.pos.visualization.styles.Scale;
import ru.crystals.pos.visualization.styles.Style;
import ru.crystals.pos.visualization.utils.ScaleDimension;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

/**
 * Специальная панелька для ИНФО О ТОВАРЕ для алкогольного плагина
 * Дополнительные поля про содержание алкоголя и объем бутылки
 */
public class SpiritViewProductHeaderPanel extends CommonViewProductHeaderPanel {
    protected JLabel jAlcoholContent = new JLabel();
    protected JLabel jAlcoholContentValue = new JLabel();
    protected JLabel jAlcoholVolume = new JLabel();
    protected JLabel jAlcoholVolumeValue = new JLabel();

    public SpiritViewProductHeaderPanel() {
        infoLabel.setPreferredSize(new ScaleDimension(600, 50));

        jNameLabel.setPreferredSize(new ScaleDimension(640, 30));
        jNameLabel.setBorder(new EmptyBorder(0, Scale.getY(20), 0, Scale.getY(14)));
        jAlcoholContent.setText(ResBundleVisualization.getString("SPIRITS") + ":");
        jAlcoholVolume.setText(ResBundleVisualization.getString("VALUME") + ":");

        this.remove(jFirstPriceLabel);
        this.remove(jFirstPriceValueLabel);
        this.remove(jSecondPriceLabel);
        this.remove(jSecondPriceValueLabel);

        Style.setXNewCodeLabelHalfStyle(jAlcoholContent);
        Style.setXNewCodeLabelHalfStyle(jAlcoholVolume);

        Style.setXNewCodeValueLabelHalfStyle(jAlcoholContentValue);
        Style.setXNewCodeValueLabelHalfStyle(jAlcoholVolumeValue);
        this.add(jAlcoholContent);
        this.add(jAlcoholContentValue);
        this.add(jAlcoholVolume);
        this.add(jAlcoholVolumeValue);
        this.add(jFirstPriceLabel);
        this.add(jFirstPriceValueLabel);
        this.add(jSecondPriceLabel);
        this.add(jSecondPriceValueLabel);
    }

    @Override
    public void setHeaderInfo(ProductEntity product) {
        super.setHeaderInfo(product);
        if (product instanceof ProductSpiritsEntity) {
            ProductSpiritsEntity sproduct = (ProductSpiritsEntity) product;
            jAlcoholContentValue.setText(sproduct.getAlcoholicContent() != null ? sproduct.getAlcoholicContent().toString() : "");
            jAlcoholVolumeValue.setText(sproduct.getVolume() != null ? sproduct.getVolume().toString() + " " + ResBundleVisualization.getString("L") : "");
        }
    }
}
