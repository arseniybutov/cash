package ru.crystals.pos.plugins.purchases.v1;

import org.junit.Test;
import org.mockito.Mockito;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.pos.catalog.PluginProperty;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.SessionEntity;
import ru.crystals.pos.check.ShiftEntity;
import ru.crystals.pos.check.UserEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.plugins.purchases.v1.converter.PurchaseConverter;
import ru.crystals.pos.plugins.purchases.v1.generated.CardType;
import ru.crystals.pos.plugins.purchases.v1.generated.CardTypeType;
import ru.crystals.pos.plugins.purchases.v1.generated.ClientType;
import ru.crystals.pos.plugins.purchases.v1.generated.PaymentType;
import ru.crystals.pos.plugins.purchases.v1.generated.PositionType;
import ru.crystals.pos.plugins.purchases.v1.generated.PurchaseType;
import ru.crystals.pos.plugins.purchases.v1.generated.Purchases;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PurchaseConverterTest {
    private Date dateCommit = new Date();

    @Test
    public void testGetPurchase() throws Exception {
        //тестовый чек
        PurchaseEntity purchaseEntity = getPurchaseEntity(dateCommit, ru.crystals.cards.common.ClientType.JURISTIC_PERSON);

        //system under test
        PurchaseConverter sut = new PurchaseConverter();

        //Класс для JAXB, который передается веб-сервису, полученный из тестового чека
        Purchases xmlPurchases = sut.getPurchases(Arrays.asList(purchaseEntity));

        validatePurchase(xmlPurchases, purchaseEntity);
    }

    @Test
    public void testGetPurchaseWithEmptyClientType() throws Exception {
        //тестовый чек
        PurchaseEntity purchaseEntity = getPurchaseEntity(dateCommit, null);

        //system under test
        PurchaseConverter sut = new PurchaseConverter();

        //Класс для JAXB, который передается веб-сервису, полученный из тестового чека
        Purchases xmlPurchases = sut.getPurchases(Arrays.asList(purchaseEntity));

        validatePurchase(xmlPurchases, purchaseEntity);
    }

    /**
     * Тестовые данные 11 - вложено в 1. 2 - на одном уровне с 1
     *
     * @return
     */
    private List<PluginProperty> getTestPluginProperties1() {

        List<PluginProperty> result = new ArrayList<>();

        PluginProperty property1 = new PluginProperty("key1", "value1");
        PluginProperty property2 = new PluginProperty("key2", "value2");
        PluginProperty property11 = new PluginProperty("key1.1", "value1.1");
        property1.getProperties().add(property11);

        result.add(property1);
        result.add(property2);

        return result;
    }

    /**
     * Тестовые данные - 3 вложенных плагинных свойства  (1 - 11 - 111)
     *
     * @return
     */
    private List<PluginProperty> getTestPluginProperties2() {

        List<PluginProperty> result = new ArrayList<>();

        PluginProperty property1 = new PluginProperty("key1", "value1");
        PluginProperty property11 = new PluginProperty("key1.1", "value1.1");
        PluginProperty property111 = new PluginProperty("key1.1.1", "value1.1.1");
        property11.getProperties().add(property111);
        property1.getProperties().add(property11);

        result.add(property1);

        return result;
    }

    /**
     * Сравним полученное JAXB представление с исходными данными
     *
     * @param xmlPurchases
     * @param purchaseEntity
     */
    private void validatePurchase(Purchases xmlPurchases, PurchaseEntity purchaseEntity) throws DatatypeConfigurationException {
        List<PurchaseType> purchases = xmlPurchases.getPurchase();

        assertNotNull("Purchase is null", purchases);
        assertEquals("Unexpected count of purchsases passed to WS", 1, purchases.size());

        PurchaseType purchase = purchases.get(0);

        //проверим базовые свойства чека
        checkPurchaseBaseProperties(purchase, purchaseEntity);

        //проверим позиции
        List<PositionType> positions = purchase.getPositions().getPosition();
        assertEquals(2, positions.size());

        checkPositions(positions);

        //проверим оплаты
        List<PaymentType> payments = purchase.getPayments().getPayment();
        assertEquals(2, positions.size());

        checkPayments(payments);

        //проверим оплаты
        List<CardType> cards = purchase.getCard();
        assertEquals(3, cards.size());

        checkCards(cards);

        checkDiscountCards(purchase.getDiscountCards().getDiscountCard());
    }

    private void checkDiscountCards(List<String> discountCard) {
        assertEquals(3, discountCard.size());
        assertEquals(Arrays.<String>asList("11", "1122", "112233"), discountCard);
    }

    private void checkCards(List<CardType> cards) {
        assertEquals("11", cards.get(0).getNumber());
        assertEquals(CardTypeType.INTERNAL, cards.get(0).getType());
        assertEquals(Long.valueOf(5L), cards.get(0).getCardTypeGuid());

        assertEquals("1122", cards.get(1).getNumber());
        assertEquals(CardTypeType.EXTERNAL, cards.get(1).getType());
        assertEquals(Long.valueOf(6L), cards.get(1).getCardTypeGuid());

        assertEquals("112233", cards.get(2).getNumber());
        assertEquals(CardTypeType.COUPON_RECEIPT, cards.get(2).getType());
        assertEquals(Long.valueOf(7L), cards.get(2).getCardTypeGuid());
    }

    private void checkPurchaseBaseProperties(PurchaseType purchase, PurchaseEntity purchaseEntity) throws DatatypeConfigurationException {
        assertEquals(1L, purchase.getNumber());
        assertEquals(Double.valueOf(90.32), purchase.getAmount());
        assertEquals(Double.valueOf(10.23), purchase.getDiscountAmount());
        assertEquals("Surname N. .", purchase.getUserName());
        assertEquals("Tabnum", purchase.getTabNumber());

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(dateCommit);
        assertEquals(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal), purchase.getSaletime());
        assertEquals(1L, purchase.getShift());
        assertEquals(2L, purchase.getShop());
        assertEquals(3L, purchase.getCash());
        assertEquals(true, purchase.isDenyPrintToDocuments());
        if (purchaseEntity.getClientType() == null) {
            assertNull(purchase.getClientType());
        } else {
            assertEquals(purchaseEntity.getClientType().toString(), purchase.getClientType().toString());
        }
        assertEquals(Long.valueOf(67L), purchase.getClientGuid());
    }

    private void checkPositions(List<PositionType> positions) {
        PositionType pos1 = positions.get(0);
        assertEquals(3L, pos1.getDepartNumber());
        assertEquals(3.3, pos1.getCost(), 0.001);
        assertEquals(3.0, pos1.getCostWithDiscount(), 0.001);
        assertEquals(0.3, pos1.getDiscountValue(), 0.001);
        assertEquals(2, pos1.getPluginProperty().size());
        assertEquals(1, pos1.getPluginProperty().get(0).getPluginProperty().size());
        assertEquals(0, pos1.getPluginProperty().get(1).getPluginProperty().size());
        assertEquals(77L, pos1.getOrder());

        PositionType pos2 = positions.get(1);
        assertEquals(4L, pos2.getDepartNumber());
        assertEquals(1, pos2.getPluginProperty().size());
        assertEquals("key1", pos2.getPluginProperty().get(0).getKey());
        assertEquals("value1", pos2.getPluginProperty().get(0).getValue());
        assertEquals(78L, pos2.getOrder());

        assertEquals(1, pos2.getPluginProperty().get(0).getPluginProperty().size());
        assertEquals("key1.1", pos2.getPluginProperty().get(0).getPluginProperty().get(0).getKey());
        assertEquals("value1.1", pos2.getPluginProperty().get(0).getPluginProperty().get(0).getValue());

        assertEquals(1, pos2.getPluginProperty().get(0).getPluginProperty().get(0).getPluginProperty().size());
        assertEquals("key1.1.1", pos2.getPluginProperty().get(0).getPluginProperty().get(0).getPluginProperty().get(0).getKey());
        assertEquals("value1.1.1", pos2.getPluginProperty().get(0).getPluginProperty().get(0).getPluginProperty().get(0).getValue());
    }

    private void checkPayments(List<PaymentType> payments) {
        PaymentType pay1 = payments.get(0);
        assertEquals(Double.valueOf(302.22).doubleValue(), pay1.getAmount(), 0.001);
        assertEquals(2, pay1.getPluginProperty().size());
        assertEquals(1, pay1.getPluginProperty().get(0).getPluginProperty().size());
        assertEquals(0, pay1.getPluginProperty().get(1).getPluginProperty().size());

        PaymentType pay2 = payments.get(1);
        assertEquals(Double.valueOf(21.84).doubleValue(), pay2.getAmount(), 0.001);
        assertEquals(1, pay2.getPluginProperty().size());
        assertEquals("key1", pay2.getPluginProperty().get(0).getKey());
        assertEquals("value1", pay2.getPluginProperty().get(0).getValue());

        assertEquals(1, pay2.getPluginProperty().get(0).getPluginProperty().size());
        assertEquals("key1.1", pay2.getPluginProperty().get(0).getPluginProperty().get(0).getKey());
        assertEquals("value1.1", pay2.getPluginProperty().get(0).getPluginProperty().get(0).getValue());

        assertEquals(1, pay2.getPluginProperty().get(0).getPluginProperty().get(0).getPluginProperty().size());
        assertEquals("key1.1.1", pay2.getPluginProperty().get(0).getPluginProperty().get(0).getPluginProperty().get(0).getKey());
        assertEquals("value1.1.1", pay2.getPluginProperty().get(0).getPluginProperty().get(0).getPluginProperty().get(0).getValue());
    }

    protected PurchaseEntity getPurchaseEntity(Date dateCommit, ru.crystals.cards.common.ClientType juristicPerson) {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setNumber(1L);
        purchaseEntity.setCheckSumStart(10055L);
        SessionEntity session = new SessionEntity();
        purchaseEntity.setSession(session);
        purchaseEntity.getSession().setUser(new UserEntity());
        purchaseEntity.getSession().getUser().setFirstName("Name");
        purchaseEntity.getSession().getUser().setLastName("Surname");
        purchaseEntity.getSession().getUser().setTabNum("Tabnum");
        purchaseEntity.setOperationType(true);
        purchaseEntity.setDiscountValueTotal(1023L);
        purchaseEntity.setDateCommit(dateCommit);
        purchaseEntity.setDenyPrintToDocuments(true);
        purchaseEntity.setClientType(juristicPerson);
        purchaseEntity.setClientGUID(67L);


        ShiftEntity shift = new ShiftEntity();
        shift.setSessionStart(session);
        shift.setNumShift(1L);
        shift.setShopIndex(2L);
        shift.setCashNum(3L);
        purchaseEntity.setShift(shift);

        //тестовые позиции
        PositionEntity positionEntity1 = Mockito.spy(new PositionEntity());
        positionEntity1.setNds((float) 18.0);
        positionEntity1.setDepartNumber(3L);
        positionEntity1.setPriceStart(330L);
        positionEntity1.setPriceEnd(300L);
        positionEntity1.setSumDiscount(30L);
        positionEntity1.setNumber(77L);
        PositionEntity positionEntity2 = Mockito.spy(new PositionEntity());
        positionEntity2.setNds((float) 18.0);
        positionEntity2.setDepartNumber(4L);
        positionEntity2.setNumber(78L);
        Mockito.doReturn(getTestPluginProperties1()).when(positionEntity1).getPluginProperties();
        Mockito.doReturn(getTestPluginProperties2()).when(positionEntity2).getPluginProperties();

        //тестовые оплаты
        PaymentEntity paymentEntity1 = Mockito.spy(new PaymentEntity());
        paymentEntity1.setSumPay(30222L);
        PaymentEntity paymentEntity2 = Mockito.spy(new PaymentEntity());
        paymentEntity2.setSumPay(2184L);
        Mockito.doReturn(getTestPluginProperties1()).when(paymentEntity1).getPluginProperties();
        Mockito.doReturn(getTestPluginProperties2()).when(paymentEntity2).getPluginProperties();

        //тестовые карты
        PurchaseCardsEntity card1 = new PurchaseCardsEntity("11", CardTypes.InternalCard, purchaseEntity);
        card1.setCardTypeGUID(5L);
        PurchaseCardsEntity card2 = new PurchaseCardsEntity("1122", CardTypes.ExternalCard, purchaseEntity);
        card2.setCardTypeGUID(6L);
        PurchaseCardsEntity card3 = new PurchaseCardsEntity("112233", CardTypes.ChequeCoupon, purchaseEntity);
        card3.setCardTypeGUID(7L);

        //тестовый чек
        purchaseEntity.getPayments().add(paymentEntity1);
        purchaseEntity.getPayments().add(paymentEntity2);
        purchaseEntity.getPositions().add(positionEntity1);
        purchaseEntity.getPositions().add(positionEntity2);
        purchaseEntity.addCard(card1);
        purchaseEntity.addCard(card2);
        purchaseEntity.addCard(card3);
        return purchaseEntity;
    }
}