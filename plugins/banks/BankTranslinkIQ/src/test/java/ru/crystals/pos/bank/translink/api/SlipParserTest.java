package ru.crystals.pos.bank.translink.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.crystals.pos.bank.translink.api.dto.events.BaseEvent;
import ru.crystals.pos.bank.translink.api.dto.events.OnPrintEvent;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SlipParserTest {

    private SlipParser parser = new SlipParser();

    private static ObjectMapper om;

    @BeforeClass
    public static void beforeClass() {
        om = new TranslinkJsonConverter().getObjectMapper();
    }

    @Test
    public void parseCloseDaySlip() throws IOException {
        final List<List<String>> result = parser.parse(loadReceipt("closeday.json"));

        final List<List<String>> expectedResult = Collections.singletonList(
                Arrays.asList(
                        "          End of Day report",
                        "--------------------------------------",
                        "",
                        "",
                        "           Random Response",
                        "Merchant: name",
                        "Address: Vil",
                        "",
                        "Terminal ID: T2543543",
                        "",
                        "No successful transactions were made",
                        "______________________________________",
                        "OK                 2020-04-22 18:30:16",
                        "",
                        ""
                )
        );
        assertEquals(expectedResult, result);
    }

    @Test
    public void parseSaleSlip() throws IOException {
        final List<List<String>> result = parser.parse(loadReceipt("approved.json"));

        final List<List<String>> expectedResult = Arrays.asList(
                Arrays.asList(
                        "  ",
                        "                 name",
                        "                 crys",
                        "               Vil, Vil",
                        "          Įmonės kodas: 5154",
                        "  ",
                        "          MOKĖJIMAS KORTELE",
                        "               PIRKIMAS",
                        "  ",
                        "Operacijos Nr.                  100004",
                        "Terminalo Nr.                 T2543543",
                        "Prekybininko Nr.       T35453435435435",
                        "V.       SmartPOS 1.1.119.TET@008BC92C",
                        "RRN                       0111RR100004",
                        "Data ir laikas     2020-04-20 14:20:45",
                        "Kortelė            ***************0055",
                        "                             Test Card",
                        "  ",
                        "Atsiskaitymo suma             9.99 Eur",
                        "  ",
                        "Aut. k.           15739Z           T:1",
                        "  ",
                        "             PATVIRTINTA",
                        "  ",
                        "    Operacija patvirtinta PIN kodu",
                        "#: 00 Подтверждено",
                        "  ",
                        "   Išsaugokite čekį, kad galėtumėte",
                        "     patikrinti sąskaitos išrašą",
                        ""
                ),
                Arrays.asList(
                        "  ",
                        "                 name",
                        "                 crys",
                        "               Vil, Vil",
                        "          Įmonės kodas: 5154",
                        "  ",
                        "          MOKĖJIMAS KORTELE",
                        "               PIRKIMAS",
                        "  ",
                        "Operacijos Nr.                  100004",
                        "Terminalo Nr.                 T2543543",
                        "Prekybininko Nr.       T35453435435435",
                        "V.       SmartPOS 1.1.119.TET@008BC92C",
                        "RRN                       0111RR100004",
                        "Data ir laikas     2020-04-20 14:20:45",
                        "Kortelė            ***************0055",
                        "                             Test Card",
                        "  ",
                        "Atsiskaitymo suma             9.99 Eur",
                        "  ",
                        "Aut. k.           15739Z           T:1",
                        "  ",
                        "             PATVIRTINTA",
                        "  ",
                        "    Operacija patvirtinta PIN kodu",
                        "#: 00 Подтверждено")
        );
        assertEquals(expectedResult, result);
    }

    @Test
    public void parseSaleCopySlip() throws IOException {
        final List<List<String>> result = parser.parse(loadReceipt("copy.json"));

        final List<List<String>> expectedResult = Arrays.asList(
                Arrays.asList(
                        "  ",
                        "       K V I T O   K O P I J A",
                        "  ",
                        "                 name",
                        "                 crys",
                        "               Vil, Vil",
                        "          Įmonės kodas: 5154",
                        "  ",
                        "          MOKĖJIMAS KORTELE",
                        "              ATŠAUKIMAS",
                        "  ",
                        "Operacijos Nr.                  400004",
                        "Terminalo Nr.                 T2543543",
                        "Prekybininko Nr.       T35453435435435",
                        "V.       SmartPOS 1.1.119.TET@008BC92C",
                        "RRN                       0111RR100004",
                        "Data ir laikas     2020-04-20 14:20:45",
                        "Kortelė            ***************0055",
                        "                             Test Card",
                        "  ",
                        "Atsiskaitymo suma            -9.99 Eur",
                        "  ",
                        "                                   T:1",
                        "  ",
                        "   Išsaugokite čekį, kad galėtumėte",
                        "     patikrinti sąskaitos išrašą",
                        ""
                ),
                Arrays.asList(
                        "  ",
                        "       K V I T O   K O P I J A",
                        "  ",
                        "                 name",
                        "                 crys",
                        "               Vil, Vil",
                        "          Įmonės kodas: 5154",
                        "  ",
                        "          MOKĖJIMAS KORTELE",
                        "              ATŠAUKIMAS",
                        "  ",
                        "Operacijos Nr.                  400004",
                        "Terminalo Nr.                 T2543543",
                        "Prekybininko Nr.       T35453435435435",
                        "V.       SmartPOS 1.1.119.TET@008BC92C",
                        "RRN                       0111RR100004",
                        "Data ir laikas     2020-04-20 14:20:45",
                        "Kortelė            ***************0055",
                        "                             Test Card",
                        "  ",
                        "Atsiskaitymo suma            -9.99 Eur",
                        "  ",
                        "                                   T:1")
        );
        assertEquals(expectedResult, result);
    }

    @Test
    public void parseSaleDeclinedSlip() throws IOException {
        final List<List<String>> result = parser.parse(loadReceipt("declined.json"));

        final List<List<String>> expectedResult = Collections.singletonList(
                Arrays.asList("  ",
                        "                 name",
                        "                 crys",
                        "               Vil, Vil",
                        "          Įmonės kodas: 5154",
                        "  ",
                        "          MOKĖJIMAS KORTELE",
                        "               PIRKIMAS",
                        "  ",
                        "Operacijos Nr.                  100002",
                        "Terminalo Nr.                 T2543543",
                        "Prekybininko Nr.       T35453435435435",
                        "V.       SmartPOS 1.1.119.TET@008BC92C",
                        "RRN                       0111RR100002",
                        "Data ir laikas     2020-04-20 14:17:36",
                        "Kortelė            ***************0055",
                        "                             Test Card",
                        "  ",
                        "Atsiskaitymo suma             9.99 Eur",
                        "  ",
                        "       N E A P T A R N A U T I",
                        "  ",
                        "#: 1026 Action code",
                        ""
                )
        );
        assertEquals(expectedResult, result);
    }

    protected String loadReceipt(String receiptFile) throws IOException {
        final BaseEvent baseEvent = om.readValue(this.getClass().getResource("/api/slips/" + receiptFile), BaseEvent.class);
        final OnPrintEvent properties = (OnPrintEvent) baseEvent.getProperties();

        return properties.getReceiptText();
    }
}