package com.gachawaifus.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

/**
 * Bolita de tirada (rosa = banner promocional, azul = permanente).
 *
 * <p>Lo único que la separa de un {@link Item} normal es {@link #doesSneakBypassUse}: al
 * agacharse, {@code ServerPlayerGameMode} <b>se salta</b> la interacción con el bloque cuando el
 * item de ambas manos devuelve {@code false} (que es el defecto de NeoForge), y así la máquina
 * nunca llegaba a ver el Shift+clic de la tirada x10. Devolviendo {@code true} el bloque sí
 * recibe el clic con la bolita puesta.
 */
public class GachaBallItem extends Item {

    public GachaBallItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}
