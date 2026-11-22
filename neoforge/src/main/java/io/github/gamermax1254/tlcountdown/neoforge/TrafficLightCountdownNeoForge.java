package io.github.gamermax1254.tlcountdown.neoforge;

import io.github.gamermax1254.tlcountdown.TrafficLightCountdown;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(TrafficLightCountdown.MOD_ID)
public class TrafficLightCountdownNeoForge {
    public TrafficLightCountdownNeoForge() {
        TrafficLightCountdown.init();

        // На NeoForge инициализируем клиентскую часть только если мы на клиенте
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TrafficLightCountdown.initClient();
        }
    }
}
