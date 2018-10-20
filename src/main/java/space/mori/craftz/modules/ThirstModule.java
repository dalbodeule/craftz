package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.worlddata.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class ThirstModule extends Module {
    public ThirstModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @Override
    public void onPlayerTick(final Player p, final long tick) {
        FileConfiguration config = this.getConfig("config");
        PlayerData data = this.getData(p);
        if (!this.isSurvival(p) || !config.getBoolean("Config.players.medical.thirst.enable")) {
            return;
        }
        Biome biome = p.getLocation().getBlock().getBiome();
        boolean desert = biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.MUTATED_DESERT;
        int ticksNeeded = desert
                          ? config.getInt("Config.players.medical.thirst.ticks-desert")
                          : config.getInt("Config.players.medical.thirst.ticks-normal");
        if (tick % ticksNeeded != 0L) {
            return;
        }
        if (data.thirst > 0) {
            --data.thirst;
            p.setLevel(data.thirst);
        } else {
            p.damage(2.0);
        }
        if (!config.getBoolean("Config.players.medical.thirst.show-messages")) {
            return;
        }
        if (data.thirst <= 8 && data.thirst > 1 && data.thirst % 2 == 0) {
            p.sendMessage(ChatColor.RED + this.getMsg("Messages.thirsty"));
        } else if (data.thirst <= 1) {
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.thirsty-dehydrating"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerItemConsume(final PlayerItemConsumeEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        Player p = event.getPlayer();
        ItemStack item = event.getItem();
        Material type = (item != null) ? item.getType() : Material.AIR;
        if (type != Material.POTION || item.getDurability() != 0 || !this.getConfig("config")
                .getBoolean("Config.players.medical.thirst.enable") || !this.getCraftZ()
                .getPlayerManager()
                .existsInWorld(p)) {
            return;
        }
        if (p.getInventory().getItemInMainHand().getAmount() < 2) {
            p.getInventory().setItemInMainHand(new ItemStack(Material.AIR, 0));
        } else {
            p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount() - 1);
        }
        p.setLevel(this.getData(p).thirst = 20);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (!this.isWorld(p.getWorld())) {
            return;
        }
        FileConfiguration config = this.getConfig("config");
        if (config.getBoolean("Config.players.medical.thirst.enable") || config.getBoolean(
                "Config.mobs.no-exp-drops")) {
            event.setDroppedExp(0);
            event.setKeepLevel(false);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (this.isWorld(event.getEntity().getWorld()) && this.getConfig("config")
                .getBoolean("Config.mobs.no-exp-drops")) {
            event.setDroppedExp(0);
        }
    }
}
