package ru.crystals.pos.bank.commonsbpprovider;

import ru.crystals.pos.CashException;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfFullAmountDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RefundOfPartAmountRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.request.RegistrationQRRequestDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.PaymentInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.QRInfoResponseDTO;
import ru.crystals.pos.bank.commonsbpprovider.api.response.RefundInfoResponseDTO;
import ru.crystals.pos.bank.exception.BankCommunicationException;
import ru.crystals.pos.checkdisplay.PictureId;

/**
 * Общий интерфейс для реализаций интеграций оплаты/возврата через СБП
 */
public interface SBPProvider {

    /**
     * Регистрация QR кода
     * @param registrationQRRequestDTO
     * @return Информация о QR (payload, id)
     * @throws BankCommunicationException
     */
    QRInfoResponseDTO registrationQR(RegistrationQRRequestDTO registrationQRRequestDTO) throws BankCommunicationException;

    /**
     * Получение статуса оплаты
     * @param qrId
     * @param stateOfRequest
     * @return Информациея о статусе оплаты
     * @throws BankCommunicationException
     */
    PaymentInfoResponseDTO getPaymentStatus(String qrId, StateOfRequest stateOfRequest) throws BankCommunicationException;

    /**
     * Возврат всего чека
     * @param refundDTO
     * @return Информация о возврате
     * @throws BankCommunicationException
     */
    RefundInfoResponseDTO refund(RefundOfFullAmountDTO refundDTO) throws BankCommunicationException;

    /**
     * Возврат части чека
     * @param refundDTO
     * @return Информация о возврате
     * @throws BankCommunicationException
     */
    RefundInfoResponseDTO refundPartOfAmount(RefundOfPartAmountRequestDTO refundDTO) throws BankCommunicationException;

    /**
     * Получение статуса возврата
     * @param refundId
     * @param stateOfRequest
     * @return Информация о возврате
     * @throws BankCommunicationException
     */
    RefundInfoResponseDTO getRefundStatus(String refundId, StateOfRequest stateOfRequest) throws BankCommunicationException;

    /**
     * Деактивировать QR код (или произвести единоразорвый запрос на возврат оплаты - зависит от возможности API банка) при отмене оплаты кассиром
     * @param refundDTO
     * @return Информация об отмене; null для тех банков, у которых не реализована возможность деактивировать QR
     * @throws BankCommunicationException
     */
    default RefundInfoResponseDTO cancelQRCode(RefundOfFullAmountDTO refundDTO) throws BankCommunicationException {
        return null;
    }

    /**
     * Получение логотипа банка
     * @return Логотип банка
     */
    PictureId getPaymentSystemLogoId();

    /**
     * Получение конфигураций
     * @return Конфигурации
     */
    SBPProviderConfig getConfig();

    /**
     * Запуск плагина
     * @throws CashException
     */
    void start() throws CashException;

    /**
     * Получить имя провайдета
     * @return имя провайдера
     */
    String getProvider();
}
