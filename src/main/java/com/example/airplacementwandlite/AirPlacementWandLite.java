package com.example.airplacementwandlite;

import com.example.airplacementwandlite.item.ModDataComponents;
import com.example.airplacementwandlite.network.WandDistancePacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AirPlacementWandLite.MOD_ID)
public class AirPlacementWandLite {
    public static final String MOD_ID = "airplacementwandlite";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public AirPlacementWandLite(IEventBus modEventBus) {
        LOGGER.info("Air Placement Wand Lite initializing...");
        ModRegistry.register(modEventBus);
        ModDataComponents.register(modEventBus);

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerPayloads);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("Air Placement Wand Lite client setup complete.");
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID).versioned("1.0");

        registrar.playToServer(
                WandDistancePacket.TYPE,
                WandDistancePacket.STREAM_CODEC,
                WandDistancePacket::handle
        );
    }
}
