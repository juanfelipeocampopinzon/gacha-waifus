package com.gachawaifus.item;

import com.gachawaifus.color.ColorTooltip;
import com.gachawaifus.color.WaifuColor;

import com.gachawaifus.entity.BurniceWhiteEntity;
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

public class BurniceWhiteTokenItem extends Item {

    public BurniceWhiteTokenItem(Properties properties) {
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
            AABB searchArea = player.getBoundingBox().inflate(128.0D);
            List<BurniceWhiteEntity> existing = level.getEntitiesOfClass(BurniceWhiteEntity.class, searchArea,
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));

            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes una Burnice White activa! Solo puedes tener 1 activa a la vez."
                ), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        BurniceWhiteEntity burniceWhite = ModEntities.BURNICE_WHITE.get().create(level);
        if (burniceWhite != null) {
            burniceWhite.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);

            if (player != null) {
                burniceWhite.tame(player);
                player.displayClientMessage(Component.literal("§c[GachaWaifus] ¡Burnice White ha sido invocada!"), true);
            }

            level.addFreshEntity(burniceWhite);

            level.playSound(null, spawnPos, SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.2F, 1.0F);
            level.playSound(null, spawnPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                        25, 0.4, 0.6, 0.4, 0.05);
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                        6, 0.2, 0.4, 0.2, 0.05);
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
        ColorTooltip.append(tooltipComponents, WaifuColor.NARANJA);
        tooltipComponents.add(Component.literal("§7Invoca a §cBurnice White§7 (Naranja / DPS)."));
        tooltipComponents.add(Component.literal("§8Sons of Calydon"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}