package space.mori.craftz.worlddata;

import com.google.common.io.Files;
import space.mori.craftz.CraftZ;
import space.mori.craftz.util.ConfigData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WorldData {
    private static Map<String, ConfigData> configs;
    private static File dir;

    static {
        WorldData.configs = new HashMap<>();
    }

    public static void setup() {
        WorldData.dir = new File(CraftZ.getInstance().getDataFolder(), "worlds");
        if (!WorldData.dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            WorldData.dir.mkdirs();
        }
        tryUpdate();
        File[] listFiles = WorldData.dir.listFiles();
        if (listFiles == null) {
            return;
        }
        for (final File file : listFiles) {
            if (file.getName().toLowerCase().endsWith(".yml")) {
                reload(file.getName().substring(0, file.getName().length() - 4));
            }
        }
    }

    private static void tryUpdate() {
        final File old = new File(CraftZ.getInstance().getDataFolder(), "data.yml");
        if (old.exists()) {
            try {
                final File newFile = new File(WorldData.dir, CraftZ.getInstance().worldName() + ".yml");
                if (newFile.exists()) {
                    return;
                }
                //noinspection ResultOfMethodCallIgnored
                newFile.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                newFile.createNewFile();
                Files.copy(old, newFile);
                //noinspection ResultOfMethodCallIgnored
                old.delete();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void tryUpdateConfig(String world) {
        int version = get(world).getInt("Data.never-ever-modify.configversion");
        if (version < 1) {
            uc_1(world);
            get(world).set("Data.never-ever-modify.configversion", 1);
            save(world);
        }
        if (version < 2) {
            uc_2(world);
            get(world).set("Data.never-ever-modify.configversion", 2);
            save(world);
        }
    }

    private static void uc_1(String world) {
        CraftZ.info("Converting world data for '" + world + "' to version 1");
        ConfigurationSection plSec = get(world).getConfigurationSection("Data.players");
        if (plSec == null) {
            CraftZ.info(" -  No player data exists, no conversion needed");
            return;
        }
        for (final String key : plSec.getKeys(false)) {
            final UUID id = Bukkit.getOfflinePlayer(key).getUniqueId();
            if (id == null) {
                CraftZ.warn(" -  Not able to convert player '" + key + "', he will be deleted");
                plSec.set(key, null);
            } else {
                int thirst = get(world).getInt("Data.players." + key + ".thirst");
                int zombiesKilled = get(world).getInt("Data.players." + key + ".zombiesKilled");
                int playersKilled = get(world).getInt("Data.players." + key + ".playersKilled");
                int minutesSurvived = get(world).getInt("Data.players." + key + ".minsSurvived");
                boolean bleeding = get(world).getBoolean("Data.players." + key + ".bleeding");
                boolean bonesBroken = get(world).getBoolean("Data.players." + key + ".bonesBroken");
                boolean poisoned = get(world).getBoolean("Data.players." + key + ".poisoned");
                String conv = new PlayerData(thirst, zombiesKilled, playersKilled, minutesSurvived, bleeding,
                        bonesBroken, poisoned
                ).toString();
                plSec.set(id.toString(), conv);
                plSec.set(key, null);
            }
        }
        CraftZ.info(" -  Done");
    }

    private static void uc_2(String world) {
        CraftZ.info("Converting world data for '" + world + "' to version 2");
        get(world).set("Data.dead", null);
        CraftZ.info(" -  Done");
    }

    private static void load(String world) {
        FileConfiguration config = get(world);
        config.options().header("Data for the CraftZ plugin by JangoBrick\nThis is for the world \"" + world + "\"");
        config.options().copyDefaults(true);
        save(world);
        tryUpdateConfig(world);
    }

    public static void reload(String world) {
        ConfigData data = new ConfigData(new File(WorldData.dir, world + ".yml"));
        data.config = YamlConfiguration.loadConfiguration(data.configFile);
        if (!WorldData.configs.containsKey(world)) {
            WorldData.configs.put(world, data);
        }
        load(world);
    }

    public static void reload() {
        reload(CraftZ.getInstance().worldName());
    }

    public static FileConfiguration get(final String world) {
        if (!WorldData.configs.containsKey(world)) {
            reload(world);
        }
        return WorldData.configs.get(world).config;
    }

    public static FileConfiguration get() {
        return get(CraftZ.getInstance().worldName());
    }

    public static void save(String world) {
        ConfigData cd = WorldData.configs.get(world);
        if (cd.config == null || cd.configFile == null) {
            return;
        }
        try {
            cd.config.save(cd.configFile);
        } catch (IOException ex) {
            CraftZ.severe("Could not save config to " + WorldData.configs.get(world).configFile);
            ex.printStackTrace();
        }
    }

    public static void save() {
        save(CraftZ.getInstance().worldName());
    }
}
