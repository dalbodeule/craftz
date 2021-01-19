package space.mori.craftz.modules;

import org.bukkit.*;
import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.EntityChecker;
import space.mori.craftz.util.ItemRenamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class BloodParticlesModule extends Module {
    public BloodParticlesModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        final EntityType type = event.getEntityType();
        if (this.isWorld(entity.getWorld()) && !event.isCancelled() && this.getConfig("config")
                .getBoolean("Config.mobs.blood-particles-when-damaged") && type.isAlive() && (type != EntityType.PLAYER
                || this.isSurvival((Player) entity)) && type != EntityType.ARMOR_STAND) {
            final Location loc = entity.getLocation();
            final World w = entity.getWorld();
            for (int bloodCount = (int) Math.min(
                    event.getDamage() * ((type == EntityType.ZOMBIE) ? 1 : 2), 100.0), i = 0; i < bloodCount; ++i) {
                final ItemStack stack = ItemRenamer.on(new ItemStack(Material.REDSTONE))
                        .setName("blood" + CraftZ.RANDOM.nextInt())
                        .get();
                final Item blood = w.dropItemNaturally(loc, stack);
                blood.setPickupDelay(Integer.MAX_VALUE);
                blood.setMetadata("isBlood", new FixedMetadataValue(this.getCraftZ(), true));
                Bukkit.getScheduler()
                        .scheduleSyncDelayedTask(this.getCraftZ(), blood::remove,
                                (long) (4 + CraftZ.RANDOM.nextInt(6))
                        );
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(final ChunkLoadEvent event) {
        if (!this.isWorld(event.getWorld())) {
            return;
        }
        if (!this.getConfig("config").getBoolean("Config.world.world-changing.allow-new-chunks")
                && event.isNewChunk()) {
            event.getChunk().unload(false);
            return;
        }
        Entity[] entities = event.getChunk().getEntities();
        for (final Entity ent : entities) {
            MetadataValue value = EntityChecker.getMeta(ent, "isBlood");
            if (ent.getType() == EntityType.DROPPED_ITEM && value != null && value.asBoolean()) {
                ent.remove();
            }
        }
    }
}
