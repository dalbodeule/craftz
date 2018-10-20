package space.mori.craftz.worlddata;

import space.mori.craftz.CraftZ;
import space.mori.craftz.util.BlockChecker;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;

public class Spawnpoint extends WorldDataObject {
    private final Location loc;

    public Spawnpoint(World world, @Nonnull ConfigurationSection data) {
        this(
                data.getName(),
                new Location(world, data.getInt("coords.x"), data.getInt("coords.y"), data.getInt("coords.z"))
        );
    }

    public Spawnpoint(String id, Location loc) {
        super(id);
        this.loc = loc;
    }

    public static Location findSafeLocation(@Nonnull Location loc) {
        Location sloc = BlockChecker.getSafeSpawnLocationOver(loc)
                .orElse(BlockChecker.getSafeSpawnLocationUnder(loc).orElse(null));
        return CraftZ.centerOfBlock(sloc);
    }

    public Location getLocation() {
        return this.loc.clone();
    }

    public Location getSafeLocation() {
        return findSafeLocation(this.loc);
    }

    @Override
    public void store(@Nonnull ConfigurationSection section) {
        section.set("coords.x", this.loc.getBlockX());
        section.set("coords.y", this.loc.getBlockY());
        section.set("coords.z", this.loc.getBlockZ());
    }
}
