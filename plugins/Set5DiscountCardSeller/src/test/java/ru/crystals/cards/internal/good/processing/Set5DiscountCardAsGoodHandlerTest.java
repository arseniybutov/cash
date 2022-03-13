package ru.crystals.cards.internal.good.processing;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.bundles.BundleManager;
import ru.crystals.cards.CardEntity;
import ru.crystals.pos.cards.external.ExternalLoyaltyService;
import ru.crystals.pos.techprocess.TechProcessInterface;

/**
 * Тест плагина Set5 для ДК
 */
@RunWith(MockitoJUnitRunner.class)
public class Set5DiscountCardAsGoodHandlerTest {

    private static final String CARD_NUMBER = "987654321";
    private static final String HOLDER_ID = "123456";
    private static final Integer CHECK_NUMBER = 1;

    @Mock
    private ExternalLoyaltyService service;

    @Spy
    @InjectMocks
    private Set5BackedDiscountCardAsGoodHandler handler = new Set5BackedDiscountCardAsGoodHandler();

    /**
     * Проверка вызова привязки карты
     */
    @Test
    public void testActivateCard() throws Exception {
        TechProcessInterface mock = Mockito.mock(TechProcessInterface.class);
        BundleManager.add(TechProcessInterface.class, mock);
        Mockito.when(mock.getCurrentCheckNum()).thenReturn(CHECK_NUMBER);

        DiscountCardAsGoodVO cardVO = new DiscountCardAsGoodVO();
        cardVO.setNumber(CARD_NUMBER);

        Mockito.doReturn(new CardEntity()).when(service).linkCardToClient(Mockito.anyString(), Mockito.anyString());

        List<String> results = handler.activateCard(HOLDER_ID, cardVO);

        Assert.assertTrue(results.isEmpty());
        ArgumentCaptor<String> holderCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> cardCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(service).linkCardToClient(cardCaptor.capture(), holderCaptor.capture());

        Assert.assertEquals(HOLDER_ID, holderCaptor.getValue());
        Assert.assertEquals(CARD_NUMBER, cardCaptor.getValue());
    }
}
