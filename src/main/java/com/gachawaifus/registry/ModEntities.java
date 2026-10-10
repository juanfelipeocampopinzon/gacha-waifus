package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.BillyKidEntity;
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
import com.gachawaifus.entity.DoctorHouseEntity;
import com.gachawaifus.entity.WilsonEntity;
import com.gachawaifus.entity.FernanflooEntity;
import com.gachawaifus.entity.VonLycaonEntity;
import com.gachawaifus.entity.YidhariEntity;
import com.gachawaifus.entity.RinaEntity;
import com.gachawaifus.entity.NekomataEntity;
import com.gachawaifus.entity.YixuanEntity;
import com.gachawaifus.entity.ZhuYuanEntity;
import com.gachawaifus.entity.QingyiEntity;
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
                            .sized(1.6F, 4.2F)
                            .clientTrackingRange(12)
                            .build("ricardo_milos"));

    /**
     * Doctor House — jefe de recompensa (neutral): invoca a 2 Wilsons y al morir suelta
     * 5 Bolitas Azules (tiradas del banner permanente).
     */
    public static final DeferredHolder<EntityType<?>, EntityType<DoctorHouseEntity>> DOCTOR_HOUSE =
            ENTITY_TYPES.register("doctor_house", () ->
                    EntityType.Builder.of(DoctorHouseEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 4.2F)
                            .clientTrackingRange(12)
                            .build("doctor_house"));

    /** Wilson — el médico amigo de House: solo aparece cuando él lo invoca (o por huevo). */
    public static final DeferredHolder<EntityType<?>, EntityType<WilsonEntity>> WILSON =
            ENTITY_TYPES.register("wilson", () ->
                    EntityType.Builder.of(WilsonEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("wilson"));

    /**
     * Fernanfloo — jefe de recompensa (neutral): pega con su chorizo y al morir suelta
     * 3 Bolitas Rosas (tiradas del banner de eventos).
     */
    public static final DeferredHolder<EntityType<?>, EntityType<FernanflooEntity>> FERNANFLOO =
            ENTITY_TYPES.register("fernanfloo", () ->
                    EntityType.Builder.of(FernanflooEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 4.2F)
                            .clientTrackingRange(12)
                            .build("fernanfloo"));

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

    /** Soldier 11 — "Attack" (Fire / Attack) */
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.Soldier11Entity>> SOLDIER_11 =
            ENTITY_TYPES.register("soldier_11", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.Soldier11Entity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("soldier_11"));

    /** Tobichi Origami — "Defense" (White / Defense) */
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.TobichiOrigamiEntity>> TOBICHI_ORIGAMI =
            ENTITY_TYPES.register("tobichi_origami", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.TobichiOrigamiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("tobichi_origami"));

    /** Nekomiya Mana (Nekomata) — "Cunning Hares" (Physical / Attack) */
    public static final DeferredHolder<EntityType<?>, EntityType<NekomataEntity>> NEKOMATA =
            ENTITY_TYPES.register("nekomata", () ->
                    EntityType.Builder.of(NekomataEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("nekomata"));

    /** Billy Kid — "Sons of Calydon" (Gris / Attack) */
    public static final DeferredHolder<EntityType<?>, EntityType<BillyKidEntity>> BILLY_KID =
            ENTITY_TYPES.register("billy_kid", () ->
                    EntityType.Builder.of(BillyKidEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("billy_kid"));

    /** ★★★★★ Zhu Yuan — Seguridad Pública NEPS (Éter / Attack) */
    public static final DeferredHolder<EntityType<?>, EntityType<ZhuYuanEntity>> ZHU_YUAN =
            ENTITY_TYPES.register("zhu_yuan", () ->
                    EntityType.Builder.of(ZhuYuanEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("zhu_yuan"));

    /** ★★★★★ Yixuan — Alta Preceptora de Yunkui (Éter / Attack) */
    public static final DeferredHolder<EntityType<?>, EntityType<YixuanEntity>> YIXUAN =
            ENTITY_TYPES.register("yixuan", () ->
                    EntityType.Builder.of(YixuanEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("yixuan"));

    public static final DeferredHolder<EntityType<?>, EntityType<QingyiEntity>> QINGYI =
            ENTITY_TYPES.register("qingyi", () ->
                    EntityType.Builder.of(QingyiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("qingyi"));

    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.AntonEntity>> ANTON =
            ENTITY_TYPES.register("anton", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.AntonEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("anton"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.BenBiggerEntity>> BEN_BIGGER =
            ENTITY_TYPES.register("ben_bigger", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.BenBiggerEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("ben_bigger"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.CorinWickesEntity>> CORIN_WICKES =
            ENTITY_TYPES.register("corin_wickes", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.CorinWickesEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("corin_wickes"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.LucyEntity>> LUCY =
            ENTITY_TYPES.register("lucy", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.LucyEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("lucy"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.PiperWheelEntity>> PIPER_WHEEL =
            ENTITY_TYPES.register("piper_wheel", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PiperWheelEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("piper_wheel"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.SoukakuEntity>> SOUKAKU =
            ENTITY_TYPES.register("soukaku", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SoukakuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("soukaku"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.SethLowellEntity>> SETH_LOWELL =
            ENTITY_TYPES.register("seth_lowell", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SethLowellEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("seth_lowell"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.JaneDoeEntity>> JANE_DOE =
            ENTITY_TYPES.register("jane_doe", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.JaneDoeEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("jane_doe"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.CaesarEntity>> CAESAR =
            ENTITY_TYPES.register("caesar", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.CaesarEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("caesar"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.LighterEntity>> LIGHTER =
            ENTITY_TYPES.register("lighter", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.LighterEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("lighter"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.TsukishiroYanagiEntity>> TSUKISHIRO_YANAGI =
            ENTITY_TYPES.register("tsukishiro_yanagi", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.TsukishiroYanagiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("tsukishiro_yanagi"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.HarumasaEntity>> HARUMASA =
            ENTITY_TYPES.register("harumasa", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.HarumasaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("harumasa"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.EvelynChevalierEntity>> EVELYN_CHEVALIER =
            ENTITY_TYPES.register("evelyn_chevalier", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.EvelynChevalierEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("evelyn_chevalier"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.PulchraEntity>> PULCHRA =
            ENTITY_TYPES.register("pulchra", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PulchraEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("pulchra"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.TriggerEntity>> TRIGGER =
            ENTITY_TYPES.register("trigger", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.TriggerEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("trigger"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.HugoVladEntity>> HUGO_VLAD =
            ENTITY_TYPES.register("hugo_vlad", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.HugoVladEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("hugo_vlad"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.VivianEntity>> VIVIAN =
            ENTITY_TYPES.register("vivian", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.VivianEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("vivian"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.JuFufuEntity>> JU_FUFU =
            ENTITY_TYPES.register("ju_fufu", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.JuFufuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("ju_fufu"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.PanYinhuEntity>> PAN_YINHU =
            ENTITY_TYPES.register("pan_yinhu", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PanYinhuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("pan_yinhu"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.AliceEntity>> ALICE =
            ENTITY_TYPES.register("alice", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.AliceEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("alice"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.OrphieEntity>> ORPHIE =
            ENTITY_TYPES.register("orphie", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.OrphieEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("orphie"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.SeedEntity>> SEED =
            ENTITY_TYPES.register("seed", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SeedEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("seed"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.ManatoEntity>> MANATO =
            ENTITY_TYPES.register("manato", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.ManatoEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("manato"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.LuciaEntity>> LUCIA =
            ENTITY_TYPES.register("lucia", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.LuciaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("lucia"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.BanyueEntity>> BANYUE =
            ENTITY_TYPES.register("banyue", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.BanyueEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("banyue"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.ZhaoEntity>> ZHAO =
            ENTITY_TYPES.register("zhao", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.ZhaoEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("zhao"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.AriaEntity>> ARIA =
            ENTITY_TYPES.register("aria", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.AriaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("aria"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.SunnaEntity>> SUNNA =
            ENTITY_TYPES.register("sunna", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SunnaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("sunna"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.CissiaEntity>> CISSIA =
            ENTITY_TYPES.register("cissia", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.CissiaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("cissia"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.NangongYuEntity>> NANGONG_YU =
            ENTITY_TYPES.register("nangong_yu", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.NangongYuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("nangong_yu"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.PyroisEntity>> PYROIS =
            ENTITY_TYPES.register("pyrois", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PyroisEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("pyrois"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.NormaEntity>> NORMA =
            ENTITY_TYPES.register("norma", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.NormaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("norma"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.VelinaEntity>> VELINA =
            ENTITY_TYPES.register("velina", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.VelinaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("velina"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.SigridEntity>> SIGRID =
            ENTITY_TYPES.register("sigrid", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SigridEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("sigrid"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.ClaretEntity>> CLARET =
            ENTITY_TYPES.register("claret", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.ClaretEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("claret"));
    public static final DeferredHolder<EntityType<?>, EntityType<com.gachawaifus.entity.RoxyEntity>> ROXY =
            ENTITY_TYPES.register("roxy", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.RoxyEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("roxy"));}