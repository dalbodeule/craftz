package space.mori.craftz.modules;

import org.bukkit.event.world.PortalCreateEvent;
import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.StackParser;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerWorldProtectionModule extends Module {
    public PlayerWorldProtectionModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack hand = event.getItemInHand();
        Block block = event.getBlock();
        Player p = event.getPlayer();
        if (!this.isWorld(p.getWorld())) {return;}
        FileConfiguration config = this.getConfig("config");
        if (!config.getBoolean("Config.players.interact.block-placing") && !p.hasPermission("craftz.build")) {
            boolean allow = config.getStringList("Config.players.interact.placeable-blocks")
                    .stream()
                    .anyMatch(s -> StackParser.compare(hand, s, false) || StackParser.compare(block, s));
            if (!allow) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        FileConfiguration config = this.getConfig("config");
        Player p = event.getPlayer();
        event.setExpToDrop(0);
        if (config.getBoolean("Config.players.interact.block-breaking") || p.hasPermission("craftz.build")) {
            return;
        }
        final ItemStack hand = p.getInventory().getItemInMainHand();
        final Block block = event.getBlock();
        final ConfigurationSection sec = config.getConfigurationSection("Config.players.interact.breakable-blocks");
        boolean allow = false;
        for (String key : sec.getKeys(false)) {
            if (StackParser.compare(block, key)) {
                String value = sec.getString(key);
                if (value.equalsIgnoreCase("all") || value.equalsIgnoreCase("any") || StackParser.compare(
                        hand, value, false)) {
                    allow = true;
                    break;
                }
                break;
            }
        }
        if (!allow) {
            event.setCancelled(true);
        }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player p = event.getPlayer();
        if (this.isWorld(p.getWorld()) && !this.getConfig("config").getBoolean("Config.players.interact.block-placing")
                && !p.hasPermission("craftz.build")) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.errors.not-enough-permissions"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (this.isWorld(event.getEntity().getWorld()) && event.getRemover().getType() == EntityType.PLAYER) {
            Player p = (Player) event.getRemover();
            if (!this.getConfig("config").getBoolean("Config.players.interact.block-breaking") && !p.hasPermission(
                    "craftz.build")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (!this.isWorld(event.getBlock().getWorld()) || this.getConfig("config")
                .getBoolean("Config.world.world-changing.allow-burning")) {
            return;
        }
        Block block = event.getBlock();
        Material type = block.getType();
        Player p = event.getPlayer();
        if (type == Material.OBSIDIAN) {
            return;
        }
        if (p != null && !p.hasPermission("craftz.build")) {
            event.setCancelled(true);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalCreate(PortalCreateEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity();
        if (!this.isWorld(entity.getWorld()) || !(entity instanceof Player) || this.getConfig("config")
                .getBoolean("Config.players.interact.block-placing")) {
            return;
        }
        Player p = (Player) entity;
        if (!p.hasPermission("craftz.build")) {
            event.setCancelled(true);
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.errors.not-enough-permissions"));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerBedEnter(final PlayerBedEnterEvent event) {
        boolean cancel = this.isWorld(event.getBed().getWorld()) && !this.getConfig("config")
                .getBoolean("Config.players.interact.sleeping");

        event.setCancelled(cancel);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerShearEntity(final PlayerShearEntityEvent event) {
        Player p = event.getPlayer();

        boolean cancel = this.isWorld(p.getWorld()) && !this.getConfig("config").getBoolean("Config.animals.shearing")
                && !p.hasPermission("craftz.admin");

        event.setCancelled(cancel);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSheepDyeWool(final SheepDyeWoolEvent event) {
        boolean cancel = this.isWorld(event.getEntity().getWorld());
        event.setCancelled(cancel);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onStructureGrow(final StructureGrowEvent event) {
        if (!this.isWorld(event.getWorld()) || this.getConfig("config")
                .getBoolean("Config.world.world-changing.allow-tree-grow") || !event.isFromBonemeal()) {
            return;
        }
        final Player p = event.getPlayer();

        boolean cancel =
                !this.getConfig("config").getBoolean("Config.players.interact.block-placing") && !p.hasPermission(
                        "craftz.build");
        event.setCancelled(cancel);
    }
}
