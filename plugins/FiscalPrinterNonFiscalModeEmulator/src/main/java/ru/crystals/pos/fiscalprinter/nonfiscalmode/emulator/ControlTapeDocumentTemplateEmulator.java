package ru.crystals.pos.fiscalprinter.nonfiscalmode.emulator;

import ru.crystals.pos.fiscalprinter.templates.ControlTapeDocumentTemplate;

import java.io.IOException;
import java.io.InputStream;

public class ControlTapeDocumentTemplateEmulator extends ControlTapeDocumentTemplate {

    private static ControlTapeDocumentTemplateEmulator template;

    public static ControlTapeDocumentTemplateEmulator getInstance() throws IOException {
        if (template == null) {
            template = new ControlTapeDocumentTemplateEmulator();
            template.generate();
        }
        return template;
    }

    private void generate() throws IOException {
        String templateName = "/control-tape.xml";
        try (InputStream is = getClass().getResourceAsStream(templateName)) {
            if (is == null) {
                throw new IOException(String.format("Control tape template resource \"%s\" not found.", templateName));
            }
            generate(is);
        }
    }
}