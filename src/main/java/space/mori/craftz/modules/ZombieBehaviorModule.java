package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.EntityChecker;
import space.mori.craftz.util.Rewarder;
import space.mori.craftz.worlddata.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.*;
import org.bukkit.util.Vector;

import java.util.List;

public class ZombieBehaviorModule extends Module {
    public ZombieBehaviorModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityTargetLivingEntity(final EntityTargetLivingEntityEvent event) {
        if (!this.isWorld(event.getEntity().getWorld())) {
            return;
        }
        Entity ent = event.getEntity();
        if (!this.getCraftZ().isEnemy(ent) || !(event.getTarget() instanceof Player)) {
            return;
        }
        Player p = (Player) event.getTarget();
        if (!this.getCraftZ().getPlayerManager().isPlaying(p) || !ent.getWorld()
                .getName()
                .equals(p.getWorld().getName())) {
            return;
        }
        float vis = this.getCraftZ().getVisibilityBar().getVisibility(p);
        double blocks = 50.0f * vis;
        double dist = ent.getLocation().distance(p.getLocation());
        if (dist > blocks) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityCombust(final EntityCombustEvent event) {
        Entity entity = event.getEntity();
        if (this.isWorld(entity.getWorld()) && !(event instanceof EntityCombustByBlockEvent)
                && !(event instanceof EntityCombustByEntityEvent) && this.getCraftZ().isEnemy(entity)
                && !this.getConfig("config").getBoolean("Config.mobs.zombies.burn-in-sunlight")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(final EntityDeathEvent event) {
        if (!this.isWorld(event.getEntity().getWorld())) {
            return;
        }
        LivingEntity entity = event.getEntity();
        if (!this.getCraftZ().isEnemy(entity)) {
            return;
        }
        Player killer = entity.getKiller();
        if (killer == null || this.getCraftZ().getPlayerManager().isInsideOfLobby(killer)) {
            return;
        }
        PlayerData data = this.getData(killer);
        ++data.zombiesKilled;
        if (this.getConfig("config").getBoolean("Config.players.send-kill-stat-messages")) {
            killer.sendMessage(ChatColor.GOLD + this.getMsg("Messages.killed.zombie")
                    .replaceAll("%k", String.valueOf(this.getData(killer).zombiesKilled)));
        }
        Rewarder.RewardType.KILL_ZOMBIE.reward(killer);
    }

    @Override
    public void onPlayerTick(final Player p, final long tick) {
        if (this.isSurvival(p) && tick % 20L == 0L && this.getConfig("config")
                .getBoolean("Config.mobs.zombies.pull-players-down") && Math.random() < 0.15) {
            Vector plocv = p.getLocation().toVector();
            List<Entity> entities = EntityChecker.getNearbyEntities(p, 2.5);
            for (Entity ent : entities) {
                if (this.getCraftZ().isEnemy(ent)) {
                    Location zloc = ent.getLocation();
                    if (zloc.getY() + 1.0 >= plocv.getY()) {
                        continue;
                    }
                    p.setVelocity(zloc.toVector().subtract(plocv).normalize().multiply(0.5 + Math.random() * 0.4));
                }
            }
        }
    }
}
