package ru.crystals.pos.bank.bpc;

/**
 * Таги
 */
public interface Tag {

    /**
     * Входные параметры
     */
    interface Input {
        int ERN = 0x03;
        int RRN = 0x18;
        int MESSAGE_ID = 0x01;
        int ECR_NUMBER = 0x02;
        int TRANSACTION_AMOUNT = 0x04;
        int SRV_SUB_FUNCTION = 0x1A;
        int CURRENCY = 0x1B;
    }

    /**
     * Выходные параметры
     */
    interface Output {
        int ERN = 0x83;
        int RRN = 0x98;
        int RECEIPT = 0x9C;
        int RESPONSE_CODE = 0x9B;
        int VISUAL_HOST_RESPONSE = 0xA0;
        int PAN = 0x89;
        int APPROVE = 0xA1;
    }
}
