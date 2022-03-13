package ru.crystals.pos.fiscalprinter.uz.fiscaldrive;

import com.fasterxml.jackson.databind.ObjectMapper;
import ru.crystals.json.DefaultJsonParser;
import ru.crystals.pos.configurator.core.Constants;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.uz.fiscaldrive.api.pos.dto.ShiftVO;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.time.LocalDateTime;

public class EmulatedCounters {

    private static final String COUNTERS_STORAGE = "emulated_counters.properties";
    private static final String COUNTERS_FILE_PATH = Constants.PATH_MODULES + Constants.FISCAL_PRINTER + File.separator + COUNTERS_STORAGE;

    private EmulatedCountersVO countersVO;
    private final ObjectMapper jsonParser;

    public EmulatedCounters() {
        jsonParser = new DefaultJsonParser(true, false, "yyyy-MM-dd'T'HH:mm:sss")
                .getObjectMapper().findAndRegisterModules();
    }

    public void loadState() throws FiscalPrinterException {
        try {
            countersVO = null;
            File file = new File(COUNTERS_FILE_PATH);
            if (file.exists()) {
                try (FileInputStream is = new FileInputStream(file)) {
                    String kpkJson = getFileContent(is);
                    countersVO = jsonParser.readValue(kpkJson, EmulatedCountersVO.class);
                }
            }
        } catch (Exception e) {
            throw new FiscalPrinterException(e.getMessage());
        }

        boolean needSave = false;
        if (countersVO == null) {
            countersVO = new EmulatedCountersVO();
            needSave = true;
        }
        if (countersVO.getSoftShift() == null) {
            countersVO.setSoftShift(new SoftShift());
            needSave = true;
        }
        if (needSave) {
            saveState();
        }
    }

    private static String getFileContent(FileInputStream fis) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    private void saveState() throws FiscalPrinterException {
        try (PrintStream ps = new PrintStream(new FileOutputStream(COUNTERS_FILE_PATH))) {
            String kpkJson = jsonParser.writeValueAsString(countersVO);
            ps.print(kpkJson);
            ps.flush();
        } catch (IOException e) {
            throw new FiscalPrinterException(e.getMessage());
        }
    }

    long getCashAmount() {
        return countersVO.getCashAmount();
    }


    void incReturn(long cashPayment) throws FiscalPrinterException {
        countersVO.setCashAmount(Math.max(0, countersVO.getCashAmount() - cashPayment));
        saveState();
    }

    void incSale(long cashPayment) throws FiscalPrinterException {
        countersVO.setCashAmount(countersVO.getCashAmount() + cashPayment);
        saveState();
    }

    void incCashIn(long sum) throws FiscalPrinterException {
        countersVO.setCashAmount(countersVO.getCashAmount() + sum);
        saveState();
    }

    void incCashOut(long sum) throws FiscalPrinterException {
        countersVO.setCashAmount(Math.max(0, countersVO.getCashAmount() - sum));
        saveState();
    }

    /**
     * Открыть программную смену
     * @param shiftNumber данные ФР смены
     */
    void openShift(ShiftVO shiftNumber) throws FiscalPrinterException {
        SoftShift softShift = new SoftShift();
        softShift.setOpenTime(shiftNumber.getOpenTime() == null ? LocalDateTime.now() : shiftNumber.getOpenTime());
        softShift.setNumber(countersVO.getSoftShift().getNumber());
        countersVO.setSoftShift(softShift);
        saveState();
    }

    /**
     * Закрыть программную смену
     */
    void closeShift() throws FiscalPrinterException {
        countersVO.getSoftShift().setCloseTime(LocalDateTime.now());
        countersVO.getSoftShift().setNumber(countersVO.getSoftShift().getNumber() + 1);
        saveState();
    }

    /**
     * Используется ли программная смена
     */
    boolean isSoftShiftOpened() {
        return countersVO.getSoftShift().getOpenTime() != null && countersVO.getSoftShift().getCloseTime() == null;
    }

    /**
     * Счетчик программной смены
     */
    long getSoftShiftNumber() {
        return countersVO.getSoftShift().getNumber();
    }

    /**
     * Первичная инициализация и взятие номера смены из настоящего ФР
     * @param number номер смены из ФР
     */
    void initShiftNumber(long number) {
        if (countersVO.getSoftShift().getNumber() == -1) {
            countersVO.getSoftShift().setNumber(number);
        }
    }

    void setTryingCloseHardShift(boolean value) throws FiscalPrinterException {
        countersVO.getSoftShift().setTryingCloseHardShift(value);
        saveState();
    }

    boolean isTryingCloseHardShift() {
        return countersVO.getSoftShift().isTryingCloseHardShift();
    }
}
