package io.github.gamermax1254.tlcountdown.client;

import dev.architectury.event.events.client.ClientTickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientTickTracker {

    private ClientTickTracker() {}

    private static final Map<Long, Entry> DATA = new HashMap<>();
    private static final int TTL_TICKS = 20 * 30; // 30 секунд
    public static final int MAX_DRIFT_TICKS = 40; // 2 секунды рассинхрона

    private static long globalTicks = 0;

    private static class Entry {
        int lastServerTicker;
        int clientOffsetTicks;
        int inactivityTicks;
    }

    public static long getGlobalTicks() {
        return globalTicks;
    }

    public static void init() {
        // Используем Architectury API вместо Fabric API
        ClientTickEvent.CLIENT_POST.register(client -> {
            if (client.isPaused()) return;

            globalTicks++;
            RemoteScheduleLearner.tick();

            Iterator<Map.Entry<Long, Entry>> it = DATA.entrySet().iterator();
            while (it.hasNext()) {
                Entry e = it.next().getValue();
                e.clientOffsetTicks++;
                e.inactivityTicks++;
                if (e.inactivityTicks > TTL_TICKS) {
                    it.remove();
                }
            }
        });
    }

    public static int getSmoothTicker(BlockPos pos, int serverTicker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isPaused()) return serverTicker;

        long key = pos.asLong();
        Entry e = DATA.get(key);

        if (e == null) {
            e = new Entry();
            e.lastServerTicker = serverTicker;
            e.clientOffsetTicks = 0;
            e.inactivityTicks = 0;
            DATA.put(key, e);
            return serverTicker;
        }

        e.inactivityTicks = 0;

        if (serverTicker != e.lastServerTicker) {
            e.lastServerTicker = serverTicker;
            e.clientOffsetTicks = 0;
        }

        return e.lastServerTicker + e.clientOffsetTicks;
    }

    public static boolean isStale(BlockPos pos, int serverTicker) {
        return Minecraft.getInstance().isPaused();
    }

    public static void clear() {
        DATA.clear();
    }
}