package ru.crystals.pos.siebel;

import ru.crystals.pos.cards.siebel.results.SiebelTokenResult;
import ru.crystals.pos.cards.siebel.results.SiebelTokenThreshold;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.check.CheckContainer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class SiebelTokenSelectorController {
    private AtomicBoolean lock;
    private SiebelTokenSelectorView view;
    // Просто из соображений не ударяться в кровавый интерпрайз не будем делать модельку, будем прямо в контроллере
    // данные хранить. Конечно, это делает его хранящим состояние, но нас это не заботит.
    private List<Map.Entry<Long, List<SiebelTokenThreshold>>> sortedMapEntries;
    private BigDecimal tokenAmount;
    private SiebelTokenResult tokenResult;
    private PositionEntity currentlySelectedPosition;

    public SiebelTokenSelectorController(SiebelTokenSelectorView view) {
        this.view = view;
        this.view.setController(this);
    }

    public void start(SiebelTokenResult tokenResult) {
        this.tokenResult = new SiebelTokenResult();
        this.tokenResult.setCardNumber(tokenResult.getCardNumber());
        this.currentlySelectedPosition = null;
        lock = new AtomicBoolean(true);
        this.tokenAmount = tokenResult.getCardPayment();
        view.reset();
        // Множество неупорядоченное, а мы хотим последовательно по позициям пробегать, посему отсортируем.
        // Сортировка строится на том, что позициям идентификаторы выдавались в том же порядке, в котором позиции добавлялись в чек.
        // Это несколько наивно, но работает.
        sortedMapEntries = new LinkedList<>(tokenResult.getPositionThresholds().entrySet());
        sortedMapEntries.sort((Map.Entry.comparingByKey()));
        next();
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(view, lock);
    }

    public void selectBestOffer() {
        tokenResult = new SiebelTokenResult();
        tokenResult.setBestOffer(true);
        abort();
    }

    public void next() {
        next(null);
    }

    public void next(SiebelTokenThreshold selectedValue) {
        // Простоты ради инициализируем всё.
        if (currentlySelectedPosition != null && tokenResult.getPositionThresholds().get(currentlySelectedPosition.getId()) == null) {
            tokenResult.getPositionThresholds().put(currentlySelectedPosition.getId(), new ArrayList<>());
        }
        if (selectedValue != null && currentlySelectedPosition != null) {
            tokenAmount = tokenAmount.subtract(selectedValue.getTokens());
            tokenResult.getPositionThresholds().get(currentlySelectedPosition.getId()).add(selectedValue);
        } else {
            // Добавим нулевое количество марок в качестве маркера того, что эту позицию мы уже посетили. Потом уберём.
            if (currentlySelectedPosition != null) {
                tokenResult.getPositionThresholds().get(currentlySelectedPosition.getId()).add(new SiebelTokenThreshold());
            }
        }
        if (tokenAmount.compareTo(BigDecimal.ZERO) <= 0) {
            abort();
            return;
        }
        view.setTokenAmount(tokenAmount);

        // Исключим позицию из выбора только при условии, товары в ней закончились или же пользователь отказался выбирать на неё марки
        if ((currentlySelectedPosition != null && selectedValue == null) || (currentlySelectedPosition != null && tokenResult.getPositionThresholds().get(currentlySelectedPosition.getId()).size() == currentlySelectedPosition.getQnty() / 1000)) {
            sortedMapEntries.remove(0);
        }
        if (sortedMapEntries.isEmpty()) {
            abort();
            return;
        }

        Map.Entry<Long, List<SiebelTokenThreshold>> entry = sortedMapEntries.get(0);
        // Уберём предложения для которых марок уже недостаточно.
        entry.getValue().removeIf(token -> token.getTokens().compareTo(tokenAmount) > 0);
        // Если для данной позиции марок уже недостаточно, возьмём следующую или вообще выйдем.
        // Едва ли в чеке будет столько позиций, чтобы случилось переполнение стека, посему
        // неоптимиированная рекурсия здесь нормально.
        if (entry.getValue().isEmpty()) {
            next();
            return;
        }

        currentlySelectedPosition = getPositionById(entry.getKey());
        if (currentlySelectedPosition == null) {
            abort();
            return;
        }
        Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().selectPosition(currentlySelectedPosition.getNumberInt() - 1);
        view.showTokens(entry.getValue());
    }

    private PositionEntity getPositionById(Long id) {
        for (PositionEntity position : Factory.getTechProcessImpl().getCheck().getPositions()) {
            if (id.equals(position.getId())) {
                return position;
            }
        }
        return null;
    }

    public void abort() {
        Factory.getInstance().getMainWindow().getCheckContainer().getPositionsListContainer().selectPosition(-1);
        CheckContainer.unlockComponent(lock);
        // Удалим позиции с нулевыми токенами.
        if (tokenResult != null) {
            tokenResult.getPositionThresholds().entrySet().stream()
                    .forEach(c -> c.getValue().removeIf(v -> v.getTokens() == null || BigDecimal.ZERO.equals(v.getTokens())));
            tokenResult.getPositionThresholds().entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().isEmpty());
        }
    }

    public void reset() {
        tokenResult = null;

    }

    public SiebelTokenResult getTokenResult() {
        return tokenResult;
    }
}
