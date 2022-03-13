package ru.crystals.pos.visualization.products.mobilepay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.CashPluginQualifier;
import ru.crystals.pos.PluginType;
import ru.crystals.pos.ProductCashPluginComponent;
import ru.crystals.pos.catalog.ProductDiscriminators;
import ru.crystals.pos.catalog.ProductEntity;
import ru.crystals.pos.catalog.ProductMobilePayEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.check.PositionMobilePayEntity;
import ru.crystals.pos.services.ServicesResult;
import ru.crystals.pos.services.ServicesServiceAsync;
import ru.crystals.pos.services.ServicesServiceAsync.ServicesCallback;
import ru.crystals.pos.visualization.Factory;
import ru.crystals.pos.visualization.commonplugin.controller.AbstractProductController;
import ru.crystals.pos.visualization.commonplugin.model.ProductPluginModel;
import ru.crystals.pos.visualization.eventlisteners.DotEventListener;
import ru.crystals.pos.visualization.eventlisteners.EnterEventListener;
import ru.crystals.pos.visualization.eventlisteners.EscEventListener;
import ru.crystals.pos.visualization.eventlisteners.NumberEventListener;
import ru.crystals.pos.visualization.products.CommonProductContainer;

import java.math.BigDecimal;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@ProductCashPluginComponent(typeName = ProductDiscriminators.PRODUCT_MOBILE_PAY_ENTITY, mainEntity = ProductMobilePayEntity.class)
@CashPluginQualifier(PluginType.GOODS)
public class MobilePayProductContainer extends CommonProductContainer implements DotEventListener, NumberEventListener, EnterEventListener, EscEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(MobilePayProductContainer.class);

	public enum MobilePayState {
		ENTER_PHONE, ENTER_PAYMENT
	};

	private MobilePayProductComponent visualPanel = null;
	private String phone = "";
	private ProductMobilePayEntity product = null;
	private MobilePayState state = MobilePayState.ENTER_PHONE;
	private SummaHelper summaHelper = null;
	private boolean summaScanned = false;
	private final BigDecimal zeroPrice = BigDecimal.valueOf(100L, 2);

	@Override
	protected ProductPluginModel getModel() {
		return null;
	}

	@Override
	protected AbstractProductController getController() {
		return null;
	}

	private void dispatchUpdateWithSumma(Long summa) {
		PositionEntity pos = new PositionEntity();
		pos.setProduct(getProduct());
		pos.setQnty(1000L);
		pos.setPriceStart(summa);
		BigDecimal sum = BigDecimal.valueOf(getFactory().getTechProcess().getCheckSum(pos), 2);
		dispatchUpdateEvent(sum);
	}

	@Override
	public void enter() {
		switch (state) {
		case ENTER_PHONE:
			if (phone.length() == 10) {
				state = MobilePayState.ENTER_PAYMENT;
				getVisualPanel().setState(state);
				dispatchUpdateWithSumma(getSummaHelper().getLongSummaX100());
			} else
				getFactory().getTechProcess().error();
			break;
		case ENTER_PAYMENT:
			if (ableToAddPosition()) {
				doPositionAdd();
			} else {
				getFactory().getTechProcess().error();
			}
			break;
		}
	}

	@Override
	public boolean ableToAddPosition() {
		return opportunity(getCurrentPosition()) && ((state == MobilePayState.ENTER_PAYMENT) && (getSummaHelper().isSummaAvailable() || summaScanned))
				&& (!isCallDone());
	}

	@Override
	public void doPositionAdd() {
		if (ableToAddPosition()) {
			PositionEntity position = getCurrentPosition();
			doSetPosition(position);
			try {
			    Factory.getTechProcessImpl().addPosition(position, position.getInsertType());
                dispatchCloseEvent(true);
				setCallDone(true);
			} catch (Exception e) {
				setCallDone(false);
				getFactory().showMessage(e.getMessage());
			}
		}
	}

	private boolean opportunity(final PositionMobilePayEntity positionService) {
		final AtomicBoolean isSuccessful = new AtomicBoolean();
		final ServicesServiceAsync services = getFactory().getTechProcess().getServicesModule();

		final Object lock = new Object();

		synchronized (lock) {
			services.opportunity(positionService, new ServicesCallback() {

				@Override
				public Executor getExecutor() {
					return null;
				}

				@Override
				public void onSuccess(ServicesResult result) {
					if (result.isResult()) {
						isSuccessful.set(true);
					}
					synchronized (lock) {
						lock.notify();
					}
				}
			});

			try {
				lock.wait();
			} catch (InterruptedException e) {
				LOG.error("", e);
			}
		}

		return isSuccessful.get();
	}

	@Override
	public void esc() {
		if (getProductState() == ProductState.VIEW)
			reset();
		super.esc();
		if (!isReset())
			switch (state) {
			case ENTER_PHONE:
				reset();
				dispatchUpdateEvent(zeroPrice);
				break;
			case ENTER_PAYMENT:
				if (getSummaHelper().getSumma() == 0 && !summaScanned) {
					state = MobilePayState.ENTER_PHONE;
					getVisualPanel().setState(state);
					reset();
					dispatchUpdateEvent(zeroPrice);
				} else {
					summaScanned = false;
					getSummaHelper().reset();
					getVisualPanel().setPayment(getSummaHelper().getSumma());
					dispatchUpdateWithSumma(getSummaHelper().getLongSummaX100());
				}
				break;
			}
	}

	@Override
	public MobilePayProductComponent getVisualPanel() {
		if (visualPanel == null)
			visualPanel = new MobilePayProductComponent();
		return visualPanel;
	}

	@Override
	public boolean isVisualPanelCreated() {
		return visualPanel != null;
	}

	@Override
	public void number(Byte num) {
		setReset(false);
		switch (state) {
		case ENTER_PHONE:
			if (phone.length() < 10) {
				phone += new Integer(num).toString();
				getVisualPanel().setPhoneNumber(phone);
				dispatchUpdateEvent(zeroPrice);
			}
			break;
		case ENTER_PAYMENT:
			if (!summaScanned) {
				getSummaHelper().number(num);
				getVisualPanel().setPayment(getSummaHelper().getSumma());
				dispatchUpdateWithSumma(getSummaHelper().getLongSummaX100());
			}
			break;
		}
	}

	@Override
    public ProductEntity getProduct(){
        return product;
    }

	@Override
	public void setProduct(ProductEntity product) {
		this.product = (ProductMobilePayEntity) product;
		visualPanel.setProduct(product);
		summaScanned = false;
		if (this.product.getPhoneNumber() != null
				&& this.product.getPhoneNumber().length() != 0
				&& !this.product.getPhoneNumber().equals("0000000000")) {
			state = MobilePayState.ENTER_PAYMENT;
			getVisualPanel().setState(state);
			phone = this.product.getPhoneNumber();
			getVisualPanel().setPhoneNumber(phone);
			if (this.product.getPrice().getPrice() != 0) {
				getSummaHelper().reset();
				getVisualPanel().setPayment(
						this.product.getPrice().getPrice() / 100.0);
				getVisualPanel().setNotEditing(true);
				summaScanned = true;
			} else {
				getSummaHelper().reset();
			}
			setReset(false);
			dispatchUpdateWithSumma(getSummaHelper().getLongSummaX100());
		} else {
			reset();
			dispatchUpdateEvent(zeroPrice);
		}
		if (getProductState() == ProductState.VIEW) {
			getVisualPanel().setPayment(this.product.getPrice().getPrice() / 100.0);
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
		phone = "";
		setReset(true);
		state = MobilePayState.ENTER_PHONE;
		getSummaHelper().reset();
		getVisualPanel().setState(state);
		dispatchUpdateEvent(zeroPrice);
	}

	@Override
	public void dot() {
		if (!getSummaHelper().isDot() && getSummaHelper().getSumma() == 0.0) {
			number((byte) 0);
		}
		getSummaHelper().dot();

	}

	@Override
	public void setProductState(ProductState state) {
        super.setProductState(state);
        visualPanel.setProductState(state);
    }

	public void setSummaHelper(SummaHelper summaHelper) {
		this.summaHelper = summaHelper;
	}

	public SummaHelper getSummaHelper() {
		if (summaHelper == null)
			summaHelper = new SummaHelper(getFactory(), 0.01);
		return summaHelper;
	}

	@Override
	public boolean isProductStateAllowed(ProductState state) {
		return state == ProductState.ADD || state == ProductState.VIEW || (state == ProductState.EDIT) || (state == ProductState.EDIT_REFUND);
	}

	public PositionMobilePayEntity getCurrentPosition() {
		if (!getSummaHelper().isSummaAvailable())
			getSummaHelper().setSumma(product.getPrice().getPrice() / 100.0);

		PositionMobilePayEntity position = (PositionMobilePayEntity) super.makeNewPosition(PositionMobilePayEntity.class);
		position.setAccountNumber(phone);
		position.setSum(getSummaHelper().getLongSummaX100());
		position.setPriceStart(getSummaHelper().getLongSummaX100());
		position.setQnty(1000L);
		product.setPhoneNumber(phone);
		position.setProduct(product);
		position.setCalculateDiscount(product.getProductConfig().getIsDiscountApplicable());
		return position;
	}

	@Override
	public boolean isAvailable() {
		ServicesServiceAsync services = getFactory().getTechProcess().getServicesModule();
		return services != null && services.getStatus();
	}

	@Override
    public boolean canRepeatPosition() {
        return false;
    }

    @Override
    public long getCurrentPositionCount() {
        return 0;
    }
}
