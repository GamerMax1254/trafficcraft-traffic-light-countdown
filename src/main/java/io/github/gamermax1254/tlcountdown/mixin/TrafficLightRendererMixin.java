package io.github.gamermax1254.tlcountdown.mixin;

import de.mrjulsen.mcdragonlib.client.ber.BERGraphics;
import de.mrjulsen.trafficcraft.block.TrafficLightBlock;
import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.data.TrafficLightModel;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import de.mrjulsen.trafficcraft.client.ber.TrafficLightBlockEntityRenderer;
import io.github.gamermax1254.tlcountdown.client.ClientTickTracker;
import io.github.gamermax1254.tlcountdown.client.CountdownTextRenderer;
import io.github.gamermax1254.tlcountdown.client.TimerCalculator;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.gamermax1254.tlcountdown.client.ActivePhase;
import de.mrjulsen.trafficcraft.block.data.TrafficLightControlType;
import io.github.gamermax1254.tlcountdown.client.RemoteScheduleLearner;

@Mixin(TrafficLightBlockEntityRenderer.class)
public class TrafficLightRendererMixin {

    @Inject(method = "renderBlock*", at = @At("HEAD"), remap = false)
    private void tlcountdown$drawCountdown(BERGraphics<TrafficLightBlockEntity> graphics, float partialTick, CallbackInfo ci) {
        TrafficLightBlockEntity be = graphics.blockEntity();
        if (be == null) return;

        BlockState state = be.getCachedState();
        TrafficLightModel model = state.get(TrafficLightBlock.MODEL);
        int segments = model.getLightsCount();

        if (segments <= 1) return;

        TrafficLightControlType controlType = be.getControlType();

        int remaining;
        int currentPhaseTotal;

        if (controlType == TrafficLightControlType.OWN_SCHEDULE) {
            // Свой режим — считаем из расписания
            if (!be.isRunning()) return;

            int quickCheck = TimerCalculator.getRemainingTicks(be);
            if (quickCheck <= 0) return;

            int serverTicker = ((TrafficLightBlockEntityAccessor) be).tlcountdown$getTicker();
            int smoothTicker = ClientTickTracker.getSmoothTicker(be.getPos(), serverTicker);
            remaining = TimerCalculator.remainingForTicker(be.getSchedule(), smoothTicker);
            currentPhaseTotal = getCurrentPhaseTotalTicks(be, smoothTicker);

        } else if (controlType == TrafficLightControlType.REMOTE) {
            // Управление контроллером — учимся по наблюдению
            long globalTick = ClientTickTracker.getGlobalTicks();
            RemoteScheduleLearner.observe(be, globalTick);

            remaining = RemoteScheduleLearner.getRemainingTicks(be, globalTick);
            if (remaining <= 0) return; // ещё учимся или ошибка
            currentPhaseTotal = RemoteScheduleLearner.getCurrentPhaseTotalTicks(be);

        } else {
            // STATIC или неизвестный — не показываем
            return;
        }

        if (remaining <= 0) return;

        int seconds = TimerCalculator.ticksToDisplaySeconds(remaining);
        if (seconds <= 0) return;

// Если вся текущая фаза короче 3 сек — не показываем
        if (currentPhaseTotal < 60) return;

// Определяем цвет
        ActivePhase phase = ActivePhase.detect(be);
        if (phase != ActivePhase.RED && phase != ActivePhase.GREEN) return;

        boolean stale = ClientTickTracker.isStale(be.getPos(), 0);

        int[] targetSlots = pickTargetSlots(be, segments);
        for (int slot : targetSlots) {
            CountdownTextRenderer.renderOnSlot(
                    graphics.poseStack(),
                    graphics.multiBufferSource(),
                    slot,
                    segments,
                    seconds,
                    stale,
                    phase.color,
                    graphics.packedLight(),
                    ClientTickTracker.getGlobalTicks()
            );
        }
    }

    /**
     * Возвращает массив слотов, на которых нужно нарисовать таймер.
     *  - 3 сегмента: только слот с YELLOW
     *  - 2 сегмента: ОБА слота (пешеходный — рисуем на каждом)
     *  - иначе: []
     */
    private static int[] pickTargetSlots(TrafficLightBlockEntity be, int segments) {
        if (segments == 3) {
            for (int i = 0; i < be.getColorSlotCount() && i < segments; i++) {
                TrafficLightColor c = be.getColorOfSlot(i);
                if (c != null && c.isSimilar(TrafficLightColor.YELLOW)) {
                    return new int[]{i};
                }
            }
            return new int[]{1};
        }

        if (segments == 2) {
            // Ищем сегмент, где цвет НЕ активен, чтобы цифра не сливалась
            int fallback = -1;
            for (int i = 0; i < be.getColorSlotCount() && i < segments; i++) {
                TrafficLightColor c = be.getColorOfSlot(i);
                if (c == null || c == TrafficLightColor.NONE) continue;
                boolean lit = be.isColorEnabled(c, true);
                if (!lit) return new int[]{i};
                fallback = i;
            }
            return fallback >= 0 ? new int[]{fallback} : new int[]{};
        }

        return new int[]{};
    }

    /**
     * Логика выбора сегмента:
     *  - 3 сегмента: сегмент с YELLOW (или похожим по группе — например F4)
     *  - 2 сегмента: сегмент, который сейчас НЕ горит. (чтобы цифра не сливалась с лампой)
     *  - иначе: -1
     */
    private static int pickTargetSlot(TrafficLightBlockEntity be, int segments) {
        if (segments == 3) {
            for (int i = 0; i < be.getColorSlotCount() && i < segments; i++) {
                TrafficLightColor c = be.getColorOfSlot(i);
                if (c != null && c.isSimilar(TrafficLightColor.YELLOW)) {
                    return i;
                }
            }
            return 1; // на всякий: средний слот
        }

        if (segments == 2) {
            int fallback = -1;
            for (int i = 0; i < be.getColorSlotCount() && i < segments; i++) {
                TrafficLightColor c = be.getColorOfSlot(i);
                if (c == null || c == TrafficLightColor.NONE) continue;
                boolean lit = be.isColorEnabled(c, true);
                if (!lit) return i;
                fallback = i;
            }
            return fallback;
        }

        return -1;
    }

    /**
     * Возвращает длительность (в тиках) той фазы расписания, в которой сейчас находится ticker.
     */
    private static int getCurrentPhaseTotalTicks(TrafficLightBlockEntity be, int ticker) {
        var schedule = be.getSchedule();
        if (schedule == null) return 0;
        int total = schedule.getTotalDurationTicks();
        if (total <= 0) return 0;

        int normalized = schedule.isLoop() ? Math.floorMod(ticker, total) : Math.min(Math.max(ticker, 0), total);

        int keyTime = 0;
        for (var entry : schedule.getEntries()) {
            int dur = entry.getDurationTicks();
            keyTime += dur;
            if (normalized < keyTime) {
                return dur;
            }
        }
        return 0;
    }
}