package com.gachawaifus;

import com.gachawaifus.color.ColorEvents;
import com.gachawaifus.command.CollectionCommand;
import com.gachawaifus.command.ColorCommand;
import com.gachawaifus.entity.BossSpawnHandler;
import com.gachawaifus.entity.WaifuRecallHandler;
import com.gachawaifus.entity.WanderGoalsHandler;
import com.gachawaifus.entity.WildWaifuSpawner;
import com.gachawaifus.entity.WildWaifus;
import com.gachawaifus.gacha.DailyMissions;
import com.gachawaifus.gacha.DailyPulls;
import com.gachawaifus.gacha.GuideGiver;
import com.gachawaifus.gacha.StarterWaifu;
import com.gachawaifus.registry.ModBlockEntities;
import com.gachawaifus.registry.ModBlocks;
import com.gachawaifus.registry.ModCreativeTabs;
import com.gachawaifus.registry.ModEntities;
import com.gachawaifus.registry.ModItems;
import com.gachawaifus.registry.ModMenuTypes;
import com.gachawaifus.registry.ModSounds;
import com.gachawaifus.team.TeamEvents;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GachaWaifusMod implements ModInitializer {
    public static final String MODID = "gachawaifus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Gacha Waifus Mod for Fabric 1.21.1!");

        ModSounds.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModItems.init();
        ModCreativeTabs.init();
        ModMenuTypes.init();

        WaifuRecallHandler.init();
        WanderGoalsHandler.init();
        WildWaifus.init();
        WildWaifuSpawner.init();
        BossSpawnHandler.init();
        TeamEvents.init();
        ColorEvents.init();
        GuideGiver.init();
        StarterWaifu.init();
        DailyPulls.init();
        DailyMissions.init();
        CollectionCommand.init();
        ColorCommand.init();
    }
}
