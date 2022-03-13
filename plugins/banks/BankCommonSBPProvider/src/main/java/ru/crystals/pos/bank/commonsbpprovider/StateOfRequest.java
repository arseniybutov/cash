package ru.crystals.pos.bank.commonsbpprovider;

import ru.crystals.pos.bank.commonsbpprovider.api.response.PaymentInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.RefundInfoResponseDTO;
import ru.crystals.utils.time.Timer;

import java.time.Duration;

public class StateOfRequest {
    private final Timer timer;
    private final Duration duration;
    private PaymentInfoResponseDTO cachedResponse;
    private RefundInfoResponseDTO cachedRefundResponse;
    private final int maxNumberOfRequests;
    private final int maxNumberOfRetries;

    private int counterOfRequests;
    private int counterOfRetries;

    public StateOfRequest(long delayInSeconds, int maxNumberOfRequests, int maxNumberOfRetries) {
        this.duration = Duration.ofSeconds(delayInSeconds);
        this.timer = Timer.of(Duration.ZERO);
        this.cachedResponse = null;
        this.cachedRefundResponse = null;
        this.maxNumberOfRequests = maxNumberOfRequests;
        this.maxNumberOfRetries = maxNumberOfRetries;
        this.counterOfRequests = 0;
        this.counterOfRetries = maxNumberOfRetries;
    }

    public boolean isCanExecuteRequest() {
        return timer.isExpired() && counterOfRetries != 0;
    }

    public void requestExecutedSuccessfully(PaymentInfoResponseDTO paymentInfoResponseDTO) {
        cachedResponse = paymentInfoResponseDTO;
        counterOfRequests++;
        timer.restart(duration);
    }

    public PaymentInfoResponseDTO requestExecutedWithException() {
        counterOfRetries--;
        timer.restart(duration);
        return cachedResponse;
    }

    public void requestRefundExecutedSuccessfully(RefundInfoResponseDTO refundInfoResponseDTO) {
        cachedRefundResponse = refundInfoResponseDTO;
        counterOfRequests++;
        timer.restart(duration);
    }

    public RefundInfoResponseDTO requestRefundExecutedWithException() {
        counterOfRetries--;
        timer.restart(duration);
        return cachedRefundResponse;
    }

    public boolean isRetriesLeft() {
        return counterOfRetries >= 0;
    }

    public PaymentInfoResponseDTO getCachedResponse() {
        return cachedResponse;
    }

    public RefundInfoResponseDTO getCachedRefundResponse() {
        return cachedRefundResponse;
    }

    public void refreshCounterOfRetries() {
        this.counterOfRetries = maxNumberOfRetries;
    }

    public long getMaxNumberOfRetries() {
        return counterOfRetries;
    }

    public int getCounterOfRequests() {
        return counterOfRequests;
    }

    public int getMaxNumberOfRequests() {
        return maxNumberOfRequests;
    }

    public int getCounterOfRetries() {
        return counterOfRetries;
    }
}
