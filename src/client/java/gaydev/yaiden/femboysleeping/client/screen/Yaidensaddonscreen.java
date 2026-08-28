package gaydev.yaiden.femboysleeping.client.screen;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Yaidensaddonscreen {
    public static int percentage = 5;

    public static Screen create(Screen parent) {

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("YaidensApi"));

        // makes an catogory
        var category = builder.getOrCreateCategory(
                Component.literal("general")
         );
         var gay = builder.getOrCreateCategory(
                Component.literal("gay")
         );
         var secret = builder.getOrCreateCategory(
                Component.empty()
         );

        // interger INPUT
        category.addEntry(
                builder.entryBuilder()
                        .startIntField(
                                Component.literal("percentage of spawning"),
                                percentage
                        ).setMin(0)
                        .setMax(100)
                        .setSaveConsumer(value -> percentage = value)
                        .build()
        );

        // SLIDER
        category.addEntry(
                builder.entryBuilder()
                        .startIntSlider(
                                Component.literal(""),
                                0,
                                0,
                                100
                        )
                        .build()
        );

        // BOOLEAN / TOGGLE
        gay.addEntry(
                builder.entryBuilder()
                        .startBooleanToggle(
                                Component.literal("allow gay"),
                                true
                        )
                        .build()
        );

        // string INPUT
        secret.addEntry(
                builder.entryBuilder()
                        .startStrField(
                                Component.literal(""),
                                "..."
                        )
                        .build()
        );

        return builder.build();
    }
}