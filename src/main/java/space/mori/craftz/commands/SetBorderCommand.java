package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.modules.WorldBorderModule;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public final class SetBorderCommand extends CraftZCommand {
    public SetBorderCommand(final CraftZ craftZ) {
        super(craftZ, "{cmd} disable | round|square <radius>");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        Location loc = this.p.getLocation();
        if (!this.hasPerm("craftz.setborder")) {
            return Result.NO_PERMISSION;
        }
        if (this.args.length > 0 && this.args[0].equalsIgnoreCase("disable")) {
            this.getCraftZ().getWorldBorder().setEnabled(false);
            this.send(ChatColor.AQUA + this.getMsg("Messages.cmd.setborder-disable"));
            return Result.SUCCESS;
        }
        if (this.args.length < 2) {
            return Result.WRONG_USAGE;
        }
        String shape = this.args[0].toLowerCase();
        if (!shape.equals("round") && !shape.equals("square")) {
            return Result.WRONG_USAGE;
        }
        double radius;
        try {
            radius = Double.parseDouble(this.args[1]);
        } catch (NumberFormatException ex) {
            return Result.WRONG_USAGE;
        }
        WorldBorderModule worldBorder = this.getCraftZ().getWorldBorder();
        worldBorder.setEnabled(true);
        worldBorder.setShape(shape);
        worldBorder.setRadius(radius);
        worldBorder.setLocation(Math.round(loc.getX() * 100.0) / 100.0, Math.round(loc.getZ() * 100.0) / 100.0);
        this.send(ChatColor.AQUA + this.getMsg("Messages.cmd.setborder"));
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).player().permission("craftz.setborder");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            CraftZCommand.addCompletions(
                    options, (args.length == 0) ? "" : args[0], true, "disable", "round", "square");
        }
        return options;
    }
}
