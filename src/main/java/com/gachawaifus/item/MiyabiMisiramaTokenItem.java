package com.gachawaifus.item;

import com.gachawaifus.entity.MiyabiMisiramaEntity;
import com.gachawaifus.registry.ModEntities;
import net.minecraft.ChatFormatting;
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

public class MiyabiMisiramaTokenItem extends Item {

    public MiyabiMisiramaTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("★★★★★ S-Rank — Hoshimi Miyabi").withStyle(ChatFormatting.AQUA));
        tooltipComponents.add(Component.literal("La Dama de la Luz | Jefa de la Sección 6").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("Elemento: Hielo | Katana Ancestral del Zorro").withStyle(ChatFormatting.WHITE));
        tooltipComponents.add(Component.translatable("item.gachawaifus.miyabi_token.desc").withStyle(ChatFormatting.DARK_AQUA));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player != null) {
            // Verificar si el jugador ya tiene una Miyabi activa en el mundo
            AABB searchArea = player.getBoundingBox().inflate(128.0D);
            List<MiyabiMisiramaEntity> existing = level.getEntitiesOfClass(MiyabiMisiramaEntity.class, searchArea,
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));

            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes una Hoshimi Miyabi activa! Solo puedes tener 1 activa a la vez."
                ), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        MiyabiMisiramaEntity miyabi = ModEntities.MIYABI.get().create(level);
        if (miyabi != null) {
            miyabi.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);

            if (player != null) {
                miyabi.tame(player);
                player.displayClientMessage(Component.translatable("message.gachawaifus.miyabi_summoned"), true);
            }

            level.addFreshEntity(miyabi);

            // Efecto de sonido y fanfarria al invocar
            level.playSound(null, spawnPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.4F);
            level.playSound(null, spawnPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.2F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                        30, 0.5, 0.8, 0.5, 0.1);
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        spawnPos.getX() + 0.5, spawnPos.getY() + 1.0, spawnPos.getZ() + 0.5,
                        15, 0.5, 0.8, 0.5, 0.1);
            }

            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }
}