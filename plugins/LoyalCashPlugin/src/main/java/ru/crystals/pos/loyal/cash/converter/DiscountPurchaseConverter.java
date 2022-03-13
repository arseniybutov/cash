package ru.crystals.pos.loyal.cash.converter;

import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.discounts.ApplyMode;
import ru.crystals.discounts.enums.MessageDisplayTime;
import ru.crystals.loyal.check.CardInfo;
import ru.crystals.loyal.check.PrintObject;
import ru.crystals.loyal.check.PrintObjectType;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.TemplatedPrintObject;
import ru.crystals.loyal.check.discount.AdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.BonusAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.ChequeAdvertiseActionResultEntity;
import ru.crystals.loyal.check.discount.ChequeCouponAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.check.discount.QuestionnaireAdvertisingActionResultEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.discountresults.AdvActionInPurchaseEntity;
import ru.crystals.pos.check.discountresults.BonusPositionEntity;
import ru.crystals.pos.check.discountresults.BonusTransactionEntity;
import ru.crystals.pos.check.discountresults.ChequeAdvertEntity;
import ru.crystals.pos.check.discountresults.ChequeCouponEntity;
import ru.crystals.pos.check.discountresults.DiscountCardEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;
import ru.crystals.pos.check.discountresults.MessageEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCodeType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CouponTemplatedServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TemplatedServiceDocument;
import ru.crystals.pos.loyal.cash.service.CashAdvResultPersistenceManager;

import java.util.LinkedList;
import java.util.List;

/**
 * @author ppavlov
 */
public class DiscountPurchaseConverter {
    private static AdvertisingActionEntity[] actions = null;

    public static AdvertisingActionEntity[] getActions() {
        return actions;
    }

    public static void setActions(AdvertisingActionEntity[] actions) {
        DiscountPurchaseConverter.actions = actions;
    }

    private static AdvActionInPurchaseEntity findOrCreateAdvActionByGuid(Long advActionGuid) {
        if (advActionGuid == null) {
            return null;
        }

        AdvActionInPurchaseEntity result = new AdvActionInPurchaseEntity();
        result.setId(advActionGuid);
        result.setActionName("Unknown");
        result.setAdvertType("Unknown");
        result.setManual(false);
        if (actions != null) {
            for (AdvertisingActionEntity a : actions) {
                if (advActionGuid.equals(a.getGuid())) {
                    result.setActionName(a.getName());
                    result.setManual(a.getMode() == ApplyMode.MANUAL);
                    result.setExternalCode(a.getExternalCode());
                    result.setPriority(a.getPriority());
                    break;
                }
            }
        }
        return result;
    }

    private static BonusPositionEntity convertBonusPosition(ru.crystals.loyal.check.bonus.BonusPosition bonusPosition, DiscountPurchaseEntity discountPurchase) {
        BonusPositionEntity result = new BonusPositionEntity();
        result.setAdvAction(findOrCreateAdvActionByGuid(bonusPosition.getAdvertActGuid()));
        result.setBonusAmount(bonusPosition.getBonusAmount());
        result.setGoodCode(bonusPosition.getGoodsCode());
        result.setPositionOrder((int) bonusPosition.getPositionOrder());
        result.setDiscountTransaction(discountPurchase);

        return result;
    }

    private static TemplatedServiceDocument convertPrintObjects(List<PrintObject> printObjects) {
        if (printObjects == null || printObjects.isEmpty()) {
            return null;
        }

        TemplatedServiceDocument tpd = null;
        BarCode barcode = null;

        for (PrintObject row : printObjects) {
            if (row instanceof TemplatedPrintObject) {
                TemplatedPrintObject tpo = (TemplatedPrintObject) row;
                tpd = new TemplatedServiceDocument(tpo.getTemplate(), tpo.getDataset());
            } else if (isBarcodeContent(row)) {
                barcode = convertBarcode(row);
            }
        }

        if (tpd != null && barcode != null) {
            tpd.setBarcode(barcode);
        }

        return tpd;
    }

    /**
     * Преобразование печатного купона лояльности в фискальный документ
     *
     * @param printObjects набор печатных объектов лояльности (текстовое сообщение или баркод)
     * @return сервисный документ купона, печатаемый по шаблону
     */
    public static CouponTemplatedServiceDocument convertCouponPrintObjects(List<PrintObject> printObjects) {
        if (printObjects == null || printObjects.isEmpty()) {
            return null;
        }

        CouponTemplatedServiceDocument tpd = null;
        BarCode barcode = null;

        for (PrintObject row : printObjects) {
            if (row instanceof TemplatedPrintObject) {
                TemplatedPrintObject tpo = (TemplatedPrintObject) row;
                tpd = new CouponTemplatedServiceDocument(tpo.getTemplate(), tpo.getDataset());
            } else if (isBarcodeContent(row)) {
                barcode = convertBarcode(row);
            }
        }

        if (tpd != null && barcode != null) {
            tpd.setBarcode(barcode);
        }

        return tpd;
    }

    /**
     * Информация для печати содержит ШК
     *
     * @param content - объект описывающий информацию для печати
     * @return - true если информация для печати ШК
     */
    private static boolean isBarcodeContent(PrintObject content) {
        return content != null &&
                (content.getType() == PrintObjectType.BARCODE || content.getType() == PrintObjectType.QR);
    }

    /**
     * Из текущего объекта для печати, достает информацию по ШК
     *
     * @param content - объект для печати
     * @return - инфомация по ШК
     */
    private static BarCode convertBarcode(PrintObject content) {
        BarCode barcode = null;
        if (content != null) {
            barcode = new BarCode(content.getValue());
            if (content.getType() == PrintObjectType.QR) {
                barcode.setType(BarCodeType.QR);
                barcode.setMaxQRCodeWidth(true);
                barcode.setPrintBarcodeLabel(true);
            }
        }
        return barcode;
    }

    /**
     * Формурируем DiscountPurchaseEntity (из чека лояльности)
     *
     * @param purchase
     * @param dpe
     * @return
     */
    private static DiscountPurchaseEntity convertDiscountPurchase(Purchase purchase, DiscountPurchaseEntity dpe) {
        DiscountPurchaseEntity result = (dpe != null) ? dpe : new DiscountPurchaseEntity();

        result.setCashNumber(purchase.getShift() != null ? purchase.getShift().getCashNum() : -1);
        result.setShopNumber(purchase.getShift() != null ? purchase.getShift().getShopIndex() : -1);
        result.setShiftNumber(purchase.getShift() != null ? purchase.getShift().getNumShift() : -1);
        result.setPurchaseNumber(purchase.getNumber() != null ? purchase.getNumber() : -1);
        result.setSaleTime(purchase.getDateCommit()); // ??
        result.setOperationType(purchase.getOperationType());
        result.setStatus(0);// ??

        //Бонусы по позициям
        if (purchase.getBonusPositions() != null && !purchase.getBonusPositions().isEmpty()) {
            List<BonusPositionEntity> list = new LinkedList<>();
            for (ru.crystals.loyal.check.bonus.BonusPosition bonusPosition : purchase.getBonusPositions()) {
                list.add(convertBonusPosition(bonusPosition, result));
            }
            result.setBonusPositions(list);
        }

        result.setDiscountCards(new LinkedList<>());
        //Карты
        for (CardInfo cardInfo : purchase.getCardsInfo()) {
            DiscountCardEntity discountCardEntity = new DiscountCardEntity();
            discountCardEntity.setCardNumber(cardInfo.getCardNumber());
            discountCardEntity.setCardType(cardInfo.getCardTypeEnum().toString());
            result.getDiscountCards().add(discountCardEntity);
        }

        // разбор AdvertisingActionResultEntity
        if (purchase.getAdvertisingActionResults() != null) {
            for (AdvertisingActionResultEntity actionResult : purchase.getAdvertisingActionResults()) {

                AdvActionInPurchaseEntity advAction = findOrCreateAdvActionByGuid(actionResult.getAdvertisingActionGUID());

                if (actionResult instanceof BonusAdvertisingActionResultEntity) {
                    BonusAdvertisingActionResultEntity src = (BonusAdvertisingActionResultEntity) actionResult;
                    BonusTransactionEntity dest = new BonusTransactionEntity();
                    dest.setAdvAction(advAction);
                    dest.setBonusAccountType(src.getAccountTypeId() == null ? -1 : src.getAccountTypeId());
                    dest.setBonusAmount(src.getValueBonus() == null ? 0 : src.getValueBonus());
                    dest.setSumAmount(src.getSumValue() == null ? 0 : src.getSumValue());
                    dest.setBonusPeriodStart(CashAdvResultPersistenceManager.checkStartDate(src.getDateStart()));
                    dest.setBonusPeriodFinish(CashAdvResultPersistenceManager.checkFinishDate(src.getDateFinish()));
                    dest.setDiscountTransaction(result);
                    if (src.getCardNumber() != null) {
                        dest.setDiscountCard(src.getCardNumber());
                    } else {
                        dest.setDiscountCard("");
                    }

                    result.addBonusTransaction(dest);
                } else if (actionResult instanceof ChequeCouponAdvertisingActionResultEntity) {
                    ChequeCouponAdvertisingActionResultEntity src = (ChequeCouponAdvertisingActionResultEntity) actionResult;
                    ChequeCouponEntity dest = new ChequeCouponEntity();
                    dest.setAdvAction(advAction);
                    dest.setCouponBarcode("");
                    dest.setCouponText(convertCouponPrintObjects(src.getPrintObjects()));
                    if (src.getPrintObjects() != null) {
                        for (PrintObject printObject : src.getPrintObjects()) {
                            if (isBarcodeContent(printObject)) {
                                dest.setCouponBarcode(printObject.getValue());
                                break;
                            }
                        }
                    }
                    if (dest.getCouponText() != null) {
                        dest.getCouponText().setSeparatePrint(src.isSeparatePrint());
                    }

                    if (src.getCouponTypeGuid() != null) {
                        dest.setCouponTypeGuid(src.getCouponTypeGuid());
                    }
                    dest.setDiscountTransaction(result);

                    result.addChequeCoupon(dest);
                } else if (actionResult instanceof MessageAdvertisingActionResultEntity) {
                    MessageAdvertisingActionResultEntity src = (MessageAdvertisingActionResultEntity) actionResult;
                    MessageEntity dest = new MessageEntity();

                    dest.setAdvAction(advAction);
                    dest.setDisplayTime(src.getDisplayTime());
                    StringBuilder cashierMessage = new StringBuilder();
                    for (String str : src.getOperatorMsg()) {
                        cashierMessage.append(str).append(" \n");
                    }
                    dest.setCashierMessage(cashierMessage.toString());

                    StringBuilder customerMessage = new StringBuilder();
                    for (String str : src.getClientMsg()) {
                        customerMessage.append(str).append(" \n");
                    }
                    dest.setCustomerMessage(customerMessage.toString());
                    dest.setDiscountTransaction(result);

                    result.addMessage(dest);
                } else if (actionResult.getPrintObjects() != null && !actionResult.getPrintObjects().isEmpty() && !(actionResult instanceof QuestionnaireAdvertisingActionResultEntity)) {
                    ChequeAdvertEntity dest = new ChequeAdvertEntity();

                    TemplatedServiceDocument serviceDoc = convertPrintObjects(actionResult.getPrintObjects());
                    if (serviceDoc != null) {
                        serviceDoc.setSeparatePrint(actionResult.isSeparatePrint());
                        serviceDoc.setPromo(true);
                        dest.setChequeText(serviceDoc);
                    }
                    dest.setAdvAction(advAction);
                    dest.setDiscountTransaction(result);

                    if (actionResult instanceof ChequeAdvertiseActionResultEntity) {
                        dest.setRestorableToPurchase(false);
                    }

                    result.addChequeAdvert(dest);
                }
            }
        }

        return result;
    }

    public static PurchaseEntity applyPurchaseDiscount(PurchaseEntity originalPurchase, Purchase purchase, AdvertisingActionEntity[] actions) {
        setActions(actions);
        if (originalPurchase != null) {
            DiscountPurchaseEntity discountPurchase = originalPurchase.getDiscountPurchaseEntity();
            if (purchase != null) {
                discountPurchase = convertDiscountPurchase(purchase, discountPurchase);
            }
            originalPurchase.setDiscountPurchaseEntity(discountPurchase);
        }
        return originalPurchase;
    }

    /**
     * Добавить сообщение кассиру
     *
     * @param purchase        чек
     * @param text            текст сообщения
     * @param displayTime     время отображения
     * @param displayRequired безусловное отображение сообщения (системное)
     */
    public static void addCashierMessage(PurchaseEntity purchase, String text, MessageDisplayTime displayTime, boolean displayRequired) {
        if (purchase == null || text == null || displayTime == null) {
            return;
        }

        if (purchase.getDiscountPurchaseEntity() == null) {
            purchase.setDiscountPurchaseEntity(new DiscountPurchaseEntity());
        } else if (purchase.getDiscountPurchaseEntity().getMessages() != null && purchase.getDiscountPurchaseEntity().getMessages()
                .stream().anyMatch(m -> text.equals(m.getCashierMessage()) && displayTime == m.getDisplayTime())) {
            return;
        }

        MessageEntity message = new MessageEntity();
        message.setDisplayTime(displayTime);
        message.setDisplayRequired(displayRequired);
        message.setCashierMessage(text);
        message.setDiscountTransaction(purchase.getDiscountPurchaseEntity());
        purchase.getDiscountPurchaseEntity().addMessage(message);
    }
}
