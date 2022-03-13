package ru.crystals.pos.visualization.products.spirits.view;

/**
 * Реакция UI формы сканирования АМ
 * Позволяет использовать в контроллере логику, требующую немедленную обработку на UI
 *
 * @author Irodion1
 */
public interface ScanFormUIConsumer {
    /**
     * Показать сообщение
     *
     * @param message - сообщение
     * @param warning - ставить статус warning, или нет
     */
    void showMessage(String message, boolean warning);

    /**
     * Показать сообщение о необходимости сканирования АМ
     */
    void showScanExciseLabel();

    /**
     * Обновить количество на форме. Вернет true, если
     */
    boolean updateQuantity();
}
