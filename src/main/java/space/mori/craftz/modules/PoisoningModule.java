package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.Rewarder;
import space.mori.craftz.worlddata.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PoisoningModule extends Module {
    public PoisoningModule(CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        Entity damager = event.getDamager();
        if (!this.isWorld(entity.getWorld())) {
            return;
        }
        if (this.getCraftZ().isEnemy(damager) && entity instanceof Player && !event.isCancelled()
                && event.getDamage() > 0.0 && this.getConfig("config")
                .getBoolean("Config.players.medical.poisoning.enable") && CraftZ.RANDOM.nextDouble() < this.getConfig(
                "config").getDouble("Config.players.medical.poisoning.chance")) {
            this.getData((Player) event.getEntity()).poisoned = true;
            ((Player) event.getEntity()).playSound(
                    event.getEntity().getLocation(), Sound.ENTITY_ZOMBIE_INFECT, 1.0f, 1.0f);
            event.getEntity().sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.poisoned"));
        }
        if (!(damager instanceof Player) || !(entity instanceof Player)) {
            return;
        }
        Player pdamager = (Player) damager;
        ItemStack hand = pdamager.getInventory().getItemInMainHand();
        Player player = (Player) event.getEntity();
        if (hand.getType() != Material.INK_SAC || hand.getDurability() != 10 || !this.getConfig("config")
                .getBoolean("Config.players.medical.poisoning.cure-with-limegreen")) {
            return;
        }
        event.setCancelled(true);
        player.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        pdamager.playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
        if (hand.getAmount() < 2) {
            pdamager.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
        }
        this.getData(player).poisoned = false;
        player.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.unpoisoned"));
        Rewarder.RewardType.HEAL_PLAYER.reward(pdamager);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        Player p = event.getPlayer();
        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
                && event.getMaterial() == Material.INK_SAC && event.getItem().getDurability() == 10 && this.getConfig(
                "config").getBoolean("Config.players.medical.poisoning.cure-with-limegreen")) {
            this.reduceInHand(p);
            this.getData(p).poisoned = false;
            p.playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CURE, 1.0f, 1.0f);
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.unpoisoned"));
        }
    }

    @Override
    public void onPlayerTick(Player p, long tick) {
        PlayerData data = this.getData(p);
        int ticks = this.getConfig("config").getInt("Config.players.medical.poisoning.damage-interval");
        if (this.isSurvival(p) && tick % ticks == 0L && data.poisoned) {
            p.damage(1.0);
            p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10, 1));
            p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 30, 1));
        }
    }
}
