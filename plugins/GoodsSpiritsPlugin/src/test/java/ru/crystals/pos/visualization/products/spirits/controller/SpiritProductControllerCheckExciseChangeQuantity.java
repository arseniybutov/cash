package ru.crystals.pos.visualization.products.spirits.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import ru.crystals.pos.egais.EGAISBridgeImpl;
import ru.crystals.pos.egais.excise.validation.ExciseValidationImpl;
import ru.crystals.pos.techprocess.TechProcessInterface;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.test.MockInjectors;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.regex.Pattern;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Factory.class)
public class SpiritProductControllerCheckExciseChangeQuantity {
    private TechProcessInterface techProcessInterface;

    private SpiritProductController spiritProductController;

    private EGAISBridgeImpl egaisBridge;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        techProcessInterface = mock(TechProcessInterface.class);
        mockStatic(Factory.class);
        spiritProductController = new SpiritProductController();
        injectEGAISBridgeImpl();
    }


    private void injectEGAISBridgeImpl() throws NoSuchFieldException, IllegalAccessException {
        Field egaisBridgeField = SpiritProductController.class.getDeclaredField("egaisBridge");
        egaisBridgeField.setAccessible(true);

        egaisBridge = new EGAISBridgeImpl();

        MockInjectors.injectField(egaisBridge, Pattern.compile("^(?=.*[a-zA-Z]+.*)([a-zA-Z0-9]{68})$"), "exciseBarcodePatternNew");

        MockInjectors.injectField(egaisBridge, Pattern.compile("\\d\\d[a-zA-Z0-9]{21}\\d[0-1]\\d[0-3]\\d{10}[a-zA-Z0-9]{31}|[a-zA-Z0-9]{40}"), "exciseBarcodePatternOld");

        egaisBridgeField.set(spiritProductController, egaisBridge);

        Field egaisExciseCheckValidationField = SpiritProductController.class.getDeclaredField("egaisExciseCheckValidation");
        egaisExciseCheckValidationField.setAccessible(true);
        ExciseValidationImpl egaisExciseValidation = new ExciseValidationImpl();
        egaisExciseCheckValidationField.set(spiritProductController, egaisExciseValidation);

        Field techProcessField = ExciseValidationImpl.class.getDeclaredField("techProcess");
        techProcessField.setAccessible(true);
        techProcessField.set(egaisExciseValidation, techProcessInterface);
    }

    @Test
    public void compareExcisePositionQuantityWrongStateTest() throws Exception {
        BigDecimal currentQuantityValue = new BigDecimal(3);
        BigDecimal defaultQuantityValue = new BigDecimal(2);
        when(Factory.getTechProcessImpl()).thenReturn(techProcessInterface);
        boolean stateConditionEquality = false;
        when(Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE)).thenReturn(false);
        Assert.assertEquals(true, spiritProductController.
                compareExcisePositionQuantity(currentQuantityValue, defaultQuantityValue, stateConditionEquality));
    }

    @Test
    public void compareExcisePositionQuantityTrueStateTest() throws Exception {
        BigDecimal currentQuantityValue = new BigDecimal(3);
        BigDecimal defaultQuantityValue = new BigDecimal(2);
        when(Factory.getTechProcessImpl()).thenReturn(techProcessInterface);
        boolean stateConditionEquality = true;
        when(Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE)).thenReturn(false);
        Assert.assertEquals(true, spiritProductController.
                compareExcisePositionQuantity(currentQuantityValue, defaultQuantityValue, stateConditionEquality));
    }

    @Test
    public void compareExcisePositionCountReduceDeniedStateTest() throws Exception {
        BigDecimal currentQuantityValue = new BigDecimal(3);
        BigDecimal defaultQuantityValue = new BigDecimal(2);
        when(Factory.getTechProcessImpl()).thenReturn(techProcessInterface);
        boolean stateConditionEquality = true;
        when(Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE)).thenReturn(false);
        Assert.assertEquals(true, spiritProductController.
                compareExcisePositionQuantity(currentQuantityValue, defaultQuantityValue, stateConditionEquality));
    }

    @Test
    public void compareExcisePositionCountReduceStateTest() throws Exception {
        BigDecimal currentQuantityValue = new BigDecimal(3);
        BigDecimal defaultQuantityValue = new BigDecimal(2);
        when(Factory.getTechProcessImpl()).thenReturn(techProcessInterface);
        boolean stateConditionEquality = true;
        when(Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE)).thenReturn(true);
        Assert.assertEquals(true, spiritProductController.
                compareExcisePositionQuantity(currentQuantityValue, defaultQuantityValue, stateConditionEquality));
    }

    @Test
    public void compareExcisePositionWrongInputQnty() throws Exception {
        BigDecimal currentQuantityValue = new BigDecimal(2);
        BigDecimal defaultQuantityValue = new BigDecimal(3);
        when(Factory.getTechProcessImpl()).thenReturn(techProcessInterface);
        boolean stateConditionEquality = true;
        when(Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE)).thenReturn(true);
        Assert.assertEquals(false, spiritProductController.
                compareExcisePositionQuantity(currentQuantityValue, defaultQuantityValue, stateConditionEquality));
    }

    @Test
    public void compareExcisePositionZeroQnty() throws Exception {
        BigDecimal currentQuantityValue = new BigDecimal(2);
        BigDecimal defaultQuantityValue = new BigDecimal(0);
        when(Factory.getTechProcessImpl()).thenReturn(techProcessInterface);
        boolean stateConditionEquality = true;
        when(Factory.getTechProcessImpl().checkUserRight(Right.COUNT_REDUCE)).thenReturn(true);
        Assert.assertEquals(false, spiritProductController.
                compareExcisePositionQuantity(currentQuantityValue, defaultQuantityValue, stateConditionEquality));
    }
}