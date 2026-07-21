package io.github.gamermax1254.tlcountdown.forge;

import io.github.gamermax1254.tlcountdown.TrafficLightCountdown;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(TrafficLightCountdown.MOD_ID)
public class TrafficLightCountdownForge {
    public TrafficLightCountdownForge() {
        TrafficLightCountdown.init();

        // На Forge инициализируем клиентскую часть только если мы на клиенте
        if (FMLEnvironment.dist == Dist.CLIENT) {
            TrafficLightCountdown.initClient();
        }
    }
}