package ru.crystals.loyal.providers;

import static ru.crystals.loyal.providers.DebugReceiptReport.COLUMN_DELIMITER;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.payments.PaymentEntity;

public class DebugReceiptEntityReport {

    public String createReport(PurchaseEntity receipt) {
        StringBuilder sb = new StringBuilder();
        sb.append(DebugReceiptReport.BORDERLINE);
        sb.append("Receipt number ").append(receipt.getNumber()).append(" of type ").append(DebugReceiptReport.getOperationType(receipt.getOperationType()))
                .append(" created on ").append(receipt.getDateCreate()).append(" on ").append(getShiftData(receipt)).append("\n");
        createPositionReport(receipt, sb);
        sb.append("\n");
        createCardReport(receipt, sb);
        sb.append("\n");
        createPaymentReport(receipt, sb);
        sb.append("\nTotal: ").append(receipt.getCheckSumEndBigDecimal()).append("\n");
        sb.append("Discount: ").append(receipt.getDiscountValueTotalBigDecimal()).append("\n");
        sb.append(DebugReceiptReport.BORDERLINE);
        return sb.toString();
    }

    private void createPaymentReport(PurchaseEntity receipt, StringBuilder target) {
        target.append("Payments:\n");
        if(receipt.getPaymentType() != null) {
            target.append("ID: ").append(receipt.getPaymentType().getPaymentId()).append("\n")
                    .append("Class: ").append(receipt.getPaymentType().getMainClass()).append("\n")
                    .append("Type: ").append(receipt.getPaymentType().getPaymentType()).append("\n")
                    .append("Amount: ").append(receipt.getPaymentType().getPaymentTypeAmount()).append("\n")
                    .append("Name: ").append(receipt.getPaymentType().getPaymentTypeName()).append("\n");
        }
        if(receipt.getPayments() == null || receipt.getPayments().isEmpty()) {
            return;
        }
        target.append("Type | Sum | End sum\n");
        for(PaymentEntity p : receipt.getPayments()) {
            target.append(p.getPaymentType()).append(COLUMN_DELIMITER)
                    .append(p.getSumPayBigDecimal()).append(COLUMN_DELIMITER)
                    .append(p.getEndSumPayBigDecimal())
                    .append("\n");
        }
    }

    private void createCardReport(PurchaseEntity receipt, StringBuilder target) {
        if(receipt.getCards() == null || receipt.getCards().isEmpty()) {
            return;
        }
        target.append("Cards:\n");
        for(PurchaseCardsEntity card : receipt.getCards()) {
            target.append(card.getNumber())
                    .append("\n");
        }
    }

    private void createPositionReport(PurchaseEntity receipt, StringBuilder target) {
        target.append("Positions\n");
        target.append("Number | Barcode | Name | Quantity | Price | Final price | Discount | Sum\n");
        for(PositionEntity p : receipt.getPositions()) {
            target.append(p.getNumberInt()).append(DebugReceiptReport.COLUMN_DELIMITER)
                    .append(p.getBarCode()).append(DebugReceiptReport.COLUMN_DELIMITER)
                    .append(p.getName()).append(DebugReceiptReport.COLUMN_DELIMITER)
                    .append(p.getQnty()).append(DebugReceiptReport.COLUMN_DELIMITER)
                    .append(p.getPriceStart()).append(DebugReceiptReport.COLUMN_DELIMITER)
                    .append(p.getPriceEnd()).append(DebugReceiptReport.COLUMN_DELIMITER)
                    .append(p.getSumDiscount()).append(DebugReceiptReport.COLUMN_DELIMITER)
                    .append(p.getSum())
                    .append("\n");
            if(p.getCouponsToApply() != null && !p.getCouponsToApply().isEmpty()) {
                target.append("Applied coupons:\nGUID | Name | Processing\n");
                for(CardTypeEntity cte : p.getCouponsToApply()) {
                    target.append(cte.getGuid()).append(DebugReceiptReport.COLUMN_DELIMITER)
                            .append(cte.getName()).append(DebugReceiptReport.COLUMN_DELIMITER)
                            .append(cte.getProcessingName())
                            .append("\n");
                }
            }
            if(p.getCoupons() != null && !p.getCoupons().isEmpty()) {
                target.append("Coupons:\nNumber | Processing | GUID\n");
                for(PurchaseCardsEntity pce : p.getCoupons()) {
                    target.append(pce.getNumber()).append(DebugReceiptReport.COLUMN_DELIMITER)
                            .append(pce.getProcessingName()).append(DebugReceiptReport.COLUMN_DELIMITER)
                            .append(pce.getGuid())
                            .append("\n");
                }
            }

            target.append("Position properties:\n");
            target.append("\tCollapsible: ").append(p.isCollapsible()).append("\n")
                    .append("\tFixed price: ").append(p.isFixedPrice()).append("\n")
                    .append("\tExcise: ").append(p.isExcise()).append("\n")
                    .append("\tGift: ").append(p.isGift()).append("\n")
                    .append("\tReturnable: ").append(p.isReturnPossible()).append("\n");
        }
    }

    private String getShiftData(PurchaseEntity receipt) {
        if(receipt.getShift() == null) {
            return "NO SHIFT DATA FOUND";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("POS ")
                .append(receipt.getShift().getCashNum())
                .append(" shop ")
                .append(receipt.getShift().getShopIndex())
                .append(" shift ")
                .append(receipt.getShift().getNumShift());
        return sb.toString();
    }
}
