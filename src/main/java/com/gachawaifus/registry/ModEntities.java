package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.AstraYaoEntity;
import com.gachawaifus.entity.BurniceWhiteEntity;
import com.gachawaifus.entity.EtherBlastEntity;
import com.gachawaifus.entity.MiyabiMisiramaEntity;
import com.gachawaifus.entity.EllenJoeEntity;
import com.gachawaifus.entity.YeShunguangEntity;
import com.gachawaifus.entity.PromeiaEntity;
import com.gachawaifus.entity.AnbyDemaraEntity;
import com.gachawaifus.entity.NicoleDemaraEntity;
import com.gachawaifus.entity.UkinamiYuzuhaEntity;
import com.gachawaifus.entity.RemielleEntity;
import com.gachawaifus.entity.TokisakiKurumiEntity;
import com.gachawaifus.entity.RicardoMilosEntity;
import com.gachawaifus.entity.VonLycaonEntity;
import com.gachawaifus.entity.YidhariEntity;
import com.gachawaifus.entity.RinaEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, GachaWaifusMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<AstraYaoEntity>> ASTRA_YAO =
            ENTITY_TYPES.register("astra_yao", () ->
                    EntityType.Builder.of(AstraYaoEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("astra_yao"));

    public static final DeferredHolder<EntityType<?>, EntityType<EllenJoeEntity>> ELLEN_JOE =
            ENTITY_TYPES.register("ellen_joe", () ->
                    EntityType.Builder.of(EllenJoeEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("ellen_joe"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.KoledaEntity>> KOLEDA =
            ENTITY_TYPES.register("koleda", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.KoledaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("koleda"));

    /** Hoshimi Miyabi — "La Dama de la Luz" (Zenless Zone Zero) */
    public static final DeferredHolder<EntityType<?>, EntityType<MiyabiMisiramaEntity>> MIYABI =
            ENTITY_TYPES.register("miyabi", () ->
                    EntityType.Builder.of(MiyabiMisiramaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("miyabi"));

    /** Burnice White — "The Flame of Winter" (Ice Mage / Fire Manipulator) */
    public static final DeferredHolder<EntityType<?>, EntityType<BurniceWhiteEntity>> BURNICE_WHITE =
            ENTITY_TYPES.register("burnice_white", () ->
                    EntityType.Builder.of(BurniceWhiteEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("burnice_white"));

    /** ★★★★★ Ye Shunguang (Yixuan): Alto Preceptor de Yunkui (Éter / Tinta Áurica) */
    public static final DeferredHolder<EntityType<?>, EntityType<YeShunguangEntity>> YE_SHUNGUANG =
            ENTITY_TYPES.register("ye_shunguang", () ->
                    EntityType.Builder.of(YeShunguangEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("ye_shunguang"));

    /** ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares" (A-Rank / Ether Support & Gravity Control) */
    public static final DeferredHolder<EntityType<?>, EntityType<NicoleDemaraEntity>> NICOLE_DEMARA =
            ENTITY_TYPES.register("nicole_demara", () ->
                    EntityType.Builder.of(NicoleDemaraEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("nicole_demara"));

    /** ★★★★★ Anby Demara — Soldier 0 (Physical / Combatant) */
    public static final DeferredHolder<EntityType<?>, EntityType<AnbyDemaraEntity>> ANBY_DEMARA =
            ENTITY_TYPES.register("anby_demara", () ->
                    EntityType.Builder.of(AnbyDemaraEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("anby_demara"));

        /** ★★★★★ Ether Blast — Proyectil de energía */
    public static final DeferredHolder<EntityType<?>, EntityType<EtherBlastEntity>> ETHER_BLAST =
            ENTITY_TYPES.register("ether_blast", () ->
                    EntityType.Builder.<EtherBlastEntity>of(EtherBlastEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("ether_blast"));

    /** ★★★★★ Ukinami Yuzuha — "La Zarigüeya de la Suerte" (Físico / Support) */
    public static final DeferredHolder<EntityType<?>, EntityType<UkinamiYuzuhaEntity>> UKINAMI_YUZUHA =
            ENTITY_TYPES.register("ukinami_yuzuha", () ->
                    EntityType.Builder.of(UkinamiYuzuhaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("ukinami_yuzuha"));

    /** ★★★★★ Promeia — "La Juez del Krampus Compliance Authority" (Hielo / Anomalía) */
    public static final DeferredHolder<EntityType<?>, EntityType<PromeiaEntity>> PROMEIA =
            ENTITY_TYPES.register("promeia", () ->
                    EntityType.Builder.of(PromeiaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("promeia"));

    /** Yidhari Murphy — "Spook Shack" (Ice / Attack) */
    public static final DeferredHolder<EntityType<?>, EntityType<YidhariEntity>> YIDHARI =
            ENTITY_TYPES.register("yidhari", () ->
                    EntityType.Builder.of(YidhariEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("yidhari"));

    /** Remielle — "Void Hunter" (Éter / Anomaly DPS) */
    public static final DeferredHolder<EntityType<?>, EntityType<RemielleEntity>> REMIELLE =
            ENTITY_TYPES.register("remielle", () ->
                    EntityType.Builder.of(RemielleEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("remielle"));

    /** Tokisaki Kurumi — "Time and Space" (Éter / Anomaly) */
    public static final DeferredHolder<EntityType<?>, EntityType<TokisakiKurumiEntity>> TOKISAKI_KURUMI =
            ENTITY_TYPES.register("tokisaki_kurumi", () ->
                    EntityType.Builder.of(TokisakiKurumiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("tokisaki_kurumi"));

    /**
     * Ricardo Milos — jefe de recompensa (neutral): al morir suelta 10 tiradas.
     *
     * <p>Es de categoría {@link MobCategory#MONSTER} e implementa {@code Enemy}, que es justo lo
     * que buscan las waifus en su {@code NearestAttackableTargetGoal}: ellas empiezan la pelea y él
     * solo devuelve el golpe. Al jugador le cuesta un 80 % más de daño hacerle cosquillas.
     */
    public static final DeferredHolder<EntityType<?>, EntityType<RicardoMilosEntity>> RICARDO_MILOS =
            ENTITY_TYPES.register("ricardo_milos", () ->
                    EntityType.Builder.of(RicardoMilosEntity::new, MobCategory.MONSTER)
                            .sized(0.8F, 2.1F)
                            .clientTrackingRange(12)
                            .build("ricardo_milos"));

    /** Von Lycaon — "Victoria Housekeeping" (Ice / Stun) */
    public static final DeferredHolder<EntityType<?>, EntityType<VonLycaonEntity>> VON_LYCAON =
            ENTITY_TYPES.register("von_lycaon", () ->
                    EntityType.Builder.of(VonLycaonEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("von_lycaon"));

    /** Rina — "Victoria Housekeeping Co." (Electric / Defense) */
    public static final DeferredHolder<EntityType<?>, EntityType<RinaEntity>> RINA_ENTITY =
            ENTITY_TYPES.register("rina", () ->
                    EntityType.Builder.of(RinaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("rina"));

    /** Grace Howard — "Anomaly" (Electric / Support-Anomaly) */
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.GraceHowardEntity>> GRACE_HOWARD =
            ENTITY_TYPES.register("grace_howard", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.GraceHowardEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("grace_howard"));
}