package com.gachawaifus.entity;

import com.gachawaifus.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Atributos por tipo de entidad, compartidos entre cargadores: NeoForge los vuelca en
 * EntityAttributeCreationEvent y Fabric en FabricDefaultAttributeRegistry.
 *
 * <p>Se guarda el builder sin construir porque los dos lo consumen distinto (NeoForge llama a
 * build(), Fabric registra el builder tal cual). Toda entidad viva registrada en ModEntities
 * necesita su línea aquí: sin ella el juego revienta al spawnearla, en ambos cargadores.
 */
public final class WaifuAttributeBindings {

    /** Tipo de entidad y sus atributos, todavía sin construir. */
    public record Binding(EntityType<? extends LivingEntity> type,
                          Supplier<AttributeSupplier.Builder> attributes) {}

    public static final List<Binding> ATTRIBUTES = new ArrayList<>();

    private WaifuAttributeBindings() {}

    private static <T extends LivingEntity> void add(List<Binding> list, EntityType<T> type,
                                                     Supplier<AttributeSupplier.Builder> attributes) {
        list.add(new Binding(type, attributes));
    }

    static {
        add(ATTRIBUTES, ModEntities.MIYABI.get(), MiyabiMisiramaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ASTRA_YAO.get(), AstraYaoEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ELLEN_JOE.get(), EllenJoeEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.BURNICE_WHITE.get(), BurniceWhiteEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.YE_SHUNGUANG.get(), YeShunguangEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.NICOLE_DEMARA.get(), NicoleDemaraEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ANBY_DEMARA.get(), AnbyDemaraEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.GRACE_HOWARD.get(), GraceHowardEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.UKINAMI_YUZUHA.get(), UkinamiYuzuhaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.PROMEIA.get(), PromeiaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.REMIELLE.get(), RemielleEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.TOKISAKI_KURUMI.get(), TokisakiKurumiEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.YIDHARI.get(), YidhariEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.VON_LYCAON.get(), VonLycaonEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.RICARDO_MILOS.get(), RicardoMilosEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.DOCTOR_HOUSE.get(), DoctorHouseEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.WILSON.get(), WilsonEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.FERNANFLOO.get(), FernanflooEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.CURLY.get(), CurlyEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.TUNG_TUNG_SAHUR.get(), TungTungSahurEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.RINA_ENTITY.get(), RinaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.KOLEDA.get(), KoledaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.SOLDIER_11.get(), Soldier11Entity::createAttributes);
        add(ATTRIBUTES, ModEntities.TOBICHI_ORIGAMI.get(), TobichiOrigamiEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.NEKOMATA.get(), NekomataEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.BILLY_KID.get(), BillyKidEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.YIXUAN.get(), YixuanEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ZHU_YUAN.get(), ZhuYuanEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.QINGYI.get(), QingyiEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.PYROIS.get(), PyroisEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.NORMA.get(), NormaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.VELINA.get(), VelinaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.SIGRID.get(), SigridEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.CLARET.get(), ClaretEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ROXY.get(), RoxyEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ORPHIE.get(), OrphieEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.SEED.get(), SeedEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.MANATO.get(), ManatoEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.LUCIA.get(), LuciaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.BANYUE.get(), BanyueEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ZHAO.get(), ZhaoEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ARIA.get(), AriaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.SUNNA.get(), SunnaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.CISSIA.get(), CissiaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.NANGONG_YU.get(), NangongYuEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.TSUKISHIRO_YANAGI.get(), TsukishiroYanagiEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.HARUMASA.get(), HarumasaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.EVELYN_CHEVALIER.get(), EvelynChevalierEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.PULCHRA.get(), PulchraEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.TRIGGER.get(), TriggerEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.HUGO_VLAD.get(), HugoVladEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.VIVIAN.get(), VivianEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.JU_FUFU.get(), JuFufuEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.PAN_YINHU.get(), PanYinhuEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ALICE.get(), AliceEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.ANTON.get(), AntonEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.BEN_BIGGER.get(), BenBiggerEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.CORIN_WICKES.get(), CorinWickesEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.LUCY.get(), LucyEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.PIPER_WHEEL.get(), PiperWheelEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.SOUKAKU.get(), SoukakuEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.SETH_LOWELL.get(), SethLowellEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.JANE_DOE.get(), JaneDoeEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.CAESAR.get(), CaesarEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.LIGHTER.get(), LighterEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.AQUA.get(), AquaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.MEGUMIN.get(), MeguminEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.DARKNESS.get(), DarknessEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.YATOGAMI_TOHKA.get(), YatogamiTohkaEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.PHOENIX.get(), PhoenixEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.HATSUNE_MIKU.get(), HatsuneMikuEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.KASANE_TETO.get(), KasaneTetoEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.AKITA_NERU.get(), AkitaNeruEntity::createAttributes);
        add(ATTRIBUTES, ModEntities.BELLE.get(), BelleEntity::createAttributes);
    }
}
