package ru.crystals.pos.visualization.products.weight;

import com.google.common.collect.Collections2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.crystals.commons.JAXBContextFactory;
import ru.crystals.pos.catalog.ProductWeightEntity;
import ru.crystals.pos.visualization.products.weight.tare.Tare;
import ru.crystals.pos.visualization.products.weight.tare.Tares;

import javax.swing.table.AbstractTableModel;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TareTableModel extends AbstractTableModel {
    public static final String TARE_XML_NAME = "tare.xml";
    private List<Tare> availableTare = new ArrayList<>();
    private Tares tares = new Tares();
    private Logger log = LoggerFactory.getLogger(TareTableModel.class);
    private File tareConfig = new File("modules" + File.separator + "goods" + File.separator + TARE_XML_NAME);
    private Path configFolderPath = tareConfig.getParentFile().toPath();
    private WatchService watcher;
    private WatchKey key;

    public TareTableModel() {
        try {
            watcher = configFolderPath.getFileSystem().newWatchService();
        } catch (IOException e) {
            log.error("", e);
        }
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public int getRowCount() {
        return availableTare.size();
    }

    @Override
    public Tare getValueAt(int rowIndex, int columnIndex) {
        return availableTare.get(rowIndex);
    }

    public boolean isTareAvailable() {
        return !availableTare.isEmpty();
    }

    public void refreshTare(ProductWeightEntity product) {
        loadTare();
        availableTare.clear();
        availableTare.addAll(tares.getTare());
        for (Tare tare : tares.getTare()) {
            if (product.getWeightBigDecimal().compareTo(tare.getWeight()) <= 0) {
                availableTare.remove(tare);
            }
        }
        fireTableDataChanged();
    }

    public void loadTare() {
        if (tares.getTare().isEmpty() || isTareModified()) {
            parseTare();
        }
    }

    private boolean isTareModified() {
        if (key == null) {
            registerWatchKey();
        }
        List<WatchEvent<?>> watchEvents = key.pollEvents();
        Collection<WatchEvent<?>> filtered = Collections2.filter(watchEvents, watchEvent -> ((Path) watchEvent.context()).endsWith(TARE_XML_NAME));
        return !filtered.isEmpty();
    }

    private void registerWatchKey() {
        try {
            key = configFolderPath
                    .register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private void parseTare() {
        try (InputStream stream = new FileInputStream(tareConfig)) {
            JAXBContext jaxbContext = JAXBContextFactory.getContext(Tares.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            tares = (Tares) jaxbUnmarshaller.unmarshal(stream);
        } catch (Exception ignore) {
            tares.getTare().clear();
            log.error("Unable to parse {}", TARE_XML_NAME);
        }
    }
}
