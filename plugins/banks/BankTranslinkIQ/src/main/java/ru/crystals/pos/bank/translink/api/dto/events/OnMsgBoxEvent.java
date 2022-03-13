package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * При получении данного события программное обеспечение ECR должно отобразить полученное текстовое сообщение
 * в экране ECR в отдельном окне с возможностью выбора переданных указаний кассирy.
 */
public class OnMsgBoxEvent implements EventProperties {

    /**
     * Текст, который будет отображаться на экране, пока оператор ECR не выполнит требуемое действие.
     */
    private final String displayText;

    /**
     * Доступные действия для покупателя
     * Системы поддерживают следующие типы:
     * <ul>
     * <li>OK</li>
     * <li>OK_CANCEL</li>
     * <li>YES_NO</li>
     * <li>YES_NO_CANCEL</li>
     * </ul>
     */
    private final String boxButtons;

    /**
     * Возможные причины:
     * <ul>
     * <li>оther</li>
     * <li>signatureCheck</li>
     * <li>last4DigitsCheck</li>
     * </ul>
     */
    private final String reasonType;

    @JsonCreator
    public OnMsgBoxEvent(@JsonProperty("displayText") String displayText,
                         @JsonProperty("boxButtons") String boxButtons,
                         @JsonProperty("reasonType") String reasonType) {
        this.displayText = displayText;
        this.boxButtons = boxButtons;
        this.reasonType = reasonType;
    }

    public String getDisplayText() {
        return displayText;
    }

    public String getBoxButtons() {
        return boxButtons;
    }

    public String getReasonType() {
        return reasonType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnMsgBoxEvent that = (OnMsgBoxEvent) o;
        return Objects.equals(displayText, that.displayText) &&
                Objects.equals(boxButtons, that.boxButtons) &&
                Objects.equals(reasonType, that.reasonType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayText, boxButtons, reasonType);
    }

    @Override
    public String toString() {
        return "OnMsgBoxEvent{" +
                "displayText='" + displayText + '\'' +
                ", boxButtons='" + boxButtons + '\'' +
                ", reasonType='" + reasonType + '\'' +
                '}';
    }
}
