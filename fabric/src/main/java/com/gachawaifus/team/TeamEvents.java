package com.gachawaifus.team;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TeamEvents {

    public static void init() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide) {
                return InteractionResult.PASS;
            }
            ItemStack stack = player.getItemInHand(hand);
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
            if (!itemId.endsWith("_token")) {
                return InteractionResult.PASS;
            }
            if (TeamRules.canSummon(player)) {
                return InteractionResult.PASS;
            }
            TeamRules.messageFull(player);
            return InteractionResult.FAIL;
        });
    }

    private TeamEvents() {
    }
}
