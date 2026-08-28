package gaydev.yaiden.femboysleeping.functions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gaydev.yaiden.femboysleeping.Yaidensaddon;

import javax.swing.JFrame;
import javax.swing.JLabel;



public class Fabricatedfuncs {
            private static String MOD_ID = Yaidensaddon.MOD_ID;
            private static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
        public static void print(Player client, String message) {
                client.sendSystemMessage(Component.literal(message));
            };
        public static void Eprint(String message) {
            LOGGER.error(message);
            };
            // terminates your session on error
        public static void terminate(Player player) {
            JFrame frame = new JFrame("yaidens crash.report");
            JLabel label = new JLabel("The femboy engine has exploded.");
            frame.setSize(400, 200);
            frame.add(label);
            frame.setLocationRelativeTo(null);
            
        }
        public static void SystemMessage(String message) {
            System.out.println(message);
        }
        public static void throwEprint(String message, Throwable throwable) {
            throw new UnsupportedOperationException(message, throwable);
        }
}
