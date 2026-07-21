package io.github.gamermax1254.tlcountdown;

import io.github.gamermax1254.tlcountdown.client.ClientTickTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrafficLightCountdown {
    public static final String MOD_ID = "tlcountdown";
    public static final String MOD_NAME = "TrafficCraft: Traffic Light Countdown";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("{} initialized", MOD_NAME);
    }

    // Отдельный метод для клиентской части
    public static void initClient() {
        ClientTickTracker.init();
    }
}