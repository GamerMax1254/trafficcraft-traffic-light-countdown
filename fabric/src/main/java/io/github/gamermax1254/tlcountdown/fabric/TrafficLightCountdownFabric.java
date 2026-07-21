package io.github.gamermax1254.tlcountdown.fabric;

import io.github.gamermax1254.tlcountdown.TrafficLightCountdown;
import net.fabricmc.api.ClientModInitializer;

public class TrafficLightCountdownFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        TrafficLightCountdown.init();
        TrafficLightCountdown.initClient();
    }
}