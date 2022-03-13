package ru.crystals.pos.loyal.cash.converter;

import java.util.LinkedList;
import java.util.List;

import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyBonusTransactionEntity;
import ru.crystals.discount.processing.entity.LoyChequeCouponEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyPurchaseCardEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.discountresults.AdvActionInPurchaseEntity;
import ru.crystals.pos.check.discountresults.BonusPositionEntity;
import ru.crystals.pos.check.discountresults.BonusTransactionEntity;
import ru.crystals.pos.check.discountresults.ChequeCouponEntity;
import ru.crystals.pos.check.discountresults.DiscountCardEntity;
import ru.crystals.pos.check.discountresults.DiscountPositionEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;

/**
 * @author ppavlov
 */
public class DiscountLoyTransactionConverter {

	private static DiscountPositionEntity convertDiscountPosition(LoyDiscountPositionEntity loyDiscountPosition, DiscountPurchaseEntity discountPurchase) {
		DiscountPositionEntity discountPosition = new DiscountPositionEntity();

		discountPosition.setDiscountAmount(loyDiscountPosition.getDiscountAmount());
		discountPosition.setDiscountPurchase(loyDiscountPosition.isDiscountPurchase());
		discountPosition.setDiscountTransaction(discountPurchase);
		discountPosition.setGoodCode(loyDiscountPosition.getGoodCode());
		LoyAdvActionInPurchaseEntity loyAdvAction = loyDiscountPosition.getAdvAction();
		if(loyAdvAction != null) {
			AdvActionInPurchaseEntity advAction = new AdvActionInPurchaseEntity();
			advAction.setId(loyAdvAction.getGuid());
			advAction.setActionName(loyAdvAction.getActionName());
			discountPosition.setAdvAction(advAction);
		}
		discountPosition.setId(null);
		discountPosition.setPositionOrder(loyDiscountPosition.getPositionOrder());
		if(loyDiscountPosition.getAdvAction() != null) {
			discountPosition.setActionType(loyDiscountPosition.getAdvAction().getActionType());
		}
		return discountPosition;
	}

	private static DiscountCardEntity convertPurchaseCard(LoyPurchaseCardEntity card, DiscountPurchaseEntity discountPurchase) {
		DiscountCardEntity discountCard = new DiscountCardEntity();

		discountCard.setCardNumber(card.getCardNumber());
		discountCard.setCardType(card.getCardType());
		discountCard.setDiscountTransaction(discountPurchase);
		discountCard.setId(null);

		return discountCard;
	}

	// TODO
	@SuppressWarnings("unused")
	private static BonusTransactionEntity convertBonusTransaction(LoyBonusTransactionEntity loyBonusTransaction, DiscountPurchaseEntity discountPurchase) {
		BonusTransactionEntity bonusTransaction = new BonusTransactionEntity();

		return bonusTransaction;
	}

	// TODO
	@SuppressWarnings("unused")
	private static BonusPositionEntity convertBonusPosition(LoyBonusPositionEntity bonusPosition, DiscountPurchaseEntity discountPurchase) {
		BonusPositionEntity result = new BonusPositionEntity();

		return result;
	}

	// TODO
	@SuppressWarnings("unused")
	private static ChequeCouponEntity convertChequeCoupon(LoyChequeCouponEntity chequeCoupon, DiscountPurchaseEntity discountPurchase) {
		ChequeCouponEntity result = new ChequeCouponEntity();

		return result;
	}

	private static DiscountPurchaseEntity convertLoyTransaction(PurchaseEntity purchase, LoyTransactionEntity loyTransaction) {
		DiscountPurchaseEntity result = (purchase.getDiscountPurchaseEntity() != null) ? purchase.getDiscountPurchaseEntity() : new DiscountPurchaseEntity();

		result.setCashNumber(purchase.getShift() != null ? purchase.getShift().getCashNum() : -1);
		result.setShopNumber(purchase.getShift() != null ? purchase.getShift().getShopIndex() : -1);
		result.setShiftNumber(purchase.getShift() != null ? purchase.getShift().getNumShift() : -1);
		result.setPurchaseNumber(purchase.getNumber() != null ? purchase.getNumber() : -1);
		result.setSaleTime(purchase.getDateCreate()); // ??
		result.setOperationType(purchase.getOperationType());
		result.setStatus(0);// ??

		//Позицонные скидки
		if (loyTransaction.getDiscountPositions() != null && !loyTransaction.getDiscountPositions().isEmpty()) {
			List<DiscountPositionEntity> list = new LinkedList<>();
			for (LoyDiscountPositionEntity position : loyTransaction.getDiscountPositions()) {
				list.add(convertDiscountPosition(position, result));
			}
			result.setDiscountPositions(list);
		}

		//Применённые карты
		if (loyTransaction.getDiscountCards() != null && !loyTransaction.getDiscountCards().isEmpty()) {
			List<DiscountCardEntity> list = new LinkedList<>();
			for (LoyPurchaseCardEntity card : loyTransaction.getPurchaseCards()) {
				list.add(convertPurchaseCard(card, result));
			}
			result.setDiscountCards(list);
		}

		return result;
	}

	public static PurchaseEntity applyPurchaseDiscount(PurchaseEntity originalPurchase, LoyTransactionEntity loyTransaction) {
		if (originalPurchase != null) {
			DiscountPurchaseEntity discountPurchase = null;
			if (loyTransaction != null) {
				discountPurchase = convertLoyTransaction(originalPurchase, loyTransaction);
			}
			originalPurchase.setDiscountPurchaseEntity(discountPurchase);
		}
		return originalPurchase;
	}

}
