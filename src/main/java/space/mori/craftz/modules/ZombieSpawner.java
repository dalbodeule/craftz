package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.EntityChecker;
import space.mori.craftz.worlddata.Spawnpoint;
import space.mori.craftz.worlddata.WorldData;
import space.mori.craftz.worlddata.ZombieSpawnpoint;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ZombieSpawner extends Module {
    private double autoSpawnTicks = 0.0;
    private List<ZombieSpawnpoint> spawns = new ArrayList<>();

    public ZombieSpawner(CraftZ craftZ) {
        super(craftZ);
    }

    public static String makeID(final Location signLoc) {
        return "x" + signLoc.getBlockX() + "y" + signLoc.getBlockY() + "z" + signLoc.getBlockZ();
    }

    @Override
    public void onLoad(final boolean configReload) {
        this.spawns.clear();
        ConfigurationSection sec = WorldData.get().getConfigurationSection("Data.zombiespawns");
        if (sec != null) {
            for (String entry : sec.getKeys(false)) {
                ConfigurationSection data = sec.getConfigurationSection(entry);
                ZombieSpawnpoint spawn = new ZombieSpawnpoint(this, data);
                this.spawns.add(spawn);
            }
        }
    }

    public int getSpawnCount() {
        return this.spawns.size();
    }

    public ZombieSpawnpoint getSpawnpoint(String signID) {
        return this.spawns.stream().filter(spawn -> spawn.getID().equals(signID)).findFirst().orElse(null);
    }

    public ZombieSpawnpoint getSpawnpoint(Location signLoc) {
        return this.getSpawnpoint(makeID(signLoc));
    }

    public void addSpawn(Location signLoc, int maxInRadius, int maxRadius, String type) {
        String id = makeID(signLoc);
        ZombieSpawnpoint spawn = new ZombieSpawnpoint(
                this, id, signLoc, maxRadius, maxInRadius, (type == null || type.trim().isEmpty()) ? null : type);
        this.spawns.add(spawn);
        spawn.save();
    }

    public void removeSpawn(String signID) {
        WorldData.get().set("Data.zombiespawns." + signID, null);
        WorldData.save();
        ZombieSpawnpoint spawn = this.getSpawnpoint(signID);
        if (spawn != null) {
            this.spawns.remove(spawn);
        }
    }

    @Nonnull
    public Optional<LivingEntity> spawnAt(@Nullable Location loc, String type) {
        return this.spawnAt(loc, this.getCraftZ().getEnemyDefinition(type));
    }

    @Nonnull
    public Optional<LivingEntity> spawnAt(@Nullable Location loc, @Nullable ConfigurationSection sec) {
        if (loc == null || sec == null) {
            return Optional.empty();
        }
        int zombies = EntityChecker.getEntityCountInWorld(this.world(), t -> t.hasMetadata("enemyType"));
        int maxZombies = this.getConfig("config").getInt("Config.mobs.zombies.spawning.maxzombies");
        if (zombies < maxZombies || maxZombies < 0) {
            return this.getCraftZ().spawnEnemy(sec, loc);
        }
        return Optional.empty();
    }

    @Override
    public void onServerTick(long tick) {
        for (ZombieSpawnpoint spawn : this.spawns) {
            spawn.onServerTick();
        }
        FileConfiguration config = this.getConfig("config");
        int pc = this.getCraftZ().getPlayerManager().getPlayerCount();
        if (config.getBoolean("Config.mobs.zombies.spawning.enable-auto-spawn") && pc > 0) {
            ++this.autoSpawnTicks;
            double perPlayer = config.getDouble("Config.mobs.zombies.spawning.auto-spawning-interval") * 20.0 / pc;
            while (this.autoSpawnTicks >= perPlayer) {
                this.autoSpawnTicks -= perPlayer;
                if (this.autoSpawnTicks < 0.0) {
                    this.autoSpawnTicks = 0.0;
                }
                Player p = this.getCraftZ().getPlayerManager().randomPlayer().orElse(null);
                if (p == null) {
                    break;
                }
                List<ConfigurationSection> types = this.getCraftZ().getAutoSpawnEnemyDefinitions();
                if (types.isEmpty()) {
                    continue;
                }
                Location loc = p.getLocation()
                        .add(CraftZ.RANDOM.nextInt(128) - 64, 0.0, CraftZ.RANDOM.nextInt(128) - 64);
                this.spawnAt(Spawnpoint.findSafeLocation(loc), types.get(CraftZ.RANDOM.nextInt(types.size())));
            }
        }
    }
}
