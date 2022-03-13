package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator.preview;

import org.apache.commons.lang.StringUtils;

import javax.swing.DefaultComboBoxModel;

/**
 * Модель выпадающего списка JComboBox.
 * Ограничения на добавление строк:
 * 1. Нельзя добавлять пустые строки (в т.ч. состоящие из одних пробелов)
 * 2. Нельзя добавлять строки, которые уже добавлены.
 * 3. Ограничение на размер списка (по-умолчанию, 10 срок).
 */
class StringComboBoxModel extends DefaultComboBoxModel {
    /**
     * Ограничение на количество запоминаемых строк.
     */
    private int sizeLimit = 20;

    StringComboBoxModel() {
    }

    StringComboBoxModel(int sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    @Override
    public void addElement(Object element) {
        if (needAdd(element)) {
            if (getSize() >= sizeLimit) {
                removeElementAt(0);
            }
            super.addElement(element);
        }
    }

    /**
     * Элемент удовлетворяет условиям добавления?
     */
    protected boolean needAdd(Object element) {
        String selectedStr = (String) element;
        return !StringUtils.isEmpty(selectedStr) && !alreadyAdded(selectedStr);
    }

    private boolean alreadyAdded(String item) {
        return getIndexOf(item) >= 0;
    }

    /**
     * Ограничение на количество запоминаемых строк.
     */
    public int getSizeLimit() {
        return sizeLimit;
    }
}