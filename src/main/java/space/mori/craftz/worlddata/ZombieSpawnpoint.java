package space.mori.craftz.worlddata;

import space.mori.craftz.modules.ZombieSpawner;
import space.mori.craftz.util.EntityChecker;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Optional;

public class ZombieSpawnpoint extends Spawnpoint {
    private final ZombieSpawner spawner;
    private final int maxInRadius;
    private final int maxRadius;
    private final String type;
    private int countdown;

    public ZombieSpawnpoint(@Nonnull final ZombieSpawner spawner, @Nonnull ConfigurationSection data) {
        super(spawner.world(), data);
        this.spawner = spawner;
        this.maxInRadius = data.getInt("max-zombies-in-radius");
        this.maxRadius = data.getInt("max-zombies-radius");
        this.type = data.getString("type");
    }

    public ZombieSpawnpoint(ZombieSpawner spawner, String id, Location loc, int maxInRadius, int maxRadius,
            String type) {
        super(id, loc);
        this.spawner = spawner;
        this.maxInRadius = maxInRadius;
        this.maxRadius = maxRadius;
        this.type = type;
    }

    public int getMaxInRadius() {
        return this.maxInRadius;
    }

    public int getMaxRadius() {
        return this.maxRadius;
    }

    public void save() {
        this.save("Data.zombiespawns");
    }

    @Override
    public void store(@Nonnull ConfigurationSection section) {
        super.store(section);
        section.set("max-zombies-in-radius", this.maxInRadius);
        section.set("max-zombies-radius", this.maxRadius);
        section.set("type", this.type);
    }

    @Nonnull
    public Optional<LivingEntity> spawn() {
        Location loc = this.getSafeLocation();
        if (loc == null) {
            return Optional.empty();
        }
        ConfigurationSection sec = this.spawner.getCraftZ().getEnemyDefinition(this.type);
        if (sec == null) {
            return Optional.empty();
        }
        boolean near = EntityChecker.areEntitiesNearby(
                loc, this.maxRadius, t -> t.hasMetadata("enemyType"), this.maxInRadius);
        if (near) {
            return Optional.empty();
        }
        return this.spawner.spawnAt(loc, this.type);
    }

    public void onServerTick() {
        --this.countdown;
        if (this.countdown <= 0) {
            this.spawn();
            this.countdown = this.spawner.getConfig("config").getInt("Config.mobs.zombies.spawning.interval") * 20;
        }
    }
}
