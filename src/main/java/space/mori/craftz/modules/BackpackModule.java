package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.ItemRenamer;
import space.mori.craftz.worlddata.Backpack;
import space.mori.craftz.worlddata.WorldData;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BackpackModule extends Module {
    private List<Backpack> backpacks = new ArrayList<>();

    public BackpackModule(CraftZ craftZ) {
        super(craftZ);
    }

    @Override
    public void onLoad(boolean configReload) {
        if (configReload) {
            return;
        }
        this.backpacks.clear();
        ConfigurationSection sec = WorldData.get().getConfigurationSection("Data.backpacks");
        if (sec == null) {
            return;
        }
        for (String id : sec.getKeys(false)) {
            ConfigurationSection data = sec.getConfigurationSection(id);
            this.backpacks.add(new Backpack(data));
        }
    }

    @Override
    public void onDisable() {
        WorldData.get().set("Data.backpacks", null);
        this.backpacks.forEach(Backpack::save);
        WorldData.save();
    }

    public Optional<Backpack> find(ItemStack stack) {
        return this.backpacks.stream().filter(bp -> bp.is(stack)).findFirst();
    }

    public void open(HumanEntity p, Backpack backpack) {
        Inventory inv = backpack.getInventory();
        p.openInventory(inv);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
    }

    public boolean open(HumanEntity p, ItemStack item) {
        if ((p instanceof Player && this.getCraftZ().getKits().isEditing((Player) p)) || !this.isWorld(p.getWorld())) {
            return false;
        }
        Backpack bp = this.find(item).orElse(null);
        if (bp == null) {
            if (item.getAmount() > 1) {
                ItemStack st = item.clone();
                st.setAmount(st.getAmount() - 1);
                p.getWorld().dropItem(p.getLocation(), st).setPickupDelay(0);
                item.setAmount(1);
            }
            bp = Backpack.create(item).orElse(null);
            if (bp == null) {
                return false;
            }
            this.backpacks.add(bp);
            ItemRenamer.on(item).copyFrom(bp.getItem());
        }
        this.open(p, bp);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player p = event.getPlayer();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && Backpack.isBackpack(item) && this.open(p, item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDespawn(ItemDespawnEvent event) {
        this.find(event.getEntity().getItemStack()).ifPresent(bp -> this.backpacks.remove(bp));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(EntityCombustEvent event) {
        if (event.getEntityType() == EntityType.DROPPED_ITEM) {
            Item item = (Item) event.getEntity();
            this.find(item.getItemStack()).ifPresent(bp -> this.backpacks.remove(bp));
        }
    }

    @Override
    public int getNumberAllowed(@Nonnull Inventory inv, @Nullable ItemStack item) {
        if (!Backpack.isBackpack(item)) {
            return -1;
        }
        if (inv.getType() != InventoryType.PLAYER) {
            return 0;
        }
        int bps = Arrays.stream(inv.getContents()).filter(Backpack::isBackpack).mapToInt(ItemStack::getAmount).sum();
        return bps > 0 ? 0 : 1;
    }
}
