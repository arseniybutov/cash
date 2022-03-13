package ru.crystals.pos.fiscalprinter.shtrihminifrk.presentation;

import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.fiscalprinter.Font;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.FiscalDocumentData;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AbstractDocument;
import ru.crystals.pos.fiscalprinter.datastruct.documents.BarCode;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalPrinterInfo;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.ShiftNonNullableCounters;
import ru.crystals.pos.fiscalprinter.shtrihminifrk.exceptions.ShtrihException;
import ru.crystals.pos.utils.PortAdapterException;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Коннектор/Средство для инфо-обмена с ФР семейства "Штрих" согласно "Протоколу работы ФР v. 1.12" (уровень представления).
 * <p/>
 * Допускается, что некоторые реализации данного интерфейса могут не все методы реализовать (т.е., реализация метода будет пустой) - например, из-за
 * того, что реальное устройство слишком "древнее" и про новинки версии протокола v. 1.12 просто "не знает".
 * <p/>
 * Список сокращений, принятых в документе: <og>
 * <li>ФР - Фискальный Регистратор;
 * <li>ФП - Фискальная Плата;
 * <li>ВУ - Внешнее Устройство;
 * <li>МДЕ - Минимальная Денежная Единица, "копейка";
 * <li>LRC - Longitude Redundancy Check - контрольная сумма </og>
 *
 * @author aperevozchikov
 */
public interface ShtrihConnector {

    /**
     * Код ошибки, сигнализирующий об успешном выполнении операции (запрос-ответ)
     */
    byte NO_ERROR = 0x00;

    /**
     * Ошибка. что возникает при установке времени (если включен контроль времени)
     */
    byte CONFIRM_DATE_ERROR = (byte) 0xC0;

    /**
     * Ошибка: "Команда не поддерживается в данной реализации ФР"
     */
    byte COMMAND_NOT_SUPPORTED = 0x37;

    /**
     * Ошибка: "Идет печать предыдущей команды"
     */
    byte PREVIOUS_PRINT_ORDER_IS_PROGRESS_ERROR = 0x50;

    /**
     * Ошибка: "Ожидание команды продолжения печати"
     */
    byte CONTINUE_PRINTING_COMMAND_EXPECTED_ERROR = 0x58;

    /**
     * Значение в таблице Штриха, соответствующее ФФД 1.2
     */
    int FFD_1_2 = 52;

    /**
     * Открывает порт для общения с внешним устройством (ФР) и настраивает это внешенее устройство для начала работы.
     * <p/>
     * NOTE: это метод жизненного цикла: этот метод следует вызывать ДО вызова любого другого метода данного объекта.
     *
     * @throws IOException
     *             если возникли проблемы ввода/вывода
     * @throws PortAdapterException
     *             при различных [логических] ошибках при открытии порта: порт не существует, уже занят другим приложением, порт не удалось настроить
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void open() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Задает "шапку", что будет печататься в заголовке <b>следующего</b> документа по завершению печати <b>текущего</b>.
     *
     * @param lines
     *            строки, что должны быть распечатаны в заголовке документа
     * @throws IOException
     *             если возникли проблемы ввода/вывода
     * @throws PortAdapterException
     *             при различных [логических] ошибках при открытии порта: порт не существует, уже занят другим приложением, порт не удалось настроить
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void setHeader(List<FontLine> lines) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Устанавливает налоговые ставки и названия этих ставок в указанные значения.
     *
     * @param taxes
     *            налоговые ставки, что надо записать в ФР; ключ - название налога; значение -
     *            ставка налога, в <em>сотых долях процента</em> (т.е., 1800 == 18.0%);
     *            <p/>
     *            NOTE: если название налога длинее допустимого - название будет обрезано;
     *            <p/>
     *            NOTE2: название налога == <code>null</code> распознается как пустая строка;
     *            <p/>
     *            NOTE3: крайне рекомендуется в качестве данного аргумента использовать реализацию {@link LinkedHashMap}: т.к.
     *            {@link Collection#iterator() последовательность} {@link Map#entrySet() элементов} в данной коллекции задает индекс налога в
     *            "налоговых" регистрах.
     *            <p/>
     *            NOTE4: если количество элементов в данной коллекции больше допустимого количества налоговых ставок (для данной модели ФР), то лишние
     *            элементы будут просто проигнорированы;
     *            <p/>
     *            NOTE5: если количество элементов в данной коллекции МЕНЬШЕ допустимого количества налоговых ставок (для данной модели ФР), то
     *            оставшиеся налоговые ставки будут просто <i>обнулены</i> (у них будет пустой название и ставка будет == 0%);
     *            <p/>
     *            NOTE6: и вообще рекомендуется сначала вызвать {@link #getTaxes()} - чтоб знать существующую структуру налоговых ставок (в т.ч. и
     *            узнать максимально поддерживаемое количество налоговых ставок)
     *            <p/>
     *            NOTE7: <code>null</code> распознается как пустая коллекция - т.е., в таком случае ВСЕ налоговые ставки будут обнулены - см. NOTE5
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void setTaxes(Map<String, Long> taxes) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет налоговые ставки, что сейчас "зашиты" в ФР.
     *
     * @return не <code>null</code> - в крайнем случае вернет пустую коллекцию; ключ - название налога; значение - ставка налога,
     *         в <em>сотых долях процента</em> (т.е., 18.0% ==1800). Результат будет упорядочен (именно поэтому используется {@link LinkedHashMap}) ... в порядке
     *         хранения этих ставок в ФР (т.е., в 0м элементе будет ставка 1го налога из регистров ФР, и т.д).
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    LinkedHashMap<String, Long> getTaxes() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Установит имя кассира с указанным номером [в списке кассиров, "зарегистрированных" в ФР] в указанное значение.
     * <p/>
     * NOTE: если номер кассира не является допустимым (меньше минимального, либо больше максимального), то ничего не будет сделано.
     * <p/>
     * NOTE2: если <code>cashierName</code> длиннее максимально допустимого, то имя кассира будет обрезано перед записью в ФР.
     * <p/>
     * NOTE3: если <code>cashierName</code> == <code>null</code>, то имя кассира будет просто <i>очищено</i>.
     *
     * @param cashierNo
     *            номер кассира (нумерация с 1), имя которого надо отредактировать
     * @param cashierName
     *            новое имя этого кассира
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void setCashierName(byte cashierNo, String cashierName) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Устанавливает номер [кассы/ККМ] подключенного ФР в указанное значение.
     *
     * @param cashNum
     *            номер кассы, которую "обслуживает" подключенный ФР;
     *            <p/>
     *            NOTE: если данное значение менее <code>1</code> или более <code>99</code>, то аргумент будет распознан как <code>1</code>.
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void setCashNumber(byte cashNum) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Отключение печати документов
     */
    default void disableDocumentPrinting(boolean disabled) throws ShtrihException {
    }

    /**
     * Вернет состояние по коду 1 ЭКЛЗ.
     *
     * @return не <code>null</code> - в крайнем случае будет Exception
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    ShtrihEklzStateOne getEklzStateOne() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет номер фискального накопителя.
     *
     * @return <code>String</code> - в крайнем случае будет Exception
     */
    String getFNNumber() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет  состояние фискального накопителя.
     *
     * @return <code>String</code> - в крайнем случае будет Exception
     */
    ShtrihFNStateOne getFNState() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет данные последней фискализации (перерегистрации)
     *
     * @return <code>null</code>, если не удалось найти (или корректно распознать) данные
     */
    ShtrihFiscalizationResult getLastFiscalizationResult() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Установим данные пользователя для отправки электронного чека
     *
     * @return <code>String</code> - в крайнем случае будет Exception
     */
    void setClientData(String clientData) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет количество накопившихся денег в кассе [за смену], в "копейках" (в МДЕ - Минимальных Денежных Единицах).
     *
     * @return накопление наличности в кассе, в "копейках"
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    long getCashAccumulation() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет показания сменных счетчиков.
     *
     * @return не <code>null</code> - в крайнем случае будет Exception
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    ShtrihShiftCounters getShiftCounters() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет показания сменных счетчиков для указанной смены.
     * <p/>
     * NOTE: будут заполнены только поля: {@link ShtrihShiftCounters#getSumSale() сумма продаж} и {@link ShtrihShiftCounters#getSumReturn() сумма
     * возвратов}.
     *
     * @param shiftNo
     *            номер смены, показания счетчиков для которой надо вернуть
     * @return <code>null</code>, если не удалось найти (или корректно распознать) данные по указанной смене
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    ShtrihShiftCounters getShiftCounters(int shiftNo) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет количество внесений денежных сумм за смену.
     *
     * @return количество внесений за смену
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    long getCashInCount() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет количество изъятий денежных сумм за смену (количество выплат денежных сумм за смену).
     *
     * @return количество внесений за смену
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    long getCashOutCount() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет количество отмененных документов.
     *
     * @return количество отмененных документов
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    long getAnnulCount() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет сквозной номер документа.
     *
     * @return сквозной номер документа
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    long getSpnd() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет номер последнего Z отчета.
     *
     * @return номер Z отчета
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    long getZReportCount() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Устанавливает дату-время подключенного ФР в указанное значение.
     *
     * @param dateTime
     *            дата и время, в которое надо установить внутренние часы ФР; если <code>null</code>, то ничего не будет сделано
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void setDateTime(Date dateTime) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Гудок (издать звуковой сигнал).
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void beep() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Аннулировать текущий чек. Если нету открытого документа, что надо аннулировать, то просто ничего не будет сделано.
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void annul() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Регистрация покупки указанной позиции.
     *
     * @param position
     *            позиция, факт продажи которой надо зарегистрировать
     *            <p/>
     *            NOTE: если {@link ShtrihPosition#getDeptNo() номер отдела} позиции невалиден (не положителен или превышает допустимый максимальный
     *            номер отдела), то продажа будет зарегистрирована в отделе с номером <code>1</code>.
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void regSale(ShtrihPosition position) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Регистрация указанной операции
     *
     * @param operation операция, которую надо зарегистрировать
     * @throws IOException          при возникновении ошибок ввода/вывода
     * @throws PortAdapterException при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException      при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *                              какого типа ошибки могут возникнуть
     */
    void regOperation(ShtrihOperation operation) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Добавление тегов counterparty: 1227, 1228, 1254, входящих в тег 1256
     */
    void addCounterpartyData(Check check) throws ShtrihException;

    /**
     * Регистрация возврата указанной позиции.
     *
     * @param position
     *            позиция, факт возврата которой надо зарегистрировать
     *            <p/>
     *            NOTE: если {@link ShtrihPosition#getDeptNo() номер отдела} позиции невалиден (не положителен или превышает допустимый максимальный
     *            номер отдела), то возврат будет зарегистрирован в отделе с номером <code>1</code>.
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void regReturn(ShtrihPosition position) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Регистрация скидки на позицию.
     *
     * @param discount
     *            скидка, что надо зарегистрировать
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void regDiscount(ShtrihDiscount discount) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Регистрация надбавки на позицию.
     *
     * @param discount
     *            надбавка, что надо зарегистрировать
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void regMargin(ShtrihDiscount discount) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Передача кода товарной номенклатуры позиции в ФР. Вызывается после добавления позиции.
     *
     * @param itemCode
     *            Код товарной номенклатуры который нужно передать в ФР.
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void sendItemCode(ShtrihItemCode itemCode) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Закрытие чека. Только 4 типа оплат и все кроме первого(наличные) суммируются и считаются ЭЛЕКТРОННЫМИ.
     *
     * @param receiptTotal
     *            описание (и суммы денег) чека что закрываем.
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void closeReceipt(ShtrihReceiptTotal receiptTotal) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Закрытие чека (расширенное). Суммирует все типы оплат кроме наличных и считает их ЭЛЕКТРОННЫМИ.
     *
     * @param receiptTotalEx
     *            описание (и суммы денег) чека что закрываем(расширенное).
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void closeReceiptEx(ShtrihReceiptTotalEx receiptTotalEx) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Закрытие чека (расширенное вариант 2). Если нужны типы оплат согласно ФФД (Н-р: Предварительная оплата (аванс)).
     *
     * @param receiptTotalV2Ex
     *            описание (и суммы денег) чека что закрываем(расширенное вариант 2).
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void closeReceiptV2Ex(ShtrihReceiptTotalV2Ex receiptTotalV2Ex) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Открыть ДЯ (денежный ящик).
     *
     * @param cashDrawerNumber
     *            номер ДЯ. что надо открыть (0..1)
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void openCashDrawer(byte cashDrawerNumber) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет краткое состояние ФР.
     *
     * @return не <code>null</code>
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    ShtrihShortStateDescription getShortState() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Получить тип устройства.
     *
     * @return не <code>null</code>
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    ShtrihDeviceType getDeviceType() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет [полное] состояние ФР.
     *
     * @return не <code>null</code>; и ни одно из его полей не будет <code>null</code>
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    ShtrihStateDescription getState() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет суммы записей в Фискальной Памяти.
     *
     * @param all
     *            флаг-признак: возвращать ли суммы всех записей; если <code>false</code>, то будут возвращены только суммы после последней
     *            пере-регистрации (после последней фискализации ККМ)
     * @return не <code>null</code>
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    FiscalMemorySums getFiscalMemorySums(boolean all) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Распечатает указанный ШК.
     *
     * @param barcode
     *            ШК, что надо распечатать
     * @param label
     *            подпись, что надо распечатать под ШК; если <code>null</code>, то никакой подписи не будет
     * @param alignment
     *            выравнивание, что надо использовать при печати этого ШК; <code>null</code> распознается как {@link ShtrihAlignment#CENTRE по центру}
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     * @throws IllegalArgumentException
     *             если аргументневалиден: <code>null</code>, его {@link BarCode#getType() тип} == <code>null</code>, его {@link BarCode#getValue()
     *             значение} пустое
     */
    void printBarcode(BarCode barcode, String label, ShtrihAlignment alignment) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Устанавливает название указаного отдела в указанное значение.
     *
     * @param deptNo
     *            номер отдела, название которого надо изменить;
     *            <p/>
     *            NOTE: если отдела с таким номером нет (не поддерживается данной моделью ФР), то ничего не будет сделано
     * @param deptName
     *            новое название отдела
     *            <p/>
     *            NOTE: если <code>null</code>, то название отдела будет просто очищено;
     *            <p/>
     *            NOTE2: если слишком длинное, то название будет обрезано.
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     * @throws IllegalArgumentException
     *             если аргументневалиден: <code>null</code>, его {@link BarCode#getType() тип} == <code>null</code>, его {@link BarCode#getValue()
     *             значение} пустое
     */
    void setDepartmentName(byte deptNo, String deptName) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Печатает загруженную в ККМ картинку на чеке
     *
     * @throws IOException
     * @throws PortAdapterException
     * @throws ShtrihException
     */
    default void printLogo() throws IOException, PortAdapterException, ShtrihException {
    }

    /**
     * Распечатает указанную строку.
     *
     * @param line
     *            строка, что надо распечатать;
     *            <p/>
     *            если {@link FontLine#getContent() содержимое} == <code>null</code>, то будет распечатана пустая строка
     *            <p/>
     *            если {@link FontLine#getFont() шрифт} == <code>null</code>, то будет использован шрифт по умолчанию
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     * @throws IllegalArgumentException
     *             если аргументневалиден: <code>null</code>
     */
    void printLine(FontLine line) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Открывает нефискальный документ.
     * <p/>
     * NOTE: если команда не поддерживается данной реализацией ФР, то эта ошибка будет просто проигнорирована и метод завершится без исключений. На
     * самом деле очень мало фискальников поддерживают эту команду.
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void openNonFiscalDocument() throws IOException, PortAdapterException, ShtrihException;

    /**
     * закрывает текущий нефискальный документ и заодно "допечатывает" его: печатает подвал и картинки подвала <b>текущего</b> документа, отрезает
     * ленту, и печатает заголовок и картинку заголовка <b>следующего</b> документа.
     * <p/>
     * NOTE: если команда не поддерживается данной реализацией ФР, то эта ошибка будет просто проигнорирована и метод завершится без исключений. На
     * самом деле очень мало фискальников поддерживают эту команду.
     *
     * @param document текущий документ
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void closeNonFiscalDocument(AbstractDocument document) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Печататет суточный отчет БЕЗ гашения.
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void printXReport() throws IOException, PortAdapterException, ShtrihException;

    /**
     * Печататет суточный отчет с гашением.
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void printZReport(Cashier cashier) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Печататет отчет о состояние расчетов
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void printFNReport(Cashier cashier) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Регистрирует внесение на указанную сумму. И заодно печататет шапку следующего документа.
     *
     * @param sum
     *            сумма внесения, в МДЕ
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void regCashIn(long sum) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Регистрирует изъятие указанной суммы. И заодно печататет шапку следующего документа.
     *
     * @param sum
     *            сумма изъятия, в МДЕ
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void regCashOut(long sum) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Открывает смену.
     *
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    void openShift(Cashier cashier) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Вернет регистрационный (РНМ) и заводской номера ФР.
     *
     * @return не {@code null} - в крайнем случае будет {@code Exception}
     * @throws IOException
     *             при возникновении ошибок ввода/вывода
     * @throws PortAdapterException
     *             при других ошибках информационного обмена - когда внятного ответа от внешнего устройства получить не удалось
     * @throws ShtrihException
     *             при возникновении любой ошибки уровня выше Уровня Соединения (Data Link Layer) - см. потомков этой ошибки для понимания того,
     *             какого типа ошибки могут возникнуть
     */
    ShtrihRegNum getRegNum() throws IOException, PortAdapterException, ShtrihException;


    /**
     * Завершение сеанса работы с вневним устройством (ФР).
     */
    void close();

    /**
     * Напечатать чек коррекции
     * @param correctionReceipt сущность из базы с параметрами
     * @param cashier
     * @return номер ФД
     * @throws PortAdapterException
     * @throws ShtrihException
     * @throws IOException
     */
    Optional<Long> printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws PortAdapterException, ShtrihException, IOException;

    /**
     * Отправить ИНН кассира с помощью TLV тега, если версия ФФД 1.05
     */
    void sendCashierInnIfNeeded(String inn) throws PortAdapterException, IOException, ShtrihException;

    /**
     * Вернет информацию о последнем документе из ФН по номеру ФД
     * @param docNumber номер документа
     * @return данные последнего закрытого документа в ФР
     * @throws IOException
     * @throws PortAdapterException
     * @throws ShtrihException
     */
    FiscalDocumentData getLastDocInfo(long docNumber) throws IOException, PortAdapterException, ShtrihException;

    /**
     * Отмена документа в ФН.
     * Если начали формировать документ, но не смогли закончить,
     * при повторе операции Штрих не даст снова начать документ.
     */
    void cancelDocument();

    /**
     * Возвращает номер последней закрытой смены
     */
    int getLastShiftNumber() throws ShtrihException;

    /**
     * Возвращает необнуляемые сменные счетчики
     */
    ShiftNonNullableCounters getShiftNonNullableCounters() throws ShtrihException;

    /**
     * Печатать баркод по заданному формату(работа со статусами кассиров)
     * @param barcode ШК для печати
     */
    default void printBarcodeBlock(BarCode barcode) {
        //в данный момент реализованно только в коннекторе через драйвере штриха, который будет работать в глобусе (https://crystals.atlassian.net/browse/SRTB-3311)
    }

    /**
     * Сформировать информацию о ККТ для отправки на сервер
     */
    default FiscalPrinterInfo getFiscalPrinterInfo() throws ShtrihException {
        return null;
    }

    /**
     * Для указанного "нашего" шрифта вернет код соответсвующего ему шрифта ФР
     * @param font наш шрифт
     * @return код шрифта ФР (1..7); если что пойдет не так - вернет <code>1</code> - значение шрифта по умолчанию
     */
    default byte getFontSize(Font font) {
        switch (font) {
            case DOUBLEHEIGHT:
                return 2;
            case SMALL:
                return 3;
            case DOUBLEWIDTH:
                return 4;
            case NORMAL:
            case UNDERLINE:
            default:
                return 1;
        }
    }

    default FiscalMarkValidationResult validateMarkCode(PositionEntity position, MarkData markData, boolean isSale)
            throws ShtrihException {
        return null;
    }

    int getVersionFFD();

    default void updateVersionFFD() throws ShtrihException {
    }
}
