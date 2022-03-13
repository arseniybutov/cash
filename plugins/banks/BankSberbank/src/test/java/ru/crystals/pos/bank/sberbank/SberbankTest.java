package ru.crystals.pos.bank.sberbank;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.pos.bank.datastruct.AuthorizationData;
import ru.crystals.pos.bank.datastruct.BankOperationType;
import ru.crystals.pos.bank.exception.BankConfigException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SberbankTest {

    @Spy
    private SberbankServiceImpl bank = new SberbankServiceImpl();

    @Mock
    private File mockFile;

    @Mock
    private Properties mockProperties;

    private static final List<String> commonSlip = Arrays.asList(
            "---------------",
            "Первая строка",
            "Вторая строка",
            "Третья строка",
            "Четвертая строка",
            "Пятая строка");

    private static final List<String> commonEndOfSlip = Arrays.asList(
            "========================================",
            "",
            "",
            "",
            "");
    private static final String firstSlipZeroLine = "Первый слип";
    private static final String secondSlipZeroLine = "Второй слип";
    private static final String DEFAULT_DELIMITER = "\u0001";

    private static final List<String> rawSlipWithDefaultDelimiter = getRawSlipWithDefaultDelimiter();
    private static final List<String> rawSlipWithoutDefaultDelimiter = getRawSlipWithoutDefaultDelimiter();
    private static final List<List<String>> finalSlip = getFinalSlipWithDefaultDelimiter();
    private static final String CHECKING_SETTINGS_PARAM = "ComPort";
    private static final String SHOW_SCREEN = "27";


    private static List<List<String>> getFinalSlipWithDefaultDelimiter() {
        List<List<String>> result = new ArrayList<>();
        List<String> firstSlip = new ArrayList<>();
        firstSlip.add(firstSlipZeroLine);
        firstSlip.addAll(commonSlip);
        firstSlip.add("========================================");
        List<String> secondSlip = new ArrayList<>();
        secondSlip.add(secondSlipZeroLine);
        secondSlip.addAll(commonSlip);
        secondSlip.add("========================================");
        result.add(firstSlip);
        result.add(secondSlip);
        return result;
    }

    private static List<String> getRawSlipWithoutDefaultDelimiter() {
        List<String> result = new ArrayList<>();
        result.add(firstSlipZeroLine);
        result.addAll(commonSlip);
        result.addAll(commonEndOfSlip);
        result.add(secondSlipZeroLine);
        result.addAll(commonSlip);
        result.addAll(commonEndOfSlip);
        return result;
    }

    private static List<String> getRawSlipWithDefaultDelimiter() {
        List<String> result = new ArrayList<>();
        result.add(firstSlipZeroLine);
        result.addAll(commonSlip);
        result.addAll(commonEndOfSlip);
        result.add(DEFAULT_DELIMITER + secondSlipZeroLine);
        result.addAll(commonSlip);
        result.addAll(commonEndOfSlip);
        result.add(DEFAULT_DELIMITER);
        return result;
    }

    @Test
    public void verifySlipWithDefaultDelimiter() {
        AuthorizationData ad = new AuthorizationData();

        bank.makeSlip(ad, new SberbankResponseData().parseResponseFile(Collections.singletonList("0")), rawSlipWithDefaultDelimiter, BankOperationType.SALE);
        Assert.assertEquals(finalSlip.size(), ad.getSlips().size());
        Assert.assertEquals(finalSlip, ad.getSlips());
    }

    @Test
    public void verifySlipWithoutDefaultDelimiter() {
        AuthorizationData ad = new AuthorizationData();

        bank.makeSlip(ad, new SberbankResponseData().parseResponseFile(Collections.singletonList("0")), rawSlipWithoutDefaultDelimiter, BankOperationType.SALE);
        Assert.assertEquals(finalSlip.size(), ad.getSlips().size());
        Assert.assertEquals(finalSlip, ad.getSlips());
    }

    @Test
    public void verifySlipWithTwoCopy() {
        AuthorizationData ad = new AuthorizationData();

        final int slipCount = 2;

        bank.setInnerSlipCount(slipCount);
        bank.makeSlip(ad, new SberbankResponseData().parseResponseFile(Collections.singletonList("0")), rawSlipWithoutDefaultDelimiter, BankOperationType.SALE);
        Assert.assertEquals(slipCount, ad.getSlips().size());
    }

    @Test
    public void verifySlipWithOneCopy() {
        AuthorizationData ad = new AuthorizationData();

        final int slipCount = 1;

        bank.setInnerSlipCount(slipCount);
        bank.makeSlip(ad, new SberbankResponseData().parseResponseFile(Collections.singletonList("0")), rawSlipWithoutDefaultDelimiter, BankOperationType.SALE);
        Assert.assertEquals(slipCount, ad.getSlips().size());
    }

    @Test
    public void verifySlipWithDefaultCopy() {
        AuthorizationData ad = new AuthorizationData();

        final int slipCount = 0;

        bank.setInnerSlipCount(slipCount);
        bank.makeSlip(ad, new SberbankResponseData().parseResponseFile(Collections.singletonList("0")), rawSlipWithoutDefaultDelimiter, BankOperationType.SALE);
        Assert.assertEquals(2, ad.getSlips().size());
    }

    @Test
    public void verifyShowScreenCommandPreparedCorrectly() {
        Assert.assertEquals(Arrays.asList(SHOW_SCREEN, "160", null), bank.prepareShowScreenCommand("TERMINAL_READER_SHOP_LOGO_SCREEN"));
        Assert.assertEquals(Arrays.asList(SHOW_SCREEN, "160", StringUtils.replace(ResBundleBankSberbank.getString("TERMINAL_READER_SWIPE_CARD"),
                " ", "_"), null), bank.prepareShowScreenCommand("TERMINAL_READER_SHOP_LOGO_SCREEN", "TERMINAL_READER_SWIPE_CARD"));
    }

    @Test
    public void testRequestTerminalIfOfflineProcessingNotConfigured() {
        //given
        doReturn(false).when(bank).isProcessingConfigured();

        //when
        boolean result = bank.requestTerminalStateIfOffline();
        //then
        verify(bank).isProcessingConfigured();
        verify(bank, never()).requestTerminalStateIfOnline();
        Assert.assertFalse(result);
    }

    @Test
    public void testRequestTerminalIfOfflineProcessingConfiguredTerminalOffline() {
        //given
        doReturn(true).when(bank).isProcessingConfigured();
        doReturn(false).when(bank).requestTerminalStateIfOnline();
        //when
        boolean result = bank.requestTerminalStateIfOffline();
        //then
        verify(bank).isProcessingConfigured();
        verify(bank).requestTerminalStateIfOnline();
        Assert.assertFalse(result);
    }

    @Test
    public void testRequestTerminalIfOfflineProcessingConfiguredTerminalOnline() {
        //given
        doReturn(true).when(bank).isProcessingConfigured();
        doReturn(true).when(bank).requestTerminalStateIfOnline();
        //when
        boolean result = bank.requestTerminalStateIfOffline();
        //then
        verify(bank).isProcessingConfigured();
        verify(bank).requestTerminalStateIfOnline();
        Assert.assertTrue(result);
    }

    @Test
    public void testIsProcessingConfiguredReturnFalseExecutableNotFound() {
        //given
        doReturn(mockFile).when(bank).getExecutableFile();
        doReturn(false).when(mockFile).exists();

        //when
        boolean result = bank.isProcessingConfigured();
        //then
        verify(bank).getExecutableFile();
        verify(mockFile).exists();
        verify(bank, never()).checkSettingsFileCorrect();
        Assert.assertFalse(result);
    }

    @Test
    public void testIsProcessingConfiguredReturnTrueSettingsFileNotFound() {
        //given
        doReturn(mockFile).when(bank).getExecutableFile();
        doReturn(true).when(mockFile).exists();
        doReturn(false).when(bank).checkSettingsFileCorrect();
        doReturn(false).when(bank).checkSettingsFileFound();
        //when
        boolean result = bank.isProcessingConfigured();
        //then
        verify(bank).getExecutableFile();
        verify(mockFile).exists();
        verify(bank).checkSettingsFileFound();
        verify(bank, never()).checkSettingsFileCorrect();
        Assert.assertTrue(result);
    }

    @Test
    public void testIsProcessingConfiguredReturnFalseSettingsNotExists() {
        //given
        doReturn(mockFile).when(bank).getExecutableFile();
        doReturn(true).when(mockFile).exists();
        doReturn(false).when(bank).checkSettingsFileCorrect();
        doReturn(true).when(bank).checkSettingsFileFound();
        //when
        boolean result = bank.isProcessingConfigured();
        //then
        verify(bank).getExecutableFile();
        verify(mockFile).exists();
        verify(bank).checkSettingsFileFound();
        verify(bank).checkSettingsFileCorrect();
        Assert.assertFalse(result);
    }

    @Test
    public void testIsProcessingConfiguredReturnTrue() {
        //given
        doReturn(mockFile).when(bank).getExecutableFile();
        doReturn(true).when(mockFile).exists();
        doReturn(true).when(bank).checkSettingsFileCorrect();
        doReturn(true).when(bank).checkSettingsFileFound();
        //when
        boolean result = bank.isProcessingConfigured();
        //then
        verify(bank).getExecutableFile();
        verify(mockFile).exists();
        verify(bank).checkSettingsFileFound();
        verify(bank).checkSettingsFileCorrect();
        Assert.assertTrue(result);
    }

    @Test
    public void testCheckSettingsFileExistsReturnFalseGetPropertiesThrowsException() throws Exception {
        //given
        doThrow(new BankConfigException()).when(bank).getProperties(anyString(), anyString());

        //when
        boolean result = bank.checkSettingsFileCorrect();
        //then
        verify(bank).getProperties(anyString(), anyString());
        Assert.assertFalse(result);
    }

    @Test
    public void testCheckSettingsFileExistsReturnFalsePropertiesIsNull() throws Exception {
        //given
        doReturn(null).when(bank).getProperties(anyString(), anyString());

        //when
        boolean result = bank.checkSettingsFileCorrect();
        //then
        Assert.assertFalse(result);
        verify(bank).getProperties(anyString(), anyString());
    }

    @Test
    public void testCheckSettingsFileExistsReturnFalseTargetPropertyNotFound() throws Exception {
        //given
        doReturn(mockProperties).when(bank).getProperties(anyString(), anyString());
        doReturn(null).when(mockProperties).getProperty(anyString());

        //when
        boolean result = bank.checkSettingsFileCorrect();
        //then
        Assert.assertFalse(result);
        verify(bank).getProperties(anyString(), anyString());
        verify(mockProperties).getProperty(CHECKING_SETTINGS_PARAM);
    }

    @Test
    public void testCheckSettingsFileExistsReturnFalseTargetPropertyIsEmpty() throws Exception {
        //given
        doReturn(mockProperties).when(bank).getProperties(anyString(), anyString());
        doReturn("").when(mockProperties).getProperty(anyString());

        //when
        boolean result = bank.checkSettingsFileCorrect();
        //then
        Assert.assertFalse(result);
        verify(bank).getProperties(anyString(), anyString());
        verify(mockProperties).getProperty(CHECKING_SETTINGS_PARAM);
    }

    @Test
    public void testCheckSettingsFileExistsReturnFalseTargetPropertyIsEmptyAfterTrim() throws Exception {
        //given
        doReturn(mockProperties).when(bank).getProperties(anyString(), anyString());
        doReturn("   ").when(mockProperties).getProperty(anyString());

        //when
        boolean result = bank.checkSettingsFileCorrect();
        //then
        Assert.assertFalse(result);
        verify(bank).getProperties(anyString(), anyString());
        verify(mockProperties).getProperty(CHECKING_SETTINGS_PARAM);
    }

    @Test
    public void testCheckSettingsFileExistsReturnTrue() throws Exception {
        //given
        doReturn(mockProperties).when(bank).getProperties(anyString(), anyString());
        doReturn(SHOW_SCREEN).when(mockProperties).getProperty(anyString());

        //when
        boolean result = bank.checkSettingsFileCorrect();
        //then
        Assert.assertTrue(result);
        verify(bank).getProperties(anyString(), anyString());
        verify(mockProperties).getProperty(CHECKING_SETTINGS_PARAM);
    }

    @Test
    public void testGenerateRequestIdCommandParam() {
        bank.setNeedGenerateRequestId(true);
        String requestIdCommand = bank.generateRequestIdCommandParam(false);
        Assert.assertTrue("Неверный формат команды - " + requestIdCommand, requestIdCommand.matches("/q=[\\dA-Fa-f]{8}"));
        bank.setNeedGenerateRequestId(false);
        requestIdCommand = bank.generateRequestIdCommandParam(false);
        Assert.assertNull(requestIdCommand);
    }
}
