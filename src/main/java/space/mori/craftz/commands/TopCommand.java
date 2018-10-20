package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.modules.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public final class TopCommand extends CraftZCommand {
    public TopCommand(CraftZ craftZ) {
        super(craftZ, "{cmd}");
    }

    @Override
    public Result execute() {
        if (!this.hasPerm("craftz.top")) {
            return Result.NO_PERMISSION;
        }
        this.send("");

        this.send(ChatColor.GOLD + "==== " + this.getMsg("Messages.cmd.top.minutes-survived") + " ====");
        sendAll(PlayerManager.sortHighscores(this.getCraftZ().getPlayerManager().getHighscores("minutes-survived")));

        this.send(ChatColor.GOLD + "==== " + this.getMsg("Messages.cmd.top.zombies-killed") + " ====");
        sendAll(PlayerManager.sortHighscores(this.getCraftZ().getPlayerManager().getHighscores("zombies-killed")));

        this.send(ChatColor.GOLD + "==== " + this.getMsg("Messages.cmd.top.players-killed") + " ====");
        sendAll(PlayerManager.sortHighscores(this.getCraftZ().getPlayerManager().getHighscores("players-killed")));

        this.send("");
        return Result.SUCCESS;
    }

    private void sendAll(SortedSet<Map.Entry<String, Integer>> scores) {
        int i = 0;
        for (Map.Entry<String, Integer> entry : scores) {
            this.send(String.valueOf(ChatColor.RED) + entry.getValue() + ChatColor.WHITE + " - " + ChatColor.YELLOW
                    + entry.getKey());
            if (++i >= 3) {
                break;
            }
        }
    }

    @Override
    public CanExecute canExecute(CommandSender sender) {
        return CanExecute.on(sender).permission("craftz.top");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
