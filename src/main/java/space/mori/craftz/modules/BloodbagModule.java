package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.Rewarder;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;

public class BloodbagModule extends Module {
    public BloodbagModule(CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        FileConfiguration config = this.getConfig("config");
        Player p = event.getPlayer();
        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                && event.getMaterial() == Material.RED_DYE && event.getItem().getDurability() == 1
                && config.getBoolean("Config.players.medical.healing.heal-with-rosered") && !config.getBoolean(
                "Config.players.medical.healing.only-healing-others")) {
            this.reduceInHand(p);
            p.setHealth(20.0);
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.bloodbag"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (!this.isWorld(entity.getWorld()) || event.isCancelled() || !(damager instanceof Player)
                || !(entity instanceof Player)) {
            return;
        }
        Player pdamager = (Player) damager;
        ItemStack hand = pdamager.getInventory().getItemInMainHand();
        Player player = (Player) event.getEntity();
        if (hand.getType() != Material.RED_DYE || ((Dye) hand.getData()).getColor() != DyeColor.RED || !this.getConfig(
                "config").getBoolean("Config.players.medical.healing.heal-with-rosered")) {
            return;
        }
        event.setCancelled(true);
        if (hand.getAmount() < 2) {
            pdamager.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
        }
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.bloodbag"));
        Rewarder.RewardType.HEAL_PLAYER.reward(pdamager);
    }
}
