package ru.crystals.pos.spi.ui.forms;

import javax.swing.JPanel;
import ru.crystals.pos.listeners.XKeyListenerInt;
import ru.crystals.pos.visualization.configurator.view.util.ScalableDimension;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualizationtouch.components.ScaleLayout;

/**
 * Базовый класс для формы, которая расположена ниже стрипа с информационными значками, но выше статусбара интерфейса кассы.
 * Задаёт базовый вид, поведение и имплементацию такой формы.<br/>
 * Другое название этой формы - контекстная панель.
 * <pre>
 * {@code
 * ╔════════════════════════════════════╗
 * ║                                    ║
 * ║                                    ║
 * ╠════════════════════════════════════╣
 * ╠════════════════════════════════════╣
 * ║         Это она                    ║
 * ║                                    ║
 * ╠════════════════════════════════════╣
 * ╚════════════════════════════════════╝
 * }
 * </pre>
 */
public abstract class ContextPanelBase extends JPanel implements XKeyListenerInt {
    
    /**
     * Длина формы.
     */
    protected static final int WIDTH_DEFAULT = 640;
    /**
     * Высота формы.
     */
    protected static final int HEIGHT_DEFAULT = 260;

    public ContextPanelBase() {
        super(new ScaleLayout());
        super.setPreferredSize(new ScalableDimension(WIDTH_DEFAULT, HEIGHT_DEFAULT));
        super.setBackground(Color.greyBackground);
    }
}
