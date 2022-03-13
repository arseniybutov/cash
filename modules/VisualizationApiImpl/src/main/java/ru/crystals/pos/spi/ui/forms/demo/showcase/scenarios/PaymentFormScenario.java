package ru.crystals.pos.spi.ui.forms.demo.showcase.scenarios;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import ru.crystals.pos.api.ui.listener.SumToPayFormListener;
import ru.crystals.pos.spi.receipt.Card;
import ru.crystals.pos.spi.receipt.ExciseBottle;
import ru.crystals.pos.spi.receipt.LineItem;
import ru.crystals.pos.spi.receipt.Receipt;
import ru.crystals.pos.spi.receipt.ReceiptType;
import ru.crystals.pos.spi.ui.UIForms;
import ru.crystals.pos.spi.ui.payment.SumToPayFormParameters;

public class PaymentFormScenario extends ShowcaseScenario {
    private static final BigDecimal PAYMENT_AMOUNT = BigDecimal.valueOf(1000);

    public PaymentFormScenario(UIForms forms) {
        super(forms);
    }

    @Override
    public void run() {
        formManager.getPaymentForms().showSumToPayForm(
                new SumToPayFormParameters("Оплата наличными", "СУММА", new Receipt() {
                    @Override
                    public BigDecimal getSumWithDiscount() {
                        return BigDecimal.valueOf(1232345, 2);
                    }

                    @Override
                    public BigDecimal getSurchargeSum() {
                        return BigDecimal.valueOf(4655, 2);
                    }

                    @Override
                    public List<LineItem> getLineItems() {
                        return new LinkedList<>();
                    }

                    @Override
                    public int getShiftNo() {
                        return 0;
                    }

                    @Override
                    public int getNumber() {
                        return 0;
                    }

                    @Override
                    public Collection<Card> getCards() {
                        return new LinkedList<>();
                    }

                    @Override
                    public Collection<ExciseBottle> getExciseBottles() {
                        return new LinkedList<>();
                    }

                    @Override
                    public ReceiptType getType() {
                        return null;
                    }

                    @Override
                    public Receipt getSaleReceipt() {
                        return null;
                    }

                    @Override
                    public Date getDateCreate() {
                        return null;
                    }
                }, PAYMENT_AMOUNT),
                new SumToPayFormListener() {
                    @Override
                    public void eventSumEntered(BigDecimal sumToPay) {
                        // Intentionally left blank.
                    }

                    @Override
                    public void eventCanceled() {
                        // Intentionally left blank.
                    }
                });
    }
}
