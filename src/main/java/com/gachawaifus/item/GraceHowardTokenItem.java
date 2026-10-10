package com.gachawaifus.item;

import com.gachawaifus.color.ColorTooltip;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.entity.GraceHowardEntity;
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

/**
 * Token de invocación de <b>Grace Howard</b>. Mismo patrón que {@code RinaTokenItem}: solo una
 * Grace activa por jugador y el token se gasta al invocar.
 *
 * <p>La IA lo dejó como una clase vacía (un {@code Item} sin {@code useOn}), así que el token se
 * podía craftear y tener en la mano pero <b>no invocaba nada</b> al usarlo. Reescrito en v3.9.1.
 */
public class GraceHowardTokenItem extends Item {
    public GraceHowardTokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player != null) {
            List<GraceHowardEntity> existing = level.getEntitiesOfClass(GraceHowardEntity.class,
                    player.getBoundingBox().inflate(128.0D),
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));
            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes una Grace Howard activa! Solo puedes tener 1 activa a la vez."), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        GraceHowardEntity entity = ModEntities.GRACE_HOWARD.get().create(level);
        if (entity != null) {
            entity.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);
            if (player != null) {
                entity.tame(player);
                player.displayClientMessage(Component.translatable("message.gachawaifus.grace_howard_summoned"), true);
            }
            level.addFreshEntity(entity);
            level.playSound(null, spawnPos, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.2F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLASH, spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D,
                        spawnPos.getZ() + 0.5D, 2, 0.2D, 0.5D, 0.2D, 0.0D);
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
        ColorTooltip.append(tooltipComponents, WaifuColor.AZUL);   // §7Color, fuerte y débil
        tooltipComponents.add(Component.literal("§7Invoca a §bGrace Howard§7 (Azul / Anomalía)."));
        tooltipComponents.add(Component.literal("§8Charged Bolt · Magnetic Field · Overclocked Barrage"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
