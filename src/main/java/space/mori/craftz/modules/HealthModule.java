package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HealthModule extends Module {
    public HealthModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        if (this.isWorld(event.getEntity().getWorld()) && event.getEntityType() == EntityType.PLAYER
                && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        if (this.isWorld(event.getEntity().getWorld()) && event.getEntityType() == EntityType.PLAYER) {
            final Player p = (Player) event.getEntity();
            if (this.getCraftZ().getPlayerManager().isInsideOfLobby(p)) {
                event.setCancelled(true);
            } else if (event.getFoodLevel() > p.getFoodLevel()) {
                p.setHealth(Math.min(p.getHealth() + 2.0, p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue()));
            }
        }
    }
}
