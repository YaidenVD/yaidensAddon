package gaydev.yaiden.femboysleeping.keysbindedwithlove;

import gaydev.yaiden.femboysleeping.client.belly.renderer.PregnantBellyLayer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Keybined {

    private static final Logger LOGGER = LoggerFactory.getLogger("yaidensaddon-belly");

    public static KeyMapping TEST_KEY;

    // F12 cycles the belly through these sizes on YOU (client-side test only).
    private static final float[] TEST_STEPS = {0.0F, 1.2F, 2.4F, 3.6F, 4.8F};
    private static int testIndex = 0;

    public static void register() {
        TEST_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.yaidensaddon.test",
                GLFW.GLFW_KEY_H,
                "category.yaidensaddon"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TEST_KEY.consumeClick()) {
                if (client.player == null) {
                    continue;
                }
                testIndex = (testIndex + 1) % TEST_STEPS.length;
                float growth = TEST_STEPS[testIndex];
                PregnantBellyLayer.setBellyGrowth(client.player, growth);
                LOGGER.info("[belly] {} pressed, growth set to {}", TEST_KEY, growth);
                client.player.displayClientMessage(
                        Component.literal("Belly growth: " + growth), true);
            }
        });
    }
}
