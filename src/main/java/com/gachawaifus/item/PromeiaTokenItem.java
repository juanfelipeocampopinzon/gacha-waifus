package com.gachawaifus.item;

import com.gachawaifus.entity.PromeiaEntity;
import com.gachawaifus.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.AABB;
import java.util.List;

public class PromeiaTokenItem extends Item {
    public PromeiaTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player != null) {
            List<PromeiaEntity> existing = level.getEntitiesOfClass(PromeiaEntity.class,
                    player.getBoundingBox().inflate(128.0D),
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));
            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes una Promeia activa!"), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        PromeiaEntity entity = ModEntities.PROMEIA.get().create(level);
        if (entity != null) {
            entity.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);
            if (player != null) entity.tame(player);
            level.addFreshEntity(entity);
            level.playSound(null, spawnPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FLASH, spawnPos.getX()+0.5, spawnPos.getY()+1.0, spawnPos.getZ()+0.5, 2, 0.2, 0.5, 0.2, 0.0);
            }
            if (player != null && !player.getAbilities().instabuild) context.getItemInHand().shrink(1);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7Un token para invocar a la juez del Krampus Compliance Authority."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
