package ru.crystals.pos.bank.tusson.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Пакет запроса к терминалу
 */
public class Request {

    private int packetNumber;
    private long uniqueNumber;
    private Operation operation;
    private int operationSum;
    private short currencyCode = 933;

    private Request() {
        //
    }

    private Request(long uniqueNumber, Operation operation, int operationSum) {
        this.uniqueNumber = uniqueNumber;
        this.operation = operation;
        this.operationSum = operationSum;
    }

    public static Request getRequestForSaleOperation(int operationSum) {
        return new Request(System.currentTimeMillis(), Operation.SALE, operationSum);
    }

    public static Request getRequestForRefundOperation(int operationSum) {
        return new Request(System.currentTimeMillis(), Operation.REFUND, operationSum);
    }

    public static Request getRequestForReversalOperation(long uniqueNumber, int operationSum) {
        return new Request(uniqueNumber, Operation.REVERSAL, operationSum);
    }

    public static Request getRequestForDailyLogOperation() {
        return new Request(System.currentTimeMillis(), Operation.DAILY_LOG, 0);
    }

    /**
     * Формирует буффер для отправки команды терминалу
     */
    public ByteBuffer toByteBuffer() {
        ByteBuffer buffer;
        if (operation == null) {
            buffer = ByteBuffer.allocate(12);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(packetNumber);
            buffer.putLong(uniqueNumber);
        } else {
            buffer = ByteBuffer.allocate(19);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.putInt(packetNumber);
            buffer.putLong(uniqueNumber);
            buffer.put(operation.getOperationCode());
            buffer.putInt(operationSum);
            buffer.putShort(currencyCode);
        }
        return (ByteBuffer) buffer.flip();
    }

    public Operation getOperation() {
        return operation;
    }

    public long getUniqueNumber() {
        return uniqueNumber;
    }

    /**
     * На основе данного запроса, формирует короткий запрос к терминалу для опроса статуса выполнения
     *
     * @param packetNumber порядковый номер  пакета для нового запроса
     * @return Запрос статуса выполнения родительской команды
     */
    public Request getSimpleStatusPingRequest(int packetNumber) {
        Request result = new Request();
        result.packetNumber = packetNumber;
        result.uniqueNumber = this.uniqueNumber;
        return result;
    }

    public void setPacketNumber(int packetNumber) {
        this.packetNumber = packetNumber;
    }

    public boolean equalsExceptUnique(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Request request = (Request) o;

        if (packetNumber != request.packetNumber) {
            return false;
        }
        if (operationSum != request.operationSum) {
            return false;
        }
        if (currencyCode != request.currencyCode) {
            return false;
        }
        return operation == request.operation;

    }

}
