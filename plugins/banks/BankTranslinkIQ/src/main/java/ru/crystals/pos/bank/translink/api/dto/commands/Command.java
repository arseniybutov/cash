package ru.crystals.pos.bank.translink.api.dto.commands;

import ru.crystals.pos.bank.translink.api.dto.Result;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Command {
    /**
     * Команда выполняет разблокировку POS, для выполнения последующих операций. Данная команда должна выполняться перед началом любой операцией расчёта,
     * инициированной с ECR.
     */
    UNLOCKDEVICE(UnlockDeviceCommand.class),
    /**
     * Команда выполняет блокировку POS, с возможностью отображения на экране сообщения, переданного в параметре idleText.
     */
    LOCKDEVICE(LockDeviceCommand.class),
    /**
     * Команда инициирует процедуру авторизации (оплата покупок) на POS.
     */
    AUTHORIZE(AuthorizeCommand.class),
    /**
     * Команда инициирует на POS процедуру возврата средств на счета карты
     */
    REFUND(RefundCommand.class),
    /**
     * Отмена/аннулирование ранее авторизованной карточной транзакции.  Данная операция возможна в течение банковского дня.
     */
    VOID(VoidCommand.class),
    /**
     * Частичная отмена ранее авторизованной карточной операции.
     */
    VOIDPARTIAL(VoidPartialCommand.class),
    /**
     * Команда инициирует процедуру закрытия финансового дня на POS.
     */
    CLOSEDAY(CloseDayCommand.class),
    /**
     * Каждая операция инициированная из ECR должна подтверждаться вызовом данного метода. При вызове данного метода POS может инициировать создание дополнительных
     * квитанций, для получения которых будет сгенерировано событие ONPRINT.
     */
    CLOSEDOC(CloseDocCommand.class),
    /**
     * Команда инициирует создание отчета транзакций на POS. Если в терминале присутствует интегрированный принтер,
     * печать отчета будет выполняться на принтере POS, в противном случае будет инициировано событие ONPRINT, в котором будет передан текст отчёта.
     */
    PRINTTOTALS(PrintTotalsCommand.class),
    /**
     * Команда выполняет запрос о статусе карточной транзакции.
     */
    GETTRNSTATUS(GetTrnStatusCommand.class),
    /**
     * Команда используется для информирования через устройство POS держателя карты, о том, что данная карта не обслуживается. Команда инициирует отображение
     * информационного сообщения для держателя карты, в котором указывается причина, из-за которой данная карта не обслуживается.
     */
    REMOVECARD(RemoveCardCommand.class),
    /**
     * Команда возвращает состояние POS.
     */
    GETPOSSTATUS(null),
    /**
     * Оплата в рассрочку
     * <p>
     * The command triggers the consumer credit procedure for the payment transaction in progress. To prepare the terminal for this operation, UNLOCKDEVICE with
     * Operation code 0 (NOOPERATION) with sum 0 must be sent
     */
    INSTALLMENT(InstallmentCommand.class);

    private static final Map<Class<? extends CommandParams>, Command> MAPPED_BY_PARAM_CLASS = Stream.of(values())
            .filter(cmd -> Objects.nonNull(cmd.paramClass))
            .collect(Collectors.toMap(Command::getParamClass, Function.identity()));

    private final Class<? extends CommandParams> paramClass;

    private final Class<?> resultClass;

    Command(Class<? extends CommandParams> paramClass) {
        this(paramClass, Result.class);
    }

    Command(Class<? extends CommandParams> paramClass, Class<?> resultClass) {
        this.paramClass = paramClass;
        this.resultClass = resultClass;
    }

    public Class<? extends CommandParams> getParamClass() {
        return paramClass;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }

    public static Command getByParamClass(Class<? extends CommandParams> paramsClass) {
        return MAPPED_BY_PARAM_CLASS.get(paramsClass);
    }

    public void checkApplicableParams(CommandParams params) {
        if (paramClass == null && params == null) {
            return;
        }
        if (paramClass == null) {
            throw new IllegalArgumentException(String.format("Command %s doesn't expect parameters", name()));
        }
        if (params == null || paramClass != params.getClass()) {
            throw new IllegalArgumentException(String.format("Command %s expects parameters %s", name(), paramClass));
        }

    }
}
