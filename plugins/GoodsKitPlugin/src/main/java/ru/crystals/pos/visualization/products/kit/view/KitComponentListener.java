package ru.crystals.pos.visualization.products.kit.view;

public interface KitComponentListener {

    /**
     * результат добавление компонента набора
     * @param success - true компонент добавлен,
     *                - false отмена/ошибка при добавление компонента
     */
    void addComponentResult(boolean success);

    void showPluginView();

    void hidePluginView();
}
