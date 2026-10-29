package com.gachawaifus.item;

import com.gachawaifus.color.ColorTooltip;
import com.gachawaifus.color.WaifuColor;
import com.gachawaifus.entity.Soldier11Entity;
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
 * Token de invocación de <b>Soldier 11</b>. Mismo patrón que {@code RinaTokenItem} /
 * {@code VonLycaonTokenItem}: solo una Soldier 11 activa por jugador y el token se gasta al invocar.
 *
 * <p>Reescrito a mano (v3.9.1): el código que generó la IA no compilaba — importaba
 * {@code net.minecraft.world.item.UseOnContext} (está en {@code ...item.context}),
 * inventaba {@code entity.setOwner(...)} y {@code level.add(...)}, y firmaba el tooltip con
 * {@code org.eclipse.angus.collections.MutableList}, un paquete que no existe.
 */
public class Soldier11TokenItem extends Item {
    public Soldier11TokenItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player != null) {
            List<Soldier11Entity> existing = level.getEntitiesOfClass(Soldier11Entity.class,
                    player.getBoundingBox().inflate(128.0D),
                    e -> e.isAlive() && player.getUUID().equals(e.getOwnerUUID()));
            if (!existing.isEmpty()) {
                player.displayClientMessage(Component.literal(
                        "§c[GachaWaifus] ¡Ya tienes una Soldier 11 activa! Solo puedes tener 1 activa a la vez."), true);
                return InteractionResult.FAIL;
            }
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        Soldier11Entity entity = ModEntities.SOLDIER_11.get().create(level);
        if (entity != null) {
            entity.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D,
                    player != null ? player.getYRot() : 0.0F, 0.0F);
            if (player != null) {
                entity.tame(player);
                player.displayClientMessage(Component.literal("§6[GachaWaifus] ¡Soldier 11 ha sido invocada!"), true);
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
        ColorTooltip.append(tooltipComponents, WaifuColor.NARANJA);   // §7Color, fuerte y débil
        tooltipComponents.add(Component.literal("§7Invoca a §6Soldier 11§7 (Naranja / Attack)."));
        tooltipComponents.add(Component.literal("§8Fire Suppression · Warmup · Molten Barrage"));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
