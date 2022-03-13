package ru.crystals.cards.customers.good.processing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.cards.CardEntity;
import ru.crystals.cards.CardRangeEntity;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.ClientEntity;
import ru.crystals.cards.common.CardStatus;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodVO;
import ru.crystals.cards.internalcards.InternalCardsEntity;
import ru.crystals.pos.customers.SetCustomersCashException;
import ru.crystals.pos.customers.SetCustomersCashService;
import ru.crystals.pos.persistence.cards.CardsDao;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CustomersDiscountCardAsGoodHandlerTest {

    private static final long TYPE_GUID = 7L;
    private static final String CARD_NUMBER = "1";
    private static final long GUID = 2L;

    @Mock
    private SetCustomersCashService customersService;

    @Mock
    private CardsDao cardsDao;

    @Spy
    @InjectMocks
    private CustomersDiscountCardAsGoodHandler handler = new CustomersDiscountCardAsGoodHandler();


    @Test
    public void getCardType() {
        InternalCardAsGoodVO cardVO = new InternalCardAsGoodVO();
        InternalCardsEntity internalCardsEntity = new InternalCardsEntity();
        cardVO.setCardsType(internalCardsEntity);
        internalCardsEntity.setGuid(TYPE_GUID);
        internalCardsEntity.setCardTypeEnumValue(CardTypes.InternalCard);
        internalCardsEntity.setPercentageDiscount(0L);

        CardTypeEntity cardType = handler.getCardType(cardVO);

        assertThat(cardType.getCardTypeEnumValue()).isEqualTo(CardTypes.InternalCard);
        assertThat(cardType.getGuid()).isEqualTo(TYPE_GUID);
        assertThat(((InternalCardsEntity) cardType).getPercentageDiscount()).isEqualTo(0);
    }

    @Test
    public void getNullCardType() {
        CardTypeEntity cardType = handler.getCardType(null);
        assertThat(cardType).isNull();
    }

    @Test
    public void canGetCardInfo() {
        doReturn(true).when(customersService).isEnabled();

        assertFalse(handler.canGetInfo(null));
        assertFalse(handler.canGetInfo(""));
        assertFalse(handler.canGetInfo(anyString()));

        CardRangeEntity cardRange = new CardRangeEntity();
        when(cardsDao.getCardRange(eq(CARD_NUMBER))).thenReturn(cardRange);
        assertFalse(handler.canGetInfo(CARD_NUMBER));

        cardRange.setCardType(new InternalCardsEntity());
        assertTrue(handler.canGetInfo(CARD_NUMBER));
    }

    @Test
    public void validateCard() {
        assertThat(handler.validateCard(new DiscountCardAsGoodVO())).isEmpty();
        assertThat(handler.validateCard(new InternalCardAsGoodVO())).isEmpty();

        InternalCardAsGoodVO cardVO = new InternalCardAsGoodVO();
        cardVO.setErrorReason(SetCustomersCashException.Reason.OFFLINE);
        assertThat(handler.validateCard(cardVO)).isNotEmpty();
        assertThat(handler.validateCard(cardVO).get(0)).isNotBlank();
    }

    @Test
    public void getCardInfo() throws Exception {
        ClientEntity client = new ClientEntity();
        CardEntity card = new CardEntity();
        client.getCards().add(card);
        card.setNumber(CARD_NUMBER);
        InternalCardsEntity cardType = new InternalCardsEntity();
        cardType.setGuid(GUID);
        card.setCardType(cardType);
        doReturn(client).when(customersService).purchaseCard(CARD_NUMBER);

        DiscountCardAsGoodVO info = handler.getCardInfo(CARD_NUMBER, false);
        assertThat(info).isInstanceOf(InternalCardAsGoodVO.class);
        InternalCardAsGoodVO result = (InternalCardAsGoodVO) info;
        assertThat(result.getErrorReason()).isNull();
        assertThat(result.getCardsType()).isSameAs(cardType);
        assertThat(result.getNumber()).isEqualTo(CARD_NUMBER);
        assertThat(result.isPersonalized()).isTrue();
        assertThat(result.isApplicableOnCheck()).isTrue();
    }

    @Test
    public void getCardInfoWithError() throws Exception {
        doThrow(new RuntimeException()).when(customersService).purchaseCard(CARD_NUMBER);

        DiscountCardAsGoodVO info = handler.getCardInfo(CARD_NUMBER, false);
        assertThat(info).isNull();
    }

    @Test
    public void getCardInfoDuringRefund() throws Exception {
        ClientEntity client = new ClientEntity();
        CardEntity card = new CardEntity();
        client.getCards().add(card);
        card.setNumber(CARD_NUMBER);
        card.setStatus(CardStatus.Active);
        InternalCardsEntity cardType = new InternalCardsEntity();
        cardType.setGuid(GUID);
        card.setCardType(cardType);
        doReturn(Optional.of(client)).when(customersService).findClientByCardNumber(CARD_NUMBER);

        DiscountCardAsGoodVO info = handler.getCardInfo(CARD_NUMBER, true);
        assertThat(info).isInstanceOf(InternalCardAsGoodVO.class);
        InternalCardAsGoodVO result = (InternalCardAsGoodVO) info;
        assertThat(result.getErrorReason()).isNull();
        assertThat(result.getCardsType()).isSameAs(cardType);
        assertThat(result.getNumber()).isEqualTo(CARD_NUMBER);
        assertThat(result.isPersonalized()).isTrue();
        assertThat(result.isApplicableOnCheck()).isTrue();
    }

}