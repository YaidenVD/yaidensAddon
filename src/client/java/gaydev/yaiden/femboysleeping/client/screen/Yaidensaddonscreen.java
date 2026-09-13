package gaydev.yaiden.femboysleeping.client.screen;

import gaydev.yaiden.femboysleeping.config.YaidensaddonConfigManager;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Yaidensaddonscreen {

        public static Screen create(Screen parent) {

        var config = YaidensaddonConfigManager.CONFIG;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("YaidensApi"));

        // GENERAL

        var general = builder.getOrCreateCategory(
                Component.literal("General")
        );

        general.addEntry(
                builder.entryBuilder()
                        .startIntSlider(
                                Component.literal("Baby spawn chance (%)"),
                                config.sliderValue,
                                0,
                                100
                        )
                        .setSaveConsumer(value -> config.sliderValue = value)
                        .build()
        );

        // GAY

        var gay = builder.getOrCreateCategory(
                Component.literal("Gay")
        );

        gay.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Component.literal("Allow gay"),
                                config.allowGay
                        )
                        .setSaveConsumer(value -> config.allowGay = value)
                        .build()
        );

        // SECRET

        // Save everything when the Cloth Config screen is saved
        builder.setSavingRunnable(
                YaidensaddonConfigManager::save
        );

        return builder.build();
    }
}