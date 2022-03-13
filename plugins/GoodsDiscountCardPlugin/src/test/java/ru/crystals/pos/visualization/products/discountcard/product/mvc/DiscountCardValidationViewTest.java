package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandler;
import ru.crystals.cards.internal.good.processing.DiscountCardAsGoodHandlersRegistry;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.visualization.Factory;

/**
 * Логика валидации позиции ДК при возврате по номеру чека.
 */
public class DiscountCardValidationViewTest {

    private static final String CARD_NUMBER = "666";

    private class RedundantCardNumberRequest extends AssertionError {}
    private class MessageShown extends AssertionError {}

    private static final DiscountCardAsGoodHandlersRegistry registry = mock(DiscountCardAsGoodHandlersRegistry.class);


    @BeforeClass
    public static void setupDiscountCardHandlerRegistry() {
        Factory.getInstance().setCardAsGoodHandlersRegistry(registry);
    }

    @Test
    public void skipCardNumberInput() {
        DiscountCardValidationView view = newDiscountCardValidationView("", true);

        List<PositionEntity> validatedList = view.validatePositions(generatePositions(1), Collections.emptyList());

        assertThat(validatedList).isEmpty();
    }

    @Test(expected = RedundantCardNumberRequest.class)
    public void tryAgainCardNumberInput() {
        DiscountCardValidationView view = newDiscountCardValidationView("", false);
        view.validatePositions(generatePositions(1), Collections.emptyList());
    }

    @Test
    public void successValidation() {
        when(registry.getHandler(eq(CARD_NUMBER))).thenReturn(mock(DiscountCardAsGoodHandler.class));

        DiscountCardValidationView view = newDiscountCardValidationView(CARD_NUMBER, false);

        List<PositionEntity> validatedList = view.validatePositions(generatePositions(1), Collections.emptyList());

        assertThat(validatedList).hasSize(1);

        assertEquals(CARD_NUMBER, ((PositionDiscountCardEntity) validatedList.get(0)).getCardNumber());
    }

    @Test(expected = MessageShown.class)
    public void cardHandlerNotFound() {
        when(registry.getHandler(eq(CARD_NUMBER))).thenReturn(null);

        DiscountCardValidationView view = newDiscountCardValidationView(CARD_NUMBER, false);
        view.validatePositions(generatePositions(1), Collections.emptyList());
    }

    @Test(expected = MessageShown.class)
    public void duplicatedCardNumber() {
        when(registry.getHandler(eq(CARD_NUMBER))).thenReturn(mock(DiscountCardAsGoodHandler.class));

        LinkedList<String> cardNumbersToEnter = new LinkedList<>();
        cardNumbersToEnter.add(CARD_NUMBER);
        cardNumbersToEnter.add(CARD_NUMBER);

        DiscountCardValidationView view = newDiscountCardValidationView(cardNumbersToEnter, false);
        view.validatePositions(generatePositions(2), Collections.emptyList());
    }

    @Test(expected = MessageShown.class)
    public void duplicatedCardNumberFromPreviouslyValidatedPosition() {
        when(registry.getHandler(eq(CARD_NUMBER))).thenReturn(mock(DiscountCardAsGoodHandler.class));

        DiscountCardValidationView view = newDiscountCardValidationView(CARD_NUMBER, false);

        PositionDiscountCardEntity previouslyValidatedPosition = new PositionDiscountCardEntity();
        previouslyValidatedPosition.setCardNumber(CARD_NUMBER);

        view.validatePositions(generatePositions(1), Collections.singletonList(previouslyValidatedPosition));
    }

    @Test
    public void alreadyValidatedPosition() {
        when(registry.getHandler(eq(CARD_NUMBER))).thenReturn(mock(DiscountCardAsGoodHandler.class));

        DiscountCardValidationView view = newDiscountCardValidationView(CARD_NUMBER, false);

        PositionDiscountCardEntity alreadyValidatedPosition = new PositionDiscountCardEntity();

        List<PositionEntity> validatedList = view.validatePositions(
                Collections.singletonList(alreadyValidatedPosition),
                Collections.singletonList(alreadyValidatedPosition));

        assertThat(validatedList).isEmpty();
    }


    private DiscountCardValidationView newDiscountCardValidationView(String enteredCardNumber, boolean questionAnswer) {
        LinkedList<String> cardNumbers = new LinkedList<>();
        cardNumbers.add(enteredCardNumber);
        return newDiscountCardValidationView(cardNumbers, questionAnswer);
    }

    private DiscountCardValidationView newDiscountCardValidationView(Deque<String> expectedCardNumbers, boolean questionAnswer) {
        return new DiscountCardValidationView() {
            @Override
            protected String requestCardNumber(PositionEntity position) {
                if (!expectedCardNumbers.isEmpty()) {
                    return expectedCardNumbers.pop();
                } else {
                    throw new RedundantCardNumberRequest();
                }
            }

            @Override
            protected boolean showQuestion(PositionEntity position, String question) {
                return questionAnswer;
            }

            @Override
            protected void showMessage(PositionEntity position, String message) {
                throw new MessageShown();
            }
        };
    }

    private List<PositionEntity> generatePositions(int count) {
        return IntStream.range(0, count).mapToObj(i -> new PositionDiscountCardEntity()).collect(Collectors.toList());
    }

}