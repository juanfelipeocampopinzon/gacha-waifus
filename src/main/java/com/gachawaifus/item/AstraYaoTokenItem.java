package com.gachawaifus.item;

import com.gachawaifus.entity.AstraYaoEntity;
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

import java.util.List;

public class AstraYaoTokenItem extends Item {

    public AstraYaoTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockPos spawnPos = clickedPos.relative(face);

        AstraYaoEntity astraYao = ModEntities.ASTRA_YAO.get().create(level);
        if (astraYao != null) {
            astraYao.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);

            if (player != null) {
                astraYao.tame(player);
                player.displayClientMessage(Component.translatable("message.gachawaifus.astra_yao_summoned"), true);
            }

            level.addFreshEntity(astraYao);

            // Efecto de sonido y fanfarria al invocar
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

            // Consumir 1 token si no está en creativo
            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }

            return InteractionResult.CONSUME;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.gachawaifus.astra_yao_token.desc"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
