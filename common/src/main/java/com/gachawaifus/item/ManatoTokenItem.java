package com.gachawaifus.item;

import com.gachawaifus.color.ColorTooltip;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.entity.ManatoEntity;
import com.gachawaifus.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import java.util.List;

public class ManatoTokenItem extends Item {
    public ManatoTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player != null) {
            List<ManatoEntity> existing = level.getEntitiesOfClass(ManatoEntity.class,
                    player.getBoundingBox().inflate(128.0D),
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));
            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes a Komano Manato invocado/a! Solo puedes tener 1 activo/a a la vez."), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        ManatoEntity entity = ModEntities.MANATO.get().create(level);
        if (entity != null) {
            entity.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);
            if (player != null) {
                entity.tame(player);
                player.displayClientMessage(Component.translatable("message.gachawaifus.manato_summoned"), true);
            }
            level.addFreshEntity(entity);
            level.playSound(null, spawnPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D,
                        spawnPos.getZ() + 0.5D, 16, 0.4D, 0.6D, 0.4D, 0.05D);
                serverLevel.sendParticles(ParticleTypes.NOTE, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.2D,
                        spawnPos.getZ() + 0.5D, 12, 0.4D, 0.6D, 0.4D, 0.3D);
            }
            if (player != null && !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        ColorTooltip.append(tooltipComponents, WaifuColor.NARANJA);
        tooltipComponents.add(Component.literal("§7Invoca a §6Komano Manato§7 (Naranja / Attack)."));
        tooltipComponents.add(Component.literal("§8Blazing Wind Slash · Return to Ashes - Fall · Musou Aratama"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
