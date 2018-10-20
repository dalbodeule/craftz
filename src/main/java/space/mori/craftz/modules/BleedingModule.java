package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.worlddata.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class BleedingModule extends Module {
    public BleedingModule(final CraftZ craftZ) {
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
        if (this.getConfig("config").getBoolean("Config.players.medical.bleeding.enable")
                && p.getGameMode() != GameMode.CREATIVE && !event.isCancelled()
                && CraftZ.RANDOM.nextDouble() < this.getConfig("config")
                .getDouble("Config.players.medical.bleeding.chance")) {
            this.getData(p).bleeding = true;
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.bleeding"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        Player p = event.getPlayer();
        Action action = event.getAction();
        if ((action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK)
                || event.getMaterial() != Material.PAPER || !this.getConfig("config")
                .getBoolean("Config.players.medical.bleeding.heal-with-paper")) {
            return;
        }
        this.reduceInHand(p);
        PlayerData data = this.getData(p);
        if (data.bleeding) {
            data.bleeding = false;
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.bandaged"));
        } else {
            p.sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.bandaged-unnecessary"));
        }
        p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_FLAP, 1.0f, 1.0f);
    }

    @Override
    public void onPlayerTick(Player p, long tick) {
        PlayerData data = this.getData(p);
        int ticks = this.getConfig("config").getInt("Config.players.medical.bleeding.damage-interval");
        if (this.isSurvival(p) && tick % ticks == 0L && data.bleeding) {
            p.damage(1.0);
        }
    }
}
