package ru.crystals.pos.loyal.cash.transport.actions;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import ru.crystals.discounts.transport.ActionFileInfo;
import ru.crystals.discounts.transport.IAdvertiseActionsFileTransferRemote;
import ru.crystals.pos.InternalCashPoolExecutor;
import ru.crystals.pos.loyal.cash.service.LoyalServiceImpl;
import ru.crystals.pos.loyal.cash.transport.persistence.ActionsTransportAuxiliariesDao;
import ru.crystals.pos.properties.PropertiesManager;
import ru.crystals.pos.property.Properties;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Тесты для класса ActionsFilesReader
 */
@RunWith(MockitoJUnitRunner.class)
public class ActionsFilesReaderTest {

    @Mock
    private InternalCashPoolExecutor executor;

    @Mock
    private PropertiesManager propertiesManager;

    @Mock
    private ActionsTransportAuxiliariesDao trsAuxDao;

    @Mock
    private IAdvertiseActionsFileTransferRemote discountsMan;

    @Mock
    private Properties properties;

    @Mock
    private LoyalServiceImpl service;

    @Spy
    @InjectMocks
    private ActionsFilesReader actionsFilesReader;

    @Before
    public void init() {
        Mockito.doNothing().when(actionsFilesReader).scheduleDeferredTask();
    }

    @Test
    public void testReadActions() {
        String filePath = System.getProperty("user.dir") + "/failFiles/advertactions.ser";
        File file = new File(filePath);
        if (file.exists()) {
            FileUtils.deleteQuietly(file);
        }
        Assert.assertFalse(file.exists());
        URL resource = this.getClass().getResource("/advertactions.ser");
        List<ActionFileInfo> fileInfos = new ArrayList<>();
        fileInfos.add(makeActionFileInfo(5L, resource.toString(), 10));
        fileInfos.add(makeActionFileInfo(2L, resource.toString(), 14));
        fileInfos.add(makeActionFileInfo(4L, resource.toString(), 4));
        fileInfos.add(makeActionFileInfo(1L, resource.toString(), 6));
        Mockito.doReturn(fileInfos).when(discountsMan).getNewAdvertiseActionFileInfoForCash(Mockito.any(), Mockito.anyLong(), Mockito.anyBoolean());

        actionsFilesReader.run();

        Mockito.verify(discountsMan).acknowledgeAdvertiseActions(Mockito.any(), Mockito.eq(1L), Mockito.eq(true), Mockito.eq(Collections.singletonList(16203L)));

        Assert.assertTrue(file.exists());
    }

    private ActionFileInfo makeActionFileInfo(Long id, String path, Integer count) {
        ActionFileInfo info = new ActionFileInfo();
        info.setFilePath(path);
        info.setId(id);
        info.setObjectsCount(count);
        return info;
    }
}
