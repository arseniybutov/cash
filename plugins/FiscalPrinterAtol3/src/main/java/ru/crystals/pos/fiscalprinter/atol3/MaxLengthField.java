// copy of ru.crystals.pos.fiscalprinter.atol.MaxLengthField

package ru.crystals.pos.fiscalprinter.atol3;

enum MaxLengthField {
    /**
     * Длина строки при внесении/изъятие
     */
    MONEYINOUTSTRING,
    /**
     * Наименование товара
     */
    GOODSNAME,
    /**
     * Артикул товара или штриховой код
     */
    ITEMNAME,
    /**
     * Артикул товара или штриховой код при отсутствии кода отдела
     */
    ITEMNAMEWITHDEPART,
    /**
     * Имя кассира
     */
    CASHIERNAME,
    /**
     * Наименование скидки/наценки
     */
    DISCOUNTNAME,
    /**
     * Наименование платежа
     */
    PAYMENTNAME,
    /**
     * Вывод строки на дисплей покупателя
     */
    ADDTOLCD,
    /**
     * Любая строка использующаяся в прочих местах
     */
    DEFAULTTEXT,
    /**
     * Запрограммированное значение строки клише или рекламного текста
     */
    REQUISIT,
    /**
     * Программирование таблицы
     */
    PROGRAMMING_TABLE,
    /**
     * Наименование налоговой ставки
     */
    TAXNAME
}
