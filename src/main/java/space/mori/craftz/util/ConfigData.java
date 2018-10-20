package space.mori.craftz.util;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class ConfigData {
    public FileConfiguration config;
    public File configFile;

    public ConfigData(final File configFile) {
        this.configFile = configFile;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((this.configFile == null) ? 0 : this.configFile.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final ConfigData other = (ConfigData) obj;
        if (this.configFile == null) {
            return other.configFile == null;
        } else {
            return this.configFile.equals(other.configFile);
        }
    }
}
