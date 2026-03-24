package com.example.airplacementwandlite.item;

import com.example.airplacementwandlite.AirPlacementWandLite;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, AirPlacementWandLite.MOD_ID);

    /** 空中設置杖の設置距離 (1-15) */
    public static final Supplier<DataComponentType<Integer>> WAND_DISTANCE =
            DATA_COMPONENTS.register("wand_distance", () ->
                    DataComponentType.<Integer>builder()
                            .persistent(Codec.INT)
                            .networkSynchronized(ByteBufCodecs.INT)
                            .build());

    /** 空中設置杖で設置したブロックの位置リスト */
    public static final Supplier<DataComponentType<List<BlockPos>>> WAND_PLACED_BLOCKS =
            DATA_COMPONENTS.register("wand_placed_blocks", () ->
                    DataComponentType.<List<BlockPos>>builder()
                            .persistent(BlockPos.CODEC.listOf())
                            .networkSynchronized(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()))
                            .build());

    public static void register(IEventBus bus) {
        DATA_COMPONENTS.register(bus);
    }
}
