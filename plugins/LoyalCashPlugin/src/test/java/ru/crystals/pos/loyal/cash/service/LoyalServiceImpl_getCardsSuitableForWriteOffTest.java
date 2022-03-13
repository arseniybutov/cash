package ru.crystals.pos.loyal.cash.service;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import ru.crystals.cards.CardBonusBalance;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.discounts.AdvertisingActionEntity;
import ru.crystals.loyal.check.bonus.BonusDiscountType;
import ru.crystals.loyalty.LoyaltyProperties;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.loyal.bridge.service.LoyaltyPropertiesFactory;
import ru.crystalservice.setv6.discounts.plugins.DiscountActionResult;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertNotNull;

public class LoyalServiceImpl_getCardsSuitableForWriteOffTest {
    private static final String CARD_PLUGIN_ID = "card.plugin.id.1";
    private static final String CARD_PLUGIN_ID_2 = "card.plugin.id.2";
    private static final String CARD_NUMBER = "123-456-789";
    private static final String PHONE_NUMBER = "89051232233";

    @Test
    public void testGetLoyaltyPluginCardsSuitableForWriteoff() {
        LoyalServiceImpl utils = new LoyalServiceImpl();
        LoyaltyPropertiesFactory mock = Mockito.mock(LoyaltyPropertiesFactory.class);
        LoyaltyProperties loyaltyProperties = new LoyaltyProperties();
        Mockito.doReturn(loyaltyProperties).when(mock).get();
        Whitebox.setInternalState(utils, "loyaltyPropertiesFactory", mock);
        utils.setCache(Mockito.mock(AdvActionsCache.class));
        Mockito.when(utils.getCache().getActiveActions(Mockito.any(Date.class), Mockito.anySet()))
                .thenReturn(Lists.newArrayList(createAction(CARD_PLUGIN_ID, 1000), createAction(CARD_PLUGIN_ID_2, 1001)));

        PurchaseEntity pe = new PurchaseEntity();
        pe.setSale();
        PurchaseCardsEntity pce = new PurchaseCardsEntity();

        pce.setNumber(CARD_NUMBER);
        CardTypeEntity cte = new CardTypeEntity();
        CardEntity ce = new CardEntity();
        CardBonusBalance cbe = new CardBonusBalance();
        cbe.setBalance(BigDecimal.valueOf(128));
        cbe.setAvailableChargeOffBalance(128L);
        cbe.setSponsorId(BonusDiscountType.SET_API);
        cbe.setStrSponsorId(CARD_PLUGIN_ID);
        ce.setCardBonusBalance(cbe);
        cte.getCards().add(ce);
        pce.setCardType(cte);

        pe.getCards().add(pce);

        // 1. Чек null.
        assertNotNull(utils.getCardsSuitableForWriteOff(null, BonusDiscountType.SET_API));
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(null, BonusDiscountType.SET_API).size());

        // 2. Не чек продажи.
        pe.setReturn();
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 3. Чек продажи, валидная карта.
        pe.setSale();
        Assert.assertEquals(1, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 3.1 Чек продажи, валидная карта, но по номеру телефона
        pce.setAddedBy(PurchaseCardsEntity.ApplyMean.PHONE);
        pce.setPhoneNumber(PHONE_NUMBER);
        Assert.assertEquals(1, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 3.2 Чек продажи, валидная карта, но по номеру телефона запрещено
        loyaltyProperties.setNoWriteOffByPhone(true);
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 4. Валидная карта, но плагинов нет.
        pce.setAddedBy(PurchaseCardsEntity.ApplyMean.HAND);
        pce.setPhoneNumber(null);
        Mockito.when(utils.getCache().getActiveActions(Mockito.any(Date.class), Mockito.anySet())).thenReturn(Collections.emptyList());
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 5. Две валидных карты
        Mockito.when(utils.getCache().getActiveActions(Mockito.any(Date.class), Mockito.anySet()))
                .thenReturn(Lists.newArrayList(createAction(CARD_PLUGIN_ID, 1001), createAction(CARD_PLUGIN_ID_2, 1000)));
        pe.getCards().add(createCard());
        Assert.assertEquals(2, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());
        // карта первого плагина должна идти второй согласно приоритету акции
        Assert.assertSame(pce, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).get(1));

        // 6. Одна валидная, другая невалидная карты.
        pe.getCards().get(1).setCardType(null);
        Assert.assertEquals(1, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 7. Чек без карт.
        pe.getCards().clear();
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 8. Карта без имени процессинга
        pe.getCards().add(pce);
        cbe.setStrSponsorId(null);
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 9. Карта без типа карты.
        cbe.setStrSponsorId(CARD_PLUGIN_ID);
        pce.setCardType(null);
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 10. Карта без карт внутри типа карты.
        pce.setCardType(cte);
        cte.getCards().clear();
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 11. Карта без бонусного баланся.
        cte.getCards().add(ce);
        ce.setCardBonusBalance(null);
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 12. Карта без доступных к списанию бонусов.
        ce.setCardBonusBalance(cbe);
        cbe.setAvailableChargeOffBalance(0L);
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 13. Карта с неизвестным плагином
        cbe.setAvailableChargeOffBalance(12L);
        cbe.setStrSponsorId("Unknown processing name");
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());

        // 14. Карта, по которой уже было списание.
        cbe.setStrSponsorId(CARD_PLUGIN_ID);
        cbe.setChargeOffAmount(2L);
        Assert.assertEquals(0, utils.getCardsSuitableForWriteOff(pe, BonusDiscountType.SET_API).size());
    }

    private PurchaseCardsEntity createCard() {
        PurchaseCardsEntity pce = new PurchaseCardsEntity();
        pce.setNumber("9999-9999-9");
        CardTypeEntity cte = new CardTypeEntity();
        CardEntity ce = new CardEntity();
        CardBonusBalance cbe = new CardBonusBalance();
        cbe.setBalance(BigDecimal.valueOf(897));
        cbe.setAvailableChargeOffBalance(546L);
        cbe.setSponsorId(BonusDiscountType.SET_API);
        cbe.setStrSponsorId(CARD_PLUGIN_ID_2);
        ce.setCardBonusBalance(cbe);
        cte.getCards().add(ce);
        pce.setCardType(cte);
        return pce;
    }

    private AdvertisingActionEntity createAction(String pluginId, double priority) {
        AdvertisingActionEntity action = new AdvertisingActionEntity();
        action.setPriority(priority);
        action.getDeserializedPlugins().add(new DiscountActionResult(DiscountActionResult.ValueType.EXTERNAL_LOYALTY, pluginId));
        return action;
    }
}
