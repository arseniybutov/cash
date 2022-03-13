package ru.crystals.pos.loyal.cash.calculation;

import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.check.Position;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.discount.DiscountPositionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class for viewing various data during discount calculation. For debug only.
 *
 * @author ppavlov
 */
public class PurchaseViewer {

    public static void printPositionDiscounts(Purchase purchase) {
        Map<Long, Long> purchaseDiscounts = new HashMap<Long, Long>();
        Map<Long, Long> dpeDiscounts = new HashMap<Long, Long>();
        for (Position position : purchase.getPositions()) {
            if (position.getDiscountBatch() != null && position.getDiscountBatch().getDiscountValue() != null) {
                purchaseDiscounts.put(position.getNumber(), ((purchaseDiscounts.get(position.getNumber()) != null) ? purchaseDiscounts.get(position.getNumber()) + position.getDiscountBatch().getDiscountValue() : position.getDiscountBatch().getDiscountValue()));

                for (DiscountPositionEntity dpe : position.getDiscountBatch().getDiscounts()) {
                    if (dpe.getValueCalculated() != null) {
                        dpeDiscounts.put(position.getNumber(), ((dpeDiscounts.get(position.getNumber()) != null) ? dpeDiscounts.get(position.getNumber()) + dpe.getValueCalculated() : dpe.getValueCalculated()));
                    }
                }
            }

        }
        System.out.println("======================= purchase discounts ===========================");
        for (Entry<Long, Long> purchaseDiscount : purchaseDiscounts.entrySet()) {
            String goodsCode = "";
            for (Position position : purchase.getPositions()) {
                if (position.getNumber() == purchaseDiscount.getKey().longValue()) {
                    goodsCode = position.getGoods().getName();
                    break;
                }
            }

            System.out.println(purchaseDiscount.getKey() + " purchase: " + purchaseDiscount.getValue() + "; discountBatch: " + dpeDiscounts.get(purchaseDiscount.getKey()) + "\t " + goodsCode);
        }
        System.out.println("======================================================================");
    }


    public static void printPositionDiscounts(LoyTransactionEntity loyTransaction, PurchaseEntity returnPurchase) {
        Map<Long, Long> purchaseDiscounts = new HashMap<Long, Long>();
        Map<Long, Long> loyTransactionDiscounts = new HashMap<Long, Long>();
        for (PositionEntity position : returnPurchase.getPositions()) {
            purchaseDiscounts.put(position.getNumber(), ((purchaseDiscounts.get(position.getNumber()) != null) ? purchaseDiscounts.get(position.getNumber()) + position.getSumDiscount() : position.getSumDiscount()));
        }

        for (LoyDiscountPositionEntity discountPosition : loyTransaction.getDiscountPositions()) {
            loyTransactionDiscounts.put((long) discountPosition.getPositionOrder(), ((loyTransactionDiscounts.get(discountPosition.getPositionOrder()) != null) ? loyTransactionDiscounts.get(discountPosition.getPositionOrder()) + discountPosition.getDiscountAmount() : discountPosition.getDiscountAmount()));
        }

        System.out.println("=============== loyTransaction and purchase discounts ================");
        for (Entry<Long, Long> purchaseDiscount : purchaseDiscounts.entrySet()) {
            String goodsCode = "";
            for (PositionEntity position : returnPurchase.getPositions()) {
                if (position.getNumber() == purchaseDiscount.getKey().longValue()) {
                    goodsCode = position.getName();
                    break;
                }
            }
            System.out.println(purchaseDiscount.getKey() + " purchase: " + purchaseDiscount.getValue() + "; loyTransaction: " + loyTransactionDiscounts.get(purchaseDiscount.getKey()) + "\t " + goodsCode);
        }

        System.out.println("======================================================================");
    }
}
