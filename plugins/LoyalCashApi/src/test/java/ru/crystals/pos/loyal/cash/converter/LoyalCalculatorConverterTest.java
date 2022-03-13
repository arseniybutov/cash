package ru.crystals.pos.loyal.cash.converter;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.discount.processing.entity.LoyAdvActionInPurchaseEntity;
import ru.crystals.discount.processing.entity.LoyBonusPositionEntity;
import ru.crystals.discount.processing.entity.LoyChequeAdvertiseEntity;
import ru.crystals.discount.processing.entity.LoyDiscountPositionEntity;
import ru.crystals.discount.processing.entity.LoyGiftNoteEnity;
import ru.crystals.discount.processing.entity.LoySetApiPluginTransactionEntity;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.discounts.ActionType;
import ru.crystals.discounts.enums.GiftDisplayTime;
import ru.crystals.loyal.check.Purchase;
import ru.crystals.loyal.check.TemplatedPrintObject;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyal.check.discount.ChequeAdvertiseActionResultEntity;
import ru.crystals.loyal.check.discount.ExternalLoyaltyCheckMessageActionResult;
import ru.crystals.loyal.check.discount.GiftNoteDescription;
import ru.crystals.loyal.check.discount.MessageAdvertisingActionResultEntity;
import ru.crystals.loyal.product.LoyalProductType;
import ru.crystals.pos.cards.siebel.SiebelService;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.InsertType;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionProductionDateEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.check.discountresults.ChequeAdvertEntity;
import ru.crystals.pos.check.discountresults.ChequeCouponEntity;
import ru.crystals.pos.check.discountresults.DiscountPurchaseEntity;
import ru.crystals.pos.check.discountresults.MessageEntity;
import ru.crystals.pos.fiscalprinter.datastruct.documents.CouponTemplatedServiceDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.ExternalLoyaltyCheckMessageDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.TemplatedServiceDocument;
import ru.crystals.pos.loyal.Loyal;
import ru.crystals.pos.payments.PaymentSuspensionData;
import ru.crystals.pos.techprocess.TechProcessEvents;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.techprocess.TechProcessServiceAsync;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для конвертера чека
 */
public class LoyalCalculatorConverterTest {

    private static final long POSITION_SUM = 1001L;
    private static final Long POSITION_MIN_PRICE = 100L;
    private static final long DOC_NUM = 1002L;
    private static final long ACTION_GUID = 1003L;
    private static final String GOOD_CODE = "111";
    private static final String CASHIER_MESSAGE = "wewg sdvsdv qwefqw\n";
    private static final List<String> EXT_LOYALTY_MESSAGE_TEXT = Collections.singletonList("EXT_LOYALTY_MESSAGE_TEXT");
    private static final String EXT_LOYALTY_MESSAGE_BARCODE = "EXT_LOYALTY_MESSAGE_BARCODE";
    private static final String JSON_MESSAGE = "[ {\n" +
            "  \"className\" : \"ru.crystals.loyal.check.TemplatedPrintObject\",\n" +
            "  \"template\" : {\n" +
            "    \"document\" : \"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\" standalone=\\\"yes\\\"?><document xmlns=\\\"http://crystals" +
            ".ru/pos/fiscalprinter/templates/parser\\\"><section id=\\\"0\\\"><line font=\\\"normal\\\" multyple=\\\"true\\\" align=\\\"left\\\" " +
            "notTrim=\\\"true\\\"><text value=\\\"12415151\\\" multyple=\\\"true\\\" trim=\\\"false\\\"/></line></section><collectionConditions xsi:nil=\\\"true\\\" " +
            "xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\"/><collectionFunctions xsi:nil=\\\"true\\\" xmlns:xsi=\\\"http://www.w3" +
            ".org/2001/XMLSchema-instance\\\"/></document>\"\n" +
            "  },\n" +
            "  \"dataset\" : { },\n" +
            "  \"value\" : \"\",\n" +
            "  \"type\" : \"TEXT\"\n" +
            "} ]";
    private static final String ERP_CODE = "123456";

    /**
     * Проверим, что конвертятся все скидки
     */
    @Test
    public void testDiscountsConverted() {
        // data
        PurchaseEntity purchaseEntry = createFakePurchase();
        addMessageResult(purchaseEntry);
        ChequeAdvertEntity adv1 = createChequeAdvert("ad1", false);
        ChequeAdvertEntity adv2 = createChequeAdvert("ad2", true);
        purchaseEntry.getDiscountPurchaseEntity().getChequeAdverts().add(adv1);
        purchaseEntry.getDiscountPurchaseEntity().getChequeAdverts().add(adv2);
        ChequeCouponEntity cpn1 = createChequeCoupon("cp1", true);
        ChequeCouponEntity cpn2 = createChequeCoupon("cp2", false);
        purchaseEntry.getDiscountPurchaseEntity().setChequeCoupons(Lists.newArrayList(cpn1, cpn2));
        PaymentSuspensionData paymentSuspensionData = new PaymentSuspensionData();
        paymentSuspensionData.setPaymentType("Bank");
        paymentSuspensionData.setPaymentSum(501L);
        paymentSuspensionData.setAttributes(ImmutableMap.of("k1", "v1"));
        purchaseEntry.setPaymentSuspensionData(paymentSuspensionData);

        LoyTransactionEntity tx = new LoyTransactionEntity();
        LoyDiscountPositionEntity discountPositionEntity = new LoyDiscountPositionEntity();
        discountPositionEntity.setPositionOrder(1);
        discountPositionEntity.setGoodCode(GOOD_CODE);
        tx.getDiscountPositions().add(discountPositionEntity);
        discountPositionEntity = new LoyDiscountPositionEntity();
        discountPositionEntity.setPositionOrder(1);
        discountPositionEntity.setGoodCode(GOOD_CODE);
        tx.getDiscountPositions().add(discountPositionEntity);
        LoyGiftNoteEnity giftNoteEntity = createGiftNote();
        tx.getGiftNotes().add(giftNoteEntity);
        LoyChequeAdvertiseEntity checkAdv = new LoyChequeAdvertiseEntity();
        LoyAdvActionInPurchaseEntity advAction = new LoyAdvActionInPurchaseEntity();
        advAction.setGuid(ACTION_GUID);
        checkAdv.setAdvAction(advAction);
        checkAdv.setText(JSON_MESSAGE);
        tx.getChequeAdverts().add(checkAdv);
        LoySetApiPluginTransactionEntity setApiTx = new LoySetApiPluginTransactionEntity();
        setApiTx.setId(9L);
        tx.getSetApiLoyaltyTransactions().add(setApiTx);
        LoyBonusPositionEntity bp = new LoyBonusPositionEntity();
        bp.setQuantity(2_000L);
        bp.setBonusAmount(3_000L);
        LoyAdvActionInPurchaseEntity bonusAdvAction = new LoyAdvActionInPurchaseEntity();
        bonusAdvAction.setGuid(9L);
        bonusAdvAction.setActionType(ActionType.BONUS_SR10);
        bp.setAdvAction(bonusAdvAction);
        tx.getBonusPositions().add(bp);
        // mocks
        TechProcessServiceAsync techProcess = Mockito.mock(TechProcessServiceAsync.class);
        TechProcessInterface techProcessInterface = Mockito.mock(TechProcessInterface.class);
        Loyal synchLoyalty = Mockito.mock(Loyal.class);
        CatalogService catalogService = Mockito.mock(CatalogService.class);
        Mockito.when(synchLoyalty.findLoyTransaction(purchaseEntry)).thenReturn(tx);
        ShiftEntity shift = new ShiftEntity();
        shift.setShopIndex(2L);
        shift.setCashNum(3L);
        Mockito.when(techProcessInterface.getShift()).thenReturn(shift);
        Mockito.when(techProcessInterface.getExpectedDocNum(Mockito.anyString())).thenReturn(DOC_NUM);
        List collect = purchaseEntry.getPositions().stream().map(position ->
                LoyalProductsConverter.getLoyalProductByItem(position.getItem(), true)).collect(Collectors.toList());
        Mockito.when(catalogService.getLoyalGoodsBatchByItems(Mockito.anyListOf(String.class))).thenReturn(collect);
        purchaseEntry.getServiceDocs().add(
                new ExternalLoyaltyCheckMessageDocument(EXT_LOYALTY_MESSAGE_TEXT, EXT_LOYALTY_MESSAGE_BARCODE));
        addSiebelDiscountCard(purchaseEntry);
        // добавление плагинного товара: особенность в том, что он не будет найден методом CatalogService.getLoyalGoodsBatchByItems
        PositionEntity position2 = createPositionWithPluginProduct();
        purchaseEntry.getPositions().add(position2);

        // test
        Purchase purchase = LoyalCalculatorConverter.convertPurchase(purchaseEntry, techProcess, techProcessInterface, synchLoyalty,
                Mockito.mock(TechProcessEvents.class), catalogService, true, true);
        Assert.assertEquals(2, purchase.getPositions().get(0).getDiscountBatch().getDiscounts().size());
        Assert.assertEquals(1, purchase.getPositions().get(0).getProductionDates().size());
        Assert.assertNotNull(purchase.getPositions().get(0).getExpirationDate());
        Assert.assertEquals(InsertType.HAND, purchase.getPositions().get(0).getInsertType());
        Assert.assertFalse(purchase.getPositions().get(0).getGoods().isDiscountable());
        Assert.assertEquals(POSITION_SUM, purchase.getPositions().get(0).getSum());

        // проверка передачи полей плагинного товара
        Assert.assertEquals(position2.getProduct().getName(), purchase.getPositions().get(1).getGoods().getName());
        Assert.assertEquals(position2.getProduct().getCountryCode(), purchase.getPositions().get(1).getGoods().getCountry().getCode());
        Assert.assertEquals(position2.getProduct().getManufacturerCode(), purchase.getPositions().get(1).getGoods().getManufacturer().getCode());
        Assert.assertEquals(position2.getProduct().getGroupCode(), purchase.getPositions().get(1).getGoods().getGroup().getCode());
        Assert.assertEquals(new BigDecimal(position2.getProduct().getNds()), purchase.getPositions().get(1).getGoods().getVat());
        Assert.assertEquals(LoyalProductType.ProductType.WEIGHT, purchase.getPositions().get(1).getGoods().getProductType().getType());
        Assert.assertEquals(position2.getProduct().getPrice(1L).getPrice(), purchase.getPositions().get(1).getGoods().getPrice(1L).getPrice());

        Assert.assertEquals(shift.getShopIndex(), purchase.getShift().getShopIndex());
        Assert.assertEquals(shift.getCashNum(), purchase.getShift().getCashNum());
        Assert.assertEquals(DOC_NUM, purchase.getNumber().longValue());
        List<String> operatorMsgs = purchase.getAdvertisingActionResults().stream()
                .filter(res -> res instanceof MessageAdvertisingActionResultEntity).findFirst()
                .map(res -> (MessageAdvertisingActionResultEntity) res).get().getOperatorMsg();
        assertThat(operatorMsgs).containsOnlyElementsOf(Arrays.asList(CASHIER_MESSAGE)).hasSize(1);
        GiftNoteDescription giftNote = (GiftNoteDescription) purchase.getAdvertisingActionResults().stream()
                .filter(res -> res instanceof GiftNoteDescription).findFirst().orElse(null);
        assertGiftNoteStructuresEquals(giftNote, giftNoteEntity);
        ExternalLoyaltyCheckMessageActionResult extLoyaltyMessage =
                (ExternalLoyaltyCheckMessageActionResult) purchase.getAdvertisingActionResults().stream()
                .filter(res -> res instanceof ExternalLoyaltyCheckMessageActionResult).findFirst().orElse(null);
        Assert.assertEquals(EXT_LOYALTY_MESSAGE_BARCODE, extLoyaltyMessage.getBarcode());
        Assert.assertNotNull(extLoyaltyMessage.getAdvertisingActionGUID());
        assertThat(extLoyaltyMessage.getText()).containsOnlyElementsOf(EXT_LOYALTY_MESSAGE_TEXT).hasSize(1);

        assertPurchaseContainsSlip(purchase, (TemplatedServiceDocument) adv1.getChequeText());
        assertPurchaseContainsSlip(purchase, (TemplatedServiceDocument) adv2.getChequeText());
        assertPurchaseContainsSlip(purchase, cpn1.getCouponText());
        assertPurchaseContainsSlip(purchase, cpn2.getCouponText());
        Assert.assertEquals(paymentSuspensionData.getPaymentSum(), purchase.getPayments().get(0).getSumPay());
        Assert.assertEquals(paymentSuspensionData.getPaymentType(), purchase.getPayments().get(0).getPaymentType());
        assertThat(purchase.getPayments().get(0).getProperties().entrySet())
                .containsOnlyElementsOf(paymentSuspensionData.getAttributes().entrySet()).hasSize(1);
        assertThat(purchase.getSetApiLoyaltyTransactions()).containsOnlyElementsOf(tx.getSetApiLoyaltyTransactions()).hasSize(1);
        Assert.assertEquals(bp.getQuantity(), purchase.getBonusPositions().get(0).getQuantity());
        Assert.assertEquals(bp.getBonusAmount(), purchase.getBonusPositions().get(0).getBonusAmount());
        Assert.assertEquals(bp.getAdvAction().getGuid().longValue(), purchase.getBonusPositions().get(0).getAdvertActGuid());

        ChequeAdvertiseActionResultEntity checkResult = purchase.getAdvertisingActionResults().stream()
                .filter(res -> res instanceof ChequeAdvertiseActionResultEntity).findFirst()
                .map(res -> (ChequeAdvertiseActionResultEntity) res).get();
        Assert.assertEquals(Long.valueOf(ACTION_GUID), checkResult.getAdvertisingActionGUID());
        Assert.assertEquals(1, checkResult.getPrintObjects().size());

        Assert.assertEquals(POSITION_MIN_PRICE, purchase.getPositions().get(0).getExternalMinPriceRestriction());
        Assert.assertEquals(ERP_CODE, purchase.getPositions().get(0).getErpCode());
    }

    private LoyGiftNoteEnity createGiftNote() {
        LoyGiftNoteEnity lgn = new LoyGiftNoteEnity();
        lgn.setTotalCount(2);
        lgn.setRequireScan(true);
        lgn.setAddAsProducts(true);
        lgn.setDisplayTime(GiftDisplayTime.SUBTOTAL);
        lgn.setCashierMessage("CashierMessage2");
        lgn.setCustomerMessage("CustomerMessage2");
        return lgn;
    }

    private void assertGiftNoteStructuresEquals(GiftNoteDescription giftNote, LoyGiftNoteEnity giftNoteEntity) {
        Assert.assertEquals(giftNoteEntity.getTotalCount(), giftNote.getTotalQnty());
        Assert.assertEquals(giftNoteEntity.isRequireScan(), giftNote.isRequireScan());
        Assert.assertEquals(giftNoteEntity.isAddAsProducts(), giftNote.isAddAsProducts());
        Assert.assertEquals(giftNoteEntity.getDisplayTime(), giftNote.getDisplayTime());
        assertThat(giftNote.getCashierMessage()).containsOnlyElementsOf(Arrays.asList(giftNoteEntity.getCashierMessage())).hasSize(1);
        assertThat(giftNote.getCustomerMessage()).containsOnlyElementsOf(Arrays.asList(giftNoteEntity.getCustomerMessage())).hasSize(1);
    }

    private void assertPurchaseContainsSlip(Purchase purchase, TemplatedServiceDocument serviceDoc) {
        Assert.assertEquals(purchase.getAdvertisingActionResults().stream()
                .filter(r -> r.getPrintObjects().stream().map(TemplatedPrintObject.class::cast).findFirst().get()
                        .getDataset().equals(serviceDoc.getDataset())).findFirst().get().isSeparatePrint(), serviceDoc.isSeparatePrint());
    }

    private PurchaseEntity createFakePurchase() {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setId(1L);
        PositionEntity pos = new PositionEntity();
        pos.setItem(GOOD_CODE);
        pos.setNumber(1L);
        pos.setNds(10F);
        pos.setSum(POSITION_SUM);
        pos.setPriceStart(1000L);
        pos.setQnty(1000L);
        pos.setProductType("ProductPieceEntity");
        pos.setPurchase(purchaseEntity);
        pos.setInsertType(InsertType.HAND);
        PositionProductionDateEntity pd = new PositionProductionDateEntity();
        pd.setProductionDate(new Date());
        pd.setQuantity(1000L);
        pos.getProductionDates().add(pd);
        pos.setExpirationDate(new Date());
        pos.setProductSilent(new ProductEntity());
        pos.getProduct().setProductConfig(new ProductConfig());
        pos.getProduct().getProductConfig().setIsDiscountApplicable(false);
        pos.setErpCode(ERP_CODE);

        purchaseEntity.getPositions().add(pos);
        purchaseEntity.setDiscountPurchaseEntity(new DiscountPurchaseEntity());
        return purchaseEntity;
    }

    private void addMessageResult(PurchaseEntity purchase) {
        MessageEntity message = new MessageEntity();
        message.setCashierMessage(CASHIER_MESSAGE);
        purchase.getDiscountPurchaseEntity().setMessages(Arrays.asList(message));
    }

    private ChequeAdvertEntity createChequeAdvert(String text, boolean separatePrint) {
        ChequeAdvertEntity advert = new ChequeAdvertEntity();
        TemplatedServiceDocument tsd = new TemplatedServiceDocument(null, ImmutableMap.of(text, text));
        tsd.setSeparatePrint(separatePrint);
        advert.setChequeText(tsd);
        return advert;
    }

    private ChequeCouponEntity createChequeCoupon(String text, boolean separatePrint) {
        ChequeCouponEntity coupon = new ChequeCouponEntity();
        CouponTemplatedServiceDocument tsd = new CouponTemplatedServiceDocument(null, ImmutableMap.of(text, text));
        tsd.setSeparatePrint(separatePrint);
        coupon.setCouponText(tsd);
        return coupon;
    }

    private void addSiebelDiscountCard(PurchaseEntity purchase) {
        CardTypeEntity cardTypeEntity = new CardTypeEntity();
        cardTypeEntity.getTransientPropertyBag().put(SiebelService.SIEBEL_POSITIONS_MIN_PRICES, Collections.singletonMap(1L, POSITION_MIN_PRICE));

        CardEntity cardEntity = new CardEntity();
        cardEntity.setNumber("10001");
        cardEntity.setCardType(cardTypeEntity);

        InternalCardsEntity cardType = new InternalCardsEntity();
        cardType.getCards().add(cardEntity);
        purchase.getBonusDiscountCards().put(BonusDiscountType.BONUS_SIEBEL, cardType.getCards());
    }

    private PositionEntity createPositionWithPluginProduct() {
        PositionEntity pos = new PositionEntity();
        pos.setProductSilent(new ProductEntity());
        pos.getProduct().setProductConfig(new ProductConfig());
        pos.getProduct().setItem("888999");
        pos.getProduct().setName("Name 888999");
        pos.getProduct().setCountryCode("Country 888999");
        pos.getProduct().setManufacturerCode("Manufacturer 888999");
        pos.getProduct().setGroupCode("Group 888999");
        pos.getProduct().setNds(19F);
        pos.getProduct().setDiscriminator("ProductWeightEntity");
        PriceEntity price = new PriceEntity();
        price.setNumber(1);
        price.setDepartNumber(1L);
        price.setPrice(1006L);
        pos.getProduct().getPrices().add(price);
        pos.setItem(pos.getProduct().getItem());
        pos.setNumber(2L);
        pos.setSum(price.getPrice());
        pos.setPriceStart(price.getPrice());
        pos.setQnty(1000L);
        return pos;
    }
}
