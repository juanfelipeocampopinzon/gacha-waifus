package com.gachawaifus.item;

import com.gachawaifus.entity.AbstractWaifuEntity;
import com.gachawaifus.gacha.WaifuRoster;
import com.gachawaifus.gacha.WaifuStorageSavedData;
import com.gachawaifus.menu.WaifuStorageMenu;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class WaifuCapsuleItem extends Item {

    public WaifuCapsuleItem(Properties properties) {
        super(properties);
    }

    /** Agachado + clic en tu waifu: la guarda en la cápsula (despawnea y queda a salvo server-side). */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(entity instanceof AbstractWaifuEntity waifu)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable("item.gachawaifus.waifu_capsule_item.1"), true);
            return InteractionResult.PASS;
        }

        WaifuRoster.Entry entry = WaifuRoster.byId(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(waifu.getType()).getPath());
        if (entry == null) {
            return InteractionResult.PASS;
        }
        if (waifu.getOwnerUUID() == null || !waifu.getOwnerUUID().equals(player.getUUID())) {
            player.displayClientMessage(Component.translatable("item.gachawaifus.waifu_capsule_item.2"), true);
            return InteractionResult.FAIL;
        }

        WaifuStorageSavedData storage = WaifuStorageSavedData.get(player.level());
        if (storage.contains(player.getUUID(), entry.id())) {
            player.displayClientMessage(Component.translatable("gachawaifus.capsule.already_have", entry.name()), true);
            return InteractionResult.FAIL;
        }

        storage.add(player.getUUID(), entry.id());
        storage.setAffinity(player.getUUID(), entry.id(), waifu.affinity());
        ServerLevel serverLevel = (ServerLevel) player.level();
        serverLevel.sendParticles(ParticleTypes.FLASH, waifu.getX(), waifu.getY() + 1.0, waifu.getZ(), 2, 0.2, 0.4, 0.2, 0.0);
        serverLevel.sendParticles(ParticleTypes.HEART, waifu.getX(), waifu.getY() + 1.2, waifu.getZ(), 8, 0.4, 0.5, 0.4, 0.2);
        serverLevel.playSound(null, waifu.blockPosition(), SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0F, 1.4F);
        waifu.discard();
        player.displayClientMessage(Component.translatable("gachawaifus.capsule.stored", entry.name()), true);
        return InteractionResult.CONSUME;
    }

    /** Clic derecho en el aire: abre la "caja" con las waifus guardadas. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable("item.gachawaifus.waifu_capsule_item.3"), true);
            return InteractionResultHolder.pass(stack);
        }

        MenuProvider provider = new SimpleMenuProvider(
                (containerId, playerInventory, p) -> new WaifuStorageMenu(containerId, playerInventory),
                Component.translatable("item.gachawaifus.waifu_capsule_item.4"));
        player.openMenu(provider);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.waifu_capsule.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
