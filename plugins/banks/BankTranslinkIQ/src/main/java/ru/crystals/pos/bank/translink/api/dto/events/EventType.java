package ru.crystals.pos.bank.translink.api.dto.events;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum EventType {

    @JsonEnumDefaultValue
    UNKNOWN,

    NO_MORE_EVENTS,

    /**
     * При получении данного сообщения программное обеспечение ECR должно отобразить сообщение с возможностью ввода информации кассиром.
     */
    ONPROMPT(OnPromptEvent.class),

    /**
     * При получении данного события программное обеспечение ECR  должно отобразить полученное текстовое сообщение
     * на экране ECR с возможностью выбора кассиром переданных опций.
     */
    ONSELECT(OnSelectEvent.class),
    /**
     * При получении данного события программное обеспечение ECR должно отобразить полученное текстовое сообщение
     * на экране ECR в отдельном окне с возможностью выбора переданных указаний кассирy.
     */
    ONMSGBOX(OnMsgBoxEvent.class),
    /**
     * Событие информирует ECR o изменении статуса выполняемой транзакции.
     */
    ONTRNSTATUS(OnTrnStatusEvent.class),
    /**
     * Событие информирует ECR, о том что POS считал информацию о предоставленной карте.
     */
    ONCARD(OnCardEvent.class),
    /**
     * Событие предназначено для информирования ECR о том, что данные карты больше не доступны.
     */
    ONCARDREMOVE,
    /**
     * При получении данного события программное обеспечение ECR должно отобразить полученное текстовое сообщение в экране ECR.
     */
    ONDISPLAYTEXT(OnDisplayTextEvent.class),
    /**
     * Событие информирует ECR, что POS подготовил данные чека для печати.
     * Если принтер ECR во время получения чека не доступен либо занят, полученную информацию надо кешировать.
     */
    ONPRINT(OnPrintEvent.class),
    /**
     * Событие информирует ECR, что на устройстве произошло нажатие клавиши на клавиатуре POS.
     * Это событие генерируется только в том случае, если устройство находится в состоянии ожидания.
     * Под состоянием ожидания подразумевается, что POS не выполняет активной авторизации
     * и не запрашивает у пользователя какие-либо данные для ввода или выбора.
     */
    ONKBD(OnKbdEvent.class);

    private final Class<? extends EventProperties> payloadClass;

    EventType(Class<? extends EventProperties> payloadClass) {
        this.payloadClass = payloadClass;
    }

    EventType() {
        payloadClass = null;
    }

    public Class<? extends EventProperties> getPayloadClass() {
        return payloadClass;
    }

}
