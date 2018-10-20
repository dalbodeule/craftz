package space.mori.craftz;

import space.mori.craftz.util.ConfigData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static Map<String, ConfigData> configs = new HashMap<>();

    public static void newConfig(String name, @Nonnull ConfigData data, @Nonnull Map<String, Object> defaults) {
        ConfigManager.configs.put(name, data);
        final FileConfiguration c = getConfig(name);
        for (final String path : defaults.keySet()) {
            c.addDefault(path, defaults.get(path));
        }
        getConfig(name).options().copyDefaults(true);
        saveConfig(name);
    }

    public static void newConfig(String name, @Nonnull JavaPlugin plugin, @Nonnull Map<String, Object> defaults) {
        newConfig(name, new ConfigData(new File(plugin.getDataFolder(), name + ".yml")), defaults);
    }

    public static void reloadConfig(String name) {
        ConfigManager.configs.get(name).config = YamlConfiguration.loadConfiguration(
                ConfigManager.configs.get(name).configFile);
    }

    public static void reloadConfigs() {
        for (final String cfg : ConfigManager.configs.keySet()) {
            reloadConfig(cfg);
        }
    }

    public static FileConfiguration getConfig(String name) {
        if (ConfigManager.configs.get(name).config == null) {
            reloadConfig(name);
        }
        return ConfigManager.configs.get(name).config;
    }

    public static void saveConfig(String name) {
        if (ConfigManager.configs.get(name).config == null || ConfigManager.configs.get(name).configFile == null) {
            return;
        }
        try {
            getConfig(name).save(ConfigManager.configs.get(name).configFile);
        } catch (IOException ex) {}
    }
}
