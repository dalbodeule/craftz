package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;

public class GrenadeModule extends Module {
    public GrenadeModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileHit(final ProjectileHitEvent event) {
        final Projectile pr = event.getEntity();
        final Location loc = pr.getLocation();
        if (this.isWorld(pr.getWorld()) && event.getEntityType() == EntityType.ENDER_PEARL) {
            if (!this.getConfig("config").getBoolean("Config.players.weapons.grenade-enable", true)) {
                return;
            }
            pr.remove();
            pr.getWorld().createExplosion(loc, 0.0f);
            final double range = this.getConfig("config").getDouble("Config.players.weapons.grenade-range");
            final double power = this.getConfig("config").getDouble("Config.players.weapons.grenade-power");
            final List<Entity> nearby = pr.getNearbyEntities(range, range, range);
            for (final Entity ent : nearby) {
                final boolean allowPlayer = this.getConfig("config")
                        .getBoolean("Config.players.weapons.grenade-damage-players");
                final boolean isPlayer = ent instanceof Player;
                final boolean allowMobs = this.getConfig("config")
                        .getBoolean("Config.players.weapons.grenade-damage-mobs");
                final boolean isLiving = ent instanceof LivingEntity;
                if (isLiving) {
                    if (isPlayer) {
                        if (!allowPlayer) {
                            continue;
                        }
                    } else if (!allowMobs) {
                        continue;
                    }
                    final LivingEntity lent = (LivingEntity) ent;
                    final double d = 1.0 - loc.distance(lent.getLocation()) / range;
                    lent.damage(d * 4.0 * power + ((d > 0.75) ? power : 0.0));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (this.isWorld(event.getTo().getWorld())
                && event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
        }
    }
}
