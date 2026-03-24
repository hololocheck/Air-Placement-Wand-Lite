package com.example.airplacementwandlite;

import com.example.airplacementwandlite.block.FrameBlock;
import com.example.airplacementwandlite.item.AirPlacementWandItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRegistry {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(Registries.BLOCK, AirPlacementWandLite.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, AirPlacementWandLite.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AirPlacementWandLite.MOD_ID);

    // ===== Block =====
    public static final Supplier<FrameBlock> FRAME_BLOCK =
            BLOCKS.register("frame_block", FrameBlock::new);

    // ===== Item =====
    public static final Supplier<BlockItem> FRAME_BLOCK_ITEM =
            ITEMS.register("frame_block", () ->
                    new BlockItem(FRAME_BLOCK.get(), new Item.Properties()));

    public static final Supplier<AirPlacementWandItem> AIR_PLACEMENT_WAND =
            ITEMS.register("air_placement_wand", () ->
                    new AirPlacementWandItem(new Item.Properties()));

    // ===== Creative Tab =====
    public static final Supplier<CreativeModeTab> TAB = CREATIVE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.airplacementwandlite"))
                    .icon(() -> AIR_PLACEMENT_WAND.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(AIR_PLACEMENT_WAND.get());
                        output.accept(FRAME_BLOCK_ITEM.get());
                    })
                    .build());

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        CREATIVE_TABS.register(bus);
    }
}
