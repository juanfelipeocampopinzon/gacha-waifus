package com.gachawaifus.client;

import com.gachawaifus.menu.WaifuStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI de la Cápsula Waifu. Reutiliza la textura del cofre doble vanilla (5 filas):
 * filas 1-3 = waifus guardadas, fila 4 = separador, fila 5 = waifus caídas (clic = revivir con 4 diamantes).
 */
public class WaifuStorageScreen extends AbstractContainerScreen<WaifuStorageMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private final int containerRows = 5;

    public WaifuStorageScreen(WaifuStorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, Component.literal("§dCápsula Waifu §7· waifus guardadas y caídas"));
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = (this.width - this.imageWidth) / 2;
        int top = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, left, top, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        graphics.blit(TEXTURE, left, top + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }
}
