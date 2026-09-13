package gaydev.yaiden.femboysleeping.client;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import gaydev.yaiden.femboysleeping.client.crasher.chatinput;
import gaydev.yaiden.femboysleeping.keysbindedwithlove.Keybined;
import net.fabricmc.api.ClientModInitializer;
public class YaidensaddonClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new chatinput().idk();
		Keybined.register();
		// Config is now loaded from Yaidensaddon.onInitialize() (common code)
		// so a dedicated server sees the same settings the client screen saves.

		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}