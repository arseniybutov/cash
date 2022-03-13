package ru.crystals.pos.bank.tusson.protocol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.bank.BankUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Описывает ответ терминал на команды
 */
public class Response {
    private static final Logger LOG = LoggerFactory.getLogger(Response.class);
    //Все длины указаны в байтах
    public static final int MAX_RESPONSE_LENGTH = 152;
    private static final int SHORT_RESPONSE_LENGTH = 5;
    private static final int SHORT_RESPONSE_WITH_UN_LENGTH = 13;
    private static final int SHORT_RESPONSE_WITH_OPERATION_PARAMS_LENGTH = 52;
    private static final int BANK_ANSWER_FIELD_LENGTH = 12;
    private static final int PAN_FIELD_LENGTH = 19;
    ///
    private ResponseStatus status = ResponseStatus.CANCELED;
    private int packetNumber;
    private long uniqueNumber;
    private byte[] bankAnswer = new byte[BANK_ANSWER_FIELD_LENGTH];
    private boolean transactionCanceled;
    private Operation operation;
    private int operationSum;
    private short currencyCode = 974;
    private byte[] PAN = new byte[PAN_FIELD_LENGTH];
    private byte[] userData;

    public Response(byte[] response) {
        if (response != null && response.length > 0 && response.length <= MAX_RESPONSE_LENGTH) {
            ByteBuffer buffer = ByteBuffer.wrap(response);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            this.packetNumber = buffer.getInt();
            this.status = ResponseStatus.getStatusByCode(buffer.get());
            switch (buffer.capacity()) {
                case SHORT_RESPONSE_LENGTH:
                    break;
                case SHORT_RESPONSE_WITH_UN_LENGTH:
                    this.uniqueNumber = buffer.getLong();
                    break;
                case SHORT_RESPONSE_WITH_OPERATION_PARAMS_LENGTH:
                    fillOperationParams(buffer);
                    break;
                default:
                    if (response.length > SHORT_RESPONSE_WITH_OPERATION_PARAMS_LENGTH) {
                        fillOperationParams(buffer);
                        this.userData = new byte[buffer.remaining()];
                        buffer.get(userData);
                        break;
                    }
                    LOG.error("Failed to parse response! Response format is undefined!");
                    break;
            }
        } else {
            LOG.error("Failed to parse response! Response format is undefined!");
        }

    }

    public Response() {
    }

    void fillOperationParams(ByteBuffer buffer) {
        this.uniqueNumber = buffer.getLong();
        buffer.get(bankAnswer);
        this.transactionCanceled = buffer.get() == 0x01;
        this.operation = Operation.getOperationByCode(buffer.get());
        this.operationSum = buffer.getInt();
        this.currencyCode = buffer.getShort();
        buffer.get(PAN);
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public int getPacketNumber() {
        return packetNumber;
    }

    public String getPAN() {
        return BankUtils.maskCardNumber(toStringSkippingZeroBytes(PAN));
    }

    public String getBankAnswer() {
        return toStringSkippingZeroBytes(bankAnswer);
    }

    public long getUniqueNumber() {
        return uniqueNumber;
    }

    public Operation getOperation() {
        return operation;
    }

    String toStringSkippingZeroBytes(byte[] source) {
        for (int i = 0; i < source.length; i++) {
            if (source[i] == 0x00) {
                source[i] = 0x20;
            }
        }
        return new String(source);
    }

    public boolean isTransactionCanceled() {
        return transactionCanceled;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }
}
