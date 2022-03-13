package ru.crystals.pos.bank.softcase.utils;

import ru.crystals.pos.fiscalprinter.DocumentSection;
import ru.crystals.pos.fiscalprinter.FontLine;
import ru.crystals.pos.fiscalprinter.datastruct.documents.FiscalDocument;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;
import ru.crystals.pos.fiscalprinter.templates.DocumentTemplate;
import ru.crystals.pos.fiscalprinter.templates.parser.Section;
import ru.crystals.pos.templateengine.functions.ConstantLengthSupplier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: a.gaydenger Date: 16.10.13 Time: 17:08 To change this template use File | Settings | File Templates.
 */
public class SoftCaseDocumentTemplate extends DocumentTemplate<FiscalDocument> {
    private static Map<String, SoftCaseDocumentTemplateInstance> instance = new HashMap<>();

    private SoftCaseDocumentTemplate() {

    }

    @Override
    public DocumentSection processSection(Section section, FiscalDocument document) {
        return new DocumentSection(section.getId(), new ArrayList<>());
    }

    public DocumentSection processSection(Section section, Map<String, Object> dataset)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String methodName = "process" + section.getId().substring(0, 1).toUpperCase() + section.getId().substring(1) + "Section";
        Method m = DocumentTemplate.findMethod(this.getClass(), methodName, Section.class, Map.class);
        List<FontLine> listFontLines = (List<FontLine>) (m.invoke(this, section, dataset));
        return new DocumentSection(section.getId(), listFontLines);
    }

    public synchronized List<List<String>> processDocument(Map<String, Object> dataset) throws Exception {
        List<List<String>> result = new ArrayList<>();
        for (Section section : documentTemplate.getSection()) {
            if (section == null) {
                continue;
            }
            try {
                DocumentSection documentSection = processSection(section, dataset);
                if (documentSection != null) {
                    result.add(getStrings(documentSection));
                }
            } catch (NoSuchMethodException e) {
                LOG.error("Process section '" + section.getId() + "' error", e);
            }
        }
        return result;
    }

    public List<FontLine> processHeaderSection(Section section, Map<String, Object> dataset) throws FiscalPrinterException {
        if (section.getLine() != null) {
            return processLines(section.getLine(), dataset);
        } else {
            return null;
        }
    }

    public List<FontLine> processCutSection(Section section, Map<String, Object> dataset) {
        return new ArrayList<>();
    }

    private List<String> getStrings(DocumentSection documentSection) {
        List<String> result = new ArrayList<>();
        for (FontLine fontLine : documentSection.getContent()) {
            result.add(fontLine.getContent());
        }
        return result;
    }

    public static SoftCaseDocumentTemplate getInstance(String pathToTemplate) throws IOException {
        SoftCaseDocumentTemplateInstance instanceEntity = instance.get(pathToTemplate);
        File templateFile = new File(pathToTemplate);
        if (instanceEntity == null || instanceEntity.modificationDate != templateFile.lastModified()) {
            SoftCaseDocumentTemplate template = new SoftCaseDocumentTemplate();
            template.generate(pathToTemplate);
            template.setLengthSupplier(new ConstantLengthSupplier());
            if (instanceEntity != null) {
                instanceEntity.modificationDate = templateFile.lastModified();
                instanceEntity.template = template;
            } else {
                instanceEntity = new SoftCaseDocumentTemplateInstance(template, templateFile.lastModified());
                instance.put(pathToTemplate, instanceEntity);
            }
        }
        return instanceEntity.template;
    }

    private void generate(String pathToTemplate) throws IOException {
        try (FileInputStream fi = new FileInputStream(new File(pathToTemplate))) {
            byte[] bytes = new byte[fi.available()];
            fi.read(bytes);
            generate(new ByteArrayInputStream(bytes));
        }
    }

    private static class SoftCaseDocumentTemplateInstance {
        private SoftCaseDocumentTemplate template;
        private long modificationDate;

        private SoftCaseDocumentTemplateInstance(SoftCaseDocumentTemplate template, long modificationDate) {
            this.template = template;
            this.modificationDate = modificationDate;
        }
    }
}
