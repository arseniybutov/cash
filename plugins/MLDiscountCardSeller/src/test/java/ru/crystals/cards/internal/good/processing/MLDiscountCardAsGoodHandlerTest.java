package ru.crystals.cards.internal.good.processing;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.cards.CardTypeEntity;
import ru.crystals.cards.common.CardTypes;
import ru.crystals.pos.cards.CardsService;
import ru.crystals.pos.ml.MLService;

/**
 * Created by v.osipov on 19.07.2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MLDiscountCardAsGoodHandlerTest {

    private static final String CARD_NUMBER = RandomStringUtils.randomAlphanumeric(10);
    private static final long GUID_FROM_LOCAL = RandomUtils.nextInt(20);
    private static final boolean HOLDER_MANDATORY = RandomUtils.nextBoolean();


    @Mock
    private CardsService cardsService;

    @Mock
    private MLService service;

    @Spy
    @InjectMocks
    private MLBackedDiscountCardAsGoodHandler handler = new MLBackedDiscountCardAsGoodHandler();

    /**
     * Проверка генерации фейкового типа карты ML
     */
    @Test
    public void testGetCardType() {
        DiscountCardAsGoodVO cardVO = new DiscountCardAsGoodVO();
        cardVO.setTypeGuid(GUID_FROM_LOCAL);
        CardTypeEntity cardType = handler.getCardType(cardVO);
        assertThat(cardType.getCardTypeEnumValue()).isEqualTo(CardTypes.ExternalCard);
        assertThat(cardType.getProcessingName()).isEqualTo(MLService.PROVIDER_NAME);
        assertThat(cardType.getGuid()).isEqualTo(cardVO.getTypeGuid());
    }

    @Test
    public void testGetCardTypeOfNull() {
        CardTypeEntity cardType = handler.getCardType(null);
        assertThat(cardType).isNull();
    }

    @Test
    public void testGetCardInfo() {
        Mockito.when(service.isMlCard(Mockito.eq(CARD_NUMBER))).thenReturn(true);
        Mockito.when(service.isHolderMandatory(Mockito.eq(CARD_NUMBER))).thenReturn(HOLDER_MANDATORY);
        Mockito.when(cardsService.getGuidFromLocal(Mockito.eq(CARD_NUMBER))).thenReturn(GUID_FROM_LOCAL);

        DiscountCardAsGoodVO cardVO = handler.getCardInfo(CARD_NUMBER);

        Mockito.verify(service).isMlCard(Mockito.eq(CARD_NUMBER));
        Mockito.verify(service).isHolderMandatory(Mockito.eq(CARD_NUMBER));
        Mockito.verify(cardsService).getGuidFromLocal(Mockito.eq(CARD_NUMBER));

        assertThat(cardVO.isPersonalized()).isTrue();
        assertThat(cardVO.getNumber()).isEqualTo(CARD_NUMBER);
        assertThat(cardVO.isHolderMandatory()).isEqualTo(HOLDER_MANDATORY);
        assertThat(cardVO.getTypeGuid()).isEqualTo(GUID_FROM_LOCAL);
    }

    @Test
    public void testGetCardInfoNotMlCard() {
        Mockito.when(service.isMlCard(Mockito.eq(CARD_NUMBER))).thenReturn(false);

        DiscountCardAsGoodVO cardVO = handler.getCardInfo(CARD_NUMBER);

        Mockito.verify(service).isMlCard(Mockito.eq(CARD_NUMBER));
        assertThat(cardVO).isNull();
    }

}
