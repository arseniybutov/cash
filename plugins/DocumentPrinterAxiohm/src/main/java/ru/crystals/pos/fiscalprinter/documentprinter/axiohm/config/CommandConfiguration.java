package ru.crystals.pos.fiscalprinter.documentprinter.axiohm.config;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Runtime-конфигурация команд и других аспектов работы принтера, сформированная из дефолтных и пользовательских конфигов
 */
public class CommandConfiguration {

    private final StatusCommand statusCommand;
    private final DrawerStatusCommand drawerStatusCommand;

    private final SimpleCommand cutCommand;
    private final SimpleCommand feedCommand;
    private final SimpleCommand fullCutCommand;
    private final SimpleCommand initCharsetCommand;

    private final Charset encoding;

    private String toReplace;
    private String replacements;


    public CommandConfiguration(EscPosPrinterConfig config, EscPosPrinterConfig defaultConfig) {
        mergeConfigs(config, defaultConfig);

        encoding = Charset.forName(config.getPrinterEncoding());

        final Map<PrinterCommandType, ByteSequence> customCommands = config.getCommands();

        final ByteSequence feedCommandConfigured = requireNonNull(customCommands.get(PrinterCommandType.FEED));
        final byte feedLength = (byte) (Math.min(config.getFeedLength(), 0xFF) & 0xFF);
        this.feedCommand = makeCommand(feedCommandConfigured, new byte[]{feedLength});

        statusCommand = new StatusCommand(requireNonNull(customCommands.get(PrinterCommandType.STATUS)), config.getPrinterStatusMap());
        cutCommand = new SimpleCommand(requireNonNull(customCommands.get(PrinterCommandType.CUT)).getCommand());
        fullCutCommand = Optional.ofNullable(customCommands.get(PrinterCommandType.FULL_CUT))
                .map(ByteSequence::getCommand)
                .map(SimpleCommand::new)
                .orElse(cutCommand);

        initCharsetCommand = new SimpleCommand(requireNonNull(customCommands.get(PrinterCommandType.INIT_CHARSET)).getCommand());

        if (!config.getMissingSymbolsReplacement().isEmpty()) {
            toReplace = config.getMissingSymbolsReplacement().keySet().stream().map(Object::toString).collect(Collectors.joining());
            replacements = config.getMissingSymbolsReplacement().values().stream().map(Object::toString).collect(Collectors.joining());
        }

        drawerStatusCommand = new DrawerStatusCommand(requireNonNull(customCommands.get(PrinterCommandType.DRAWER_STATUS)), config.getDrawerStatusMap());
    }

    private SimpleCommand makeCommand(ByteSequence command, byte[] params) {
        return new SimpleCommand(ArrayUtils.addAll(command.getCommand(), params));
    }

    /**
     * Накладываем на дефолтный конфиг принтера то, что задана в конфиг. файле)
     */
    private void mergeConfigs(EscPosPrinterConfig config, EscPosPrinterConfig defaultConfig) {
        if (config.getPrinterEncoding() == null) {
            config.setPrinterEncoding(requireNonNull(defaultConfig.getPrinterEncoding()));
        }
        if (config.getFeedLength() == null) {
            config.setFeedLength(requireNonNull(defaultConfig.getFeedLength()));
        }
        if (config.getDrawerStatusMap().isEmpty()) {
            config.setDrawerStatusMap(defaultConfig.getDrawerStatusMap());
        }
        if (config.getPrinterStatusMap().isEmpty()) {
            config.setPrinterStatusMap(defaultConfig.getPrinterStatusMap());
        }
        if (config.getMaxCharRowMap().isEmpty()) {
            config.setMaxCharRowMap(defaultConfig.getMaxCharRowMap());
        }
        mergeMaps(config, defaultConfig, EscPosPrinterConfig::getCommands, EscPosPrinterConfig::setCommands);
        mergeMaps(config, defaultConfig, EscPosPrinterConfig::getMissingSymbolsReplacement, EscPosPrinterConfig::setMissingSymbolsReplacement);
    }

    private <K, V> void mergeMaps(EscPosPrinterConfig config,
                                  EscPosPrinterConfig defaultConfig,
                                  Function<EscPosPrinterConfig, Map<K, V>> extractor,
                                  BiConsumer<EscPosPrinterConfig, Map<K, V>> setter) {
        final HashMap<K, V> resultMap = new HashMap<>(extractor.apply(defaultConfig));
        resultMap.putAll(extractor.apply(config));
        setter.accept(config, resultMap);
    }

    public SimpleCommand getInitCharsetCommand() {
        return initCharsetCommand;
    }

    public StatusCommand getStatusCommand() {
        return statusCommand;
    }

    public SimpleCommand getCutCommand() {
        return cutCommand;
    }

    public SimpleCommand getFeedCommand() {
        return feedCommand;
    }

    public SimpleCommand getFullCutCommand() {
        return fullCutCommand;
    }

    public DrawerStatusCommand getDrawerStatusCommand() {
        return drawerStatusCommand;
    }

    public String replaceMissingSymbols(String text) {
        if (toReplace == null) {
            return text;
        }
        return StringUtils.replaceChars(text, toReplace, replacements);
    }

    public Charset getEncoding() {
        return encoding;
    }

}
