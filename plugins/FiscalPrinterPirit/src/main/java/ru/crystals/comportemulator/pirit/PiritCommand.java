package ru.crystals.comportemulator.pirit;

import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritConnector;

import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Коды команд Пирита.
 *
 * @author dalex
 */
public enum PiritCommand {

    GET_STATUS(0x00, 1500L),
    GET_STATUS_INIT(0x00, SECONDS.toMillis(10), SECONDS.toMillis(5), false),
    GET_STATUS_FOR_NORMALIZE(0x00, SECONDS.toMillis(20), SECONDS.toMillis(10), false),
    GET_COUNTERS(0x01, true),
    GET_INFO(0x02, true),
    GET_RECEIPT_DATA(0x03, true, PiritConnector.READ_TIME_OUT, SECONDS.toMillis(1)),
    GET_PRINTER_STATE(0x04, 1500L),
    GET_SERVICE_INFO(0x05, true),
    START_WORK(0x10),
    GET_CONFIGURATION_TABLE(0x11),
    SET_CONFIGURATION_TABLE(0x12),
    GET_DATE(0x13),
    SET_DATE(0x14),
    SET_LOGO(0x15),
    REMOVE_LOGO(0x16),
    PRINT_X_REPORT(0x20),
    PRINT_Z_REPORT(0x21, SECONDS.toMillis(60)),
    PRINT_CONTROL_TAPE(0x22),
    OPEN_SHIFT_IN_FN(0x23, SECONDS.toMillis(60)),
    ADDITIONAL_REQUISITES(0x24),
    OPEN_DOCUMENT(0x30),
    CLOSE_DOCUMENT(0x31, SECONDS.toMillis(20)),
    CANCEL_DOCUMENT(0x32, SECONDS.toMillis(20)),

    /**
     * Аннулировать документ (0x35) (СКНО) (Беларусь)
     */
    ANNUL_DOCUMENT(0x35, SECONDS.toMillis(10)),

    PRINT_STRING(0x40),
    PRINT_BARCODE(0x41),
    ADD_ITEM(0x42),

    SUBTOTAL(0x44),
    ADD_DISCOUNT(0x45),
    ADD_MARGIN(0x46),
    ADD_PAYMENT(0x47),
    ADD_MONEY_IN_OUT(0x48),
    PRINT_REQUISITE(0x49),
    REGISTER_DEPARTMENT_SUM(0x50),
    COMPARE_CHECK_SUM(0x52),
    OPEN_CHECK_COPY(0x53),
    PRINT_IMAGE_QR(0x55, SECONDS.toMillis(20), SECONDS.toMillis(20)),
    PRINT_GRAPHICS(0x56),
    PRINT_OFD_REQUISITE(0x57),
    PRINT_CURRENT_FN_REPORT(0x59, SECONDS.toMillis(20)),
    REGISTRATION_FISCAL(0x60),
    PRINT_FISCAL_REPORT_BY_SHIFT(0x61),
    PRINT_FISCAL_REPORT_BY_DATE(0x62),
    PRINT_IMAGE_PNG(0x67, SECONDS.toMillis(20), SECONDS.toMillis(20)),
    /**
     * Печать копии фискального документа (СКНО Беларусь)
     */
    PRINT_COPY_DOCUMENT(0x70, SECONDS.toMillis(9)),
    GET_DOCUMENT_FROM_CONTROL_TAPE(0x71, SECONDS.toMillis(20)),
    GET_FN_INFO(0x78, SECONDS.toMillis(20)),
    /**
     * Команда для работы с кодом маркировки
     */
    MARK_CODE(0x79),
    OPEN_MONEY_DRAWER(0x80),
    STATUS_MONEY_DRAWER(0x81, 5000L),
    BEEP(0x82),
    /**
     * Печать статуса GSM связи (СКНО) (Беларусь)
     */
    PRINT_SKNO(0x96),
    RESTART(0x9C),
    PRINT_LAST_Z_REPORT(0xA1),
    PRINT_CORRECTION_RECEIPT(0x58);

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(PiritCommand.class);

    private static Map<Integer, PiritCommand> commands = new HashMap<>();

    private final int commandCode;
    private boolean shouldUseAsExtended;
    private long readTimeOut = PiritConnector.READ_TIME_OUT;
    private long pingTimeOut = PiritConnector.PING_TIME_OUT;

    /**
     * признак основной команды если несколько команд с одним ID
     * /* метод getCommandID(...) - будет возвращает только команду помеченую данным признаком
     * /* метод setCommandsReadTimeout(...) - меняет timeout только у команд помеченных данным признаком
     */
    private boolean mainCommand = true;

    static {
        for (PiritCommand v : PiritCommand.values()) {
            if (v.isMainCommand()) {
                commands.put(v.getCode(), v);
            }
        }
    }

    PiritCommand(int commandId) {
        this.commandCode = commandId;
    }

    PiritCommand(int commandCode, long readTimeOut) {
        this.commandCode = commandCode;
        this.readTimeOut = readTimeOut;
    }

    PiritCommand(int commandCode, long readTimeOut, long pingTimeOut) {
        this.commandCode = commandCode;
        this.readTimeOut = readTimeOut;
        this.pingTimeOut = pingTimeOut;
    }

    PiritCommand(int commandCode, long readTimeOut, long pingTimeOut, boolean mainCommand) {
        this(commandCode, readTimeOut, pingTimeOut);
        this.mainCommand = mainCommand;
    }

    PiritCommand(int commandCode, boolean shouldUseAsExtended) {
        this.commandCode = commandCode;
        this.shouldUseAsExtended = shouldUseAsExtended;
    }

    PiritCommand(int commandCode, boolean shouldUseAsExtended, long readTimeOut, long pingTimeOut) {
        this(commandCode, readTimeOut, pingTimeOut);
        this.shouldUseAsExtended = shouldUseAsExtended;
    }

    public static PiritCommand getCommandID(int commandId) {
        PiritCommand piritCommand = commands.get(commandId);
        if (piritCommand == null) {
            log.error("Command not found. CommandID {}", commandId);
        }
        return piritCommand;
    }

    public int getCode() {
        return commandCode;
    }

    public long getReadTimeOut() {
        return readTimeOut;
    }

    public long getPingTimeOut() {
        return pingTimeOut;
    }

    public boolean isMainCommand() {
        return mainCommand;
    }

    public boolean isShouldUseAsExtended() {
        return shouldUseAsExtended;
    }

    @Override
    public String toString() {
        return name() + String.format("(%02X)", commandCode);
    }

    /**
     * Меняем таймауты ответа по умолчанию
     *
     * @param commandsReadTimeout key - код команды, value - таймаут ms
     */
    public static void setCommandsReadTimeout(Map<Integer, Long> commandsReadTimeout) {
        commandsReadTimeout.forEach((commandCode, newReadTimeout) -> {
            final PiritCommand command = commands.get(commandCode);
            if (command != null) {
                log.debug("Read timeout for command {} changed from {} to {}", command, command.getReadTimeOut(), newReadTimeout);
                command.readTimeOut = newReadTimeout;
            }
        });
    }
}
