package ru.crystals.pos.spi.ui.forms.payment;

import ru.crystals.pos.api.ui.listener.SumToPayFormListener;
import ru.crystals.pos.listeners.XKeyEvent;
import ru.crystals.pos.spi.ui.ControlFactory;
import ru.crystals.pos.spi.ui.forms.ContextPanelWithCaption;
import ru.crystals.pos.spi.ui.forms.Fonts;
import ru.crystals.pos.spi.ui.payment.SumToPayFormParameters;
import ru.crystals.pos.visualization.components.inputfield.InputFieldFlat;
import ru.crystals.pos.visualization.styles.Color;
import ru.crystals.pos.visualization.utils.ScaleRectangle;
import ru.crystals.pos.visualizationtouch.components.ResBundleComponents;
import ru.crystals.pos.visualizationtouch.components.inputfield.CurrencyFormatter;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;

/**
 * Форма визуализации, отображающая диалог оплаты.
 */
public class SumToPayForm extends ContextPanelWithCaption {
    private static final int PADDING_PX = 20;
    private static final long serialVersionUID = 1L;

    /**
     * Максимально допустимое значение для ввода по умолчанию.
     */
    private static final long VALUE_MAX = 100000000000L;

    /**
     * "Доплатить"
     */
    private JLabel labelSum;
    /**
     * "внесено"
     */
    private JLabel labelPaid;

    private JLabel labelTextSum;
    /**
     * Поле для ввода величины оплаты
     */
    private InputFieldFlat<BigDecimal> inputField;
    /**
     * Моделька эта формочка отображает.
     */
    private ExtSumToPayFormParameters model;
    /**
     * Слушатель событий формы.
     */
    private SumToPayFormListener listener;

    /**
     * "Доплатить" / "Вернуть"
     */
    private JLabel labelAddPaymentText;

    /**
     * "Внесено" / "Возвращено"
     */
    private JLabel labelPaidText;

    private BigDecimal minSum;

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SumToPayForm}
     */
    public SumToPayForm() {
        labelAddPaymentText = ControlFactory.getInstance().createLabel("", Fonts.ITALIC_LARGE, SwingConstants.RIGHT);
        labelPaidText = ControlFactory.getInstance().createLabel("", Fonts.ITALIC_SMALL, SwingConstants.RIGHT);
        setAddPaymentText(true);
        setPaidText(true);

        this.add(labelAddPaymentText, new ScaleRectangle(PADDING_PX, 45, WIDTH_DEFAULT - (PADDING_PX * 2), 33));
        this.add(labelPaidText, new ScaleRectangle(0, 85, 170, 33));

        labelSum = ControlFactory.getInstance().createLabel("0.00", Fonts.REGULAR_EXTRA_LARGE, SwingConstants.RIGHT, Color.blackText);

        this.add(labelSum, new ScaleRectangle(WIDTH_DEFAULT - PADDING_PX - 380, 85, 380, 80));

        labelPaid = ControlFactory.getInstance().createLabel("0.00", Fonts.REGULAR_MEDIUM, SwingConstants.RIGHT, Color.blackText);
        labelSum.setName("jSurcharge");
        this.add(labelPaid, new ScaleRectangle(0, 110, 170, 33));

        inputField = ControlFactory.getInstance().createCurrencyInputField();
        this.add(inputField, new ScaleRectangle(PADDING_PX, 190, WIDTH_DEFAULT - (PADDING_PX * 2), 50));

        labelTextSum = ControlFactory.getInstance().createLabel(ResBundleComponents.getString("SUMMA"), Fonts.ITALIC_SMALL, SwingConstants.RIGHT);
        this.add(labelTextSum, new ScaleRectangle(PADDING_PX, 160, WIDTH_DEFAULT - (PADDING_PX * 2), 33));
    }

    private void setPaidText(boolean sale) {
        if (sale) {
            labelPaidText.setText(ResBundleComponents.getString("PAID"));
        } else {
            labelPaidText.setText(ResBundleComponents.getString("REFUNDED"));
        }
    }

    private void setAddPaymentText(boolean sale) {
        if (sale) {
            labelAddPaymentText.setText(ResBundleComponents.getString("ADD_PAYMENT"));
        } else {
            labelAddPaymentText.setText(ResBundleComponents.getString("RETURN_PAYMENT"));
        }
    }

    /**
     * Конструктор класса. Создаёт новый экземпляр класса {@link SumToPayForm}.
     *
     * @param model    модель данных, которой инициализуется форма.
     * @param listener слушатель событий формы
     */
    public SumToPayForm(ExtSumToPayFormParameters model, SumToPayFormListener listener) {
        this();
        this.model = model;
        this.listener = listener;
        updateView();
    }

    /**
     * Обновляет визуальное состояние формы согласно модели эта форма
     * отображает.
     */
    public void updateView() {
        final SumToPayFormParameters params = model.getParams();
        minSum = nullIfZero(model.getParams().getMinSum());
        setLabelSumText();
        setCaption(params.getCaption());
        inputField.setWelcomeText(params.getInputHint());

        ((CurrencyFormatter) inputField.getTextFormatter()).setMaxValue(params.getMaxSum() == null ? BigDecimal.valueOf(VALUE_MAX) :
                params.getMaxSum());

        if (params.getDefaultSum() != null) {
            inputField.setPresetValue(params.getDefaultSum());
        } else {
            inputField.setPresetValue(model.getSum());
        }

        setAddPaymentText(model.isSale());
        setPaidText(model.isSale());
        if (model.getSum() != null) {
            labelSum.setText(model.getSum().toString());
        } else {
            labelSum.setText("");
        }
        if (model.getPaid() != null) {
            labelPaid.setText(model.getPaid().toString());
        } else {
            labelPaid.setText("");
        }
    }

    private void setLabelSumText() {
        if (minSum != null) {
            labelTextSum.setText(String.format(ResBundleComponents.getString("SUM_MIN"), minSum));
        } else {
            labelTextSum.setText(ResBundleComponents.getString("SUMMA"));
        }
    }

    private BigDecimal nullIfZero(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return value;
    }

    /**
     * Установить сумму к оплате/возврату
     *
     * @param sum
     */
    public void setSum(BigDecimal sum) {
        labelSum.setText(sum.toString());
    }

    /**
     * Возвращает модель, которую визуализует этот экземпляр формы.
     *
     * @return модель формы.
     */
    public ExtSumToPayFormParameters getModel() {
        return model;
    }

    /**
     * Устанавливает модель, которую визуализует этот экземпляр формы. Данный
     * метод сам вызывает {@link #updateView()}, поэтому нет нужды вызывать его
     * отдельно после пользования сеттером. Однако, если модель меняется через
     * {@link #getModel()}, для изменения отображения формы следует вручную
     * вызвать {@link #updateView()}
     *
     * @param model модель, которую визуализует этот экземпляр формы.
     */
    public void setModel(ExtSumToPayFormParameters model) {
        this.model = model;
        updateView();
    }

    /**
     * Получает слушателя, который слушает события формы. У формы может быть
     * только один слушатель.
     *
     * @return слушатель событий формы.
     */
    public SumToPayFormListener getListener() {
        return listener;
    }

    /**
     * Устанавливает слушателя, который слушает события формы. У формы может
     * быть только один слушатель
     *
     * @param listener слушатель событий формы.
     */
    public void setListener(SumToPayFormListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean keyPressedNew(XKeyEvent e) {
        if (listener != null) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    onEnter();
                    break;
                case KeyEvent.VK_ESCAPE:
                    if (!inputField.clear()) {
                        listener.eventCanceled();
                    }
                    break;
            }
        }
        return inputField.press(e.getKeyChar(), e.getKeyCode());
    }

    private void onEnter() {
        final BigDecimal value = inputField.getValue();
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (minSum != null && value.compareTo(minSum) < 0) {
            inputField.setValue(minSum);
            return;
        }
        listener.eventSumEntered(value);
    }

}
