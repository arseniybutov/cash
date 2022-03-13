package ru.crystals.pos.fiscalprinter.az.airconn.audit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.pos.fiscalprinter.exception.FiscalPrinterException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class AuditHandler extends AbstractHandler {

    private static final Logger log = LoggerFactory.getLogger(AuditHandler.class);

    private static final String PARAM_SEPARATOR = " ";
    private static final String AUDIT_COMMAND = "audit";
    private static final String SAVE_CHECKSUM_COMMAND = "saveSoftChecksum";
    private static final String VERIFY_CHECKSUM_COMMAND = "verifySoftChecksum";

    private final AuditConnector connector;

    private Server httpServer;
    private String auditPassword;

    public AuditHandler(AuditConnector connector) {
        this.connector = connector;
    }

    public void startWork(int auditPort, String auditUrl, String auditPassword) throws Exception {
        httpServer = new Server(InetSocketAddress.createUnresolved("localhost", auditPort));
        httpServer.setStopAtShutdown(true);
        this.auditPassword = auditPassword;

        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setContextPath(auditUrl);
        contextHandler.setHandler(this);
        httpServer.setHandler(contextHandler);
        httpServer.start();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String rawInput = StringUtils.trimToEmpty(String.join("", IOUtils.readLines(request.getInputStream(), StandardCharsets.UTF_8)));
        log.debug("Received data: {}", rawInput);
        final String[] input = splitToFirstAnRest(rawInput);
        String operation = input[0];
        if (StringUtils.isBlank(operation)) {
            handleInvalidOperation(operation, response);
            baseRequest.setHandled(true);
            return;
        }
        final String params = input[1];
        response.setContentType("text/plain");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            if (AUDIT_COMMAND.equalsIgnoreCase(operation)) {
                handleAudit(params, response);
            } else if (VERIFY_CHECKSUM_COMMAND.equalsIgnoreCase(operation)) {
                response.getWriter().println("Operation: " + VERIFY_CHECKSUM_COMMAND);
                connector.verifySoftwareChecksum();
                response.getWriter().println("Result: success operation");
            } else if (SAVE_CHECKSUM_COMMAND.equalsIgnoreCase(operation)) {
                response.getWriter().println("Operation: " + SAVE_CHECKSUM_COMMAND);
                connector.saveSoftwareChecksum();
                response.getWriter().println("Result: success operation");
            } else {
                handleInvalidOperation(operation, response);
                baseRequest.setHandled(true);
                return;
            }
        } catch (Exception e) {
            log.error("", e);
            final String responseText = String.format("Result: operation failed: %s", e.getMessage());
            response.getWriter().println(responseText);
        }
        baseRequest.setHandled(true);
    }

    private void handleInvalidOperation(String target, HttpServletResponse response) throws IOException {
        if (StringUtils.isNotBlank(target)) {
            response.getWriter().println("Invalid operation: " + target);
            response.getWriter().println("");
        }
        printAvailable(response);
    }

    private void printAvailable(HttpServletResponse response) throws IOException {
        response.getWriter().println("Available operations:");
        response.getWriter().println(String.format("    %s <password> <name>", AUDIT_COMMAND));
        response.getWriter().println("    " + SAVE_CHECKSUM_COMMAND);
        response.getWriter().println("    " + VERIFY_CHECKSUM_COMMAND);
    }

    private void handleAudit(String input, HttpServletResponse response) throws FiscalPrinterException, IOException {
        final String name;
        try {
            if (StringUtils.isBlank(input)) {
                throw new IllegalArgumentException("password and name are empty");
            }
            final String[] params = splitToFirstAnRest(input);
            final String password = params[0];
            if (StringUtils.isBlank(password)) {
                throw new IllegalArgumentException("password is empty");
            }
            if (!password.equals(auditPassword)) {
                throw new IllegalArgumentException("wrong password (" + password + ")");
            }
            name = params[1];
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("name is empty");
            }
        } catch (Exception e) {
            response.getWriter().println("Invalid operation arguments: " + e.getMessage());
            response.getWriter().println("");
            printAvailable(response);
            return;
        }
        response.getWriter().println("Operation: " + AUDIT_COMMAND);
        response.getWriter().println("Name: " + name);
        connector.softwareAudit(name);
        response.getWriter().println("Result: success operation");
    }

    public void stopWork() {
        try {
            httpServer.stop();
        } catch (Exception e) {
            log.warn("Unable to stop server", e);
        }
    }

    private String[] splitToFirstAnRest(String input) {
        return new String[]{StringUtils.substringBefore(input, PARAM_SEPARATOR), StringUtils.trimToEmpty(StringUtils.substringAfter(input, PARAM_SEPARATOR))};
    }
}
