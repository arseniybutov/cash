package ru.crystals.cards.internal.good.processing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardRangeEntity;
import ru.crystals.cards.CardSearchResult;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.ClientEntity;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.pos.cards.CardData;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.customers.SetCustomersCashService;
import ru.crystals.pos.persistence.cards.CardsDao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by v.osipov on 19.07.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class SetDiscountCardAsGoodHandlerTest {

    private static final long TYPE_GUID = 7L;
    private static final long PERCENTAGE_DISCOUNT = 13L;
    private static final String FAKE_CARD_NUMBER = "1";
    private static final long FAKE_GUID = 2L;
    private static final String FAKE_CLIENT = "6";

    @Mock
    private CardsService service;

    @Mock
    private SetCustomersCashService customersService;

    @Mock
    private CardsDao cardsDao;

    @Spy
    @InjectMocks
    private SetBackedDiscountCardAsGoodHandler handler = new SetBackedDiscountCardAsGoodHandler();

    /**
     * Проверка генерации фейкового типа карты Set10
     */
    @Test
    public void testGetCardType() {
        DiscountCardAsGoodVO cardVO = new DiscountCardAsGoodVO();
        cardVO.setTypeGuid(TYPE_GUID);
        cardVO.setPercentageDiscount(PERCENTAGE_DISCOUNT);

        CardTypeEntity cardType = handler.getCardType(cardVO);

        assertThat(cardType.getCardTypeEnumValue()).isEqualTo(CardTypes.InternalCard);
        assertThat(cardType.getGuid()).isEqualTo(TYPE_GUID);
        assertThat(((InternalCardsEntity) cardType).getPercentageDiscount()).isEqualTo(PERCENTAGE_DISCOUNT);
    }

    @Test
    public void testGetCardTypeOfNull() {
        CardTypeEntity cardType = handler.getCardType(null);
        assertThat(cardType).isNull();
    }

    @Test
    public void testGetCardInfo() throws Exception {
        CardTypeEntity cardType = new InternalCardsEntity();
        cardType.setGuid(FAKE_GUID);
        CardData cardData = new CardData(FAKE_CARD_NUMBER);
        when(service.getCardType(cardData)).thenReturn(new CardSearchResult(cardType));

        DiscountCardAsGoodVO cardInfo = handler.getCardInfo(FAKE_CARD_NUMBER);

        assertThat(cardInfo).isNotNull();
        assertThat(cardInfo.getTypeGuid()).isEqualTo(cardType.getGuid());
    }

    @Test
    public void canGetCardInfo() {
        assertFalse(handler.canGetInfo(null));
        assertFalse(handler.canGetInfo(""));
        assertFalse(handler.canGetInfo(anyString()));

        CardRangeEntity cardRange = new CardRangeEntity();
        when(cardsDao.getCardRange(eq(FAKE_CARD_NUMBER))).thenReturn(cardRange);
        assertFalse(handler.canGetInfo(FAKE_CARD_NUMBER));

        cardRange.setCardType(new InternalCardsEntity());
        assertTrue(handler.canGetInfo(FAKE_CARD_NUMBER));
    }

    @Test
    public void validateCard() throws Exception {
        handler.setCheckCardIsNotInUse(true);

        CardTypeEntity cardType = new InternalCardsEntity();
        cardType.setGuid(FAKE_GUID);
        CardEntity card = new CardEntity();
        card.setId(666L);
        card.setGuid(card.getId());
        cardType.getCards().add(card);

        CardData cardData = new CardData(FAKE_CARD_NUMBER);
        when(service.getCardType(cardData)).thenReturn(new CardSearchResult(cardType));

        DiscountCardAsGoodVO cardInfo = handler.getCardInfo(FAKE_CARD_NUMBER);

        assertThat(handler.validateCard(cardInfo)).isEmpty();

        card.setStatus(CardStatus.Active);
        assertThat(handler.validateCard(cardInfo)).isNotEmpty();

        handler.setCheckCardIsNotInUse(false);
        assertThat(handler.validateCard(cardInfo)).isEmpty();
    }

    @Test
    public void validateCardIfNotExists() throws Exception {
        handler.setCheckCardIsNotInUse(true);

        CardTypeEntity cardType = new InternalCardsEntity();
        cardType.setGuid(FAKE_GUID);
        // На сервере все-равно создается фэйковая карта (не в БД)
        CardEntity card = new CardEntity();
        card.setStatus(CardStatus.Active);
        cardType.getCards().add(card);

        CardData cardData = new CardData(FAKE_CARD_NUMBER);
        when(service.getCardType(cardData)).thenReturn(new CardSearchResult(cardType));

        DiscountCardAsGoodVO cardInfo = handler.getCardInfo(FAKE_CARD_NUMBER);

        assertThat(handler.validateCard(cardInfo)).isEmpty();
    }

    @Test
    public void validateHolderId() throws Exception {
        CardTypeEntity cardType = new InternalCardsEntity();
        cardType.setGuid(FAKE_GUID);

        CardData cardData = new CardData(FAKE_CARD_NUMBER);
        when(service.getCardType(cardData)).thenReturn(new CardSearchResult(cardType));

        DiscountCardAsGoodVO cardInfo = handler.getCardInfo(FAKE_CARD_NUMBER);

        assertThat(handler.validateHolderId(null, cardInfo)).isNotEmpty();
        assertThat(handler.validateHolderId("holderId", cardInfo)).isNotEmpty();
        assertThat(handler.validateHolderId(FAKE_CLIENT, cardInfo)).isNotEmpty();

        CardEntity card = new CardEntity();
        cardType.getCards().add(card);
        assertThat(handler.validateHolderId(FAKE_CLIENT, cardInfo)).isNotEmpty();

        ClientEntity client = new ClientEntity();
        card.setClient(client);
        assertThat(handler.validateHolderId(FAKE_CLIENT, cardInfo)).isNotEmpty();

        client.setGuid(Long.valueOf(FAKE_CLIENT));
        assertThat(handler.validateHolderId(FAKE_CLIENT, cardInfo)).isEmpty();
    }

}
