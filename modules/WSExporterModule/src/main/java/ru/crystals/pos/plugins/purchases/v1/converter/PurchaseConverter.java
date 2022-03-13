package ru.crystals.pos.plugins.purchases.v1.converter;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.ERPIntegration.operday.plugins.set10WSClient.converters.BaseXMLConverter;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.pos.catalog.PluginProperty;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseCardsEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.UserEntity;
import ru.crystals.pos.payments.CashPaymentEntity;
import ru.crystals.pos.payments.PaymentEntity;
import ru.crystals.pos.plugins.converters.IPurchasesConverter;
import ru.crystals.pos.plugins.purchases.v1.generated.CardType;
import ru.crystals.pos.plugins.purchases.v1.generated.CardTypeType;
import ru.crystals.pos.plugins.purchases.v1.generated.ClientType;
import ru.crystals.pos.plugins.purchases.v1.generated.ObjectFactory;
import ru.crystals.pos.plugins.purchases.v1.generated.PaymentType;
import ru.crystals.pos.plugins.purchases.v1.generated.PluginPropertyType;
import ru.crystals.pos.plugins.purchases.v1.generated.PositionType;
import ru.crystals.pos.plugins.purchases.v1.generated.PurchaseType;
import ru.crystals.pos.plugins.purchases.v1.generated.Purchases;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by myaichnikov on 16.03.2015.
 */
public class PurchaseConverter  implements IPurchasesConverter, BaseXMLConverter{
    private static final String VERSION = "1.0";
    private static final Logger LOG = LoggerFactory.getLogger(PurchaseConverter.class);
    public static final String CASH_PAYMENT_ENTITY = "CashPaymentEntity";
    public static final String CASH_CHANGE_PAYMENT_ENTITY = "CashChangePaymentEntity";
    private ObjectFactory factory = new ObjectFactory();

    @Override
    public byte[] convertData(List<PurchaseEntity> entityList) {
        Purchases purchases = getPurchases(entityList);
        try {
            return getBytes(purchases);
        } catch (JAXBException e) {
            LOG.warn("Failed to serialize data!", e);
            return null;
        }
    }

    public Purchases getPurchases(List<PurchaseEntity> entityList) {
        Purchases purchases = factory.createPurchases();
        for (PurchaseEntity purchaseEntity : entityList) {
            purchases.getPurchase().add(getPurchase(purchaseEntity));
        }
        purchases.setCount((long) purchases.getPurchase().size());
        return purchases;
    }

    protected PurchaseType getPurchase(PurchaseEntity purchaseEntity) {
        PurchaseType result = new PurchaseType();
        result.setNumber(purchaseEntity.getNumber());
        result.setAmount(purchaseEntity.getCheckSumEndBigDecimal().doubleValue());
        result.setTabNumber(purchaseEntity.getSession().getUser().getTabNum());
        result.setOperationType(purchaseEntity.getOperationType());
        result.setDiscountAmount(purchaseEntity.getDiscountValueTotalBigDecimal().doubleValue());
        result.setDenyPrintToDocuments(purchaseEntity.isDenyPrintToDocuments());
        result.setClientType(getClientType(purchaseEntity.getClientType()));
        result.setClientGuid(purchaseEntity.getClientGUID());
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(purchaseEntity.getDateCommit());

        try {
            result.setSaletime(DatatypeFactory.newInstance().newXMLGregorianCalendar(cal));
        } catch (DatatypeConfigurationException e) {
            LOG.error("failed to convert date of purchaseEntity {}", purchaseEntity.getDateCommit());
        }

        result.setShift(purchaseEntity.getShift().getNumShift());
        result.setShop(purchaseEntity.getShift().getShopIndex());
        result.setCash(purchaseEntity.getShift().getCashNum());

        UserEntity user = purchaseEntity.getSession().getUser();
        result.setUserName(String
            .format("%s %s. %s.", user.getLastName(), StringUtils.isEmpty(user.getFirstName()) ? "" : user.getFirstName().substring(0, 1),
                StringUtils.isEmpty(user.getMiddleName()) ? "" : user.getMiddleName().substring(0, 1)));

        result.setPositions(new ObjectFactory().createPositionsType());
        result.setPayments(new ObjectFactory().createPaymentsType());
        result.setDiscountCards(new ObjectFactory().createDiscountCardsType());

        fillPositions(purchaseEntity, result);
        fillPayments(purchaseEntity, result);
        fillCards(purchaseEntity, result);
        fillDiscountCards(purchaseEntity, result);

        return result;
    }

    /**
     * Заполнение информации по дисконтным картам в чеке
     *
     * @param purchaseEntity
     *     кассовая сущность чека
     * @param result
     *     объект для выгрузки в WS
     */
    protected void fillDiscountCards(PurchaseEntity purchaseEntity, PurchaseType result) {
        try {
            for (PurchaseCardsEntity cardsEntity : purchaseEntity.getCards()) {
                if (cardsEntity.getType() != null && cardsEntity.getNumber() != null) {
                    result.getDiscountCards().getDiscountCard().add(cardsEntity.getNumber());
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to set discount cards");
        }
    }

    /**
     * Заполнение информации по картам в чеке
     *
     * @param purchaseEntity
     *     кассовая сущность чека
     * @param result
     *     объект для выгрузки в WS
     */
    protected void fillCards(PurchaseEntity purchaseEntity, PurchaseType result) {
        try {
            for (PurchaseCardsEntity cardsEntity : purchaseEntity.getCards()) {
                CardType card = new ObjectFactory().createCardType();
                card.setNumber(cardsEntity.getNumber());
                card.setType(getCardType(cardsEntity.getType()));
                card.setCardTypeGuid(cardsEntity.getCardTypeGUID());
                result.getCard().add(card);
            }
        } catch (Exception e) {
            LOG.error("Failed to set cards");
        }
    }

    /**
     * Метод для получения из одного енума соответствие другому енуму (для выгрузки и внутренний)
     */
    protected CardTypeType getCardType(CardTypes cardType) {
        switch (cardType) {
            case CardNotFound:
                return null;
            case InternalCard:
                return CardTypeType.INTERNAL;
            case ExternalCard:
                return CardTypeType.EXTERNAL;
            case PresentCard:
                return CardTypeType.PRESENT;
            case BonusCard:
                return CardTypeType.BONUS;
            case CardCoupon:
                return CardTypeType.COUPON_CARD;
            case ChequeCoupon:
                return CardTypeType.COUPON_RECEIPT;
            case ProcessingCoupon:
                return CardTypeType.COUPON_PROCESSING;
            case UniqueCoupon:
                return CardTypeType.COUPON_UNIQUE;
            default:
                return CardTypeType.UNKNOWN;
        }
    }

    protected ClientType getClientType(ru.crystals.cards.common.ClientType type) {
        if(type == null) {
            return null;
        }
        switch (type) {
            case JURISTIC_PERSON:
                return ClientType.JURISTIC_PERSON;
            case PRIVATE:
                return ClientType.PRIVATE;
            default:
                return ClientType.PRIVATE;
        }
    }

    /**
     * Заполнение информации по оплатам в чеке
     *
     * @param purchaseEntity
     *     кассовая сущность чека
     * @param result
     *     объект для выгрузки в WS
     */
    protected void fillPayments(PurchaseEntity purchaseEntity, PurchaseType result) {
        try {
            for (PaymentEntity pay : purchaseEntity.getPayments()) {
                ObjectFactory objectFactory = new ObjectFactory();
                PaymentType payment = objectFactory.createPaymentType();
                if(CASH_PAYMENT_ENTITY.equals(pay.getPaymentType())) {
                    CashPaymentEntity cpe = (CashPaymentEntity) pay;
                    if(cpe.getChange()!=null && cpe.getChange()!=0L) {
                        PaymentType change = objectFactory.createPaymentType();
                        change.setTypeClass(CASH_CHANGE_PAYMENT_ENTITY);
                        change.setAmount(cpe.getChangeBigDecimal().doubleValue());
                        result.getPayments().getPayment().add(change);
                    }

                    payment.setTypeClass(pay.getPaymentType());
                    payment.setAmount(pay.getSumPayBigDecimal().doubleValue());
                } else {
                    payment.setAmount(pay.getEndSumPayBigDecimal().doubleValue());
                    payment.setTypeClass(pay.getPaymentType());
                }
                for (PluginProperty property : pay.getPluginProperties()) {
                    PluginPropertyType xmlProperty = getPluginPropertyType(property);
                    payment.getPluginProperty().add(xmlProperty);
                }
                result.getPayments().getPayment().add(payment);
            }
        } catch (Exception e) {
            LOG.error("Failed to set payments", e);
        }
    }

    /**
     * Метод рекурсивно заполняет JAXB объекты по PluginProperty
     *
     * @param property
     *     заполненный PluginProperty
     * @return заполненный PluginPropertyType
     */
    protected PluginPropertyType getPluginPropertyType(PluginProperty property) {
        PluginPropertyType xmlProperty = new ObjectFactory().createPluginPropertyType();
        xmlProperty.setKey(property.getKey());
        xmlProperty.setValue(property.getValue());
        if (property.getProperties() != null && !property.getProperties().isEmpty()) {
            for (PluginProperty prop : property.getProperties()) {
                xmlProperty.getPluginProperty().add(getPluginPropertyType(prop));
            }
        }
        return xmlProperty;
    }

    /**
     * Заполнение информации по позициям в чеке
     *
     * @param purchaseEntity
     *     кассовая сущность чека
     * @param result
     *     объект для выгрузки в WS
     */
    protected void fillPositions(PurchaseEntity purchaseEntity, PurchaseType result) {
        try {
            for (PositionEntity pos : purchaseEntity.getPositions()) {
                PositionType position = new ObjectFactory().createPositionType();
                if (pos.getQntyBigDecimal() != null) {
                    position.setCount(pos.getQntyBigDecimal().doubleValue());
                }
                if (pos.getSumBigDecimal() != null) {
                    position.setAmount(pos.getSumBigDecimal().doubleValue());
                }
                if (pos.getPriceStartBigDecimal() != null) {
                    position.setCost(pos.getPriceStartBigDecimal().doubleValue());
                }
                if (pos.getSumDiscountBigDecimal() != null) {
                    position.setDiscountValue(pos.getSumDiscountBigDecimal().doubleValue());
                }
                if (pos.getPriceEndBigDecimal() != null) {
                    position.setCostWithDiscount(pos.getPriceEndBigDecimal().doubleValue());
                }
                if (pos.getNdsSumBigDecimal() != null) {
                    position.setNdsSum(pos.getNdsSumBigDecimal().doubleValue());
                }

                position.setGoodsCode(pos.getItem());
                position.setBarCode(pos.getBarCode());
                position.setSoftCheckNumber(pos.getSoftCheckNumber());
                position.setNds(pos.getNds() != null ? pos.getNds() : 0);
                position.setNdsClass(pos.getNdsClass());
                position.setOrder(pos.getNumber());
                //-1 если не знаем что за номер отдела
                position.setDepartNumber(pos.getDepartNumber() != null ? pos.getDepartNumber() : -1);
                for (PluginProperty property : pos.getPluginProperties()) {
                    PluginPropertyType xmlProperty = getPluginPropertyType(property);
                    position.getPluginProperty().add(xmlProperty);
                }
                result.getPositions().getPosition().add(position);
            }
        } catch (Exception e) {
            LOG.error("Failed to set positions", e);
        }
    }

    @Override
    public String getVersion() {
        return VERSION;
    }
}
