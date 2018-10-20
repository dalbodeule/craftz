package space.mori.craftz.worlddata;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import javax.annotation.Nonnull;

public abstract class WorldDataObject {
    private final String id;

    public WorldDataObject(String id) {
        this.id = id;
    }

    public String getID() {
        return this.id;
    }

    public final void save(String basePath) {
        FileConfiguration wd = WorldData.get();
        ConfigurationSection sec = wd.createSection(basePath + "." + this.id);
        this.store(sec);
        WorldData.save();
    }

    public abstract void store(@Nonnull ConfigurationSection p0);
}
