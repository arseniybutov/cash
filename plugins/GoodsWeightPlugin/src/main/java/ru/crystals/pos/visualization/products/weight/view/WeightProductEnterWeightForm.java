package ru.crystals.pos.visualization.products.weight.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.catalog.BarcodeEntity;
import ru.crystals.pos.catalog.ProductPositionData;
import ru.crystals.pos.catalog.ProductWeightEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.listeners.XListener;
import ru.crystals.pos.localization.CoreResBundle;
import ru.crystals.pos.scale.Scale;
import ru.crystals.pos.techprocess.StatePurchase;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.utils.CommonLogger;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.ProductPluginController;
import ru.crystals.pos.visualization.commonplugin.view.form.CommonProductForm;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonDefaultProductHeaderPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductSummPanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductUnitPricePanel;
import ru.crystals.pos.visualization.commonplugin.view.panel.CommonProductWeightPanel;
import ru.crystals.pos.visualization.eventlisteners.WeightChangeEventListener;
import ru.crystals.pos.visualization.products.weight.ResBundleGoodsWeight;
import ru.crystals.pos.visualization.products.weight.controller.WeightProductController;

import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

/**
 * Форма ввода веса весового товара.
 * Ввод веса блокируется, если установлена настройка  manualWeightReject = true
 * Ввод веса блокируется, если имеются прикассовые весы и на них лежит товар
 * Ввод веса блокируется, если был просканирован товар с весом
 * События прикассовых весов блокируются, если был просканирован товар с весом
 */
public class WeightProductEnterWeightForm extends CommonProductForm<ProductWeightEntity,
        PositionEntity,
        CommonDefaultProductHeaderPanel,
        CommonProductUnitPricePanel,
        CommonProductSummPanel,
        CommonProductWeightPanel> implements WeightChangeEventListener {
    private static final Logger logger = LoggerFactory.getLogger(WeightChangeEventListener.class);
    //признак того, что вес взят из баркода товара и его нельзя менять
    private boolean weightScanned = false;
    private Scale scale;

    /**
     * Используются ли прикассовые весы
     */
    private boolean cashScaleUsed;

    /**
     * На прикассовых весах ничего не лежит
     */
    private boolean weightZero = true;

    /**
     * Пользователь начал ввод
     */
    private boolean userStartedInput = false;

    private Logger LOG = LoggerFactory.getLogger(WeightProductEnterWeightForm.class);

    public WeightProductEnterWeightForm(XListener outerListener) {
        super(outerListener);
        WeightProductEnterWeightForm.this.setName("ru.crystals.pos.visualization.products.weight.WeightProductEnterWeightForm");
    }

    /**
     * Показываем форму ввода веса
     */
    @Override
    public void showForm(ProductWeightEntity product, PositionEntity position) {
        // суперкласс тут не вызываем - переписываем тот код, т.к. allowUserInput завязывается в
        // весовом плагине на manualWeightReject
        this.product = product;
        this.position = position;
        this.weightScanned = false;
        this.weightZero = true;
        this.userStartedInput = false;

        footerPanel.reset();

        if (product != null) {
            this.allowUserInput = !product.getProductConfig().getManualWeightReject();   //настройка приходит с сервера - можно ли вводить вес руками
            this.weightScanned = product.getWeight() != null && product.getWeight() > 0; //ввод отключается, если товар пришел с весом (просканирован)
            this.allowUserInput &= !weightScanned;
        }

        //проставляем инфо в панельки
        headerPanel.setHeaderInfo(product);
        unitPanel.setProduct(product);
        //инициализируем поле ввода
        cashScaleUsed = ((WeightProductController) controller).isCashScaleUsed();
        footerPanel.setWelcomeText(((WeightProductController) controller).isCashScaleUsed() ? ResBundleGoodsWeight.getString("PUT_ON_SCALE") :
                ResBundleGoodsWeight.getString("ENTER_WEIGHT"));

        /*
         * Логика такая:
         * 1. Вес может быть зашит в самом числе баркода - это первый случай, нельзя изменять вес
         * 2. Вес может быть прикреплен к баркоду в базе (работает только если у баркода так же есть цена) - это второй случай, но так же нельзя давать менять вес
         * 3. просто введеный товар - дефолтный вес в 0
         */
        if (weightScanned) {
            logger.info("weightScanned");
            //занесем вес, если продукт сосканирован
            footerPanel.setWeight(product.getWeightBigDecimal());
        } else if (product.isFoundByBarcode() && product.getBarCode() != null) {
            logger.info("product.isFoundByBarcode() && product.getBarCode() != null");
            logger.info("product.getBarCode().getPrice() {}", product.getBarCode().getPrice());
            //анализируем пришедший баркод, если есть у него цена, то выставляем количество из него
            //этот код будет дублироваться во многих формах, его бы перенести повыше в иерархии
            BarcodeEntity barcode = product.getBarCode();
            Date now = new Date();
            if (barcode.getPrice() != null &&
                    (barcode.getBeginDate() == null || now.after(barcode.getBeginDate())) &&
                    (barcode.getEndDate() == null || now.before(barcode.getEndDate()))) {
                footerPanel.setWeight(barcode.getCountBigDecimal());
                weightScanned = true;
            }
        }

        //если отсканировали марку товара с информацией о его весе, то
        //добавим эту информацию
        ProductPositionData productPositionData = product.getProductPositionData();
        if (productPositionData != null && product.useWeightFromPositionData()) {
            long quantity = Optional.ofNullable(productPositionData.getWeight()).orElse(0L);
            if (quantity > 0) {
                footerPanel.setWeight(BigDecimal.valueOf(quantity, Optional.ofNullable(productPositionData.getWeightScale()).orElse(0)));
                weightScanned = true;
            }
        }

        if (position != null) {
            allowUserInput &= position.isCanChangeQnty();
            unitPanel.setUnitPrice(position.getPriceStartBigDecimal());
            if (position.getQnty() != null) {
                footerPanel.setWeight(position.getQntyBigDecimal());
            }
        } else
            // Если к кассе подключены весы и вес не сосканирован, считываем вес с них
            if (!weightScanned && ((WeightProductController) controller).isCashScaleUsed()) {
                logger.info("isCashScaleUsed");
                try {
                    scale = ((WeightProductController) controller).getScales();
                    long price = product.getPrice() != null ? product.getPrice().getPrice() : 0;
                    long weight = scale.getWeighWithTransmissionOfUnitPrice(Math.min(999999L, CurrencyUtil.getSumForPrint(price)));
                    if (weight > 0) {
                        product.setWeight(weight);
                        footerPanel.setWeight(product.getWeightBigDecimal());
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        updateSumm();
    }

    /**
     * Проверяем что у пользователя есть привилегия на изменение веса на прикассовых весах
     */
    protected boolean isUserCanEditWeight() {
        return Factory.getTechProcessImpl().checkUserRight(Right.EDIT_CASH_SCALES_WEIGHT);
    }

    @Override
    public CommonDefaultProductHeaderPanel createHeaderPanel() {
        return new CommonDefaultProductHeaderPanel();
    }

    @Override
    public CommonProductWeightPanel createQuantityPanel() {
        return new CommonProductWeightPanel(CoreResBundle.getStringCommon("ENTER_WEIGHT"), true);
    }

    @Override
    public CommonProductSummPanel createSummPanel() {
        return new CommonProductSummPanel();
    }

    @Override
    public CommonProductUnitPricePanel createUnitPanel() {
        return new CommonProductUnitPricePanel(true);
    }

    @Override
    public boolean dispatchKeyEvent(XKeyEvent e) {
        /*
         * Enter и Escape могут пробрасываться наружу (если вернуть false)
         * остальные нажатия обрабатываются внутри формы
         */
        if (!isVisible()) {
            return false;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (weightScanned) {   //если сосканировали товарный вес - его не надо очищать - сразу выходим
                return false;
            }
            if (footerPanel.getCurrentWeight() != null && footerPanel.getCurrentWeight().doubleValue() != 0) {
                resetScalesIfExist();
            }
            if (footerPanel.getCurrentWeight() == null || footerPanel.getCurrentWeight().compareTo(BigDecimal.ZERO) <= 0) {
                if (controller != null) {
                    controller.beepError(e.getSource().toString());
                }
                return true; //поглощаем событие - оно не пробросится выше в контроллер
            }
            return false;
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (weightScanned) {   //если сосканировали товарный вес - его не надо очищать - сразу выходим
                return false;
            }
            if (footerPanel.isReset()) {
                userStartedInput = false;
                resetScalesIfExist();
                return false;   //выходим в контроллер
            } else {
                userStartedInput = false;
                footerPanel.reset();
                updateSumm();
                ((WeightProductController) controller).sendChangeWeightEvent(getQuantity());
                return true;
            }
        } else if (XKeyEvent.isCommaOrDigitOrBackspace(e)) {
            if (e.getKeyCode() == KeyEvent.VK_SPACE && ((WeightProductController) controller).getPurchaseState() == StatePurchase.WAIT_GOODS && !footerPanel.isReset()) {
                resetScalesIfExist();
            }
            if (isUserInputAllowed()) {
                userStartedInput = true;
                footerPanel.keyPressed(e);
                updateSumm();
                ((WeightProductController) controller).sendChangeWeightEvent(getQuantity());
            } else if (controller != null) {
                controller.beepError("Cannot change weight manually in plugin.");
                controller.sendEventChangeDenied(getQuantity());
            }
            return true;
        }
        return false;
    }

    /**
     * Реагировать ли на ввод пользователя
     * 1. Если allowUserInput и не используются прикассовые весы (дефолтное поведение)
     * 2. Если allowUserInput и используются прикассовые весы, но на них ничего не лежит
     *
     * @return true если нужно реагировать, иначе - false
     */
    protected boolean isUserInputAllowed() {
        boolean userCanEditWeight = isUserCanEditWeight();
        LOG.debug("allowUserInput[{}] && (cashScaleUsed[{}] || (weightZero[{}], userCanEditWeight[{}]))", allowUserInput, cashScaleUsed, weightZero, userCanEditWeight);
        return allowUserInput && (!cashScaleUsed || (weightZero && userCanEditWeight)) && (Factory.getTechProcessImpl().checkUserRight(Right.CHANGE_POSITION_QUANTITY)
                || Factory.getInstance().getProperties().getRequireRightForCancelPosition());
    }

    private void resetScalesIfExist() {
        if (((WeightProductController) controller).isCashScaleUsed()) {
            try {
                scale.transmitUnitPrice(0L);
            } catch (Exception e1) {
                footerPanel.reset();
                logger.error("", e1);
            }
        }
    }

    @Override
    public void setController(ProductPluginController controller) {
        super.setController(controller);
        //подписываемся на внешнее событие изменение веса - это событие присылают прикассовые весы
        ((WeightProductController) controller).addWeightChangeListener(this);
    }

    @Override
    protected boolean dispatchMSREvent(String track1, String track2, String track3, String track4) {
        return false;
    }

    @Override
    protected boolean dispatchBarcodeEvent(String barcode) {
        return false;
    }

    @Override
    public BigDecimal getQuantity() {
        return footerPanel.getCurrentWeight();
    }

    @Override
    public BigDecimal getPrice() {
        if (product != null) {
            return product.getPrice().getPriceBigDecimal();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public void clear() {
        if (footerPanel != null) {
            footerPanel.clear();
        }
        updateSumm();
    }

    private void updateSumm() {
        BigDecimal summ = getSumm();
        summPanel.updateSumm(summ);
        controller.updateSumm(summ);
    }

    /**
     * Прикассовые весы изменили вес, запихнем все в панель ввода
     * Если товар был сосканирован, то его вес низзя трогать
     */
    @Override
    public void weightChange(BigDecimal weight) {
        if (isVisible() && !userStartedInput) {
            if (!this.weightScanned) {
                // Если по каким-то причинам мы не получили весы ранее, но вес мы с их получаем, сетим scale
                if (scale == null) {
                    scale = ((WeightProductController) controller).getScales();
                }
                if (weight.compareTo(BigDecimal.ZERO) > 0) {
                    footerPanel.setWeight(weight);
                    updateSumm();
                    weightZero = false;
                    ((WeightProductController) controller).sendChangeWeightEvent(getQuantity());
                } else if (weight.compareTo(BigDecimal.ZERO) < 0) {
                    clear();
                    ((WeightProductController) controller).sendChangeWeightEvent(getQuantity());
                    weightZero = true;
                } else {
                    weightZero = true;
                }
            } else {
                CommonLogger.getCommonLogger().warn("Cannot change scanned weight from scales!");
            }
        }
    }
}
