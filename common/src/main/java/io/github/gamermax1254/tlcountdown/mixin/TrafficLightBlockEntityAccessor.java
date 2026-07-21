package io.github.gamermax1254.tlcountdown.mixin;

import de.mrjulsen.trafficcraft.block.entity.TrafficLightBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TrafficLightBlockEntity.class)
public interface TrafficLightBlockEntityAccessor {

    @Accessor(value = "ticker", remap = false)
    int tlcountdown$getTicker();
}