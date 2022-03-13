package ru.crystals.pos.bank.translink.api;

import org.junit.Assert;
import org.junit.Test;
import ru.crystals.pos.bank.translink.api.dto.OpenPosRequest;
import ru.crystals.pos.bank.translink.api.dto.PosOperation;
import ru.crystals.pos.bank.translink.api.dto.commands.AuthorizeCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.CloseDayCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.CloseDocCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.Command;
import ru.crystals.pos.bank.translink.api.dto.commands.CommandParams;
import ru.crystals.pos.bank.translink.api.dto.commands.GetTrnStatusCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.InstallmentCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.InstallmentProvider;
import ru.crystals.pos.bank.translink.api.dto.commands.LockDeviceCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.PrintTotalsCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.RefundCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.RemoveCardCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.UnlockDeviceCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.VoidCommand;
import ru.crystals.pos.bank.translink.api.dto.commands.VoidPartialCommand;

import java.time.LocalDateTime;
import java.time.Month;


public class TranslinkRequestsTest extends TranslinkJsonConverterTest {

    @Test
    public void openPosRequest() {
        checkSerialization("requests/openpos", new OpenPosRequest("e034d5a6cf3212826c57f35cffb103905afe5936/f86419d06688b6336ddfe68dc00c214b9b83fb10",
                "POS1", "login", "password"));
    }

    @Test
    public void authorizeCommand() {
        checkCommandSerialization(Command.AUTHORIZE, AuthorizeCommand.builder()
                .amount(1000)
                .cashBackAmount(0)
                .currencyCode("643")
                .documentNr("129663")
                .panL4Digit("8261")
                .build());
    }

    @Test
    public void installmentCommand() {
        checkCommandSerialization(Command.INSTALLMENT, InstallmentCommand.builder()
                .amount(1000)
                .installmentPaymentCount(4)
                .installmentProvider(InstallmentProvider.MERCHANT)
                .currencyCode("643")
                .documentNr("129663")
                .panL4Digit("8261")
                .build());
    }

    @Test
    public void closeDayCommand() {
        checkCommandSerialization(Command.CLOSEDAY, new CloseDayCommand("оператор", "Имя, фамилия"));
    }

    @Test
    public void closeDocCommand() {
        checkCommandSerialization(Command.CLOSEDOC, new CloseDocCommand("A0000000041010", "129663"));
    }

    @Test
    public void getPosStatusCommand() {
        checkCommandSerialization(Command.GETPOSSTATUS, null);
    }

    @Test
    public void getTrnStatusCommand() {
        checkCommandSerialization(Command.GETTRNSTATUS, new GetTrnStatusCommand("A0000000041010", "129663",
                "4E6998DF9D9CD4CF70F90BA05160ED7D890F3134D8FA6C162FDF5B6C363085B0275AEC227F17DB99A45EB11C61925301D" +
                        "BEB71D759AA93925A9F382CD931309BF5864922B378AEEB4F6B0C0CC6C58EC"));
    }

    @Test
    public void lockDeviceCommand() {
        checkCommandSerialization(Command.LOCKDEVICE, new LockDeviceCommand("Вставьте карту"));
    }

    @Test
    public void printTotalsCommand() {
        checkCommandSerialization(Command.PRINTTOTALS, new PrintTotalsCommand("оператор", "Имя, фамилия"));
    }

    @Test
    public void refundCommand() {
        checkCommandSerialization(Command.REFUND, RefundCommand.builder()
                .amount(1000)
                .currencyCode("643")
                .documentNr("129663")
                .panL4Digit("8261")
                .stan("8261")
                .rrn("933315462707")
                .time(LocalDateTime.of(2012, Month.FEBRUARY, 12, 15, 12, 11))
                .build());
    }

    @Test
    public void removeCardCommand() {
        checkCommandSerialization(Command.REMOVECARD, new RemoveCardCommand("Данная карта не обслуживается"));
    }

    @Test
    public void unlockDeviceCommand() {
        checkCommandSerialization(Command.UNLOCKDEVICE, UnlockDeviceCommand.builder()
                .amount(1000)
                .posOperation(PosOperation.AUTHORIZE)
                .cashBackAmount(0)
                .currencyCode("643")
                .idleText("Вставьте карту")
                .language("RU")
                .ecrVersion("v.12.4.5")
                .operatorId("оператор")
                .operatorName("Имя, фамилия")
                .build());
    }

    @Test
    public void unlockDeviceForInstallmentCommand() {
        checkCommandSerialization(Command.UNLOCKDEVICE, "_installment", UnlockDeviceCommand.builder()
                .amount(0)
                .posOperation(PosOperation.NOOPERATION)
                .cashBackAmount(0)
                .currencyCode("643")
                .idleText("Вставьте карту")
                .language("RU")
                .ecrVersion("v.12.4.5")
                .operatorId("оператор")
                .operatorName("Имя, фамилия")
                .build());
    }

    @Test
    public void voidCommand() {
        checkCommandSerialization(Command.VOID, new VoidCommand("A0000000041010"));
    }

    @Test
    public void voidPartialCommand() {
        checkCommandSerialization(Command.VOIDPARTIAL, new VoidPartialCommand("A0000000041010", 1000, 2000));
    }

    private void checkCommandSerialization(Command command, CommandParams params) {
        checkCommandSerialization(command, "", params);
    }

    private void checkCommandSerialization(Command command, String modifier, CommandParams params) {
        final String actualResult = converter.serialize(command, params, writer);
        checkSerialization("requests/commands/" + command.name().toLowerCase() + modifier, actualResult);
    }

    private void checkSerialization(String expectedResultFileName, Object request) {
        final String actualResult = converter.serialize(request, writer);
        checkSerialization(expectedResultFileName, actualResult);
    }

    private void checkSerialization(String expectedResultFileName, String actualResult) {
        final String expected = readAsString(expectedResultFileName);
        Assert.assertEquals(expected, actualResult);
    }

}