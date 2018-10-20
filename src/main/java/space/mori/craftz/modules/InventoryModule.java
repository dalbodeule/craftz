package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.ItemRenamer;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryModule extends Module {
    public InventoryModule(final CraftZ craftZ) {
        super(craftZ);
    }

    public int calculateNumberAllowed(final Inventory inv, final ItemStack stack) {
        if (stack == null) {
            return -1;
        }
        int allowed = -1;
        for (final Module m : this.getCraftZ().getModules()) {
            final int ma = m.getNumberAllowed(inv, stack);
            if (ma >= 0 && (allowed < 0 || allowed > ma)) {
                allowed = ma;
            }
        }
        return allowed;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPickupItem(final EntityPickupItemEvent event) {
        final Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }
        final Player p = (Player) entity;
        final Item item = event.getItem();
        final ItemStack stack = item.getItemStack();
        if (this.isWorld(p.getWorld())) {
            final PlayerInventory inv = p.getInventory();
            final int allowed = this.calculateNumberAllowed(inv, stack);
            if (allowed >= 0 && allowed < stack.getAmount()) {
                event.setCancelled(true);
                if (allowed > 0) {
                    final ItemStack drop = stack.clone();
                    drop.setAmount(stack.getAmount() - allowed);
                    item.getWorld().dropItem(item.getLocation(), drop);
                    stack.setAmount(allowed);
                    p.getInventory().addItem(stack);
                    item.remove();
                    p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.5f, 2.0f);
                }
            }
            ItemRenamer.on(p).setSpecificNames(ItemRenamer.DEFAULT_MAP);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        if (this.isWorld(event.getWhoClicked().getWorld())) {
            final ItemStack cursor = event.getCursor();
            final boolean lower = event.getView().convertSlot(event.getRawSlot()) != event.getRawSlot();
            final Inventory inv = lower ? event.getView().getBottomInventory() : event.getView().getTopInventory();
            if (cursor != null) {
                final int allowed = this.calculateNumberAllowed(inv, cursor);
                if (allowed < 0 || allowed >= cursor.getAmount()) {
                    return;
                }
                event.setCancelled(true);
            }
        }
    }
}
