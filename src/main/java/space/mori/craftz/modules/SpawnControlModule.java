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
    public static final List<EntityType> BLOCKED = Arrays.asList(
            EntityType.BAT, EntityType.BLAZE, EntityType.BEE,
            EntityType.CAT, EntityType.CAVE_SPIDER, EntityType.CREEPER,
            EntityType.DONKEY,
            EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ELDER_GUARDIAN, EntityType.EVOKER,
            EntityType.FOX,
            EntityType.GHAST, EntityType.GUARDIAN,
            EntityType.HORSE, EntityType.HOGLIN, EntityType.HUSK,
            EntityType.ILLUSIONER,
            EntityType.MAGMA_CUBE, EntityType.MUSHROOM_COW, EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PANDA, EntityType.PIGLIN, EntityType.PILLAGER,
            EntityType.RABBIT, EntityType.RAVAGER,
            EntityType.SKELETON, EntityType.SPIDER, EntityType.SILVERFISH, EntityType.SLIME, EntityType.SQUID,
            EntityType.SHULKER, EntityType.STRAY,
            EntityType.TRADER_LLAMA,
            EntityType.VILLAGER, EntityType.VEX, EntityType.VINDICATOR,
            EntityType.WITCH, EntityType.WOLF, EntityType.WITHER, EntityType.WITHER_SKELETON,
            EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN
    );
    public static final List<EntityType> ANIMALS = Arrays.asList(
            EntityType.CAT, EntityType.CHICKEN, EntityType.COD, EntityType.COW,
            EntityType.DONKEY, EntityType.DOLPHIN,
            EntityType.PIG,
            EntityType.PANDA, EntityType.PARROT, EntityType.POLAR_BEAR, EntityType.PUFFERFISH,
            EntityType.RABBIT,
            EntityType.SALMON, EntityType.SHEEP,
            EntityType.TROPICAL_FISH, EntityType.TURTLE,
            EntityType.VILLAGER
    );

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
