package gaydev.yaiden.femboysleeping.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class YaidensaddonConfigManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static ModConfig CONFIG = new ModConfig();

    private static final Path CONFIG_PATH =
            Path.of("config/yaidensaddon.json");

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                String json = Files.readString(CONFIG_PATH);

                CONFIG = GSON.fromJson(json, ModConfig.class);

                if (CONFIG == null) {
                    CONFIG = new ModConfig();
                }
            } else {
                save();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            String json = GSON.toJson(CONFIG);

            Files.writeString(CONFIG_PATH, json);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
