package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.worlddata.PlayerSpawnpoint;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class SpawnCommand extends CraftZCommand {
    public SpawnCommand(CraftZ craftZ) {
        super(craftZ, "{cmd} [spawnpoint]");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        if (!this.hasPerm("craftz.spawn")) {
            return Result.NO_PERMISSION;
        }
        if (this.getCraftZ().getPlayerManager().isInsideOfLobby(this.p)) {
            PlayerSpawnpoint spawn = null;
            if (this.args.length > 0) {
                if (!this.p.hasPermission("craftz.spawn.choose")) {
                    return Result.NO_PERMISSION;
                }
                spawn = this.getCraftZ().getPlayerManager().matchSpawn(this.args[0]).orElse(null);
                if (spawn == null) {
                    this.send(ChatColor.RED + this.getMsg("Messages.errors.player-spawn-not-found"));
                    return Result.SUCCESS;
                }
            }
            int respawnCountdown = this.getCraftZ().getPlayerManager().getRespawnCountdown(this.p);
            if (respawnCountdown <= 0) {
                this.getCraftZ().getPlayerManager().loadPlayer(this.p, true, spawn);
            } else {
                this.send(ChatColor.RED + this.getMsg("Messages.errors.respawn-countdown")
                        .replace("%t", String.valueOf(Math.max(respawnCountdown / 1000, 1))));
            }
        } else {
            this.send(ChatColor.RED + this.getMsg("Messages.errors.not-in-lobby"));
        }
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(CommandSender sender) {
        return CanExecute.on(sender).player().permission("craftz.spawn");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (sender.hasPermission("craftz.spawn.choose")) {
            addCompletions(options, (args.length == 0) ? "" : args[0], true, Stringifier.PLAYERSPAWN,
                    this.getCraftZ().getPlayerManager().getSpawns()
            );
        }
        return options;
    }
}
