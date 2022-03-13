package ru.crystals.pos.fiscalprinter.az.airconn.commands.authorized;

import ru.crystals.pos.fiscalprinter.az.airconn.commands.BaseCommand;
import ru.crystals.pos.fiscalprinter.az.airconn.model.requests.AccessToken;
import ru.crystals.pos.fiscalprinter.az.airconn.model.responses.BaseResponse;

/**
 * Абстарктный базовый класс формирования команды единственным параметром которой является сессионный ключ доступа {@link AccessToken}.
 *
 * @param <RD> Тип данных ожидаемых в ответе {@link BaseResponse} на команду
 */
public abstract class AuthorizedBaseCommand<RD> extends BaseCommand<AccessToken, RD> {

    public void setAccessToken(String accessToken) {
        if (getParameters() == null) {
            setParameters(new AccessToken());
        }
        getParameters().setAccessToken(accessToken);
    }
}
