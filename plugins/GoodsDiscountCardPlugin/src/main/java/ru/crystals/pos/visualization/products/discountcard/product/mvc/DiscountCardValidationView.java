package ru.crystals.pos.visualization.products.discountcard.product.mvc;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.catalog.ProductDiscountCardEntity;
import ru.crystals.pos.check.PositionDiscountCardEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.events.EventProxyFactory;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.XListenerAdapter;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonMessageForm;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonYesNoForm;
import ru.crystals.pos.visualization.products.discountcard.ResBundleGoodsDiscountCard;

import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Используется при возврате ДК по чеку.
 */
public class DiscountCardValidationView implements XListener {

    private static final Logger log = LoggerFactory.getLogger(DiscountCardValidationView.class);

    private AtomicBoolean lock = new AtomicBoolean();

    private DiscountCardEnterNumberForm discountCardEnterNumberForm = new DiscountCardEnterNumberForm(this);
    private CommonMessageForm messageForm = new CommonMessageForm(this);
    private CommonYesNoForm yesNoForm = new CommonYesNoForm(this);

    public DiscountCardValidationView() {
        new XListenerAdapter(discountCardEnterNumberForm, 0) {
            @Override
            protected void show(HierarchyEvent e) {
                EventProxyFactory.addEventListener(DiscountCardValidationView.this);
                super.show(e);
            }

            @Override
            protected void hide(HierarchyEvent e) {
                EventProxyFactory.removeEventListener(DiscountCardValidationView.this);
                super.hide(e);
            }
        };
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        return eventProcessed();
    }

    @Override
    public boolean barcodeScanned(String barcode) {
        return eventProcessed();
    }

    @Override
    public boolean eventMSR(String track1, String track2, String track3, String track4) {
        return eventProcessed();
    }

    /**
     * Показывает диалоговые формы для вализации ДК при возврате по чеку.
     *
     * @param toValidatePositions позиции для валидации
     * @param validatedPositions  ранее провалидированные позиции
     * @return провалидированные в данном методе позиции
     */
    public List<PositionEntity> validatePositions(List<PositionEntity> toValidatePositions, List<PositionEntity> validatedPositions) {
        // Провалидированные позиции на текущей итерации
        List<PositionEntity> justNowValidatedPositions = new ArrayList<>();
        // Все провалидированные позиции на текущий момент
        Supplier<Stream<PositionEntity>> allValidatedPositions =
                () -> Stream.concat(validatedPositions.stream(), justNowValidatedPositions.stream());
        try {
            for (PositionEntity position : toValidatePositions) {
                handlePosition(position, allValidatedPositions, justNowValidatedPositions::add);
            }
        } catch (Exception e) {
            log.error("Validate discount card position error:", e);
        } finally {
            lock.set(false);
        }
        return justNowValidatedPositions;
    }

    private void handlePosition(
            PositionEntity position, Supplier<Stream<PositionEntity>> allValidatedPositions, Consumer<PositionEntity> consumer) {

        boolean cardNumberNotYetHandled = allValidatedPositions.get().noneMatch(p -> p == position);
        while (cardNumberNotYetHandled) {
            String cardNumber = requestCardNumber(position);
            if (StringUtils.isNotEmpty(cardNumber)) {
                Optional<String> violation = validateCardNumber(cardNumber, allValidatedPositions.get());
                if (!violation.isPresent()) {
                    ((PositionDiscountCardEntity) position).setCardNumber(cardNumber);
                    consumer.accept(position);
                    cardNumberNotYetHandled = false;
                } else {
                    showMessage(position, violation.get());
                }
            } else {
                // Был нажат ESC, предлагаем пропустить позицию,
                boolean answer = showQuestion(position, ResBundleGoodsDiscountCard.getString("REMOVE_POSITION_FROM_REFUND_RECEIPT"));
                cardNumberNotYetHandled = !answer;
            }
        }
    }

    private Optional<String> validateCardNumber(String cardNumber, Stream<PositionEntity> validatedPositions) {
        boolean cardWithThisNumberAlreadyAdded = validatedPositions
                .filter(PositionDiscountCardEntity.class::isInstance)
                .map(PositionDiscountCardEntity.class::cast)
                .map(PositionDiscountCardEntity::getCardNumber)
                .anyMatch(cardNumber::equals);

        if (cardWithThisNumberAlreadyAdded) {
            return Optional.of(ResBundleGoodsDiscountCard.getString("CARD_ALREADY_ADDED"));
        }

        boolean canNotHandleThisCard = !Optional.ofNullable(Factory.getInstance().getCardAsGoodHandlersRegistry())
                .map(registry -> registry.getHandler(cardNumber))
                .isPresent();

        if (canNotHandleThisCard) {
            return Optional.of(ResBundleGoodsDiscountCard.getString("NO_ONE_CAN_HANDLE_THIS_CARD_AS_GOOD"));
        }

        return Optional.empty();
    }

    /**
     * Показывает форму запроса номера ДК для валидации.
     */
    protected String requestCardNumber(PositionEntity position) {
        discountCardEnterNumberForm.clear();
        discountCardEnterNumberForm.changeInputFieldHeader(true);
        discountCardEnterNumberForm.showForm((ProductDiscountCardEntity) position.getProduct(), (PositionDiscountCardEntity) position);
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(discountCardEnterNumberForm, lock);
        return discountCardEnterNumberForm.getEnteredNumber();
    }

    /**
     * Показать экран сообщения.
     */
    protected void showMessage(PositionEntity position, String message) {
        messageForm.clear();
        messageForm.setMessage(message);
        messageForm.showForm(position.getProduct(), position);
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(messageForm, lock);
    }

    /**
     * Показать экран вопроса.
     *
     * @return <tt>true</tt> если кассир ответил утвердительно
     */
    protected boolean showQuestion(PositionEntity position, String question) {
        yesNoForm.clear();
        yesNoForm.setMessageLabelText(question);
        yesNoForm.showForm(position.getProduct(), position);
        Factory.getInstance().getMainWindow().getCheckContainer().showLockComponent(yesNoForm, lock);
        return !yesNoForm.isYes();
    }

    /**
     * Перехватить событие и вернуться из текущей формы.
     */
    private boolean eventProcessed() {
        lock.set(false);
        return true;
    }

}
