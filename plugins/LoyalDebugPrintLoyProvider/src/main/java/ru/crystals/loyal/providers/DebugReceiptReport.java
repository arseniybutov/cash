package ru.crystals.loyal.providers;

import ru.crystals.cards.CardEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.CardInfo;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.bonus.BonusAccrualsPurchase;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.AppliedActionInfo;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.loyal.check.discount.ManualPositionAdvActionEntity;
import ru.crystals.loyal.check.discount.ManualPurchaseAdvActionEntity;
import ru.crystals.pos.currency.CurrencyHandler;

import java.util.List;
import java.util.Map;

public class DebugReceiptReport {
    public static final String BORDERLINE = "\n ========================================================================================\n";
    public static final String COLUMN_DELIMITER = " ";
    private CurrencyHandler currencyHandler;

    public DebugReceiptReport(CurrencyHandler currencyHandler) {
        this.currencyHandler = currencyHandler;
    }

    public String getReceiptReport(Purchase receipt) {
        StringBuilder sb = new StringBuilder();
        sb.append(BORDERLINE);
        sb.append("Receipt number ").append(receipt.getNumber()).append(" of type ").append(getOperationType(receipt.getOperationType()))
                .append(" created on ").append(receipt.getDateCreate()).append(" on ").append(getShiftData(receipt)).append("\n");
        createPositionReport(receipt, sb);
        sb.append("\n");
        createCardReport(receipt, sb);
        sb.append("\n");
        createBonusDiscountCardReport(receipt, sb);
        sb.append("\n");
        createAppliedActionsReport(receipt, sb);
        sb.append("\n");
        createAdvertisingActionResultsReport(receipt, sb);
        sb.append("\n");
        createManualAdvertisingActionsReport(receipt, sb);
        sb.append("\n");
        createBonusAccrualsReport(receipt, sb);
        sb.append("\n");
        createThirdPartyOffersReport(receipt, sb);
        sb.append("\n");
        createExternalActionReport(receipt, sb);
        sb.append(BORDERLINE);
        return sb.toString();
    }

    private StringBuilder createExternalActionReport(Purchase receipt, StringBuilder target) {
        if(receipt.getExternalActions() == null || receipt.getExternalActions().isEmpty()) {
            return target;
        }
        target.append("External actions:\nGUID | Name\n");
        for(AdvertisingActionEntity aa : receipt.getExternalActions()) {
            target.append(aa.getGuid()).append(COLUMN_DELIMITER)
                    .append(aa.getName())
                    .append("\n");
        }
        return target;
    }

    private StringBuilder createThirdPartyOffersReport(Purchase receipt, StringBuilder target) {
        if(receipt.getAppliedThirdPartyOffers() == null || receipt.getAppliedThirdPartyOffers().isEmpty()) {
            return target;
        }
        target.append("Third party offers:\nKey | Value\n");
        for(Map.Entry<String, String> to : receipt.getAppliedThirdPartyOffers().entrySet()) {
            target.append(to.getKey()).append(COLUMN_DELIMITER)
                    .append(to.getValue())
                    .append("\n");
        }
        return target;
    }

    private StringBuilder createBonusAccrualsReport(Purchase receipt, StringBuilder target) {
        if(receipt.getBonusAccrualsPurchase() == null || receipt.getBonusAccrualsPurchase().isEmpty()) {
            return target;
        }
        target.append("Bonus accruals:\nAction GUID | Action Name | Amount | Card number | Processing | Transaction\n");
        for(BonusAccrualsPurchase b : receipt.getBonusAccrualsPurchase()) {
            target.append(b.getAdvertisingActionGuid()).append(COLUMN_DELIMITER)
                    .append(b.getAdvertisingActionName()).append(COLUMN_DELIMITER)
                    .append(b.getAmount()).append(COLUMN_DELIMITER)
                    .append(b.getCardNumber()).append(COLUMN_DELIMITER)
                    .append(b.getProcessingId()).append(COLUMN_DELIMITER)
                    .append(b.getTransactionId())
                    .append("\n");
        }
        return target;
    }

    private StringBuilder createManualAdvertisingActionsReport(Purchase receipt, StringBuilder target) {
        if(receipt.getManualPurchaseAdvActions() == null || receipt.getManualPurchaseAdvActions().isEmpty()) {
            return target;
        }
        target.append("Manual actions:\nGUID\n");
        for(ManualPurchaseAdvActionEntity m : receipt.getManualPurchaseAdvActions()) {
            target.append(m.getActionGuid()).append("\n");
        }
        return target;
    }

    private StringBuilder createAdvertisingActionResultsReport(Purchase receipt, StringBuilder target) {
        if(receipt.getAdvertisingActionResults() == null || receipt.getAdvertisingActionResults().isEmpty()) {
            return target;
        }
        target.append("Advertising action results:\n");
        target.append("Class | Action Guid\n");
        for(AdvertisingActionResultEntity r : receipt.getAdvertisingActionResults()) {
            target.append(r.getClass().getSimpleName()).append(COLUMN_DELIMITER)
                    .append(r.getAdvertisingActionGUID())
                    .append("\n");
        }
        return target;
    }

    private StringBuilder createAppliedActionsReport(Purchase receipt, StringBuilder target) {
        if(receipt.getAppliedActionsInfo() == null || receipt.getAppliedActionsInfo().isEmpty()) {
            return target;
        }
        target.append("Applied advertising actions:\nGUID | Name | Type\n");
        for(AppliedActionInfo ai : receipt.getAppliedActionsInfo()) {
            target.append(ai.getActionGuid()).append(COLUMN_DELIMITER)
                    .append(ai.getActionName()).append(COLUMN_DELIMITER)
                    .append(ai.getActionType())
                    .append("\n");
        }
        return target;
    }

    private StringBuilder createBonusDiscountCardReport(Purchase receipt, StringBuilder target) {
        if(receipt.getBonusDiscountCards() == null || receipt.getBonusDiscountCards().isEmpty()) {
            return target;
        }
        target.append("Bonus discount cards:\n");
        for(Map.Entry<BonusDiscountType, List<CardEntity>> s : receipt.getBonusDiscountCards().entrySet()) {
            if(s.getValue() == null || s.getValue().isEmpty()) {
                continue;
            }
            target.append(s.getKey()).append("Number | Status | Balance | Amount\n");
            for(CardEntity cte : s.getValue()) {
                target.append(cte.getNumber()).append(COLUMN_DELIMITER)
                        .append(cte.getStatus()).append(COLUMN_DELIMITER)
                        .append(cte.getBalance()).append(COLUMN_DELIMITER)
                        .append(cte.getAmount())
                        .append("\n");
            }
        }
        return target;
    }

    private StringBuilder createPositionReport(Purchase receipt, StringBuilder target) {
        target.append("Positions\n");
        target.append("Number | Barcode | Name | Quantity | Price per unit | Final price | Discount | Sum\n");
        for(Position p : receipt.getPositions()) {
            target.append(p.getNumberInt()).append(COLUMN_DELIMITER)
                    .append(p.getBarcode()).append(COLUMN_DELIMITER)
                    .append(p.getGoodsName()).append(COLUMN_DELIMITER)
                    .append(p.getCount()).append(COLUMN_DELIMITER)
                    .append(p.getCost()).append(COLUMN_DELIMITER)
                    .append(p.getEndPrice(currencyHandler)).append(COLUMN_DELIMITER)
                    .append(p.getDiscountValue()).append(COLUMN_DELIMITER)
                    .append(p.getSum())
                    .append("\n");
            target.append("\nPosition properties:\n");
            target.append("\tGood type: ").append(p.getGoods() == null ? "NULL" : p.getGoods().getPluginFullClassName()).append("\n")
                    .append("\tDiscountable: ").append(p.isDiscountable()).append("\n")
                    .append("\tBonus applicable: ").append(p.isBonusApplicable()).append("\n")
                    .append("\tCollapsible: ").append(p.isCollapsible()).append("\n")
                    .append("\tFixed price: ").append(p.isFixedPrice()).append("\n");
            if(p.getDiscountBatch() != null) {
                target.append("\nDiscounts for position ").append(p.getNumberInt()).append("\n");
                target.append("Action Guid | Name | Value | Value calculated | Card | Coupon\n");
                for(DiscountPositionEntity dpe : p.getDiscountBatch().getDiscounts()) {
                    target.append(dpe.getAdvertisingActionGUID()).append(COLUMN_DELIMITER)
                            .append(dpe.getActionName()).append(COLUMN_DELIMITER)
                            .append(dpe.getValue()).append(COLUMN_DELIMITER)
                            .append(dpe.getValueCalculated()).append(COLUMN_DELIMITER)
                            .append(dpe.getCardNumber()).append(COLUMN_DELIMITER)
                            .append(dpe.getPositionalCouponNumber())
                            .append("\n");
                }
            }
            if(p.getPositionalCouponNumbers() != null && !p.getPositionalCouponNumbers().isEmpty()) {
                target.append("Position ").append(p.getNumberInt()).append(" has the following positional coupons:\n");
                for(Long c : p.getPositionalCouponNumbers()) {
                    target.append(c).append("\n");
                }
            }
            if(p.getManualAdvActions() != null && !p.getManualAdvActions().isEmpty()) {
                target.append("Manual actions:\nGUID | Quantity\n");
                for(ManualPositionAdvActionEntity m : p.getManualAdvActions()) {
                    target.append(m.getActionGuid()).append(COLUMN_DELIMITER)
                            .append(m.getCount())
                            .append("\n");
                }
            }
        }
        return target;
    }

    private StringBuilder createCardReport(Purchase receipt, StringBuilder target) {
        if(receipt.getCardsInfo() != null && !receipt.getCardsInfo().isEmpty()) {
            target.append("Cards:\nNumber | Type | Provider | Input type\n");
            for(CardInfo ci : receipt.getCardsInfo()) {
                target.append(ci.getCardNumber()).append(COLUMN_DELIMITER)
                        .append(ci.getCardTypeEnum()).append(COLUMN_DELIMITER)
                        .append(ci.getProcessingName()).append(COLUMN_DELIMITER)
                        .append(ci.getInputType())
                        .append("\n");
            }
        }
        return target;
    }

    private String getShiftData(Purchase receipt) {
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

    public static String getOperationType(Boolean type) {
        if(type == null) {
            return "WTFNULL";
        }
        if(Boolean.TRUE.equals(type)) {
            return "SALE";
        } else {
            return "REFUND";
        }
    }
}
