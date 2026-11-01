package io.github.gamermax1254.tlcountdown.client;

import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import net.minecraft.util.math.BlockPos;

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

    // Если светофор не рендерился дольше этого времени (например, чанк выгрузился),
    // сохранённый отсчёт фазы уже недействителен.
    private static final int STALE_GAP_TICKS = 20 * 5;

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

        // Первый замкнутый цикл содержит частично наблюдённую первую фазу,
        // поэтому он используется только для «прогрева» и не сохраняется.
        boolean primed = false;
    }

    /**
     * Вызывается каждый рендер-кадр для REMOTE-светофора.
     * Использует общий тикер, который тикает вместе с игровыми тиками.
     */
    public static void observe(TrafficLightBlockEntity be, long globalTick) {
        long key = be.getPos().asLong();
        State st = STATES.computeIfAbsent(key, k -> new State());

        boolean gap = st.inactivityTicks > STALE_GAP_TICKS;
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

        if (gap) {
            // Светофор долго не рендерился: старый отсчёт фазы больше не действителен.
            // Синхронизируем начало фазы с текущим тиком. Уже выученный цикл сохраняем,
            // чтобы цифры не пропадали; следующая смена фазы уточнит отсчёт.
            st.observedPhases.clear();
            st.currentColors = nowColors;
            st.currentPhaseStartTick = (int) globalTick;
            if (st.learnedCycle == null) {
                st.confirmedCycles = 0;
                st.primed = false;
            }
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

        if (!st.primed) {
            // Первый замкнутый цикл содержит частично наблюдённую первую фазу:
            // её длительность равна не полной фазе, а остатку с момента начала
            // наблюдения. Если сохранить её как настоящую, таймер будет считать,
            // что фаза уже закончилась, и бесконечно переучиваться.
            // Поэтому первый цикл только «прогревает» состояние.
            st.primed = true;
            st.observedPhases.clear();
            st.learnedCycle = null;
            st.confirmedCycles = 0;
            return;
        }

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
    }

    private static boolean cyclesMatch(List<Phase> a, List<Phase> b) {
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).colors.equals(b.get(i).colors)) return false;
            // Небольшой допуск на длительность (±10 тиков = 0.5 сек):
            // тайминги контроллера и клиентские тики слегка расходятся.
            if (Math.abs(a.get(i).durationTicks - b.get(i).durationTicks) > 10) return false;
        }
        return true;
    }

    /**
     * Возвращает количество тиков до следующего переключения,
     * или -1, если учёба ещё не завершена.
     */
    public static int getRemainingTicks(TrafficLightBlockEntity be, long globalTick) {
        long key = be.getPos().asLong();
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
        long key = be.getPos().asLong();
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

    public static void clear() {
        STATES.clear();
    }
}