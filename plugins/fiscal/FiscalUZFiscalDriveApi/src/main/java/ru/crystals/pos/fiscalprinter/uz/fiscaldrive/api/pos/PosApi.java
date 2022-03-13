package ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos;

import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.NotSentDocInfo;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.RegisteredReceiptVO;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ShiftVO;

import java.util.Optional;

/**
 * Дополнительный слой API между FiscalDriveAPI и кассой для упрощения кода плагина, а также для упарощения сертификации.
 * При необходимости сертификации реализации с предоставлением исходных кодов, мы вынесем реализацию этого интерфейса в отдельный артефакт.
 */
public interface PosApi {

    PosApiResponse<String> getTerminalId();

    PosApiResponse<ShiftVO> openShift();

    PosApiResponse<ShiftVO> closeShift();

    PosApiResponse<ShiftVO> getCurrentShift();

    PosApiResponse<Long> getLastReceiptSeq();

    PosApiResponse<RegisteredReceiptVO> registerSale(ReceiptVO receipt);

    PosApiResponse<RegisteredReceiptVO> registerRefund(ReceiptVO receipt);

    PosApiResponse<Integer> getNotSentDocCount();

    PosApiResponse<Optional<NotSentDocInfo>> getFirstNotSentDoc();

    PosApiResponse<Void> resendUnsent();

}
