package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import ru.crystals.cards.internal.good.processing.DCSellSteps;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandler;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandlersRegistry;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodVO;
import ru.crystals.pos.cards.exception.CardsException;
import ru.crystals.pos.catalog.PriceEntity;
import ru.crystals.pos.catalog.ProductConfig;
import ru.crystals.pos.catalog.ProductDiscountCardEntity;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.check.PurchaseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;

/**
 * Валидация карты по номеру.
 */
public class DiscountCardControllerTest {

    private static final String CARD_NUMBER = "666";

    private final DiscountCardController controller = new DiscountCardController();
    private final DiscountCardAsGoodHandlersRegistry registry = mock(DiscountCardAsGoodHandlersRegistry.class);
    private final DiscountCardAsGoodHandler handler = mock(DiscountCardAsGoodHandler.class);
    private final CheckService checkService = mock(CheckService.class);
    private final DiscountCardAdapter adapter = mock(DiscountCardAdapter.class);
    private final DiscountCardModel model = mock(DiscountCardModel.class);
    private final DiscountCardAsGoodVO discountCardAsGoodVO = new DiscountCardAsGoodVO();
    private final PositionDiscountCardEntity cardPosition = new PositionDiscountCardEntity();

    @Before
    public void setupModelAndMocks() {
        ProductDiscountCardEntity product = new ProductDiscountCardEntity();
        product.setProductConfig(new ProductConfig());
        product.setPrice(new PriceEntity());
        product.getPrice().setPriceBigDecimal(BigDecimal.ZERO);
        controller.setModel(model);

        when(registry.getHandler(eq(CARD_NUMBER))).thenReturn(handler);
        when(handler.getCardInfo(eq(CARD_NUMBER), eq(false))).thenReturn(discountCardAsGoodVO);
        when(handler.getProcess(eq(discountCardAsGoodVO))).thenReturn(Collections.singletonList(DCSellSteps.ENTER_CARD_NUMBER));
        when(model.isNotRefund()).thenReturn(true);
        when(model.getProduct()).thenReturn(product);
        when(model.getCard()).thenReturn(new DiscountCardAsGoodVO());

        setInternalState(controller, "registry", registry);
        setInternalState(controller, "adapter", adapter);
        setInternalState(controller, "model", model);

        PurchaseEntity purchase = new PurchaseEntity();
        purchase.getPositions().add(cardPosition);
        when(checkService.getCheck(anyInt())).thenReturn(purchase);

        controller.setCheckService(checkService);
    }

    @Test
    public void setCardNumberAddsIsCardApplicable() throws Exception {
        Boolean isCardApplicable = model.getProduct().getProductConfig().getIsCardApplicable();
        controller.setCardNumber(CARD_NUMBER);
        ArgumentCaptor<DiscountCardAsGoodVO> captor = ArgumentCaptor.forClass(DiscountCardAsGoodVO.class);
        verify(model, times(2)).setCard(captor.capture());
        Assert.assertEquals(isCardApplicable, captor.getAllValues().get(0).isApplicableOnCheck());
    }

    @Test
    public void addPositionStraightAfterCardNumberSet() throws Exception {
        controller.setCardNumber(CARD_NUMBER);
        verify(adapter).doPositionAdd(any());
    }

    @Test(expected = CardsException.class)
    public void throwExceptionIfCardWithSameNumberAlreadyAdded() throws Exception {
        cardPosition.setCardNumber(CARD_NUMBER);
        controller.setCardNumber(CARD_NUMBER);
        verify(adapter, never()).doPositionAdd(any());
    }

    @Test(expected = CardsException.class)
    public void throwExceptionIfNoHandle() throws Exception {
        when(registry.getHandler(eq(CARD_NUMBER))).thenReturn(null);

        controller.setCardNumber(CARD_NUMBER);
        verify(adapter, never()).doPositionAdd(any());
    }

    @Test(expected = CardsException.class)
    public void throwExceptionIfNoCardInfo() throws Exception {
        when(handler.getCardInfo(eq(CARD_NUMBER), eq(false))).thenReturn(null);

        controller.setCardNumber(CARD_NUMBER);
        verify(adapter, never()).doPositionAdd(any());
    }

    @Test(expected = CardsException.class)
    public void throwExceptionIfCardValidationFailure() throws Exception {
        when(handler.validateCard(eq(discountCardAsGoodVO))).thenReturn(Collections.singletonList(null));

        controller.setCardNumber(CARD_NUMBER);
        verify(adapter, never()).doPositionAdd(any());
    }

    @Test
    public void noValidationDuringRefund() throws Exception {
        when(model.isNotRefund()).thenReturn(false);
        when(handler.validateCard(eq(discountCardAsGoodVO))).thenReturn(Collections.singletonList(null));

        controller.setCardNumber(CARD_NUMBER);
        verify(adapter).doPositionAdd(any());
    }

    @Test
    public void onlyOneStepDuringRefund() throws Exception {
        model.setRefund(true);
        when(handler.getProcess(eq(discountCardAsGoodVO))).thenReturn(Arrays.asList(DCSellSteps.ENTER_CARD_NUMBER, DCSellSteps.ENTER_CLIENT_ID));

        controller.setCardNumber(CARD_NUMBER);
        verify(adapter).doPositionAdd(any());
    }

    @Test
    public void deactivateCards() {
        PurchaseEntity purchase = new PurchaseEntity();
        purchase.setOperationType(PurchaseEntity.OPERATION_TYPE_SALE);
        PositionDiscountCardEntity position = new PositionDiscountCardEntity();
        position.setCardNumber("123456");
        purchase.getPositions().add(position);
        position = new PositionDiscountCardEntity();
        position.setCardNumber("654321");
        purchase.getPositions().add(position);

        when(registry.getHandler(anyString())).thenReturn(handler);

        controller.deactivateCards(purchase);

        verify(handler, times(2)).deactivateCard(anyString());
    }

}