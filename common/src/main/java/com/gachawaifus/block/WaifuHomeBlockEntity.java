package com.gachawaifus.block;

import com.gachawaifus.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * El estado del Bloque de Hogar: qué bloque está imitando.
 *
 * <p>El hogar en sí sigue viviendo en el NBT de cada waifu ({@link
 * com.gachawaifus.entity.AbstractWaifuEntity#setHome}); este BlockEntity existe solo para la
 * apariencia. Por eso se guarda un {@link ResourceLocation} y no un {@code BlockState} entero: la
 * copia client del estado necesitaría el codec de bloque, y con el default del bloque basta
 * ({@link #mimicState()}).
 *
 * <p>Se manda al cliente por las dos rutas que hay: {@link #getUpdateTag} para cuando el chunk llega
 * entero (login, cambio de dimensión, /reload) y {@link #getUpdatePacket} para el
 * {@code sendBlockUpdated} del cambio en vivo. Sin la primera, al volver a entrar el bloque saldría
 * con la textura por defecto hasta que alguien lo vuelva a tocar.
 */
public class WaifuHomeBlockEntity extends BlockEntity {

    private static final String TAG_MIMIC = "mimic";

    /** Bloque que se está imitando; null = la apariencia por defecto del hogar. */
    private @Nullable ResourceLocation mimicId;

    public WaifuHomeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WAIFU_HOME.get(), pos, state);
    }

    /**
     * ¿Se puede imitar este bloque? Solo los que se dibujan con un modelo estático: fuera quedan el
     * aire, los bloques sin ítem, los que tienen BlockEntity (cofres, hornos, carteles, camas…) y
     * los que se renderizan de forma invisible o animada. Así el camuflaje es solo una textura y el
     * Bloque de Hogar nunca hereda menús ni comportamientos raros del bloque imitado.
     */
    public static boolean isMimicable(@Nullable Block block) {
        if (block == null || block.asItem() == Items.AIR) {
            return false;
        }
        return block.defaultBlockState().getRenderShape() == RenderShape.MODEL
                && !(block instanceof EntityBlock);
    }

    /** El estado que pinta el renderizador. Nunca null: sin imitar, hormigón rosa. */
    public BlockState mimicState() {
        if (this.mimicId != null && BuiltInRegistries.BLOCK.containsKey(this.mimicId)) {
            Block block = BuiltInRegistries.BLOCK.get(this.mimicId);
            if (isMimicable(block)) {
                return block.defaultBlockState();
            }
        }
        return Blocks.PINK_CONCRETE.defaultBlockState();
    }

    /** Nombre para el mensaje del chat, ya traducido por vanilla. */
    public Component mimicName() {
        return mimicState().getBlock().getName();
    }

    /**
     * Pone la textura a imitar y avisa al cliente.
     *
     * @return {@code false} si el bloque no se puede imitar (no se toca el camuflaje actual)
     */
    public boolean setMimic(@Nullable Block block) {
        if (!isMimicable(block)) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        if (id.equals(this.mimicId)) {
            return true;
        }
        this.mimicId = id;
        setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(),
                    Block.UPDATE_CLIENTS);
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.mimicId != null) {
            tag.putString(TAG_MIMIC, this.mimicId.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.mimicId = tag.contains(TAG_MIMIC) ? ResourceLocation.parse(tag.getString(TAG_MIMIC)) : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
