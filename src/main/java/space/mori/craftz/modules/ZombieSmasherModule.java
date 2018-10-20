package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ZombieSmasherModule extends Module {
    public ZombieSmasherModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        final Entity entity = event.getEntity();
        final Entity damager = event.getDamager();
        if (this.isWorld(entity.getWorld()) && damager instanceof Player && this.getCraftZ().isEnemy(entity)) {
            final Player p = (Player) damager;
            final Location ploc = p.getLocation();
            final ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand != null && hand.hasItemMeta()) {
                final ItemMeta m = hand.getItemMeta();
                if (m.hasDisplayName() && m.getDisplayName().equals(ChatColor.GOLD + "Zombie Smasher")) {
                    event.setDamage(
                            ((Attributable) entity).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * 10.0);
                    for (int i = 0; i < 4; ++i) {
                        p.playSound(ploc, Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}
