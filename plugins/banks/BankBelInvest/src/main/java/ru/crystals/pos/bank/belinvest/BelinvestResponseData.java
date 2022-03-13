package ru.crystals.pos.bank.belinvest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.cm.utils.JAXBContextFactory;
import ru.crystals.pos.bank.Bank;
import ru.crystals.pos.bank.belinvest.ds.OperationResultRs;
import ru.crystals.pos.bank.belinvest.exceptions.ParseXmlException;
import ru.crystals.pos.bank.datastruct.BankCard;
import ru.crystals.pos.bank.filebased.ResponseData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tatarinov Eduard on 17.11.16.
 */
public class BelinvestResponseData implements ResponseData {
    private static final Logger log = LoggerFactory.getLogger(Bank.class);

    public static final Long SUCCESSFULL_RESPONSE_CODE = 0L;

    private OperationResultRs operationResultRs;
    private List<String> responseFile;
    private List<String> slip;

    @Override
    public BelinvestResponseData parseResponseFile(List<String> responseFile) {
        this.responseFile = responseFile;
        slip = new ArrayList<>();
        boolean isStartSplit = false;
        StringBuilder inXml = new StringBuilder();
        inXml.append("<?xml version = \"1.0\" encoding=\"UTF-8\" ?>");
        for (String row : responseFile) {
            inXml.append(row);
            // Читаем чек построчно
            if (isStartSplit) {
                slip.add(row);
            }
            if (row.contains("ChequeText")) {
                isStartSplit = !isStartSplit;
            }
        }
        if (!slip.isEmpty()) {
            slip.remove(slip.size() - 1);
        }

        try {
            JAXBContext jaxbContext = JAXBContextFactory.getContext(OperationResultRs.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            operationResultRs = (OperationResultRs) unmarshaller.unmarshal(new ByteArrayInputStream(inXml.toString().getBytes()));

        } catch (JAXBException e) {
            log.error("Error parse XML ", e);
            throw new ParseXmlException("Error parse response XML file");
        }
        return this;
    }

    @Override
    public String getMessage() {
        return operationResultRs.getResultText();
    }

    @Override
    public String getResponseCode() {
        return String.valueOf(operationResultRs.getResultCode());
    }

    @Override
    public String getAuthCode() {
        return operationResultRs.getOriginalCode();
    }

    @Override
    public String getTerminalId() {
        return operationResultRs.getTerminalId();
    }

    @Override
    public String getReferenceNumber() {
        return StringUtils.EMPTY;
    }

    @Override
    public BankCard getBankCard() {
        BankCard card = new BankCard();
        card.setCardNumber(operationResultRs.getCardNo());
        return card;
    }

    @Override
    public boolean isSuccessful() {
        return SUCCESSFULL_RESPONSE_CODE.equals(operationResultRs.getResultCode());
    }

    @Override
    public ResponseData logResponseFile() {
        for (String row : responseFile) {
            log.info(row);
        }
        return this;
    }

    public LocalDateTime getDate() {
        return operationResultRs.getDateTime() != null ? operationResultRs.getDateTime() : LocalDateTime.now();
    }

    public Long getCountSlip() {
        return operationResultRs.getChequeCount();
    }

    public List<List<String>> getSlip() {
        List<List<String>> slips = new ArrayList<>();
        long countSlip = getCountSlip() == null ? 1 : getCountSlip();
        for (int i = 0; i < countSlip; i++) {
            slips.add(slip);
        }
        return slips;
    }

    public OperationResultRs getOperationResultRs() {
        return operationResultRs;
    }

    @Override
    public String toString() {
        return "BelinvestResponseData{" +
                "operationResultRs=" + operationResultRs +
                '}';
    }
}
