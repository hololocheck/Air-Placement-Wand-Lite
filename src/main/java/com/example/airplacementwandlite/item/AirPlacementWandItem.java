package com.example.airplacementwandlite.item;

import com.example.airplacementwandlite.ModRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 空中設置杖 - 耐久値を消費してフレームブロックを空中に設置できるアイテム。
 * 耐久値400（フレームブロック400個分）。
 * 耐久・修繕エンチャント対応。
 * シフト+右クリックでこの杖で設置した全フレームブロックを一括破壊。
 */
public class AirPlacementWandItem extends Item {

    public static final int MIN_DISTANCE = 1;
    public static final int MAX_DISTANCE = 15;
    public static final int DEFAULT_DISTANCE = 5;

    public AirPlacementWandItem(Properties properties) {
        super(properties.durability(400)
                .component(ModDataComponents.WAND_DISTANCE.get(), DEFAULT_DISTANCE)
                .component(ModDataComponents.WAND_PLACED_BLOCKS.get(), List.of()));
    }

    @Override
    public int getEnchantmentValue() {
        return 15;
    }

    public static int getDistance(ItemStack stack) {
        Integer dist = stack.get(ModDataComponents.WAND_DISTANCE.get());
        return dist != null ? Mth.clamp(dist, MIN_DISTANCE, MAX_DISTANCE) : DEFAULT_DISTANCE;
    }

    public static void setDistance(ItemStack stack, int distance) {
        stack.set(ModDataComponents.WAND_DISTANCE.get(), Mth.clamp(distance, MIN_DISTANCE, MAX_DISTANCE));
    }

    /**
     * プレイヤーの視線方向から設置先のBlockPosを計算する。
     */
    public static BlockPos getTargetPos(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!stack.is(ModRegistry.AIR_PLACEMENT_WAND.get())) {
            stack = player.getOffhandItem();
        }
        int dist = getDistance(stack);
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        Vec3 target = eye.add(look.scale(dist));
        return BlockPos.containing(target);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        ItemStack stack = context.getItemInHand();
        EquipmentSlot slot = LivingEntity.getSlotForHand(context.getHand());

        // シフト+右クリック: この杖で設置した全フレームブロックを一括破壊
        if (player.isShiftKeyDown()) {
            return removeAllPlaced(level, player, stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        BlockPos placePos = context.getClickedPos().relative(context.getClickedFace());
        return tryPlace(level, player, stack, placePos, slot) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        EquipmentSlot slot = LivingEntity.getSlotForHand(hand);

        // シフト+右クリック: この杖で設置した全フレームブロックを一括破壊
        if (player.isShiftKeyDown()) {
            if (removeAllPlaced(level, player, stack)) {
                return InteractionResultHolder.success(stack);
            }
            return InteractionResultHolder.pass(stack);
        }

        // 空中設置: 視線方向の固定距離に設置
        BlockPos placePos = getTargetPos(player);
        if (tryPlace(level, player, stack, placePos, slot)) {
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    /**
     * この杖で設置した全フレームブロックを一括破壊する。
     */
    private boolean removeAllPlaced(Level level, Player player, ItemStack stack) {
        List<BlockPos> placedBlocks = getPlacedBlocks(stack);
        if (placedBlocks.isEmpty()) return false;

        if (!level.isClientSide()) {
            int removed = 0;
            for (BlockPos pos : placedBlocks) {
                BlockState state = level.getBlockState(pos);
                if (state.is(ModRegistry.FRAME_BLOCK.get())) {
                    level.destroyBlock(pos, false);
                    removed++;
                }
            }
            // リストをクリア
            stack.set(ModDataComponents.WAND_PLACED_BLOCKS.get(), List.of());

            if (removed > 0) {
                level.playSound(null, player.blockPosition(), SoundEvents.SCAFFOLDING_BREAK,
                        SoundSource.BLOCKS, 1.0f, 1.0f);
                player.displayClientMessage(
                        Component.translatable("message.airplacementwandlite.wand_removed", removed)
                                .withStyle(ChatFormatting.YELLOW), true);
            }
        }

        return true;
    }

    private boolean tryPlace(Level level, Player player, ItemStack stack, BlockPos placePos, EquipmentSlot slot) {
        if (!level.getBlockState(placePos).isAir()) {
            return false;
        }

        // 耐久値チェック（クリエイティブは免除）
        if (!player.isCreative()) {
            if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(
                            Component.translatable("message.airplacementwandlite.wand_no_durability")
                                    .withStyle(ChatFormatting.RED), true);
                }
                return false;
            }
        }

        if (!level.isClientSide()) {
            // フレームブロックを設置
            BlockState frameState = ModRegistry.FRAME_BLOCK.get().defaultBlockState();
            level.setBlock(placePos, frameState, 3);
            level.playSound(null, placePos, SoundEvents.SCAFFOLDING_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);

            // 設置位置を記録
            List<BlockPos> placed = new ArrayList<>(getPlacedBlocks(stack));
            placed.add(placePos.immutable());
            stack.set(ModDataComponents.WAND_PLACED_BLOCKS.get(), placed);

            // 耐久値消費（クリエイティブは免除）
            if (!player.isCreative()) {
                stack.hurtAndBreak(1, player, slot);
            }
        }

        return true;
    }

    private static List<BlockPos> getPlacedBlocks(ItemStack stack) {
        List<BlockPos> list = stack.get(ModDataComponents.WAND_PLACED_BLOCKS.get());
        return list != null ? list : List.of();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int distance = getDistance(stack);
        tooltip.add(Component.translatable("tooltip.airplacementwandlite.wand_distance", distance)
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.airplacementwandlite.wand_item_desc")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.airplacementwandlite.wand_use")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.airplacementwandlite.wand_shift_use")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.airplacementwandlite.wand_shift_scroll")
                .withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.airplacementwandlite.wand_shift_middle")
                .withStyle(ChatFormatting.GOLD));
    }
}
