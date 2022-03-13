package ru.crystals.pos.spi.ui.forms;

import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import ru.crystals.pos.api.ui.listener.TimeoutListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.spi.ui.ControlFactory;
import ru.crystals.pos.spi.ui.forms.components.CircularProgressBar;
import ru.crystals.pos.spi.ui.utils.TextWrapper;
import ru.crystals.pos.visualization.utils.ScaleRectangle;

/**
 * Окно ожидания выполнения длительной операции с обратным отсчетом.<br>
 * Отображает спиннер с обратным отсчетом и поясняющее сообщение. Умеет зажигать событие при завершении обратного отсчета.
 */
public class TimingOutForm extends ContextPanelBase {
    private static final int TIMER_INTERVAL_MS = 1000;
    private static final int PADDING_PX = 20;
    private static final int PROGRESS_CIRCLE_SIZE_PX = 60;
    private int timeout;
    private Timer timer;

    private JProgressBar progressBar;
    private JLabel messageLabel;

    private TimeoutListener timeoutListener;

    public TimingOutForm() {
        super();
        progressBar = new CircularProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum((int)TimeUnit.SECONDS.toMillis(10));
        progressBar.setValue(progressBar.getMaximum());
        progressBar.setStringPainted(true);
        this.add(progressBar, new ScaleRectangle(
                PADDING_PX,
                (HEIGHT_DEFAULT / 2) - (PROGRESS_CIRCLE_SIZE_PX / 2),
                PROGRESS_CIRCLE_SIZE_PX,
                PROGRESS_CIRCLE_SIZE_PX
        ));
        timer = new Timer(TIMER_INTERVAL_MS, e -> {
            timeout -= TIMER_INTERVAL_MS;
            if(timeout <= 0) {
                timeout = 0;
                ((Timer)e.getSource()).stop();
                TimeoutListener tl = getTimeoutListener();
                // Дергаем калбек только в случае, если окно действительно отображается на экране.
                // В противном случае мы можем поймать калбек даже в случае, если на экране отображается
                // совсем уже другое окно, таймер ведь мы не останавливаем и он продолжает тикать.
                if(tl != null && TimingOutForm.this.getParent() != null) {
                    tl.onTimeout();
                }
            }
            progressBar.setValue(timeout);
        });
        timer.setInitialDelay(100); // Хак на случай, если окно задержится с показом.
        messageLabel = ControlFactory.getInstance().createLabel("", Fonts.ITALIC_LARGE, SwingConstants.CENTER);
        this.add(messageLabel, new ScaleRectangle(
                PADDING_PX + PROGRESS_CIRCLE_SIZE_PX + PADDING_PX,
                PADDING_PX - 5, /* Хак для того, чтобы текст равнялся с спиннером */
                WIDTH_DEFAULT - ((2 * PADDING_PX) + PROGRESS_CIRCLE_SIZE_PX),
                HEIGHT_DEFAULT - (2 * PADDING_PX)
        ));
    }

    /**
     * {@inheritDoc}<br>
     * Данный класс не обрабатывает события клавиатуры и всегда возвращает false.
     * @param e клавиатурное событие.
     * @return false
     */
    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        return false;
    }

    /**
     * Возвращает текст поясняющего сообщения, отображаемого на форме.
     * @return текст поясняющего сообщения.
     */
    public String getMessage() {
        return messageLabel.getText();
    }

    /**
     * Устанавливает текст поясняющего сообщения, отображаемого на форме.
     * @param message текст поясняющего сообщения, отображаемого на форме.
     */
    public void setMessage(String message) {
        TextWrapper.wrapLabelText(this.messageLabel, message);
    }

    /**
     * Возвращает начальное значение счетчика для таймера обратного отсчета.
     * @return начальное значение счетчика в миллисекундах для таймера обратного отсчета
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Устанавливает начальное значение счетчика для таймера обратного отсчета.
     * @param timeoutMs начальное значение в миллисекундах счетчика обратного отсчета.
     */
    public void setTimeout(int timeoutMs) {
        this.progressBar.setMaximum(timeoutMs);
        this.timeout = timeoutMs;
    }

    /**
     * Возврващает слушатель окончания обратного отсчета.
     * @return слушатель окончания обратного отсчета.
     */
    public TimeoutListener getTimeoutListener() {
        return timeoutListener;
    }

    /**
     * Устанавливает слушатель окончания обратного отсчета.
     * @param timeoutListener слушатель окончания обратного отсчета.
     */
    public void setTimeoutListener(TimeoutListener timeoutListener) {
        this.timeoutListener = timeoutListener;
    }

    /**
     * Запускает таймер обратного отсчета.
     */
    public void start() {
        timer.stop(); // Хак для останова таймеров в иных инстансах.
        progressBar.setValue(this.progressBar.getMaximum());
        timer.start();
    }
}
