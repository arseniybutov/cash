package ru.crystals.pos.bank.translink.api.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import ru.crystals.pos.bank.translink.api.ApiMethod;

import java.util.Objects;

/**
 * Тело запроса метода {@link ApiMethod#openpos}
 */
public class OpenPosRequest {
    /**
     * Идентификатор / ключ лицензии для использования данного API
     */
    private final String licenseToken;
    /**
     * Идентификатор эквайринга в многотерминальной интеграционной среде
     */
    private final String alias;
    /**
     * Имя пользователя (оператора ECR)
     */
    private final String userName;
    /**
     * Пароль (оператора ECR)
     */
    private final String password;

    public OpenPosRequest(String licenseToken, String alias, String userName, String password) {
        this.licenseToken = Objects.requireNonNull(licenseToken);
        this.alias = alias;
        this.userName = userName;
        this.password = password;
    }

    @JsonGetter("licenseToken")
    public String getLicenseToken() {
        return licenseToken;
    }

    @JsonGetter("alias")
    public String getAlias() {
        return alias;
    }

    @JsonGetter("userName")
    public String getUserName() {
        return userName;
    }

    @JsonGetter("password")
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenPosRequest that = (OpenPosRequest) o;
        return Objects.equals(licenseToken, that.licenseToken) &&
                Objects.equals(alias, that.alias) &&
                Objects.equals(userName, that.userName) &&
                Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(licenseToken, alias, userName, password);
    }

    @Override
    public String toString() {
        return "OpenPosRequest{" +
                "licenseToken='" + licenseToken + '\'' +
                ", alias='" + alias + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
