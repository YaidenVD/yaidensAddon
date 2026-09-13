package gaydev.yaiden.femboysleeping.keysbindedwithlove;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Keybined {

    public static KeyMapping TEST_KEY;

    public static void register() {
        TEST_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.yaidensaddon.test",
                GLFW.GLFW_KEY_F12,
                "category.yaidensaddon"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TEST_KEY.consumeClick()) {
                
            }
        });
    }
}