package gaydev.yaiden.femboysleeping.client;

import gaydev.yaiden.femboysleeping.Yaidensaddon;
import gaydev.yaiden.femboysleeping.client.crasher.chatinput;
import net.fabricmc.api.ClientModInitializer;
public class YaidensaddonClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		new chatinput().idk();
		
		
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
	}
}