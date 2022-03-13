package ru.crystals.pos.fiscalprinter.nfd.transport.commnads.data.enums;

/**
 * Тип операции.
 * <li>{@link #X_REPORT} - X-отчет</li>
 * <li>{@link #Z_REPORT} - Z-отчет (закрытие смены)</li>
 */
public enum ReportType {

    /**
     * X-отчет.
     */
    X_REPORT,

    /**
     * Z-отчет (закрытие смены)
     */
    Z_REPORT

}
