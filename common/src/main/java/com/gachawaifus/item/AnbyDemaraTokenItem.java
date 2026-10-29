package com.gachawaifus.item;

import com.gachawaifus.color.ColorTooltip;
import com.gachawaifus.color.WaifuColor;

import com.gachawaifus.entity.AnbyDemaraEntity;
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

public class AnbyDemaraTokenItem extends Item {

    public AnbyDemaraTokenItem(Properties properties) {
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
            // Verificar si el jugador ya tiene una Anby Demara activa en el mundo
            AABB searchArea = player.getBoundingBox().inflate(128.0D);
            List<AnbyDemaraEntity> existing = level.getEntitiesOfClass(AnbyDemaraEntity.class, searchArea,
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));

            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes una Anby Demara activa! Solo puedes tener 1 activa a la vez."
                ), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        AnbyDemaraEntity anbyDemara = ModEntities.ANBY_DEMARA.get().create(level);
        if (anbyDemara != null) {
            anbyDemara.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);

            if (player != null) {
                anbyDemara.tame(player);
                player.displayClientMessage(Component.literal("§a[GachaWaifus] ¡Anby Demara ha sido invocada!"), true);
            }

            level.addFreshEntity(anbyDemara);

            level.playSound(null, spawnPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            level.playSound(null, spawnPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.0F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLASH,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                        2, 0.2, 0.5, 0.2, 0.0);
                serverLevel.sendParticles(ParticleTypes.NOTE,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.2, spawnPos.getZ() + 0.5,
                        15, 0.6, 0.8, 0.6, 0.1);
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
        ColorTooltip.append(tooltipComponents, WaifuColor.MORADO);
        tooltipComponents.add(Component.translatable("item.gachawaifus.anby_demara_token.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}