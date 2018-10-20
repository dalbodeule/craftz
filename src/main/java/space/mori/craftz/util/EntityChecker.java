package space.mori.craftz.util;

import space.mori.craftz.CraftZ;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.function.Predicate;

public class EntityChecker {
    public static List<Entity> getNearbyEntities(Location loc, double radius) {
        Arrow tester = loc.getWorld().spawn(loc, Arrow.class);
        List<Entity> nearby = getNearbyEntities(tester, radius);
        tester.remove();
        return nearby;
    }

    public static List<Entity> getNearbyEntities(Entity entity, double radius) {
        return entity.getNearbyEntities(radius, radius, radius);
    }

    public static boolean areEntitiesNearby(Location loc, double radius, EntityType type, int amount) {
        List<Entity> nearby = getNearbyEntities(loc, radius);
        if (nearby == null) {
            return false;
        }
        int found = 0;
        for (Entity ent : nearby) {
            if (ent.getType() == type && ++found >= amount) {
                return true;
            }
        }
        return false;
    }

    public static boolean areEntitiesNearby(Location loc, double radius, Predicate<? super Entity> predicate,
            int amount) {
        List<Entity> nearby = getNearbyEntities(loc, radius);
        if (nearby == null) {
            return false;
        }
        int found = 0;
        for (Entity ent : nearby) {
            if (predicate.test(ent) && ++found >= amount) {
                return true;
            }
        }
        return false;
    }

    public static int getEntityCountInWorld(World world, EntityType type) {
        List<Entity> entities = world.getEntities();
        int count = 0;
        for (final Entity ent : entities) {
            if (ent.getType() == type) {
                ++count;
            }
        }
        return count;
    }

    public static int getEntityCountInWorld(World world, Predicate<? super Entity> predicate) {
        List<Entity> entities = world.getEntities();
        int count = 0;
        for (Entity ent : entities) {
            if (predicate.test(ent)) {
                ++count;
            }
        }
        return count;
    }

    public static MetadataValue getMeta(Entity ent, String key) {
        if (!ent.hasMetadata(key)) {
            return null;
        }
        List<MetadataValue> values = ent.getMetadata(key);
        for (MetadataValue value : values) {
            if (value.getOwningPlugin().getName().equals(CraftZ.getInstance().getName())) {
                return value;
            }
        }
        return null;
    }
}
