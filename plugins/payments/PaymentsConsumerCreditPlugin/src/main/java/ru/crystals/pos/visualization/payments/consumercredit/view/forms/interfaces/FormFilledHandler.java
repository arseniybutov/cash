package ru.crystals.pos.visualization.payments.consumercredit.view.forms.interfaces;

/**
 * Created by myaichnikov on 20.11.2014.
 */

/**
 * Интерфейс будет использоваться на формочках для отсылки View статуса заполнения
 */
public interface FormFilledHandler {
    /**
     * Все данные заполнены. Можно передавать управление View
     */
    void proceed();

    /**
     * Заполнение формы отменено. Нужно вернуться на предыдущую
     */
    void cancel();
}
