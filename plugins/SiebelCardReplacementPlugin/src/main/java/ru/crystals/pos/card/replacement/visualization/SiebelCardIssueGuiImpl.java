package ru.crystals.pos.card.replacement.visualization;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.crystals.pos.cards.siebel.results.SiebelCardResult;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.check.CheckContainer;
import ru.crystals.siebel.SiebelCardController;
import ru.crystals.siebel.SiebelCardIssueGui;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Реализация UI выдачи карты Siebel. Сделано в виде отдельного Bundle, чтобы не допустить протекания модуля VisualInterface
 * в другие модули.
 *
 * @since 10.2.83.0
 */
// Этот модуль должен инстанцироваться лениво, чтобы на тачкассе не работать.
// Иначе на таче упадёт потому что visualization.jar у него нет.
@Component
@Lazy
public class SiebelCardIssueGuiImpl implements SiebelCardIssueGui {
    private SiebelCardReplacementView view = new SiebelCardReplacementView();

    private AtomicBoolean lock;
    private SiebelCardResult result;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SiebelCardIssueGuiImpl}.
     */
    public SiebelCardIssueGuiImpl() {
        // Должен иметь конструктор без аргументов потому что инстанцируется через рефлексию.
    }

    @Override
    public void start(SiebelCardController controller) {
        lock = new AtomicBoolean(true);
        this.result = null;
        this.view.reset();
        this.view.setIssueCardController(controller);
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(view, lock);
    }

    @Override
    public void showSpinner() {
        view.modelChanged(SiebelCardReplacementModelState.CARD_REPLACEMENT_IN_PROGRESS);
    }

    @Override
    public SiebelCardResult getResult() {
        return result;
    }

    @Override
    public void setResult(SiebelCardResult result) {
        this.result = result;
    }

    @Override
    public void finish() {
        CheckContainer.unlockComponent(lock);
    }
}
