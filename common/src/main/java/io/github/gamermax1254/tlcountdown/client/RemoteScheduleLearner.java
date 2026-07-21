package io.github.gamermax1254.tlcountdown.client;

import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import net.minecraft.core.BlockPos;

import java.util.*;

/**
 * Наблюдает за REMOTE-светофорами и учится определять длительности их фаз
 * по факту смены активных цветов.
 */
public final class RemoteScheduleLearner {

    private RemoteScheduleLearner() {}

    // TTL для забывания давно не виденных светофоров
    private static final int TTL_TICKS = 20 * 60 * 5; // 5 минут

    // Сколько зафиксированных полных циклов должно совпасть, прежде чем считать данные надёжными
    private static final int REQUIRED_CYCLES = 1;

    private static final Map<Long, State> STATES = new HashMap<>();

    private static class Phase {
        final Set<TrafficLightColor> colors;
        int durationTicks;

        Phase(Set<TrafficLightColor> colors) {
            this.colors = colors;
            this.durationTicks = 0;
        }
    }

    private static class State {
        Set<TrafficLightColor> currentColors = null;
        int currentPhaseStartTick = 0;

        // Наблюдаемая история этого цикла (обнуляется при обнаружении цикла)
        List<Phase> observedPhases = new ArrayList<>();

        // Замкнутый цикл — что мы "выучили"
        List<Phase> learnedCycle = null;
        int totalCycleTicks = 0;

        // Сколько раз подряд цикл повторился одинаково
        int confirmedCycles = 0;

        int inactivityTicks = 0;

        // Текущий индекс в learnedCycle
        int currentLearnedPhaseIndex = -1;
        int ticksIntoCurrentPhase = 0;

        boolean wasLearnedBefore = false;
    }

    /**
     * Вызывается каждый рендер-кадр для REMOTE-светофора.
     * Использует общий тикер, который тикает вместе с игровыми тиками.
     */
    public static void observe(TrafficLightBlockEntity be, long globalTick) {
        long key = be.getBlockPos().asLong();
        State st = STATES.computeIfAbsent(key, k -> new State());
        st.inactivityTicks = 0;

        Set<TrafficLightColor> nowColors = new HashSet<>(be.getEnabledColors());

        if (st.currentColors == null) {
            // Первое наблюдение
            st.currentColors = nowColors;
            st.currentPhaseStartTick = (int) globalTick;
            return;
        }

        if (nowColors.equals(st.currentColors)) {
            // Фаза продолжается
            return;
        }

        // === Смена фазы! ===
        int duration = (int) globalTick - st.currentPhaseStartTick;
        Phase completed = new Phase(st.currentColors);
        completed.durationTicks = duration;
        st.observedPhases.add(completed);

        st.currentColors = nowColors;
        st.currentPhaseStartTick = (int) globalTick;

        // Проверяем — не замкнулся ли цикл?
        tryCloseCycle(st, nowColors);
    }

    private static void tryCloseCycle(State st, Set<TrafficLightColor> newFirstColors) {
        if (st.observedPhases.isEmpty()) return;

        // Замыкание = новая начатая фаза совпадает с первой в observedPhases
        Set<TrafficLightColor> firstObserved = st.observedPhases.get(0).colors;
        if (!newFirstColors.equals(firstObserved)) return;

        // Цикл замкнулся!
        List<Phase> newCycle = new ArrayList<>(st.observedPhases);
        int newTotal = newCycle.stream().mapToInt(p -> p.durationTicks).sum();

        if (st.learnedCycle == null) {
            // Первый выученный цикл
            st.learnedCycle = newCycle;
            st.totalCycleTicks = newTotal;
            st.confirmedCycles = 1;
        } else if (cyclesMatch(st.learnedCycle, newCycle)) {
            // Совпал с предыдущим — увеличиваем confidence
            st.confirmedCycles++;
            // Обновляем данные (маленькая коррекция длительностей)
            st.learnedCycle = newCycle;
            st.totalCycleTicks = newTotal;
        } else {
            // Не совпал — значит расписание нестабильное или сложное
            // Начинаем заново
            st.learnedCycle = newCycle;
            st.totalCycleTicks = newTotal;
            st.confirmedCycles = 1;
        }

        // Обнуляем наблюдение для следующего цикла
        st.observedPhases.clear();

        // Если мы восстановились после сброса — снимаем флаг переобучения
        if (st.learnedCycle != null && st.confirmedCycles >= REQUIRED_CYCLES) {
            st.wasLearnedBefore = false;
        }
    }

    private static boolean cyclesMatch(List<Phase> a, List<Phase> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).colors.equals(b.get(i).colors)) return false;
            // Небольшой допуск на длительность (±2 тика)
            if (Math.abs(a.get(i).durationTicks - b.get(i).durationTicks) > 2) return false;
        }
        return true;
    }

    /**
     * Возвращает количество тиков до следующего переключения,
     * или -1, если учёба ещё не завершена.
     */
    public static int getRemainingTicks(TrafficLightBlockEntity be, long globalTick) {
        long key = be.getBlockPos().asLong();
        State st = STATES.get(key);
        if (st == null) return -1;
        if (st.learnedCycle == null || st.confirmedCycles < REQUIRED_CYCLES) return -1;

        Set<TrafficLightColor> nowColors = new HashSet<>(be.getEnabledColors());
        int ticksIntoPhase = (int) globalTick - st.currentPhaseStartTick;

        for (Phase p : st.learnedCycle) {
            if (p.colors.equals(nowColors)) {
                int remaining = p.durationTicks - ticksIntoPhase;
                if (remaining < 0) return -1;
                return remaining;
            }
        }
        return -1;
    }

    /**
     * Длительность текущей фазы (для правила «< 3 сек — не показывать»).
     */
    public static int getCurrentPhaseTotalTicks(TrafficLightBlockEntity be) {
        long key = be.getBlockPos().asLong();
        State st = STATES.get(key);
        if (st == null || st.learnedCycle == null) return 0;

        Set<TrafficLightColor> nowColors = new HashSet<>(be.getEnabledColors());
        for (Phase p : st.learnedCycle) {
            if (p.colors.equals(nowColors)) {
                return p.durationTicks;
            }
        }
        return 0;
    }

    /** Тикает каждый клиентский тик, чистит старые записи. */
    public static void tick() {
        Iterator<Map.Entry<Long, State>> it = STATES.entrySet().iterator();
        while (it.hasNext()) {
            State s = it.next().getValue();
            s.inactivityTicks++;
            if (s.inactivityTicks > TTL_TICKS) {
                it.remove();
            }
        }
    }

    /**
     * Сбрасывает обучение для конкретного светофора.
     * Вызывается, когда обнаружен рассинхрон.
     */
    public static void resetLearning(TrafficLightBlockEntity be) {
        State st = STATES.get(be.getBlockPos().asLong());
        if (st != null) {
            st.learnedCycle = null;
            st.confirmedCycles = 0;
            st.observedPhases.clear();
            st.wasLearnedBefore = true;
        }
    }

    /**
     * Проверяет, есть ли уже выученный цикл (нужен для определения "stale" в mixin).
     */
    public static boolean hasLearnedCycle(TrafficLightBlockEntity be) {
        State st = STATES.get(be.getBlockPos().asLong());
        return st != null && st.learnedCycle != null;
    }

    /**
     * Возвращает true, если светофор сейчас в состоянии переобучения
     * (раньше учились, но данные сбросились и мы восстанавливаемся).
     */
    public static boolean isRelearning(TrafficLightBlockEntity be) {
        State st = STATES.get(be.getBlockPos().asLong());
        return st != null && st.learnedCycle == null;
    }

    public static void clear() {
        STATES.clear();
    }
}