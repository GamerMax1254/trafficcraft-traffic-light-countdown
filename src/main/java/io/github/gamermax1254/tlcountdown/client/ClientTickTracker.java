package io.github.gamermax1254.tlcountdown.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ClientTickTracker {

    private ClientTickTracker() {}

    private static final Map<Long, Entry> DATA = new HashMap<>();
    private static final int TTL_TICKS = 20 * 30;

    // Максимально допустимый разброс между "сглаженным" тикером и серверным (в тиках)
    // Если разница больше — считаем, что данные "устарели" и рендерим "-- --"
    public static final int MAX_DRIFT_TICKS = 40; // 2 секунды

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
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // На паузе — не тикаем ничего вообще
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

    /**
     * Возвращает сглаженный ticker.
     * Если игра на паузе — возвращает "чистое" серверное значение (без клиентского оффсета).
     */
    public static int getSmoothTicker(BlockPos pos, int serverTicker) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.isPaused()) {
            // Просто отдаём то, что есть, без всяких прибавлений
            return serverTicker;
        }

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

    /**
     * Устаревшие данные = игра сейчас на паузе.
     * В таком состоянии таймер должен показать "-- --".
     */
    public static boolean isStale(BlockPos pos, int serverTicker) {
        return MinecraftClient.getInstance().isPaused();
    }

    public static void clear() {
        DATA.clear();
    }
}