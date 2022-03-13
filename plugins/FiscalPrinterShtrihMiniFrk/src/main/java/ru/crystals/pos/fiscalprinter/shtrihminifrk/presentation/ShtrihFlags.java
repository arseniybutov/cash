package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

/**
 * Флаги ФР семейства "Штрих".
 * <p/>
 * Implementation Note: класс специально сделан Immutable: создавайте объект через единственный конструктор и считывайте флажки. Изменение значений
 * флажков после создания объекта не предусматривается.
 * 
 * @author aperevozchikov
 */
public class ShtrihFlags {
    
    /**
     * Побитовое представление флагов.
     */
    private short flags;
    
    /**
     * Есдинственно правильный конструктор.
     * 
     * @param flags флаги, для которых надо создать этот объект-обертку
     */
    public ShtrihFlags(short flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        return String.format("shtrih-flags [0x%04X]", flags);
    }

    // getters

    /**
     * 0й бит во "флагах": рулон операционного журнала
     */
    public boolean isRollOfJournal() {
        return (flags & 0b0000000000000001) != 0;
    }

    /**
     * 1й бит во "флагах": рулон чековой ленты
     */
    public boolean isRollOfRibbon() {
        return (flags & 0b0000000000000010) != 0;
    }

    /**
     * 2й бит во "флагах": Верхний датчик подкладного документа
     */
    public boolean isUpperSensor() {
        return (flags & 0b0000000000000100) != 0;
    }

    /**
     * 3й бит во "флагах": Нижний датчик подкладного документа
     */
    public boolean isLowerSensor() {
        return (flags & 0b0000000000001000) != 0;
    }

    /**
     * 4й бит во "флагах": Положение десятичной точки (false – 0 знаков, true – 2 знака)
     */
    public boolean isDecimalSeparator() {
        return (flags & 0b0000000000010000) != 0;
    }

    /**
     * 5й бит во "флагах": наличие ЭКЛЗ
     */
    public boolean isEklz() {
        return (flags & 0b0000000000100000) != 0;
    }

    /**
     * 6й бит во "флагах": Оптический датчик операционного журнала (false – бумаги нет, true – бумага есть)
     */
    public boolean isJournalSensor() {
        return (flags & 0b0000000001000000) != 0;
    }

    /**
     * 7й бит во "флагах": Оптический датчик чековой ленты (false – бумаги нет, true – бумага есть)
     */
    public boolean isRibbonSensor() {
        return (flags & 0b0000000010000000) != 0;
    }

    /**
     * 8й бит во "флагах": Рычаг термоголовки контрольной ленты (false – поднят, true – опущен)
     */
    public boolean isControlRibbonLever() {
        return (flags & 0b0000000100000000) != 0;
    }

    /**
     * 9й бит во "флагах": Рычаг термоголовки чековой ленты (false – поднят, true – опущен)
     */
    public boolean isReceiptRibbonLever() {
        return (flags & 0b0000001000000000) != 0;
    }

    /**
     * 10й бит во "флагах": Крышка корпуса ФР (false – крышка опущена, true – поднята)
     */
    public boolean isCaseCover() {
        return (flags & 0b0000010000000000) != 0;
    }

    /**
     * 11й бит во "флагах": Денежный ящик (false – закрыт, true – окрыт)
     */
    public boolean isCashDrawer() {
        return (flags & 0b0000100000000000) != 0;
    }

    /**
     * 12й бит во "флагах":
     * <p/>
     * Либо (зависит от конкретной модели ФР)
     * <p/>
     * <b>Отказ правого датчика принтера (false – нет, true – да)</b>
     * <p/>
     * <b>Бумага на входе в презентер (false – нет, true – да)</b>
     */
    public boolean isRightSensorMalfuncton() {
        return (flags & 0b0001000000000000) != 0;
    }

    /**
     * 13й бит во "флагах":
     * <p/>
     * Либо (зависит от конкретной модели ФР)
     * <p/>
     * <b>Отказ левого датчика принтера (false – нет, true – да)</b>
     * <p/>
     * <b>Бумага на выходе из презентера (false – нет, true – да)</b>
     */
    public boolean isLeftSensorMalfuncton() {
        return (flags & 0b0010000000000000) != 0;
    }

    /**
     * 14й бит во "флагах": ЭКЛЗ почти заполнена
     */
    public boolean isEklzIsAlmostFull() {
        return (flags & 0b0100000000000000) != 0;
    }

    /**
     * 15й бит во "флагах":
     * <p/>
     * Либо (зависит от конкретной модели ФР)
     * <p/>
     * <b>Увеличенная точность количества (false – нормальная точность, true – увеличенная точность) [для ККМ без ЭКЛЗ]. Для ККМ с ЭКЛЗ (true –
     * нормальная точность, false – увеличенная точность)
     * <p/>
     * <b>Буфер принтера непуст (false – пуст, true – не пуст)</b>
     */
    public boolean isEnhancedPrecision() {
        return (flags & 0b1000000000000000) != 0;
    }
}
