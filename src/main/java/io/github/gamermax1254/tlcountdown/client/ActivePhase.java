package io.github.gamermax1254.tlcountdown.client;

import de.mrjulsen.trafficcraft.block.data.TrafficLightColor;
import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;

/**
 * Определяет, какой цвет светофора сейчас "главный" для отображения таймера.
 */
public enum ActivePhase {
    RED(0xFFFF3030),
    YELLOW(0xFFFFCC00),
    GREEN(0xFF30FF30),
    NONE(0);

    public final int color;

    ActivePhase(int color) {
        this.color = color;
    }

    public static ActivePhase detect(TrafficLightBlockEntity be) {
        // Приоритет: RED > YELLOW > GREEN
        if (be.isColorEnabled(TrafficLightColor.RED, true))    return RED;
        if (be.isColorEnabled(TrafficLightColor.YELLOW, true)) return YELLOW;
        if (be.isColorEnabled(TrafficLightColor.GREEN, true))  return GREEN;
        return NONE;
    }
}