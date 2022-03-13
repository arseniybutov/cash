package ru.crystals.pos.card.replacement.visualization;

import ru.crystals.pos.card.replacement.SiebelCardReplacementListener;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.cards.siebel.results.SiebelCardResult;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.visualization.components.VisualPanel;

/**
 * Адаптер сценария замены карты, совмещающий в себе модель, представление и управлятор процессом замены карты.
 *
 * @since 10.2.83.0
 */
public class SiebelCardReplacementAdapter {
    private SiebelCardReplacementView view;
    private SiebelCardReplacementController controller;
    private SiebelCardReplacementModel model;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SiebelCardReplacementAdapter}.
     *
     * @param oldCardNumber   номер карты, которую намерены заменить.
     * @param currentPurchase текущий чек.
     * @param service         доступ к сервису Siebel.
     * @param listener        слушатель процесса замены карты.
     */
    public SiebelCardReplacementAdapter(String oldCardNumber, PurchaseEntity currentPurchase, SiebelService service, SiebelCardReplacementListener listener) {
        view = new SiebelCardReplacementView();
        model = new SiebelCardReplacementModel();
        model.addListener(view);
        model.setOldCardNumber(oldCardNumber);
        model.setPurchase(currentPurchase);
        controller = new SiebelCardReplacementController(service, listener);
        controller.setModel(model);
        view.setController(controller);
    }

    /**
     * Возвращает графический интерфейс процесса замены карты.
     *
     * @return графический интерфейс процесса замены карты.
     */
    public VisualPanel getView() {
        return view;
    }

    /**
     * Возвращает результат процесса замены карты.
     *
     * @return результат процесса замены карты или null, если такого нет.
     */
    public SiebelCardResult getResult() {
        return model.getResult();
    }
}
