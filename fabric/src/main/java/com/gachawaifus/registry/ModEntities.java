package com.gachawaifus.registry;

import com.gachawaifus.GachaWaifusMod;
import com.gachawaifus.entity.BillyKidEntity;
import com.gachawaifus.entity.AstraYaoEntity;
import com.gachawaifus.entity.BurniceWhiteEntity;
import com.gachawaifus.entity.EtherBlastEntity;
import com.gachawaifus.entity.MusketShotEntity;
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
import com.gachawaifus.entity.TungTungSahurEntity;
import com.gachawaifus.entity.CurlyEntity;
import com.gachawaifus.entity.VonLycaonEntity;
import com.gachawaifus.entity.YidhariEntity;
import com.gachawaifus.entity.RinaEntity;
import com.gachawaifus.entity.NekomataEntity;
import com.gachawaifus.entity.YixuanEntity;
import com.gachawaifus.entity.ZhuYuanEntity;
import com.gachawaifus.entity.QingyiEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
        private static <T extends net.minecraft.world.entity.Entity> Supplier<EntityType<T>> register(String name, Supplier<EntityType<T>> factory) {
        EntityType<T> type = factory.get();
        Registry.register(BuiltInRegistries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(GachaWaifusMod.MODID, name), type);
        return new RegistrySupplier<>(type);
    }

    public static void init() {}

    public static final Supplier<EntityType<AstraYaoEntity>> ASTRA_YAO =
            register("astra_yao", () ->
                    EntityType.Builder.of(AstraYaoEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("astra_yao"));

    public static final Supplier<EntityType<EllenJoeEntity>> ELLEN_JOE =
            register("ellen_joe", () ->
                    EntityType.Builder.of(EllenJoeEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("ellen_joe"));

    public static final Supplier<EntityType<com.gachawaifus.entity.KoledaEntity>> KOLEDA =
            register("koleda", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.KoledaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("koleda"));

    /** Hoshimi Miyabi — "La Dama de la Luz" (Zenless Zone Zero) */
    public static final Supplier<EntityType<MiyabiMisiramaEntity>> MIYABI =
            register("miyabi", () ->
                    EntityType.Builder.of(MiyabiMisiramaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("miyabi"));

    /** Burnice White — "The Flame of Winter" (Ice Mage / Fire Manipulator) */
    public static final Supplier<EntityType<BurniceWhiteEntity>> BURNICE_WHITE =
            register("burnice_white", () ->
                    EntityType.Builder.of(BurniceWhiteEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("burnice_white"));

    /** ★★★★★ Ye Shunguang (Yixuan): Alto Preceptor de Yunkui (Éter / Tinta Áurica) */
    public static final Supplier<EntityType<YeShunguangEntity>> YE_SHUNGUANG =
            register("ye_shunguang", () ->
                    EntityType.Builder.of(YeShunguangEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(10)
                            .build("ye_shunguang"));

    /** ★★★★★ Nicole Demara — "The Sweet Hare of Cunning Hares" (A-Rank / Ether Support & Gravity Control) */
    public static final Supplier<EntityType<NicoleDemaraEntity>> NICOLE_DEMARA =
            register("nicole_demara", () ->
                    EntityType.Builder.of(NicoleDemaraEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("nicole_demara"));

    /** ★★★★★ Anby Demara — Soldier 0 (Physical / Combatant) */
    public static final Supplier<EntityType<AnbyDemaraEntity>> ANBY_DEMARA =
            register("anby_demara", () ->
                    EntityType.Builder.of(AnbyDemaraEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("anby_demara"));

        /** ★★★★★ Ether Blast — Proyectil de energía */
    public static final Supplier<EntityType<EtherBlastEntity>> ETHER_BLAST =
            register("ether_blast", () ->
                    EntityType.Builder.<EtherBlastEntity>of(EtherBlastEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("ether_blast"));

    /** Pepita de chispa del mosquete de Kurumi — Proyectil */
    public static final Supplier<EntityType<MusketShotEntity>> MUSKET_SHOT =
            register("musket_shot", () ->
                    EntityType.Builder.<MusketShotEntity>of(MusketShotEntity::new, MobCategory.MISC)
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
                            .build("musket_shot"));

    /** ★★★★★ Ukinami Yuzuha — "La Zarigüeya de la Suerte" (Físico / Support) */
    public static final Supplier<EntityType<UkinamiYuzuhaEntity>> UKINAMI_YUZUHA =
            register("ukinami_yuzuha", () ->
                    EntityType.Builder.of(UkinamiYuzuhaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("ukinami_yuzuha"));

    /** ★★★★★ Promeia — "La Juez del Krampus Compliance Authority" (Hielo / Anomalía) */
    public static final Supplier<EntityType<PromeiaEntity>> PROMEIA =
            register("promeia", () ->
                    EntityType.Builder.of(PromeiaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("promeia"));

    /** Yidhari Murphy — "Spook Shack" (Ice / Attack) */
    public static final Supplier<EntityType<YidhariEntity>> YIDHARI =
            register("yidhari", () ->
                    EntityType.Builder.of(YidhariEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("yidhari"));

    /** Remielle — "Void Hunter" (Éter / Anomaly DPS) */
    public static final Supplier<EntityType<RemielleEntity>> REMIELLE =
            register("remielle", () ->
                    EntityType.Builder.of(RemielleEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("remielle"));

    /** Tokisaki Kurumi — "Time and Space" (Éter / Anomaly) */
    public static final Supplier<EntityType<TokisakiKurumiEntity>> TOKISAKI_KURUMI =
            register("tokisaki_kurumi", () ->
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
    public static final Supplier<EntityType<RicardoMilosEntity>> RICARDO_MILOS =
            register("ricardo_milos", () ->
                    EntityType.Builder.of(RicardoMilosEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 4.2F)
                            .clientTrackingRange(12)
                            .build("ricardo_milos"));

    /**
     * Doctor House — jefe de recompensa (neutral): invoca a 2 Wilsons y al morir suelta
     * 5 Bolitas Azules (tiradas del banner permanente).
     */
    public static final Supplier<EntityType<DoctorHouseEntity>> DOCTOR_HOUSE =
            register("doctor_house", () ->
                    EntityType.Builder.of(DoctorHouseEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 4.2F)
                            .clientTrackingRange(12)
                            .build("doctor_house"));

    /** Wilson — el médico amigo de House: solo aparece cuando él lo invoca (o por huevo). */
    public static final Supplier<EntityType<WilsonEntity>> WILSON =
            register("wilson", () ->
                    EntityType.Builder.of(WilsonEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("wilson"));

    /**
     * Fernanfloo — jefe de recompensa (neutral): invoca perritos Curly y al morir suelta
     * 3 Bolitas Rosas (tiradas del banner de eventos).
     */
    public static final Supplier<EntityType<FernanflooEntity>> FERNANFLOO =
            register("fernanfloo", () ->
                    EntityType.Builder.of(FernanflooEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 4.2F)
                            .clientTrackingRange(12)
                            .build("fernanfloo"));

    /**
     * Tung Tung Sahur — jefe neutral con bate de béisbol: cada golpe manda volando lejos al
     * objetivo y al morir suelta 4 Bolitas Azules.
     */
    public static final Supplier<EntityType<TungTungSahurEntity>> TUNG_TUNG_SAHUR =
            register("tung_tung_sahur", () ->
                    EntityType.Builder.of(TungTungSahurEntity::new, MobCategory.MONSTER)
                            .sized(1.6F, 4.2F)
                            .clientTrackingRange(12)
                            .build("tung_tung_sahur"));

    /** Curly — el perrito de Fernanfloo: solo aparece cuando él lo invoca (o por huevo). */
    public static final Supplier<EntityType<CurlyEntity>> CURLY =
            register("curly", () ->
                    EntityType.Builder.of(CurlyEntity::new, MobCategory.CREATURE)
                            .sized(0.75F, 1.05F)
                            .clientTrackingRange(12)
                            .build("curly"));

    /** Von Lycaon — "Victoria Housekeeping" (Ice / Stun) */
    public static final Supplier<EntityType<VonLycaonEntity>> VON_LYCAON =
            register("von_lycaon", () ->
                    EntityType.Builder.of(VonLycaonEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("von_lycaon"));

    /** Rina — "Victoria Housekeeping Co." (Electric / Defense) */
    public static final Supplier<EntityType<RinaEntity>> RINA_ENTITY =
            register("rina", () ->
                    EntityType.Builder.of(RinaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("rina"));

    /** Grace Howard — "Anomaly" (Electric / Support-Anomaly) */
    public static final Supplier<EntityType<com.gachawaifus.entity.GraceHowardEntity>> GRACE_HOWARD =
            register("grace_howard", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.GraceHowardEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("grace_howard"));

    /** Soldier 11 — "Attack" (Fire / Attack) */
    public static final Supplier<EntityType<com.gachawaifus.entity.Soldier11Entity>> SOLDIER_11 =
            register("soldier_11", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.Soldier11Entity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("soldier_11"));

    /** Tobichi Origami — "Defense" (White / Defense) */
    public static final Supplier<EntityType<com.gachawaifus.entity.TobichiOrigamiEntity>> TOBICHI_ORIGAMI =
            register("tobichi_origami", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.TobichiOrigamiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("tobichi_origami"));

    /** Nekomiya Mana (Nekomata) — "Cunning Hares" (Physical / Attack) */
    public static final Supplier<EntityType<NekomataEntity>> NEKOMATA =
            register("nekomata", () ->
                    EntityType.Builder.of(NekomataEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("nekomata"));

    /** Billy Kid — "Sons of Calydon" (Gris / Attack) */
    public static final Supplier<EntityType<BillyKidEntity>> BILLY_KID =
            register("billy_kid", () ->
                    EntityType.Builder.of(BillyKidEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("billy_kid"));

    /** ★★★★★ Zhu Yuan — Seguridad Pública NEPS (Éter / Attack) */
    public static final Supplier<EntityType<ZhuYuanEntity>> ZHU_YUAN =
            register("zhu_yuan", () ->
                    EntityType.Builder.of(ZhuYuanEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("zhu_yuan"));

    /** ★★★★★ Yixuan — Alta Preceptora de Yunkui (Éter / Attack) */
    public static final Supplier<EntityType<YixuanEntity>> YIXUAN =
            register("yixuan", () ->
                    EntityType.Builder.of(YixuanEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("yixuan"));

    public static final Supplier<EntityType<QingyiEntity>> QINGYI =
            register("qingyi", () ->
                    EntityType.Builder.of(QingyiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("qingyi"));

    public static final Supplier<EntityType<com.gachawaifus.entity.AntonEntity>> ANTON =
            register("anton", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.AntonEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("anton"));
    public static final Supplier<EntityType<com.gachawaifus.entity.BenBiggerEntity>> BEN_BIGGER =
            register("ben_bigger", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.BenBiggerEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("ben_bigger"));
    public static final Supplier<EntityType<com.gachawaifus.entity.CorinWickesEntity>> CORIN_WICKES =
            register("corin_wickes", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.CorinWickesEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("corin_wickes"));
    public static final Supplier<EntityType<com.gachawaifus.entity.LucyEntity>> LUCY =
            register("lucy", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.LucyEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("lucy"));
    public static final Supplier<EntityType<com.gachawaifus.entity.PiperWheelEntity>> PIPER_WHEEL =
            register("piper_wheel", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PiperWheelEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("piper_wheel"));
    public static final Supplier<EntityType<com.gachawaifus.entity.SoukakuEntity>> SOUKAKU =
            register("soukaku", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SoukakuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("soukaku"));
    public static final Supplier<EntityType<com.gachawaifus.entity.SethLowellEntity>> SETH_LOWELL =
            register("seth_lowell", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SethLowellEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("seth_lowell"));
    public static final Supplier<EntityType<com.gachawaifus.entity.JaneDoeEntity>> JANE_DOE =
            register("jane_doe", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.JaneDoeEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("jane_doe"));
    public static final Supplier<EntityType<com.gachawaifus.entity.CaesarEntity>> CAESAR =
            register("caesar", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.CaesarEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("caesar"));
    public static final Supplier<EntityType<com.gachawaifus.entity.LighterEntity>> LIGHTER =
            register("lighter", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.LighterEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("lighter"));
    public static final Supplier<EntityType<com.gachawaifus.entity.TsukishiroYanagiEntity>> TSUKISHIRO_YANAGI =
            register("tsukishiro_yanagi", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.TsukishiroYanagiEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("tsukishiro_yanagi"));
    public static final Supplier<EntityType<com.gachawaifus.entity.HarumasaEntity>> HARUMASA =
            register("harumasa", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.HarumasaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("harumasa"));
    public static final Supplier<EntityType<com.gachawaifus.entity.EvelynChevalierEntity>> EVELYN_CHEVALIER =
            register("evelyn_chevalier", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.EvelynChevalierEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("evelyn_chevalier"));
    public static final Supplier<EntityType<com.gachawaifus.entity.PulchraEntity>> PULCHRA =
            register("pulchra", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PulchraEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("pulchra"));
    public static final Supplier<EntityType<com.gachawaifus.entity.TriggerEntity>> TRIGGER =
            register("trigger", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.TriggerEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("trigger"));
    public static final Supplier<EntityType<com.gachawaifus.entity.HugoVladEntity>> HUGO_VLAD =
            register("hugo_vlad", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.HugoVladEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("hugo_vlad"));
    public static final Supplier<EntityType<com.gachawaifus.entity.VivianEntity>> VIVIAN =
            register("vivian", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.VivianEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("vivian"));
    public static final Supplier<EntityType<com.gachawaifus.entity.JuFufuEntity>> JU_FUFU =
            register("ju_fufu", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.JuFufuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("ju_fufu"));
    public static final Supplier<EntityType<com.gachawaifus.entity.PanYinhuEntity>> PAN_YINHU =
            register("pan_yinhu", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PanYinhuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("pan_yinhu"));
    public static final Supplier<EntityType<com.gachawaifus.entity.AliceEntity>> ALICE =
            register("alice", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.AliceEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("alice"));
    public static final Supplier<EntityType<com.gachawaifus.entity.OrphieEntity>> ORPHIE =
            register("orphie", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.OrphieEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("orphie"));
    public static final Supplier<EntityType<com.gachawaifus.entity.SeedEntity>> SEED =
            register("seed", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SeedEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("seed"));
    public static final Supplier<EntityType<com.gachawaifus.entity.ManatoEntity>> MANATO =
            register("manato", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.ManatoEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("manato"));
    public static final Supplier<EntityType<com.gachawaifus.entity.LuciaEntity>> LUCIA =
            register("lucia", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.LuciaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("lucia"));
    public static final Supplier<EntityType<com.gachawaifus.entity.BanyueEntity>> BANYUE =
            register("banyue", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.BanyueEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("banyue"));
    public static final Supplier<EntityType<com.gachawaifus.entity.ZhaoEntity>> ZHAO =
            register("zhao", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.ZhaoEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("zhao"));
    public static final Supplier<EntityType<com.gachawaifus.entity.AriaEntity>> ARIA =
            register("aria", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.AriaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("aria"));
    public static final Supplier<EntityType<com.gachawaifus.entity.SunnaEntity>> SUNNA =
            register("sunna", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SunnaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("sunna"));
    public static final Supplier<EntityType<com.gachawaifus.entity.CissiaEntity>> CISSIA =
            register("cissia", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.CissiaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("cissia"));
    public static final Supplier<EntityType<com.gachawaifus.entity.NangongYuEntity>> NANGONG_YU =
            register("nangong_yu", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.NangongYuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("nangong_yu"));
    public static final Supplier<EntityType<com.gachawaifus.entity.PyroisEntity>> PYROIS =
            register("pyrois", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PyroisEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("pyrois"));
    public static final Supplier<EntityType<com.gachawaifus.entity.NormaEntity>> NORMA =
            register("norma", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.NormaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("norma"));
    public static final Supplier<EntityType<com.gachawaifus.entity.VelinaEntity>> VELINA =
            register("velina", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.VelinaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("velina"));
    public static final Supplier<EntityType<com.gachawaifus.entity.SigridEntity>> SIGRID =
            register("sigrid", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.SigridEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("sigrid"));
    public static final Supplier<EntityType<com.gachawaifus.entity.ClaretEntity>> CLARET =
            register("claret", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.ClaretEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("claret"));
    public static final Supplier<EntityType<com.gachawaifus.entity.RoxyEntity>> ROXY =
            register("roxy", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.RoxyEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("roxy"));
    public static final Supplier<EntityType<com.gachawaifus.entity.AquaEntity>> AQUA =
            register("aqua", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.AquaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("aqua"));
    public static final Supplier<EntityType<com.gachawaifus.entity.MeguminEntity>> MEGUMIN =
            register("megumin", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.MeguminEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("megumin"));
    public static final Supplier<EntityType<com.gachawaifus.entity.DarknessEntity>> DARKNESS =
            register("darkness", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.DarknessEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("darkness"));

    public static final Supplier<EntityType<com.gachawaifus.entity.YatogamiTohkaEntity>> YATOGAMI_TOHKA =
            register("yatogami_tohka", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.YatogamiTohkaEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("yatogami_tohka"));

    public static final Supplier<EntityType<com.gachawaifus.entity.PhoenixEntity>> PHOENIX =
            register("phoenix", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.PhoenixEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("phoenix"));

    public static final Supplier<EntityType<com.gachawaifus.entity.HatsuneMikuEntity>> HATSUNE_MIKU =
            register("hatsune_miku", () ->
                    EntityType.Builder.of(com.gachawaifus.entity.HatsuneMikuEntity::new, MobCategory.CREATURE)
                            .sized(0.6F, 1.8F)
                            .clientTrackingRange(12)
                            .build("hatsune_miku"));
}