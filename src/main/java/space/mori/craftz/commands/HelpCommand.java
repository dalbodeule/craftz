package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class HelpCommand extends CraftZCommand {
    public HelpCommand(final CraftZ craftZ) {
        super(craftZ, "");
    }

    @Override
    public Result execute() {
        if (!this.hasPerm("craftz.help")) {
            return Result.NO_PERMISSION;
        }
        this.send("");
        this.send(String.valueOf(ChatColor.GOLD) + ChatColor.BOLD + this.getMsg("Messages.help.title"));
        CraftZCommandManager cmdm = this.getCraftZ().getCommandManager();
        Set<String> cmds = cmdm.getCommands(false);
        this.printCommand("help", this);
        cmds.forEach(label -> this.printCommand(label, cmdm.getCommandExecutor(label)));
        this.send("");
        return Result.SUCCESS;
    }

    private void printCommand(String label, CraftZCommand cmd) {
        Result exec = cmd.canExecute(this.sender).result();
        if (exec == Result.MUST_BE_PLAYER) {
            this.send(ChatColor.GRAY + cmd.getUsage(label));
            this.send("    " + ChatColor.DARK_GRAY + ChatColor.ITALIC + this.getMsg("Messages.help.commands." + label));
        } else if (exec == Result.SUCCESS) {
            this.send(ChatColor.YELLOW + cmd.getUsage(label));
            this.send("    " + ChatColor.GOLD + ChatColor.ITALIC + this.getMsg("Messages.help.commands." + label));
        }
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).permission("craftz.help");
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias,
            final String[] args) {
        return Collections.emptyList();
    }
}
