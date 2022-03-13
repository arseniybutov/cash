package ru.crystals.pos.bank.tusson.printer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SlipsContainer {
    private Set<DocumentType> receivedDocuments = new HashSet<DocumentType>();
    private List<TerminalSlip> slips = new ArrayList<TerminalSlip>();

    public SlipsContainer() {
    }

    public void addSlip(TerminalSlip slip) {
        receivedDocuments.add(slip.getDocumentType());
        slips.add(slip);
    }

    public boolean allSlipsReceived(Collection<DocumentType> checkedDocumentTypes) {
        return receivedDocuments.containsAll(checkedDocumentTypes);
    }

    public List<TerminalSlip> getSlips() {
        return slips;
    }

    public List<TerminalSlip> getSlipsByType(Collection<DocumentType> documentTypes) {
        List<TerminalSlip> result = new ArrayList<>();
        for (TerminalSlip slip : slips) {
            if (documentTypes.contains(slip.getDocumentType())) {
                result.add(slip);
            }
        }
        return result;
    }
}
