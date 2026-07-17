package io.github.gamermax1254.tlcountdown.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficLightCountdownClient implements ClientModInitializer {

    public static final String MOD_ID = "tlcountdown";
    public static final String MOD_NAME = "TrafficCraft: Traffic Light Countdown";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ClientTickTracker.init();
        LOGGER.info("{} initialized", MOD_NAME);
    }
}