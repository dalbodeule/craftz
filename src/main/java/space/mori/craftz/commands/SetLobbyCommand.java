package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class SetLobbyCommand extends CraftZCommand {
    public SetLobbyCommand(CraftZ craftZ) {
        super(craftZ, "{cmd} <radius>");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        final Location loc = this.p.getLocation();
        if (!this.hasPerm("craftz.setlobby")) {
            return Result.NO_PERMISSION;
        }
        if (this.args.length < 1) {
            return Result.WRONG_USAGE;
        }
        double radius;
        try {
            radius = Double.parseDouble(this.args[0]);
        } catch (NumberFormatException ex) {
            return Result.WRONG_USAGE;
        }
        this.getCraftZ().getPlayerManager().setLobby(loc, radius);
        this.send(ChatColor.AQUA + this.getMsg("Messages.cmd.setlobby"));
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).player().permission("craftz.setlobby");
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias,
            final String[] args) {
        return Collections.emptyList();
    }
}
