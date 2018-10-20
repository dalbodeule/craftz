package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;

public class RocketLauncherModule extends Module {
    public RocketLauncherModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityShootBow(EntityShootBowEvent event) {
        LivingEntity entity = event.getEntity();
        Location loc = entity.getLocation();
        if (!this.isWorld(entity.getWorld()) || event.getEntityType() != EntityType.PLAYER) {
            return;
        }
        Player p = (Player) entity;
        PlayerInventory inv = p.getInventory();
        if (!inv.contains(Material.TNT)) {
            return;
        }
        TNTPrimed tnt = p.getWorld().spawn(loc.add(0, 1, 0), TNTPrimed.class);
        tnt.setVelocity(loc.getDirection().clone().multiply(3));
        event.setCancelled(true);
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        int first = inv.first(Material.TNT);
        ItemStack firstTnt = inv.getItem(first);
        if (firstTnt.getAmount() > 1) {
            firstTnt.setAmount(firstTnt.getAmount() - 1);
        } else {
            inv.setItem(first, null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityExplode(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        Location loc = event.getLocation();
        if (entity == null || !this.isWorld(loc.getWorld()) || event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        event.setCancelled(true);
        loc.getWorld().createExplosion(loc, 0.0f);
        List<Entity> nearby = entity.getNearbyEntities(20.0, 20.0, 20.0);
        nearby.stream()
                .filter(ent -> ent instanceof LivingEntity)
                .map(ent -> (LivingEntity) ent)
                .forEach(lent -> lent.damage(20.0 * (1.0 - loc.distance(lent.getLocation()) / 20.0)));
    }
}
