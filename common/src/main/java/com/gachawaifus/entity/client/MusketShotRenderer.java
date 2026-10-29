package com.gachawaifus.entity.client;

import com.gachawaifus.entity.MusketShotEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

/**
 * La bala se renderiza como el item que entrega {@link MusketShotEntity#getItem()} (carga de fuego),
 * a media escala y con luz propia para que se siga viendo de noche. Es el mismo renderer que usan
 * las bolas de nieve y los huevos, así que el sprite ya existe en el atlas del juego.
 */
public class MusketShotRenderer extends ThrownItemRenderer<MusketShotEntity> {

    public MusketShotRenderer(EntityRendererProvider.Context context) {
        super(context, 0.5F, true);
    }
}
