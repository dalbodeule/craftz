package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.Arrays;
import java.util.List;

public class SpawnControlModule extends Module {
    public static final List<EntityType> BLOCKED = Arrays.asList(EntityType.SKELETON, EntityType.CREEPER,
            EntityType.SPIDER, EntityType.ENDERMAN, EntityType.GHAST, EntityType.SILVERFISH, EntityType.SLIME,
            EntityType.SQUID, EntityType.PIG_ZOMBIE, EntityType.MAGMA_CUBE, EntityType.CAVE_SPIDER, EntityType.BLAZE,
            EntityType.OCELOT, EntityType.BAT, EntityType.WITCH, EntityType.WOLF, EntityType.MUSHROOM_COW,
            EntityType.HORSE, EntityType.ENDERMITE, EntityType.RABBIT
    );
    public static final List<EntityType> ANIMALS = Arrays.asList(
            EntityType.SHEEP, EntityType.PIG, EntityType.COW, EntityType.CHICKEN);

    public SpawnControlModule(CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Location loc = event.getLocation();
        CreatureSpawnEvent.SpawnReason reason = event.getSpawnReason();
        EntityType type = event.getEntityType();
        if (!this.isWorld(loc.getWorld()) || this.getConfig("config")
                .getBoolean("Config.mobs.completely-disable-spawn-control")) {
            return;
        }
        boolean plg = reason == CreatureSpawnEvent.SpawnReason.CUSTOM && this.getConfig("config")
                .getBoolean("Config.mobs.allow-all-plugin-spawning");
        for (final EntityType bt : SpawnControlModule.BLOCKED) {
            if (type == bt && !plg) {
                event.setCancelled(true);
            }
        }
        boolean allowAnimalSpawns = this.getConfig("config").getBoolean("Config.mobs.animals.spawning.enable");
        for (final EntityType at : SpawnControlModule.ANIMALS) {
            if (type == at && !allowAnimalSpawns && !plg) {
                event.setCancelled(true);
            }
        }
        if (type == EntityType.ZOMBIE && reason != CreatureSpawnEvent.SpawnReason.CUSTOM
                && reason != CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                && reason != CreatureSpawnEvent.SpawnReason.DISPENSE_EGG) {
            event.setCancelled(true);
        }
    }
}
