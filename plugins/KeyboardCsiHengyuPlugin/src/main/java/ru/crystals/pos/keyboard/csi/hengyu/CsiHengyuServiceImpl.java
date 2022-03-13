package ru.crystals.pos.keyboard.csi.hengyu;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import ru.crystals.pos.keyboard.plugin.v2.KeyLockChecker;
import ru.crystals.pos.keyboard.plugin.v2.KeyboardPluginV2Impl;

import java.util.Map;

/**
 * Обычный плагин {@link KeyboardPluginV2Impl}, только с определением положения ключа на старте кассы.
 */
public class CsiHengyuServiceImpl extends KeyboardPluginV2Impl {

    private static final String KEY_READER_DRIVER = "./lib/keyboard/keyLockReaderDrv_csi_hengyu";

    private static final String KEY_LOCK_POSITION_PREFIX = "Keylock status = FFFFFF";

    private static final Map<String, String> KEY_LOCK_BY_DRIVER_KEY_VALUE = new ImmutableMap.Builder<String, String>()
            .put("FE", "L")
            .put("FD", "O")
            .put("FB", "X")
            .put("F7", "S")
            .put("EF", "Z")
            .put("DF", "P")
            .build();

    @Override
    public void start() {
        super.start();
        new KeyLockChecker(
                KEY_READER_DRIVER,
                drvLog -> KEY_LOCK_BY_DRIVER_KEY_VALUE.get(keyLockPosition(drvLog)),
                getKeyLockMap(),
                keyLockListener
        ).check();
    }

    private String keyLockPosition(String drvLog) {
        return StringUtils.trimToNull(StringUtils.substringAfter(drvLog, KEY_LOCK_POSITION_PREFIX));
    }
}
