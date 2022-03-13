package ru.crystals.pos.visualization.products.metric;

import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductMetricEntity;
import ru.crystals.pos.check.BigDecimalConverter;
import ru.crystals.pos.check.CheckState;
import ru.crystals.pos.check.MeasurePositionEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PurchaseEntity;
import ru.crystals.pos.user.Right;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.commonplugin.model.ProductPluginModel;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.eventlisteners.EnterEventListener;
import ru.crystals.pos.visualization.eventlisteners.EscEventListener;
import ru.crystals.pos.visualization.eventlisteners.LeftListener;
import ru.crystals.pos.visualization.eventlisteners.NumberEventListener;
import ru.crystals.pos.visualization.eventlisteners.RightListener;
import ru.crystals.pos.visualization.products.CommonProductContainer;
import ru.crystals.pos.visualization.products.metric.MetricProductComponent.METRIC_COMPONENT_MODE;
import ru.crystals.pos.visualization.scenaries.DeletePositionScenario;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_METRIC_ENTITY, mainEntity = ProductMetricEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class MetricProductContainer extends CommonProductContainer
		     implements DotEventListener, NumberEventListener, EnterEventListener, EscEventListener, LeftListener, RightListener {
	private final MetricProductComponent visualPanel;
	private ProductMetricEntity product = null;
	private boolean valueScanned = false;
    PositionEntity pos = new PositionEntity();

    @Override
    protected ProductPluginModel getModel() {
        return null;
    }

    @Override
    protected AbstractProductController getController() {
        return null;
    }

    @Override
    public List<PositionEntity> getReturnPositions(PurchaseEntity returnPurchase, List<PositionEntity> checkReturnPositions, boolean fullReturn, List<PositionEntity> validatedPositions) {
        Map<Long, PositionEntity> returnPositionsMap = new HashMap<>();

        for (PositionEntity p : checkReturnPositions) {
            returnPositionsMap.put(p.getNumberInOriginal(), p);
        }

        List<PositionEntity> result = new ArrayList<>();

        PositionEntity returnPosition;
        for (PositionEntity origPos : returnPurchase.getPositions()) {
            returnPosition = returnPositionsMap.get(origPos.getNumber());
            if (returnPosition != null) {
                if (origPos.getQnty().equals(returnPosition.getQnty())) {
                    result.add(returnPosition);
                }
            }
        }
        return result;
    }

	enum Buttons {
        EDIT, DELETE, NONE
    };
    private static final double COUNT_PRECISION = 1000;
	private Buttons selectedButton = Buttons.NONE;
    private boolean editButtonEnabled = false;
    private boolean deleteButtonEnabled = false;

    public MetricProductContainer() {
        visualPanel = new MetricProductComponent();
    }

    @Override
    public ProductEntity getProduct(){
        return product;
    }

	@Override
	public void enter() {
	    if (visualPanel.getState() == ProductState.EDIT_OR_DELETE) {
            switch (selectedButton) {
                case EDIT:
                    setProductState(ProductState.EDIT);
                    break;
                case DELETE:
                    new DeletePositionScenario().execute(position, CheckState.EDIT_CHECK);
                    clean();
                    break;
                case NONE:
                    break;
            }
            return;
        }
        if (visualPanel.getMetricComponentMode() == METRIC_COMPONENT_MODE.PRICE
                && (visualPanel.getSumm().multiply(BigDecimal.valueOf(100, 0)).longValue() == 0)) {
            getFactory().getTechProcess().error();
            return;
        }
		doPositionAdd();
	}

	@Override
    public void left() {
        if (visualPanel.getState() == ProductState.EDIT_OR_DELETE) {
            changeSelection();
        }
    }

	@Override
    public void right() {
        if (visualPanel.getState() == ProductState.EDIT_OR_DELETE) {
            changeSelection();
        }
    }

	public void setProductState(ProductState state) {
        super.setProductState(state);
            visualPanel.setProductState(state);
            if (state == ProductState.EDIT_OR_DELETE) {
                checkRightsForEditAndDelete();
            }
    }

	@Override
    public void setPosition(PositionEntity position) {
        super.setPosition(position);
        visualPanel.setPosition(position);
        if (getProductState() == ProductState.EDIT_OR_DELETE) {
            if (position != null) {
                visualPanel.setSumma(BigDecimalConverter.convertMoney(position.calculateStartPositionSum()));
                visualPanel.setValue(position.getQntyBigDecimal());
            }
            checkRightsForEditAndDelete();
        }
    }

    private void checkRightsForEditAndDelete() {
        editButtonEnabled = (product != null && product.getProductConfig().getChangeQnty()
                && product.getProductPositionData().getCanChangeQuantity().orElse(true)
                && (getFactory().checkUserRight(Right.COUNT_INCREASE)
                || getFactory().checkUserRight(Right.COUNT_REDUCE)));
        deleteButtonEnabled = Factory.getTechProcessImpl().canUserDeletePosition(getPosition());

        if (editButtonEnabled) {
            selectedButton = Buttons.DELETE;
        } else if (deleteButtonEnabled) {
            selectedButton = Buttons.EDIT;
        } else {
            selectedButton = Buttons.NONE;
        }
        changeSelection();
    }

    private void changeSelection() {

        switch (selectedButton) {
            case EDIT:

                if (deleteButtonEnabled) {
                    selectedButton = Buttons.DELETE;
                    visualPanel.changeSelectedButton(false);

                    if (!editButtonEnabled) {
                        visualPanel.disableEditButton();
                    }
                } else {
                    visualPanel.changeSelectedButton(true);
                    visualPanel.disableDeleteButton();
                }

                break;

            case DELETE:

                if (editButtonEnabled) {
                    selectedButton = Buttons.EDIT;
                    visualPanel.changeSelectedButton(true);

                    if (!deleteButtonEnabled) {
                        visualPanel.disableDeleteButton();
                    }
                } else {
                    visualPanel.changeSelectedButton(false);
                    visualPanel.disableEditButton();
                }
                break;

            case NONE:
                visualPanel.disableEditButton();
                visualPanel.disableDeleteButton();
                break;
        }
    }

	@Override
	public boolean ableToAddPosition() {
	    return getCurrentPositionCount() > 0 && !isCallDone();
	}

    @Override
    public long getCurrentPositionCount() {
        return visualPanel.getValue().multiply(BigDecimal.valueOf(1000)).longValue();
    }

    private void addPosiotionToDB(PositionEntity positionEntity) throws Exception {
	    Factory.getTechProcessImpl().addPosition(positionEntity, positionEntity.getInsertType());
        if (getVisualPanel().getPresetValue() != position.getQnty()) {
            // В случае если добаляем позицию с измененным количеством (количество не по умолчанию)
            // отправляем событие изменения кол-ва в призму
            Factory.getInstance().getTechProcessEvents().eventChangePositionQuantity(Factory.getTechProcessImpl().getCheckWithNumber(), position);
        }
	}

    @Override
    public void doPositionAdd() {
        if (ableToAddPosition()) {
            switch (getProductState()) {
                case ADD:
                    PositionEntity positionEntity = makeNewPosition(PositionEntity.class);
                    doSetPosition(positionEntity);
                    try {
                        addPosiotionToDB(positionEntity);
                        dispatchCloseEvent(true);
                    } catch (Exception e) {
                        getFactory().showMessage(e.getMessage());
                    }
                    setCallDone(true);
                    break;
                case EDIT_REFUND:
                    if (getPosition().getQntyBigDecimal().subtract(visualPanel.getValue()).doubleValue() >= 0L) {
                        PositionEntity position = null;
                        try {
                            position = getPosition().cloneLight();
                        } catch (CloneNotSupportedException e) {
                            e.printStackTrace();
                        }
                        position.setQntyBigDecimal(visualPanel.getValue());
                        getPosition().setQntyBigDecimal(getPosition().getQntyBigDecimal().subtract(visualPanel.getValue()));
                        try {
                            Factory.getTechProcessImpl().addPosition(position, position.getInsertType());
                            dispatchCloseEvent(true);
                        } catch (Exception e) {
                            getFactory().showMessage(e.getMessage());
                            dispatchCloseEvent(false);
                        }
                        setPosition(null);
                    }
                    setCallDone(true);
                    break;
                case REFUND:
                    if (visualPanel.getMetricComponentMode() == METRIC_COMPONENT_MODE.PRICE) {

                        if (Factory.getTechProcessImpl().getCheck() == null) {
                            try {
                                Factory.getTechProcessImpl().refund(null);
                                MetricProductContainer.super.setProductState(ProductState.ADD);
                                doPositionAdd();
                                visualPanel.setMetricComponentMode(METRIC_COMPONENT_MODE.VALUE);
                                setCallDone(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            if (Factory.getTechProcessImpl().getCheck().isSale()) {
                                Factory.getTechProcessImpl().getCheck().setReturn();
                            }

                            super.setProductState(ProductState.ADD);
                            doPositionAdd();
                            visualPanel.setMetricComponentMode(METRIC_COMPONENT_MODE.VALUE);
                            setCallDone(true);
                        }

                    } else {
                        getFactory().getMainWindow().getCheckContainer().setPositionsRefund(true);
                        product.setValueBigDecimal(visualPanel.getValue());
                        visualPanel.enterSumma();
                        setProductPrice(product.getPrice().getPrice());
                        visualPanel.setMetricComponentMode(METRIC_COMPONENT_MODE.PRICE);
                        setReset(false);
                        doPositionAdd();
                    }
                    break;
                case EDIT:
                    if (visualPanel.getValue().doubleValue() > 0){
                        getPosition().setQntyBigDecimal(visualPanel.getValue());
                    }

                    try {
                        getFactory().getTechProcess().changePosition(getPosition());
                        dispatchCloseEvent(true);
                    } catch (Exception e) {
                        getFactory().getTechProcess().error();
                        getFactory().showMessage(e.getMessage());
                        dispatchCloseEvent(false);
                    }

                    setPosition(null);
                    setCallDone(true);
                    break;
                }

        } else {
            getFactory().getTechProcess().error();
        }
    }

    protected void setProductPrice(Long price) {
        BigDecimal sum = BigDecimal.valueOf(0L, 2);

        BigDecimal bdPrice=BigDecimal.valueOf(price, 2);

        visualPanel.setSum(bdPrice);
        visualPanel.setSumma(BigDecimal.valueOf(Math.round(product.getValue() / COUNT_PRECISION * price), 2));

        pos.setProduct(product);

        if (getProduct() != null) {
            pos.setProductType((getProduct().getClass().getSimpleName()));
        }
        pos.setQnty(this.product.getValue());
        pos.setNdsClass(product.getNdsClass());
        pos.setNds(product.getNds());
        pos.setPriceStart(price);
        pos.setBarCode(product.getBarCode() == null ? "" : product.getBarCode().getBarCode());
        pos.setItem(product.getItem());
        if (product.getMeasure() != null) {
            pos.setMeasure(new MeasurePositionEntity());
            pos.getMeasure().setCode(product.getMeasure().getCode());
            pos.getMeasure().setName(product.getMeasure().getName());
        }
        pos.setDepartNumber(product.getDepartNumber());
        pos.setName(product.getName());
        pos.setTypePriceNumber(product.getPrice().getNumber());
        pos.setProductSettings(product.getProductConfig());
        pos.setCalculateDiscount(product.getProductConfig().getIsDiscountApplicable());

        if (getProductState() == ProductState.ADD || getProductState() == ProductState.REFUND) {
            sum = BigDecimal.valueOf(Factory.getTechProcessImpl().getCheckSum(pos), 2);
        } else if (!getPosition().getQnty().toString().equals(pos.getQnty().toString()) && Factory.getTechProcessImpl().getCheck() != null) {
            BigDecimal tQuantity = getPosition().getQntyBigDecimal().subtract(product.getValueBigDecimal());
            sum = Factory.getTechProcessImpl().getCheck().getCheckSumStartBigDecimal().subtract(tQuantity.multiply(product.getPrice().getPriceBigDecimal()).setScale(2, RoundingMode.HALF_EVEN));
        }

        dispatchUpdateEvent(sum);
    }

	@Override
	public void esc() {
		super.esc();
		if (getProductState() == ProductState.VIEW){
			setReset(true);
			super.esc();
		}
		if (!isReset() && !valueScanned){
			reset();
		} else if (valueScanned){
			setReset(true);
			super.esc();
		}
	}

	@Override
	public MetricProductComponent getVisualPanel() {
		return visualPanel;
	}

	@Override
	public boolean isVisualPanelCreated() {
		return visualPanel != null;
	}

	@Override
    public void number(Byte num) {
            visualPanel.number(num);
            setReset(false);
    }

	@Override
	public void setProduct(ProductEntity product) {
        visualPanel.setProduct(product);

        this.product = (ProductMetricEntity) product;

        if (this.product.getValue() != null && this.product.getValue() > 0) {
            valueScanned = true;
            setCallDone(false);
        } else {
            valueScanned = false;
            reset();
        }


        if (getProductState() == ProductState.VIEW) {
            dispatchEventGoodInfo(product);
        }
	}

	@Override
	public void clean() {
		reset();
		setPosition(null);
		product = null;
	}


	@Override
	public void reset() {
        if (!valueScanned && getFactory().getTechProcess().checkScaleModuleState()) {
            visualPanel.setInputFieldEnabled(false);
        }
        visualPanel.reset();
        visualPanel.enterQuantity();
        setReset(true);
	}

	@Override
    public void dot() {
        if(!valueScanned){
            visualPanel.dot();
        }
    }

	public MetricProductComponent getCommonProductComponent() {
		return getVisualPanel();
	}

	@Override
	public boolean isProductStateAllowed(ProductState state) {
		return state == ProductState.ADD
		|| state == ProductState.VIEW
		|| (state == ProductState.EDIT)
		|| (state == ProductState.EDIT_REFUND);
	}

	public String getQuantityString(Double quantity, String measure){
		return String.format(ResBundleGoodsMetric.getString("WEIGHT_QUANTITY"), quantity, measure).trim();
	}

	@Override
	public PositionEntity makeNewPosition(Class<? extends PositionEntity> positionClass) {
        PositionEntity positionEntity = super.makeNewPosition(positionClass);

        visualPanel.getPrice();
        positionEntity.setPriceStart(product.getPrice().getPrice());
        positionEntity.setBeforeManualPrice(product.getBeforeManualPrice());

        if (product.getValue() != null && product.getValue()>0) {
            positionEntity.setQnty(product.getValue());
        } else {
            positionEntity.setQntyBigDecimal(visualPanel.getValue());
        }

        positionEntity.setProduct(product);

        if (getProduct() != null) {
            positionEntity.setProductType(product.getClass().getSimpleName());
        }

        positionEntity.setNdsClass(product.getNdsClass());
        positionEntity.setNds(product.getNds());
        positionEntity.setBarCode(product.getBarCode()==null?"":product.getBarCode().getBarCode());
        positionEntity.setItem(product.getItem());
        if (product.getMeasure() != null) {
            positionEntity.setMeasure(new MeasurePositionEntity());
            positionEntity.getMeasure().setCode(product.getMeasure().getCode());
            positionEntity.getMeasure().setName(product.getMeasure().getName());
        }
        positionEntity.setDepartNumber(product.getDepartNumber());
        positionEntity.setName(product.getName());
        positionEntity.setTypePriceNumber(product.getPrice().getNumber());
        positionEntity.setProductSettings(product.getProductConfig());

        final Boolean isDiscountApplicable = product.getIsDiscountApplicable();
        Boolean calculateDiscount = isDiscountApplicable != null ? isDiscountApplicable : product.getProductConfig().getIsDiscountApplicable();
        positionEntity.setCalculateDiscount(calculateDiscount);

        return positionEntity;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

}
