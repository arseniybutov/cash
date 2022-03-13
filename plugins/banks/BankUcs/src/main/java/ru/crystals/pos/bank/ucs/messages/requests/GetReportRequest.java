package ru.crystals.pos.bank.ucs.messages.requests;

public class GetReportRequest extends Request {
    private ReportType reportType;

    public GetReportRequest(ReportType reportType) {
        super(RequestType.GET_REPORT);
        this.reportType = reportType;
    }

    @Override
    protected String getDataToString() {
        return this.reportType.getValue();
    }

    public enum ReportType {
        SHORT("2"),
        FULL("3");

        private String value;

        ReportType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

    }

    @Override
    protected void setLoggableFields() {
        getLoggerUtil().add("reportType", reportType);
    }

    public ReportType getReportType() {
        return reportType;
    }
}
