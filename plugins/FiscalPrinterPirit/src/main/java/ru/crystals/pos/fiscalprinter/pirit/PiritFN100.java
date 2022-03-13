package ru.crystals.pos.fiscalprinter.pirit;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import ru.crystals.comportemulator.pirit.PiritCommand;
import ru.crystals.pos.annotation.PrototypedComponent;
import ru.crystals.pos.catalog.mark.FiscalMarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkData;
import ru.crystals.pos.catalog.mark.MarkValidationResult;
import ru.crystals.pos.catalog.mark.MarkValidationStatus;
import ru.crystals.pos.check.CorrectionReceiptEntity;
import ru.crystals.pos.check.CorrectionReceiptPaymentsEntity;
import ru.crystals.pos.check.CorrectionReceiptTaxesEntity;
import ru.crystals.pos.check.PositionEntity;
import ru.crystals.pos.currency.CurrencyUtil;
import ru.crystals.pos.fiscalprinter.FfdVersion;
import ru.crystals.pos.fiscalprinter.FiscalMarkValidationUtil;
import ru.crystals.pos.fiscalprinter.ValueAddedTax;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AdditionalInfo;
import ru.crystals.pos.fiscalprinter.datastruct.documents.AgentType;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Cashier;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Check;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Goods;
import ru.crystals.pos.fiscalprinter.datastruct.documents.Payment;
import ru.crystals.pos.fiscalprinter.datastruct.documents.PaymentType;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.pirit.core.ExtraRequisiteTypes;
import ru.crystals.pos.fiscalprinter.pirit.core.ResBundleFiscalPrinterPirit;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.ExtendedCommand;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritAgentFN100;
import ru.crystals.pos.fiscalprinter.pirit.core.connect.PiritErrorMsg;
import ru.crystals.pos.fiscalprinter.pirit.util.PiritMode;
import ru.crystals.pos.fiscalprinter.transport.DataPacket;
import ru.crystals.pos.utils.CheckUtils;
import ru.crystals.utils.time.DateConverters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@PrototypedComponent
public class PiritFN100 extends PiritFN {

    private static Pattern SPLIT_PATTERN = Pattern.compile("(?<=\\G.{2})");
    /**
     * Разделитель в "сырой" марке
     */
    private static final String GS = "$1d";

    public static final Map<String, FfdVersion> FFD_VERSION_BY_CODE = ImmutableMap.of(
            "2", FfdVersion.FFD_1_05,
            "4", FfdVersion.FFD_1_2
    );

    /**
     * Класс который хранит все возможные режимы работы, заполняется 1 раз при старте модуля.
     */
    private PiritMode piritMode;

    private FfdVersion ffdVersion = FfdVersion.UNKNOWN;

    @Override
    public void start() throws FiscalPrinterException {
        super.start();
        pa = new PiritAgentFN100(pc);
        for (TaxSystem ts : TaxSystem.values()) {
            long value = readTaxSystem();

            if ((value & ts.flag) != 0) {
                taxSystem = ts.ordinal();
                break;
            }
        }

        readPiritMode();
        ffdVersion = FFD_VERSION_BY_CODE.getOrDefault(pa.getVersionFN().getVerFfd(), FfdVersion.UNKNOWN);

        if (taxSystem == -1) {
            throw new FiscalPrinterException("Tax system was not found");
        }
    }

    @Override
    public long openShift(Cashier cashier) throws FiscalPrinterException {
        setParamsBeforeOpenShift();
        return super.openShift(cashier);
    }

    private void setParamsBeforeOpenShift() {
        if (ffdVersion.isBefore_1_2()) {
            return;
        }
        int effectiveTimeoutValue = Optional.ofNullable(config.getOismTimeout()).orElse(oismTimeout);
        if (effectiveTimeoutValue != -1) {
            piritConfig.setOismTimeout(effectiveTimeoutValue);
        }
    }

    @Override
    public void putPayments(List<Payment> payments, boolean isAsyncMode) throws Exception {
        //группировка по типу оплат и сортировка что бы 0-тип оплаты(наличные) был последним
        for (Payment payment : CheckUtils.reduceAndSortPaymentsByIndexPaymentFDD(payments)) {
            DataPacket dp = new DataPacket();

            dp.putLongValue(payment.getIndexPaymentFDD100());
            //сумма всех оплат одного типа
            dp.putDoubleValue(CurrencyUtil.convertMoney(payment.getSum()).doubleValue());
            dp.putStringValue("");

            pc.sendRequest(PiritCommand.ADD_PAYMENT, dp, isAsyncMode);
        }
    }

    @Override
    public Optional<FiscalMarkValidationResult> validateMarkCode(PositionEntity position, MarkData markData, boolean isSale) throws FiscalPrinterException {
        if (ffdVersion.isBefore_1_2()) {
            return Optional.empty();
        }
        String concatenatedMark = markData.getParser().concatMark(markData, GS);
        int tag2108 = FiscalMarkValidationUtil.formTag2108(position);
        int tag2003 = FiscalMarkValidationUtil.formTag2003(tag2108, isSale);

        DataPacket dp = new DataPacket();
        //Входные параметры
        //(Целое число) Номер запроса = 1
        //(Строка)[0..128] Код маркировки
        dp.putStringValue(concatenatedMark);
        //(Целое число) Режим обработки кода маркировки = 0
        dp.putIntValue(0);
        //(Целое число) Планируемый статус товара(тег 2003)
        dp.putIntValue(tag2003);
        //(Целое число) Количество товара (тег 1023) - всегда единица для маркированного товара
        dp.putIntValue(1);
        //(Целое число) Мера количества (тег 2108)
        dp.putIntValue(tag2108);
        //(Целое число) Режим работы (1 - проверять в ОИСМ даже если проверка в ФН прошла с отрицательными результатом)
        dp.putIntValue(1);

        DataPacket resp = pc.sendRequest(ExtendedCommand.VALIDATE_MARK_CODE, dp);
        FiscalMarkValidationResult markCodeValidationResult;
        try {
            int tag2106 = (int) resp.getLongValue(1);
            int fnCheckReason = (int) resp.getLongValue(2);
            Integer tag2005 = resp.getIntegerSafe(3).orElse(null);
            Integer tag2105 = resp.getIntegerSafe(4).orElse(null);
            Integer tag2109 = resp.getIntegerSafe(5).orElse(null);

            Map<String, Object> input = ImmutableMap.of(
                    FiscalMarkValidationUtil.MARK_KEY, concatenatedMark,
                    FiscalMarkValidationUtil.TAG_2003_KEY, tag2003,
                    FiscalMarkValidationUtil.TAG_2108_KEY, tag2108
            );

            markCodeValidationResult = new FiscalMarkValidationResult(input, tag2106, tag2005, tag2105, tag2109);
            FiscalMarkValidationUtil.logFnCheckReason(LOG, fnCheckReason);

            // Подтверждаем добавление в чек кода маркировки, сразу после валидации.
            // В дальнейшем, возможно, будем вызывать эту команду в другом месте, а также исключать марку из чека.
            confirmMarkCodeAddToDoc();
        } catch (Exception e) {
            throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("MARK_VALIDATION_ERROR"), e);
        }

        return Optional.of(markCodeValidationResult);
    }

    private void confirmMarkCodeAddToDoc() throws Exception {
        DataPacket dp = new DataPacket();
        // Принятие КМ для включение в документ
        dp.putIntValue(1);
        pc.sendRequest(ExtendedCommand.CONFIRM_MARK_CODE_ADD_TO_DOC, dp);
    }

    private void registerMarkCode(Goods position) throws FiscalPrinterException {
        if (ffdVersion.isBefore_1_2()) {
            return;
        }
        final MarkValidationResult validationResult = position.getMarkValidationResult();
        if (validationResult == null) {
            throw new IllegalStateException("Unexpected missing mark code check result for FFD 1.2 device");
        }
        if (validationResult.getStatus() == MarkValidationStatus.NOT_SUPPORTED_KKT) {
            LOG.warn("Not supported validation status on register position");
            return;
        }
        final FiscalMarkValidationResult fiscalResult = validationResult.getFiscalResult();
        if (fiscalResult == null) {
            throw new IllegalStateException("Unexpected missing mark code check result for FFD 1.2 device");
        }
        DataPacket dp = new DataPacket();
        dp.putStringValue((String) fiscalResult.getInput().get(FiscalMarkValidationUtil.MARK_KEY));
        // проставляем в 2110 "присвоенный статус товара" значение из 2003 "предполагаемый статус товара"
        dp.putIntValue((Integer) fiscalResult.getInput().get(FiscalMarkValidationUtil.TAG_2003_KEY));
        // режим обработки КМ, значение всегда 0
        dp.putIntValue(0);
        dp.putIntValue(fiscalResult.getTag2106());
        dp.putIntValue((Integer) fiscalResult.getInput().get(FiscalMarkValidationUtil.TAG_2108_KEY));

        pc.sendRequest(ExtendedCommand.REGISTER_MARK_CODE_WITH_POSITION, dp);
    }

    @Override
    public void putGoods(List<Goods> goods, boolean isAsyncMode) throws Exception {
        int posNum = 0;

        for (Goods good : goods) {
            if (StringUtils.isNotBlank(good.getExcise())) {
                registerMarkCode(good);
            }

            DataPacket dp = new DataPacket();

            putAdditionalInfo(good, isAsyncMode);

            if (config.isPrintGoodsName()) {
                if (good.getName() == null) {
                    dp.putStringValue(defaultGoodsName);
                } else {
                    // ограничение на длину названия товара (см. док-цию команда 42)
                    if (good.getName().length() > config.getGoodNameMaxLength()) {
                        dp.putStringValue(good.getName().substring(0, config.getGoodNameMaxLength()));
                    } else {
                        dp.putStringValue(good.getName());
                    }
                }
            } else {
                dp.putStringValue("");
            }

            if (config.isPrintItem()) {
                // ограничение на длину итема (см. док-цию команда 42)
                if (good.getItem().length() > ITEM_MAX_LENGTH) {
                    dp.putStringValue(good.getItem().substring(0, ITEM_MAX_LENGTH));
                } else {
                    dp.putStringValue(good.getItem());
                }
            } else {
                dp.putStringValue("");
            }

            long calculatedEndPrice = good.getEndPricePerUnit();
            long calculatedDiscount = CurrencyUtil.getPositionSum(good.getEndPricePerUnit(), good.getQuant()) - good.getEndPositionPrice();
            if (ffdVersion == FfdVersion.FFD_1_2 && good.getExcise() != null) {
                // TODO сделать покрасивше: если мы при проверке 79/1 всегда передаем 1, то и при добавлении такого товара в чек тоже должны передать 1
                dp.putDoubleValue(1);
            } else {
                dp.putDoubleValue((double) good.getQuant() / COUNT_ORDER);
            }
            dp.putDoubleValue(CurrencyUtil.convertMoney(calculatedEndPrice).doubleValue());

            ValueAddedTax tax = taxes.lookupByValue(good.getTax());
            if (tax == null) {
                throw new FiscalPrinterException(ResBundleFiscalPrinterPirit.getString("ERROR_TAX_VALUE"), PiritErrorMsg.getErrorType());
            }
            dp.putLongValue(tax.index);

            if (config.isPrintPosNum()) {
                dp.putStringValue(String.format("%3d ", ++posNum));
            } else {
                dp.putStringValue("");
            }

            dp.putLongValue(0L);
            if (calculatedDiscount > 0) {
                dp.putLongValue(2L);
                setTag2108(dp, good);
                dp.putDoubleValue(CurrencyUtil.convertMoney(calculatedDiscount).doubleValue());
            } else if (calculatedDiscount < 0) {
                dp.putLongValue(4L);
                setTag2108(dp, good);
                dp.putDoubleValue(CurrencyUtil.convertMoney(Math.abs(calculatedDiscount)).doubleValue());
            } else {
                dp.putLongValue(0L);
                setTag2108(dp, good);
                dp.putLongValue(0L);
            }

            //  Признак способа расчета
            dp.putIntValue(good.getCalculationMethod());

            // Признак предмета расчета
            dp.putIntValue(good.getCalculationSubject());

            if (canSendJuristicData()) {
                // Код страны происхождения товара
                dp.putStringValue(good.getRccw());

                // Номер таможенной декларации
                dp.putStringValue(good.getGtdNumber());
            }

            pc.sendRequest(PiritCommand.ADD_ITEM, dp, isAsyncMode);
        }
    }

    private void setTag2108(DataPacket dp, Goods good) {
        if (ffdVersion.isBefore_1_2() || good.getExcise() != null) {
            // для версий раньше 1.2 не будем ничего передавать
            // для маркированного товара начиная с 1.2 тег 2108 будет передан в 79/15
            dp.putStringValue(null);
        } else {
            dp.putIntValue(FiscalMarkValidationUtil.formTag2108(good));
        }
    }

    protected boolean isCodingMarkAvailable(Goods good) {
        return StringUtils.isNotEmpty(good.getExcise()) && canExtractExtendedVersionInfo();
    }

    protected boolean isOfdCodeAvailable(Goods good) {
        return good.isOfdCodeMandatory() && StringUtils.isNotBlank(good.getOfdCode()) && canExtractExtendedVersionInfo();
    }

    private void putCodingMark(Goods good, DataPacket dp) {
        if (ffdVersion == FfdVersion.FFD_1_2) {
            // В ФФД 1.2 мы не передаем через эту команду марки (они идут через 79 команды), а остальные КТН передаем как есть
            // (но добавляем @ - защита Пирита от последствий передачи сюда марок старыми софтами)
            if (isOfdCodeAvailable(good)) {
                dp.putStringValue("@" + good.getOfdCode());
            } else {
                String additionalInfo = Optional.ofNullable(good.getAdditionalInfo())
                        .map(AdditionalInfo::getNomenclatureCode)
                        .filter(s -> !s.isEmpty())
                        .map(nc -> "@" + nc)
                        .orElse(null);
                dp.putStringValue(additionalInfo);
            }
            return;
        }
        if (isCodingMarkAvailable(good)) {
            dp.putStringValue(codeMark(good));
        } else if (isOfdCodeAvailable(good)) {
            dp.putStringValue(codeRawOfdCode(good));
        } else {
            AdditionalInfo additionalInfo = good.getAdditionalInfo();
            dp.putStringValue(additionalInfo == null ? null : additionalInfo.getNomenclatureCode());
        }
    }

    /**
     * Отправка дополнительных реквизитов в ОФД
     */
    protected void putAdditionalInfo(Goods good, boolean isAsyncMode) throws FiscalPrinterException {
        DataPacket dp = new DataPacket();
        putCodingMark(good, dp);

        dp.putStringValue(good.getTag1191());

        AdditionalInfo additionalInfo = good.getAdditionalInfo();
        if (additionalInfo != null && piritMode.isAgentAvailable(additionalInfo.getAgentType())) {
            dp.putStringValue(additionalInfo.getMeasurement());
            //В agentSign хранится номер бита, подвигаем.
            dp.putLongValue((long) additionalInfo.getAgentType().getBitMask());
            dp.putStringValue(additionalInfo.getDebitorINN());
            dp.putStringValue(additionalInfo.getDebitorPhone());
            dp.putStringValue(additionalInfo.getDebitorName());
            dp.putStringValue(additionalInfo.getTransferOperatorAddress());
            dp.putStringValue(additionalInfo.getTransferOperatorInn());
            dp.putStringValue(additionalInfo.getTransferOperatorName());
            dp.putStringValue(additionalInfo.getTransferOperatorPhone());
            dp.putStringValue(additionalInfo.getPaymentAgentOperation());
            dp.putStringValue(additionalInfo.getPaymentAgentPhone());
            dp.putStringValue(additionalInfo.getReceivePaymentsAgentPhone());
        }

        if (dp.hasNonNullValues()) {
            pc.sendRequest(PiritCommand.ADDITIONAL_REQUISITES, dp, isAsyncMode);
        }
    }

    /**
     * В ФНД необходимо передать строку вида $xx$xx$yy$...$yySERIAL
     * где x - код товара
     * y - GTIN переведенный в hex
     * SERIAL - серийный номер(его через баксы ненадо)
     */
    private String codeMark(Goods good) {
        StringBuilder builder = new StringBuilder();

        for (String s : SPLIT_PATTERN.split(good.getMarkCode())) {
            builder.append('$').append(s);
        }

        String hexGtin = good.getMarkEanAsHex();

        for (String s : SPLIT_PATTERN.split(hexGtin)) {
            builder.append('$').append(s);
        }

        builder.append(good.getSerialNumber());
        if (good.getMarkMrp() != null) {
            builder.append(good.getMarkMrp());
        }

        return builder.toString();
    }

    /**
     * Кодирует код немаркированного товара для передачи в ОФД (CR-4125)
     * Передаем строку вида $45$0D$yy$...$yy
     * y - код товара переведенный в hex
     */
    private String codeRawOfdCode(Goods good) {
        StringBuilder builder = new StringBuilder();

        for (String s : SPLIT_PATTERN.split(Goods.RAW_OFD_CODE_PREFIX)) {
            builder.append('$').append(s);
        }

        String hexOfdCode = good.getRawOfdCodeAsHex();

        for (String s : SPLIT_PATTERN.split(hexOfdCode)) {
            builder.append('$').append(s);
        }

        return builder.toString();
    }


    @Override
    public void setPayments(List<PaymentType> payments) throws FiscalPrinterException {
        try {
            piritConfig.setPaymentsFFD100(payments);
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isFFDDevice() {
        return true;
    }

    @Override
    public FfdVersion getFfdVersion() {
        return ffdVersion;
    }

    @Override
    public Optional<Long> printCorrectionReceipt(CorrectionReceiptEntity correctionReceipt, Cashier cashier) throws FiscalPrinterException {
        try {

            DataPacket dp = new DataPacket();

            dp.putStringValue(getCashierName(cashier));

            //сумма наличного платежа
            BigDecimal cashSumTmp = new BigDecimal("0.0");
            //сумма электронного платежа
            BigDecimal electronSumTmp = new BigDecimal("0.0");
            //сумма предоплатой
            BigDecimal prepaidSumTmp = new BigDecimal("0.0");
            // сумма постоплатой
            BigDecimal postpaidSumTmp = new BigDecimal("0.0");
            // сумма встречным представлением
            BigDecimal vstSumTmp = new BigDecimal("0.0");

            for (CorrectionReceiptPaymentsEntity entity : correctionReceipt.getPayments()) {
                BigDecimal paymentSum = CurrencyUtil.convertMoney(entity.getPaymentSum());
                switch (entity.getCorrectionReceiptPaymentsEntityPK().getPaymentName()) {
                    case CASH:
                        cashSumTmp = paymentSum;
                        break;
                    case ELECTRON:
                        electronSumTmp = paymentSum;
                        break;
                    case PREPAYMENT:
                        prepaidSumTmp = paymentSum;
                        break;
                    case POSTPAY:
                        postpaidSumTmp = paymentSum;
                        break;
                    case COUNTEROFFER:
                        vstSumTmp = paymentSum;
                        break;
                    default:
                        break;
                }
            }

            dp.putBigDecimalValue(cashSumTmp);
            dp.putBigDecimalValue(electronSumTmp);
            dp.putBigDecimalValue(prepaidSumTmp);
            dp.putBigDecimalValue(postpaidSumTmp);
            dp.putBigDecimalValue(vstSumTmp);

            //5 бит всегда еденица 2^5 = 32
            long corType =
                    correctionReceipt.getCorrectionType().ordinal() | correctionReceipt.getAccountSign().ordinal() << 1 | correctionReceipt.getTaxSystem().ordinal() << 2 | 32;

            dp.putLongValue(corType);

            dp.putDateValue(DateConverters.toLocalDate(correctionReceipt.getReasonDocDate()));
            dp.putStringValue(correctionReceipt.getReasonDocNumber());
            dp.putStringValue(correctionReceipt.getReason());

            //сумма налога по ставке 20%
            BigDecimal tax20 = new BigDecimal("0.0");
            //сумма налога по ставке 10%
            BigDecimal tax10 = new BigDecimal("0.0");
            //сумма расчета по ставке 0%
            BigDecimal tax0 = new BigDecimal("0.0");
            //сумма расчета без налога
            BigDecimal taxminus1 = new BigDecimal("0.0");
            //сумма расчета по расчетной ставке 20/120
            BigDecimal tax20120 = new BigDecimal("0.0");
            //сумма расчета по расчетной ставке 10/110
            BigDecimal tax10110 = new BigDecimal("0.0");

            for (CorrectionReceiptTaxesEntity entity : correctionReceipt.getTaxes()) {
                BigDecimal taxSum = CurrencyUtil.convertMoney(entity.getTaxSum());
                switch (entity.getCorrectionReceiptTaxesEntityPK().getTax()) {
                    case TAX_20:
                        tax20 = taxSum;
                        break;
                    case TAX_10:
                        tax10 = taxSum;
                        break;
                    case TAX_0:
                        tax0 = taxSum;
                        break;
                    case TAX_NONDS:
                        taxminus1 = taxSum;
                        break;
                    case TAX_20_120:
                        tax20120 = taxSum;
                        break;
                    case TAX_10_110:
                        tax10110 = taxSum;
                        break;
                    default:
                        LOG.error("unknown tax");
                }
            }
            dp.putBigDecimalValue(tax20);
            dp.putBigDecimalValue(tax10);
            dp.putBigDecimalValue(tax0);
            dp.putBigDecimalValue(taxminus1);
            dp.putBigDecimalValue(tax20120);
            dp.putBigDecimalValue(tax10110);

            pc.sendRequest(PiritCommand.PRINT_CORRECTION_RECEIPT, dp, false);

            return Optional.of(getLastKpk());
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    /**
     * Информация о режимах работы пирита хранятся в команде 0x02/23
     * Метод выставляет значения поля в классе PiritMode.
     */
    private void readPiritMode() throws FiscalPrinterException {
        try {
            DataPacket dp = pc.sendRequest(ExtendedCommand.GET_INFO_TAX_WORK_MODE);
            piritMode = new PiritMode(dp.getLongValue(2));
        } catch (FiscalPrinterException fpe) {
            throw fpe;
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage(), e);
        }
    }

    @Override
    public void putCheckRequisites(Check check) throws FiscalPrinterException {
        putCheckAgentSign(check);
    }

    @Override
    protected void putCloseDocumentData(Check check, DataPacket dp) throws FiscalPrinterException {
        // Адрес покупателя (Тег 1008)
        dp.putStringValue(check.getClientRequisites());
        // "Разные флаги"
        dp.putLongValue(null);
        // Места расчетов (Тег 1187)
        dp.putStringValue(null);
        // Адрес отправителя чеков (Тег 1117)
        dp.putStringValue(null);
        // Номер автомата (Тег 1036)
        dp.putStringValue(null);

        // Название дополнительного реквизита пользователя (Тег 1085)
        // Значение дополнительного реквизита пользователя (Тег 1086)
        switch (pluginProvider.getCustomerExtraRequisiteType()) {
            case CASH_NUMBER:
                dp.putStringValue(ResBundleFiscalPrinterPirit.getString(ExtraRequisiteTypes.CASH_NUMBER.getNameKey()));
                dp.putStringValue(String.valueOf(check.getCashNumber()));
                break;
            case EMPTY:
            default:
                dp.putStringValue(null);
                dp.putStringValue(null);
                break;
        }

        // Покупатель (Тег 1227)
        dp.putStringValue(cutClientNameIfNeeded(check.getClientName()));
        // ИНН покупателя (Тег 1228)
        if (canSendJuristicData()) {
            dp.putStringValue(check.getClientINN());
        } else {
            dp.putStringValue(null);
        }
        // (Дата в формате ddmmyyyy) Дата рождения покупателя (тег 1243)
        dp.putStringValue(null);
        // Гражданство (тег 1244)
        dp.putIntValue(null);
        // Код вида документа, удостоверяющего личность (тег 1245)
        dp.putIntValue(null);
        // Данные документа, удостоверяющего личность (Тег 1246)
        dp.putStringValue(null);
        // Адрес покупателя (клиента), географический адрес, не email (Тег 1254)
        dp.putStringValue(check.getClientAddress());
    }

    /**
     * Добавление реквизита "Признак агента" (1057) в чек
     */
    private void putCheckAgentSign(Check check) throws FiscalPrinterException {
        final AgentType singleAgentType = check.getSingleAgentType();
        if (singleAgentType == null) {
            return;
        }
        LOG.info("Purchase has all positions with same agent type {}", singleAgentType);
        if (!piritMode.isAgentAvailable(singleAgentType)) {
            LOG.warn("KKT not registered with support of agent type {}", singleAgentType);
            return;
        }
        if (ffdVersion.isBefore_1_2()) {
            putOFDRequisite(1057, singleAgentType.getBitMask());
        }
    }

    private void putOFDRequisite(long tag, int intValue) throws FiscalPrinterException {
        LOG.info("Adding tag {} with integer value {}", tag, intValue);
        putOFDRequisiteInner(tag, 0b0100_0000L, "#" + intValue);
    }

    private void putOFDRequisiteInner(long tag, long fontAttribute, String value) throws FiscalPrinterException {
        // Если при установленном бите 6 первым символом значения реквизита является '#',
        // то атрибут рассматривается, как целое число, иначе - как денежная сумма.
        DataPacket dp = new DataPacket();
        dp.putLongValue(tag);
        dp.putLongValue(fontAttribute);
        dp.putStringValue(" ");
        dp.putStringValue(value);
        pc.sendRequest(PiritCommand.PRINT_OFD_REQUISITE, dp, true);
    }

    @Override
    public void clearBeforeMarkRevalidation() throws FiscalPrinterException {
        pc.sendRequest(PiritCommand.CANCEL_DOCUMENT);
        pc.sendRequest(ExtendedCommand.CLEAR_FN_MARK_BUFFER);
    }
}
