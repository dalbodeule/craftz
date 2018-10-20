package space.mori.craftz.modules;

import com.google.common.collect.Lists;
import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;

import java.util.List;

public class RescueModule extends Module {
    private List<Player> players = Lists.newArrayList();

    public RescueModule(CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if(players.contains(event.getPlayer())) {
            players.remove(event.getPlayer());
            event.getPlayer().sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.rescue-canceled"));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        FileConfiguration config = this.getConfig("config");
        Player p = event.getPlayer();
        Action action = event.getAction();

        PlayerManager playermanager = new PlayerManager(CraftZ.getInstance());
        Location lobby = playermanager.getLobby();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
                && event.getMaterial() == Material.MINECART
                && config.getBoolean("Config.players.rescue.enable")) {
            if (players.contains(p)) {
                p.sendMessage(ChatColor.RED + this.getMsg("Messages.rescue-already-started"));
            } else {
                players.add(p);
                p.sendMessage(ChatColor.GOLD + this.getMsg("Messages.rescue-start").replace("%s",
                        String.valueOf(config.getInt("Config.players.rescue.countdown"))));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 2);

                // Delay
                Bukkit.getScheduler().runTaskLater(this.getCraftZ(), () -> {
                    if(players.contains(p)) {
                        // teleport and message
                        p.sendMessage(ChatColor.GREEN + this.getMsg("Messages.rescue-success"));
                        p.teleport(lobby);

                        // teleport after jobs
                        event.getItem().setAmount(event.getItem().getAmount() - 1);
                        playermanager.resetPlayer(p);
                        players.remove(p);
                    }
                }, config.getInt("Config.players.rescue.countdown") * 20);
            }
        }
    }
}
