package ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.docreportprint;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.crystals.pos.fiscalprinter.cashboxsystem.core.data.CbsMoney;

import java.util.ArrayList;
import java.util.List;

/**
 * Описывает документ печати zx-отчета. Содержи итоги по операциям с чеками, налогами, скидками, наценками, необнуляемые суммы.
 */
public class DocReportPrint {
    /**
     * Адрес торговой точки, если отсутствует, то передается адрес организации
     */
    @JsonProperty("address")
    private String address;
    /**
     * Наименование организации
     */
    @JsonProperty("company_name")
    private String companyName;
    /**
     * Текущая сумма в кассе
     */
    @JsonProperty("current_cash_sum")
    private CbsMoney currentCashSum;
    /**
     * Дата/время проведения чека в формате ISO 8601 YYYY-MM-DDThh:mm:ss UTC
     */
    @JsonProperty("date_time")
    private String dateTime;
    /**
     * ИИН/БИН организации
     */
    @JsonProperty("iin_bin")
    private String iinBin;
    /**
     * Флаг отрицательной выручки
     */
    @JsonProperty("is_negative_revenue")
    private Boolean isNegativeRevenue;
    /**
     * Номер кассового места
     */
    @JsonProperty("kkm_pos")
    private Integer kkmPos;
    /**
     * Регистрационный номер кассы
     */
    @JsonProperty("kkm_register_number")
    private String kkmRegisterNumber;
    /**
     * Серийный номер кассы
     */
    @JsonProperty("kkm_serial_number")
    private String kkmSerialNumber;
    /**
     * Необнуляемая сумма на начало смены
     */
    @JsonProperty("none_nullable_sum_begin")
    private ReportNoneNullableSum noneNullableSumBegin;
    /**
     * Необнуляемая сумма
     */
    @JsonProperty("none_nullable_sum_end")
    private ReportNoneNullableSum noneNullableSum;
    /**
     * Имя оператора
     */
    @JsonProperty("operator_name")
    private String operatorName;
    /**
     * Имя отчета, “X-ОТЧЕТ” или “Z-ОТЧЕТ”
     */
    @JsonProperty("report_name")
    private String reportName;
    /**
     * Сумма выручки
     */
    @JsonProperty("revenue")
    private CbsMoney revenue;
    /**
     * Номер смены
     */
    @JsonProperty("shift_number")
    private Integer shiftNumber;
    /**
     * Итоги по скидкам
     */
    @JsonProperty("total_discount")
    private TotalOperations totalDiscount;
    /**
     * Итоги по наценкам
     */
    @JsonProperty("total_markup")
    private TotalOperations totalMarkup;
    /**
     * Итоги по операциям внесения/изъятия
     */
    @JsonProperty("total_money_operations")
    private TotalMoneyOperations totalMoneyOperations;
    /**
     * Итоги по всем операциям с учетом скидок и наценок
     */
    @JsonProperty("total_operations")
    private TotalOperations totalOperations;
    /**
     * Итоги по секциям
     */
    @JsonProperty("total_section")
    private List<TotalSection> totalSection = new ArrayList<>();
    /**
     * Массив итогов по налогам. Налог каждого типа гарантировано встречается один раз
     */
    @JsonProperty("total_tax")
    private List<TotalTax> totalTax = new ArrayList<>();
    /**
     * Итоги по чекам с учетом скидок и наценок
     */
    @JsonProperty("total_tickets")
    private TotalTickets totalTickets;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public CbsMoney getCurrentCashSum() {
        return currentCashSum;
    }

    public void setCurrentCashSum(CbsMoney currentCashSum) {
        this.currentCashSum = currentCashSum;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getIinBin() {
        return iinBin;
    }

    public void setIinBin(String iinBin) {
        this.iinBin = iinBin;
    }

    public boolean isNegativeRevenue() {
        return isNegativeRevenue;
    }

    public void setNegativeRevenue(boolean negativeRevenue) {
        isNegativeRevenue = negativeRevenue;
    }

    public Integer getKkmPos() {
        return kkmPos;
    }

    public void setKkmPos(int kkmPos) {
        this.kkmPos = kkmPos;
    }

    public String getKkmRegisterNumber() {
        return kkmRegisterNumber;
    }

    public void setKkmRegisterNumber(String kkmRegisterNumber) {
        this.kkmRegisterNumber = kkmRegisterNumber;
    }

    public String getKkmSerialNumber() {
        return kkmSerialNumber;
    }

    public void setKkmSerialNumber(String kkmSerialNumber) {
        this.kkmSerialNumber = kkmSerialNumber;
    }

    public ReportNoneNullableSum getNoneNullableSumBegin() {
        return noneNullableSumBegin;
    }

    public void setNoneNullableSumBegin(ReportNoneNullableSum noneNullableSumBegin) {
        this.noneNullableSumBegin = noneNullableSumBegin;
    }

    public ReportNoneNullableSum getNoneNullableSum() {
        return noneNullableSum;
    }

    public void setNoneNullableSum(ReportNoneNullableSum noneNullableSum) {
        this.noneNullableSum = noneNullableSum;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public CbsMoney getRevenue() {
        return revenue;
    }

    public void setRevenue(CbsMoney revenue) {
        this.revenue = revenue;
    }

    public Integer getShiftNumber() {
        return shiftNumber;
    }

    public void setShiftNumber(int shiftNumber) {
        this.shiftNumber = shiftNumber;
    }

    public TotalOperations getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(TotalOperations totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public TotalOperations getTotalMarkup() {
        return totalMarkup;
    }

    public void setTotalMarkup(TotalOperations totalMarkup) {
        this.totalMarkup = totalMarkup;
    }

    public TotalMoneyOperations getTotalMoneyOperations() {
        return totalMoneyOperations;
    }

    public void setTotalMoneyOperations(TotalMoneyOperations totalMoneyOperations) {
        this.totalMoneyOperations = totalMoneyOperations;
    }

    public TotalOperations getTotalOperations() {
        return totalOperations;
    }

    public void setTotalOperations(TotalOperations totalOperations) {
        this.totalOperations = totalOperations;
    }

    public List<TotalSection> getTotalSection() {
        return totalSection;
    }

    public void setTotalSection(List<TotalSection> totalSection) {
        this.totalSection = totalSection;
    }

    public List<TotalTax> getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(List<TotalTax> totalTax) {
        this.totalTax = totalTax;
    }

    public TotalTickets getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(TotalTickets totalTickets) {
        this.totalTickets = totalTickets;
    }
}
