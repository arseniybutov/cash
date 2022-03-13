package ru.crystals.pos.bank.ucs.messages.responses;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.fest.assertions.Assertions.assertThat;

public class AuthorizationResponseTest {
    @Test
    public void parseAuthorizationResponseTest() throws ParseException {
        //when
        Response response = ResponseFactory.parse(
            "6000199998457700000001000006432013110716593900000990999984533111600032100871341\u001B************9915\u001B*** VISA ***\u001BКОД АВТОРИЗАЦИИ:871341");
        AuthorizationResponse authorizationResponse = (AuthorizationResponse) response;
        //then
        assertThat(authorizationResponse.getTransactionSum()).isEqualTo(100000L);
        assertThat(authorizationResponse.getTransactionCurrency()).isEqualTo("643");
        assertThat(authorizationResponse.getTerminalId()).isEqualTo("0019999845");
        assertThat(authorizationResponse.getOperationType()).isEqualTo("0");
        assertThat(authorizationResponse.getDate()).isEqualTo(new SimpleDateFormat("yyyyMMddHHmmss").parse("20131107165939"));
        assertThat(authorizationResponse.getMerchantId()).isEqualTo("000009909999845");
        assertThat(authorizationResponse.getUniqueReferenceNumber()).isEqualTo("331116000321");
        assertThat(authorizationResponse.getResponseCode()).isEqualTo("00");
        assertThat(authorizationResponse.isSuccessful()).isTrue();
        assertThat(authorizationResponse.getConfirmationCode()).isEqualTo("871341");
        assertThat(authorizationResponse.getCardDetails()).isEqualTo("************9915");
        assertThat(authorizationResponse.getCardType()).isEqualTo("*** VISA ***");
        assertThat(authorizationResponse.getMessage()).isEqualTo("КОД АВТОРИЗАЦИИ:871341");
    }
}
