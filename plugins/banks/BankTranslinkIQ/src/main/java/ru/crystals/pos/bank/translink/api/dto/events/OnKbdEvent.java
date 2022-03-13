package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Событие информирует ECR, что на устройстве произошло нажатие клавиши на клавиатуре POS. Это событие генерируется только в том случае, если устройство находится в
 * состоянии ожидания. Под состоянием ожидания подразумевается, что  POS не выполняет активной авторизации и не запрашивает у пользователя какие-либо данные для ввода
 * или выбора.
 */
public class OnKbdEvent implements EventProperties {

    /**
     * Клавиша, которая была нажата на устройстве. От 0 до 9, и, если доступно, от F0 до F9, «Вниз», «Вверх», «Влево», «Вправо», «FF», «FR», «FY», «FG» (функция,
     * красный, желтый и зеленый кнопки).
     */
    private final String kbdKey;

    @JsonCreator
    public OnKbdEvent(@JsonProperty("kbdKey") String kbdKey) {
        this.kbdKey = kbdKey;
    }

    public String getKbdKey() {
        return kbdKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OnKbdEvent that = (OnKbdEvent) o;
        return Objects.equals(kbdKey, that.kbdKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kbdKey);
    }

    @Override
    public String toString() {
        return "OnKbdEvent{" +
                "kbdKey='" + kbdKey + '\'' +
                '}';
    }
}
