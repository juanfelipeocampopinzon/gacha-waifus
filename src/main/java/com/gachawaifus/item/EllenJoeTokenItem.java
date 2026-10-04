package com.gachawaifus.item;

import com.gachawaifus.entity.EllenJoeEntity;
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

public class EllenJoeTokenItem extends Item {

    public EllenJoeTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player != null) {
            // Restricción: No se pueden tener dos Ellens activas simultáneamente
            AABB searchArea = player.getBoundingBox().inflate(128.0D);
            List<EllenJoeEntity> existing = level.getEntitiesOfClass(EllenJoeEntity.class, searchArea,
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));

            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes una Ellen Joe activa! Solo puedes tener 1 activa a la vez."
                ), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        EllenJoeEntity ellenJoe = ModEntities.ELLEN_JOE.get().create(level);
        if (ellenJoe != null) {
            ellenJoe.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);

            if (player != null) {
                ellenJoe.tame(player);
                player.displayClientMessage(Component.literal("§b[GachaWaifus] ¡Ellen Joe ha sido invocada!"), true);
            }

            level.addFreshEntity(ellenJoe);

            level.playSound(null, spawnPos, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.2F, 1.4F);
            level.playSound(null, spawnPos, SoundEvents.POWDER_SNOW_FALL, SoundSource.PLAYERS, 1.5F, 1.0F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                        25, 0.4, 0.6, 0.4, 0.05);
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                        2, 0.1, 0.1, 0.1, 0.0);
            }

            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("§7Invoca a §bEllen Joe§7 (DPS de Hielo)."));
        tooltipComponents.add(Component.literal("§8Victoria Housekeeping Co."));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
