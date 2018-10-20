package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class ReloadCommand extends CraftZCommand {
    public ReloadCommand(CraftZ craftZ) {
        super(craftZ, "{cmd}");
    }

    @Override
    public Result execute() {
        if (!this.hasPerm("craftz.reload")) {
            return Result.NO_PERMISSION;
        }
        this.getCraftZ().reloadConfigs();
        this.send(ChatColor.GREEN + this.getMsg("Messages.cmd.reloaded"));
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(CommandSender sender) {
        return CanExecute.on(sender).permission("craftz.reload");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
