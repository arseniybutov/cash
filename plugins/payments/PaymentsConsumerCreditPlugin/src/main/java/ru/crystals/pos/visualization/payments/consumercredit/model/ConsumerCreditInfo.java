package ru.crystals.pos.visualization.payments.consumercredit.model;

import ru.crystals.pos.visualization.payments.common.DefaultPaymentInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Всякая информация об оплате, которую мы храним в процессе раборты внутри плагина
 */
public class ConsumerCreditInfo extends DefaultPaymentInfo {

    /**
     * Выбранный банк
     */
    private String selectedBank;

    /**
     * Выбранный продукт(кредит)
     */
    private String selectedProduct;

    /**
     * Все банки что есть
     */
    private List<String> banks = new ArrayList<>();

    /**
     * Все банковский пролукты для текущего банка
     */
    private List<String> bankProducts = new ArrayList<>();

    /**
     * ФИО клиента
     */
    private String FIO;

    /**
     * Номер контракта
     */
    private String contractNumber;

    /**
     * Показывать наэкранную клавиатуру или нет
     */
    private boolean showOnscreenKeyboard = true;

    private String errorMessage;

    public String getSelectedBank() {
        return selectedBank;
    }

    public void setSelectedBank(String selectedBank) {
        this.selectedBank = selectedBank;
    }

    public String getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(String selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public List<String> getBanks() {
        return banks;
    }

    public void setBanks(List<String> banks) {
        this.banks = banks;
    }

    public List<String> getBankProducts() {
        return bankProducts;
    }

    public void setBankProducts(List<String> bankProducts) {
        this.bankProducts = bankProducts;
    }

    public void setFIO(String FIO) {
        this.FIO = FIO;
    }

    public String getFIO() {
        return FIO;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setShowOnscreenKeyboard(boolean showOnscreenKeyboard) {
        this.showOnscreenKeyboard = showOnscreenKeyboard;
    }

    public boolean isShowOnscreenKeyboard() {
        return showOnscreenKeyboard;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
