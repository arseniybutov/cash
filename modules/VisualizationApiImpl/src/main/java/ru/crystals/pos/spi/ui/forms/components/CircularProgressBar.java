package ru.crystals.pos.spi.ui.forms.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JProgressBar;
import ru.crystals.pos.spi.ui.forms.Fonts;
import ru.crystals.pos.visualization.styles.Color;

/**
 * Кастомный прогресс-бар, который вместо полоски отображает кружочек, как в винде.<br>
 * Сделан именно отдельным классом, чтобы не возиться с L&F, который, как говорят интернеты, может слететь.<br>
 * Украдено <a href="https://stackoverflow.com/questions/36594680/how-to-create-a-circular-progress-component-in-java-swing">отсюда</a>.
 */
public class CircularProgressBar extends JProgressBar {

    public CircularProgressBar() {
        super();
        this.setBorder(null);
        this.setForeground(Color.lightGreyText);
        this.setFont(Fonts.ITALIC_LARGE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Insets b = this.getInsets();
        g.setColor(Color.greyBackground);
        g.setFont(this.getFont());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(this.getForeground());
        double degree = 360 * this.getPercentComplete();
        double sz = Math.min(this.getWidth(), this.getHeight());
        double cx = b.left + this.getWidth()  * .5;
        double cy = b.top  + this.getHeight() * .5;
        double or = sz * .5; // Внешний радиус кольца прогресса
        double ir = or * .8; // Внутренний радиус кольца прогресса.
        Shape inner = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2, ir * 2);
        Shape outer = new Arc2D.Double(cx - or, cy - or, sz, sz, 90 - degree, degree, Arc2D.PIE);
        Area area = new Area(outer);
        area.subtract(new Area(inner));
        g2.fill(area);
        g.setColor(Color.secondTextColor);
        if(this.isStringPainted()) {
            String t = String.valueOf(this.getValue() / 1000);
            Rectangle2D r = g.getFont().createGlyphVector(((Graphics2D) g).getFontRenderContext(), t).getVisualBounds();
            int sw = (int)r.getWidth() / 2;
            int h = (int)r.getHeight() / 2;
            g.drawString(t,
                    (this.getWidth() / 2) - sw,
                   (this.getHeight() / 2) + h
            );
        }
    }


}
