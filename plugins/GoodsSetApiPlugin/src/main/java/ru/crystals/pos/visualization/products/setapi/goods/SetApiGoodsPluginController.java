package ru.crystals.pos.visualization.products.setapi.goods;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.crystals.api.adapters.PluginsAdapter;
import ru.crystals.api.commons.PositionEntityBuilder;
import ru.crystals.api.commons.SetApiNewLineItemValidator;
import ru.crystals.pos.api.plugin.goods.NewLineItem;
import ru.crystals.pos.catalog.CatalogService;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckService;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.check.exception.PositionAddingException;
import ru.crystals.pos.fiscalprinter.FiscalPrinter;
import ru.crystals.pos.property.Properties;
import ru.crystals.pos.spi.plugin.goods.InvalidLineItemException;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.products.CheckAgeController;
import ru.crystals.pos.visualization.products.ProductContainer;
import ru.crystals.pos.visualization.products.setapi.goods.bl.BasicSetApiNewLineItemValidatorImpl;
import ru.crystals.pos.visualization.products.setapi.goods.i18n.ResBundleSetApiGoods;

import java.util.Map;
import java.util.Set;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SetApiGoodsPluginController extends AbstractProductController<SetApiGoodsPluginModel> {
    private static final Logger log = LoggerFactory.getLogger(SetApiGoodsPluginController.class);
    private PluginsAdapter pluginAdapter;
    private final PositionEntityBuilder positionEntityBuilder;
    private SetApiNewLineItemValidator lineItemValidator;
    private CheckService checkService;
    private final CatalogService catalogService;
    private final FiscalPrinter fiscalPrinter;
    private final Properties properties;

    @Autowired
    public SetApiGoodsPluginController(CatalogService catalogService, FiscalPrinter fiscalPrinter, Properties properties) {
        this.properties = properties;
        this.fiscalPrinter = fiscalPrinter;
        this.catalogService = catalogService;
        positionEntityBuilder = new PositionEntityBuilder(catalogService);
    }

    @Autowired
    void setPluginAdapter(PluginsAdapter pluginAdapter) {
        this.pluginAdapter = pluginAdapter;
    }

    @Autowired
    void setCheckService(CheckService checkService) {
        this.checkService = checkService;
    }

    protected final PluginsAdapter getPluginAdapter() {
        return pluginAdapter;
    }

    @Override
    public void processProductAdd(ProductEntity product) {
        clearModel();
        getModel().setProduct(product);
        getModel().changed();
    }

    /**
     * Реакция на событие начала техпроцесса добавления позиции в чек <em>продажи</em>
     */
    public void startSaleProcess() {
        log.trace("entering startSaleProcess()");


        // для начала проверим можно ли добавить позицию этого типа в чек
        String error = testPossibilityToAddPositionBeforehand();

        if (error != null) {
            getModel().setErrorMessage(error);
            getModel().setAddState(SetApiGoodsPluginState.ERROR);
        } else {
            // надо определиться в какое состояние перевести модель: в плагинные диалоги, или в проверку возраста
            int minAge = getMinAge();
            getModel().setMinAge(minAge);

            if (minAge == 0) {
                // подтверждения возраста не надо - переходим сразу к плагинным формам
                log.trace("no need to check age");
                getModel().setAddState(SetApiGoodsPluginState.PLUGIN_DIALOGS);
            } else {
                // нужен предварительный диалог о возрасте
                log.trace("it is necessary to check age: {}", minAge);
                getModel().setAddState(SetApiGoodsPluginState.CHECK_AGE);
            }
        }

        // продолжим процесс
        getModel().changed();

        log.trace("leaving startSaleProcess()");
    }

    /**
     * Реакция на событие не-подтверждения возраста покупателя при попытке продажи товара
     */
    public void ageNotConfirmed() {
        log.trace("entering ageNotConfirmed()");
        finishSale(false);
        log.trace("leaving ageNotConfirmed()");
    }

    /**
     * Реакция на событие успешного подтверждения возраста покупателя при попытке продажи товара
     */
    public void ageConfirmed() {
        log.trace("entering ageConfirmed()");
        getModel().setAddState(SetApiGoodsPluginState.PLUGIN_DIALOGS);
        getModel().changed();
        log.trace("leaving ageConfirmed()");
    }

    /**
     * Проверит валидна ли сформированная в ходе техпроцесса <em>продажи</em> позиция
     *
     * @throws InvalidLineItemException если позиция окажется невалидной. либо ее нельзя добавлять в чек
     */
    public void testLineItemToSell() throws InvalidLineItemException {
        log.trace("entering testLineItemToSell()");

        // сначала проверим не прислал ли нам плагин явный неликвид:
        testLineItemItself();

        //  а теперь проверим можно ли добавить позицию в чек
        testPossibilityToAddPosition();
    }

    /**
     * Вернет {@code true}, если текущий пользователь имеет право удалить позицию из чека.
     *
     * @return {@code false}, если этому пользователю/кассиру запрещено удалять позиции из чека
     */
    public boolean hasRightToDeletePosition(PositionEntity position) {
        return Factory.getTechProcessImpl().canUserDeletePosition(position);
    }

    public void saveExtendedReceiptAttributes(Map<String, String> receiptExtendedAttributes) {
        if (MapUtils.isEmpty(receiptExtendedAttributes)) {
            return;
        }
        getModel().getExtendedReceiptAttributes().putAll(receiptExtendedAttributes);
        saveExtendedReceiptAttributesToCheck();
    }

    private void saveExtendedReceiptAttributesToCheck() {
        if (Factory.getTechProcessImpl().saveCheckExtendedAttributes(getModel().getExtendedReceiptAttributes())) {
            getModel().getExtendedReceiptAttributes().clear();
        }
    }

    /**
     * Проверка возможности добавить позицию, что сформировал плагин для чека продажи, на возможность добавить в чек.
     *
     * @throws InvalidLineItemException если позицию в чек добавить нельзя
     */
    private void testPossibilityToAddPosition() throws InvalidLineItemException {
        CheckService cs = checkService;
        if (cs == null) {
            log.error("testPossibilityToAddPosition: <CheckService> was not detected!");
            return;
        }

        PositionEntity position = getPositionToSell();
        if (position == null) {
            // не удалось сформировать позицию?
            String errorMsg = ResBundleSetApiGoods.getString("set.api.goods.failed.to.transform.line.item.into.position");
            log.error("testPossibilityToAddPosition: the position to add is NULL");
            throw new InvalidLineItemException(errorMsg);
        }

        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        try {
            cs.isPossibleToAddPosition(position, purchaseEntity);
        } catch (PositionAddingException pae) {
            throw new InvalidLineItemException(pae.getMessage(), pae);
        }
    }

    /**
     * Проверка возможности добавить в чек позицию этого плагинного типа позицию - выполняется в самом начале процесса.
     *
     * @return {@code null}, если позицию данного типа можно добавить; иначе - вернет текст сообщения об ошибке, что можно показать кассиру
     *
     */
    private String testPossibilityToAddPositionBeforehand() {
        CheckService cs = checkService;
        if (cs == null) {
            log.error("testPossibilityToAddPositionBeforehand: <CheckService> was not detected!");
            return ResBundleSetApiGoods.getString("set.api.goods.add.position.error");
        }

        ProductEntity product = getModel().getProduct();
        if (product == null) {
            log.error("testPossibilityToAddPositionBeforehand: the product is null");
            return ResBundleSetApiGoods.getString("set.api.goods.add.position.error");
        }

        CatalogService catalogService = getCatalogService();
        if (catalogService == null) {
            log.error("testPossibilityToAddPositionBeforehand: <CatalogService> was not detected!");
            return ResBundleSetApiGoods.getString("set.api.goods.add.position.error");
        }

        PositionEntity pe = new PositionEntity();
        pe.setProductSettings(catalogService.getProductConfig(product.getDiscriminator()));
        pe.setProductType(product.getDiscriminator());

        String errorMsg = null;
        PurchaseEntity purchaseEntity = Factory.getTechProcessImpl().getCheck();
        try {
            cs.isPossibleToAddPositionWithoutSumsAndCountsTest(pe, purchaseEntity);
        } catch (Exception ex) { // NOSONAR
            errorMsg = ex.getMessage();
        }

        return errorMsg;
    }

    /**
     * Проверка позиции, что прислал нам плагин (для добавления в чек продажи), на целостность
     *
     * @throws InvalidLineItemException если позиция не валидна
     */
    private void testLineItemItself() throws InvalidLineItemException {
        SetApiNewLineItemValidator validator = getLineItemValidator();
        if (validator == null) {
            log.error("testLineItemItself: line item validator is not specified");
            return;
        }

        String errorMessage = validator.validate(getModel().getLineItem());
        if (StringUtils.isNotBlank(errorMessage)) {
            log.error("Internal error happened: {}", errorMessage);
            throw new InvalidLineItemException(errorMessage);
        }
    }

    /**
     * Завершение техпроцесса добавления позиции в чек продажи
     *
     * @param success успех/не-успех добавления
     */
    public void finishSale(boolean success) {
        log.trace("entering finishSale(boolean). The argument is: {}", success);

        if (!success) {
            // позицию не добавляем
            getAdapter().dispatchCloseEvent(false);
        } else {
            // позицию надо добавить
            PositionEntity pe = getPositionToSell();
            if (pe == null) {
                finishSale(false);
                return;
            }

            getAdapter().setProductState(ProductContainer.ProductState.ADD);
            if (getAdapter().doPositionAdd(pe)) {
                // Если позиция первая, то в процессе добавления позиции чека еще нет и чековые атрибуты не сохраняются.
                // Тут чек уже создан и позиция добавлена, поэтому чековые атрибуты успешно сохранятся.
                saveExtendedReceiptAttributesToCheck();
                getAdapter().dispatchCloseEvent(true);
            } else {
                log.warn("some other restrictions disallowed to add this position [{}]", pe);
                getAdapter().dispatchCloseEvent(false);
            }
        }

        log.trace("leaving finishSale(boolean)");
    }

    public void finishRefund(boolean success) {
        if (!success) {
            // позицию не добавляем
            getAdapter().dispatchCloseEvent(false);
        } else {
            // позицию надо добавить
            PositionEntity pe = getPositionToSell();
            if (pe == null) {
                finishRefund(false);
                return;
            }
            if(getAdapter().doPositionAdd(pe)) {
                getAdapter().dispatchCloseEvent(true);
            } else {
                log.warn("some other restrictions disallowed to add this position [{}]", pe);
                getAdapter().dispatchCloseEvent(false);
            }
        }

    }

    /**
     * Вернет позицию, что была сформирована в ходе техпроцесса добавления товара в чек для продажи
     *
     * @return {@code null}, если по техпроцессу еще не дошли до того момента, когда позиция уже есть
     */
    private PositionEntity getPositionToSell() {
        ProductEntity product = getModel().getProduct();
        if (product == null) {
            log.error("getPositionToSell: the product is null");
            return null;
        }

        NewLineItem lineItem = getModel().getLineItem();
        if (lineItem == null) {
            log.error("getPositionToSell: the new line item is null!");
            return null;
        }

        PositionEntity pe = new PositionEntity();
        fillDefaultPosition(BigDecimalConverter.convertQuantity(lineItem.getQuantity()), lineItem.getPrice(), product, pe);
        pe = positionEntityBuilder.createPositionEntity(pe, product, lineItem);

        return pe;
    }

    /**
     * Вернет возраст, достижение которого надо проверить, прежде чем продавать товар покупателю. В годах
     *
     * @return 0, если проверки возраста [более] не требуется
     */
    private int getMinAge() {
        int result;

        log.trace("entering getMinAge()");

        result = getMinAgeFromProductConfig();
        if (result <= 0 || !CheckAgeController.isNeedToConfirmAge(result)) {
            // данный возраст уже либо подтвержден (при покупке другого "возрастного" товара), либо проверки возраста не требуется
            result = 0;
        }

        log.trace("leaving getMinAge(). The result is: {}", result);

        return result;
    }

    /**
     * Вернет возраст, достижение которого надо проверить, прежде чем продавать товар покупателю
     * - из настроек типа товара. В годах
     *
     * @return 0, если проверки возраста не требуется
     */
    private int getMinAgeFromProductConfig() {
        if (getModel().getProduct() == null || getModel().getProduct().getProductConfig() == null) {
            return CheckAgeController.MIN_AGE_NO_RESTRICTION;
        } else {
            return getModel().getProduct().getProductConfig().isCheckAge() ? CheckAgeController.DEFAULT_MIN_AGE : CheckAgeController.MIN_AGE_NO_RESTRICTION;
        }
    }

    /**
     * Очистка модели - на всякий случай. Будем вызывать для профилактики в начале техпоцесса добавления позиции
     */
    private void clearModel() {
        getModel().setLineItem(null);
        getModel().setProduct(null);
        getModel().setAddState(SetApiGoodsPluginState.START);
        getModel().setMinAge(0);
        getModel().setState(ProductContainer.ProductState.ADD);
    }

    public SetApiNewLineItemValidator getLineItemValidator() {
        if (lineItemValidator == null) {
            Set<Float> taxes = fiscalPrinter.getTaxesCollection(properties.getShopINN()).getTaxesForCaclulate().keySet();
            lineItemValidator = new BasicSetApiNewLineItemValidatorImpl(taxes);
        }
        return lineItemValidator;
    }

    public void setLineItemValidator(SetApiNewLineItemValidator lineItemValidator) {
        this.lineItemValidator = lineItemValidator;
    }

    /**
     * Возвращает интерфейс для доступа к товарному каталогу.
     *
     * @return интерфейс для доступа к товарному каталогу
     */
    protected final CatalogService getCatalogService() {
        return catalogService;
    }

    @Override
    public void onAgeChecking() {
        getTechProcessEvents().eventWarningStateNotification();
    }

    @Override
    public void onAgeCheckingCompleted() {
        getTechProcessEvents().eventWorkingStateNotification();
    }

}
