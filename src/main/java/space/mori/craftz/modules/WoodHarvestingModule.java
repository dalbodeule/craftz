package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.BlockChecker;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class WoodHarvestingModule extends Module {
    public WoodHarvestingModule(CraftZ craftZ) {
        super(craftZ);
    }

    private static int getAmount(Material material, Inventory inv) {
        int a = 0;
        for (ItemStack stack : inv) {
            if (stack != null && stack.getType() == material) {
                a += stack.getAmount();
            }
        }
        return a;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        FileConfiguration config = this.getConfig("config");
        Player p = event.getPlayer();
        Material type = event.getMaterial();
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (action != Action.RIGHT_CLICK_BLOCK || type != Material.IRON_AXE || !config.getBoolean(
                "Config.players.wood-harvesting.enable")) {
            return;
        }
        if (BlockChecker.isTree(block)) {
            int limit = config.getInt("Config.players.wood-harvesting.log-limit");
            PlayerInventory inv = p.getInventory();
            if (limit < 1 || (!inv.contains(Material.LEGACY_LOG, limit) && !inv.contains(Material.LEGACY_LOG_2, limit))) {
                Item itm = p.getWorld().dropItem(p.getLocation(), new ItemStack(Material.LEGACY_LOG, 1));
                itm.setPickupDelay(0);
                p.sendMessage(this.getMsg("Messages.harvested-tree"));
            } else {
                p.sendMessage(this.getMsg("Messages.already-have-wood"));
            }
        } else {
            p.sendMessage(this.getMsg("Messages.isnt-a-tree"));
        }
    }

    @Override
    public int getNumberAllowed(Inventory inv, ItemStack item) {
        if ((item.getType() != Material.LEGACY_LOG && item.getType() != Material.LEGACY_LOG_2) || !this.getConfig("config")
                .getBoolean("Config.players.wood-harvesting.enable")) {
            return -1;
        }
        int limit = this.getConfig("config").getInt("Config.players.wood-harvesting.log-limit");
        if (limit < 0) {
            return -1;
        }
        int invAmount = getAmount(Material.LEGACY_LOG, inv) + getAmount(Material.LEGACY_LOG_2, inv);
        return Math.max(limit - invAmount, 0);
    }
}
