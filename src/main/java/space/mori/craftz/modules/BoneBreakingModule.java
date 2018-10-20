package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.worlddata.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BoneBreakingModule extends Module {
    public BoneBreakingModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        EntityType type = event.getEntityType();
        if (!this.isWorld(entity.getWorld()) || event.isCancelled() || type != EntityType.PLAYER) {
            return;
        }
        Player p = (Player) entity;
        double height = event.getDamage() + 3.0;
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && this.getConfig("config")
                .getBoolean("Config.players.medical.bonebreak.enable") && height >= this.getConfig("config")
                .getInt("Config.players.medical.bonebreak.height")) {
            this.getData(p).bonesBroken = true;
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.bones-broken"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerToggleSprint(PlayerToggleSprintEvent event) {
        Player p = event.getPlayer();
        if (this.isWorld(p.getWorld()) && event.isSprinting() && this.getCraftZ().getPlayerManager().existsInWorld(p)
                && this.getData(p).bonesBroken) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        Player p = event.getPlayer();
        Action action = event.getAction();
        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                && event.getMaterial() == Material.BLAZE_ROD && this.getConfig("config")
                .getBoolean("Config.players.medical.bonebreak.heal-with-blazerod")) {
            this.reduceInHand(p);
            this.getData(p).bonesBroken = false;
            p.removePotionEffect(PotionEffectType.SLOW);
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.bones-healed"));
        }
    }

    @Override
    public void onPlayerTick(Player p, long tick) {
        PlayerData data = this.getData(p);
        if (this.isSurvival(p) && tick % 10L == 0L && data.bonesBroken) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
        }
    }
}
