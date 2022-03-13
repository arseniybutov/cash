package ru.crystals.pos.fiscalprinter.mstar.core.connect.fn;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.transport.mstar.DataPacket;

/**
 * Параметры информационного обмена с оператором фискальных данных
 */
public class StatusOFD {

    /**
     * То что возвращает фискальник, на запрос даты последнего неотправленного документа, если таких документов нет
     */
    private static final String UNSPECIFIED_DATE = "010100";

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    /**
     * Статус информационного обмена
     */
    private long status;
    /**
     * Сообщения для ОФД
     */
    private long messagesForOFD;
    /**
     * Количество сообщений для передачи в ОФД
     */
    private long countMessagesForOFD;
    /**
     * Номер первого в очереди документа для передачи в ОФД
     */
    private long numFirstNoSentDoc;
    /**
     * Дата первого в очереди документа для передачи в ОФД(оригинальная строка)
     */
    private String dateFistNoSentDocOriginStr;
    /**
     * Дата первого в очереди документа для передачи в ОФД
     */
    private Date dateFistNoSentDoc;
    /**
     * Время первого в очереди документа для передачи в ОФД
     */
    private Date timeFistNoSentDoc;

    public StatusOFD(DataPacket dp) throws FiscalPrinterException {
        try {
            status = dp.getLongValue(0);
            messagesForOFD = dp.getLongValue(1);
            countMessagesForOFD = dp.getLongValue(2);
            numFirstNoSentDoc = dp.getLongValue(3);
            dateFistNoSentDocOriginStr = dp.getStringValue(4);
            if (!Objects.equals(UNSPECIFIED_DATE, dateFistNoSentDocOriginStr)) {
                dateFistNoSentDoc = dp.getDateValue(4);
            }
            timeFistNoSentDoc = dp.getTimeValue(5);
        } catch (Exception ex) {
            throw new FiscalPrinterException("Error parse DataPacket", ex);
        }
    }

    public long getStatus() {
        return status;
    }

    public long getMessagesForOFD() {
        return messagesForOFD;
    }

    public long getCountMessagesForOFD() {
        return countMessagesForOFD;
    }

    public long getNumFirstNoSentDoc() {
        return numFirstNoSentDoc;
    }

    public Date getDateFistNoSentDoc() {
        return dateFistNoSentDoc;
    }

    public Date getTimeFistNoSentDoc() {
        return timeFistNoSentDoc;
    }

    @Override
    public String toString() {
        return "StatusOFD{" +
                "status=" + status +
                ", messagesForOFD=" + messagesForOFD +
                ", countMessagesForOFD=" + countMessagesForOFD +
                ", numFirstNoSentDoc=" + numFirstNoSentDoc +
                ", dateFistNoSentDoc=" + Optional.ofNullable(dateFistNoSentDoc).map(date -> dateFormat.format(dateFistNoSentDoc)).orElse(dateFistNoSentDocOriginStr) +
                ", timeFistNoSentDoc=" + timeFormat.format(timeFistNoSentDoc) +
                '}';
    }
}
