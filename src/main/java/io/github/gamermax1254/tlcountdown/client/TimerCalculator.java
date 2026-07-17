package io.github.gamermax1254.tlcountdown.client;

import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.data.TrafficLightSchedule;
import de.mrjulsen.trafficcraft.data.TrafficLightScheduleEntryData;
import io.github.gamermax1254.tlcountdown.mixin.TrafficLightBlockEntityAccessor;

public final class TimerCalculator {

    private TimerCalculator() {}

    /**
     * Возвращает количество тиков до следующего переключения фазы.
     * -1 — если таймер показывать нельзя.
     */
    public static int getRemainingTicks(TrafficLightBlockEntity be) {
        if (be == null) return -1;
        if (!be.isRunning()) return -1;
        if (be.getControlType() != TrafficLightControlType.OWN_SCHEDULE) return -1;

        TrafficLightSchedule schedule = be.getSchedule();
        if (schedule == null) return -1;

        int total = schedule.getTotalDurationTicks();
        if (total <= 0) return -1;

        int ticker = ((TrafficLightBlockEntityAccessor) be).tlcountdown$getTicker();
        return remainingForTicker(schedule, ticker);
    }

    /**
     * То же самое, но с уже вычисленным (например, "сглаженным") ticker.
     */
    public static int remainingForTicker(TrafficLightSchedule schedule, int ticker) {
        if (schedule == null) return -1;
        int total = schedule.getTotalDurationTicks();
        if (total <= 0) return -1;

        int normalized = schedule.isLoop()
                ? Math.floorMod(ticker, total)
                : Math.min(Math.max(ticker, 0), total);

        int keyTime = 0;
        for (TrafficLightScheduleEntryData entry : schedule.getEntries()) {
            keyTime += entry.getDurationTicks();
            if (normalized < keyTime) {
                return keyTime - normalized;
            }
        }
        return -1;
    }

    /**
     * Секунды с округлением вверх (как на реальных светофорах).
     */
    public static int ticksToDisplaySeconds(int ticks) {
        if (ticks <= 0) return 0;
        return (ticks + 19) / 20;
    }
}