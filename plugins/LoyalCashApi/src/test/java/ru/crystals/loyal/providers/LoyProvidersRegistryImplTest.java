package ru.crystals.loyal.providers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.discount.processing.entity.LoyTransactionEntity;
import ru.crystals.loyal.actions.provider.LoyActionsProvider;
import ru.crystals.pos.check.PurchaseEntity;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoyProvidersRegistryImplTest {
    @Mock
    private LoyActionsProvider actionsProvider;

    @InjectMocks
    private LoyProvidersRegistryImpl registry = new LoyProvidersRegistryImpl();

    @Test
    public void purchaseFiscalizedTest() {
        PurchaseEntity purchase = Mockito.mock(PurchaseEntity.class);
        registry.purchaseFiscalized(purchase, Mockito.mock(LoyTransactionEntity.class));
        verify(actionsProvider).onPurchaseProcessingFinished();
    }

    @Test
    public void checkCanceledTest() {
        PurchaseEntity purchase = Mockito.mock(PurchaseEntity.class);
        registry.checkCanceled(purchase);
        verify(actionsProvider).onPurchaseProcessingFinished();
    }

    @Test
    public void cancelDiscountTest() {
        PurchaseEntity purchase = Mockito.mock(PurchaseEntity.class);
        registry.cancelDiscount(purchase);
        verify(actionsProvider).onPurchaseProcessingFinished();
    }
}
