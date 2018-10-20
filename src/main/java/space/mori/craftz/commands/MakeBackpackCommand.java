package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.worlddata.Backpack;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class MakeBackpackCommand extends CraftZCommand {
    public MakeBackpackCommand(CraftZ craftZ) {
        super(craftZ, "{cmd} <size> <name (spaces allowed)>");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        if (!this.hasPerm("craftz.makeBackpack")) {
            return Result.NO_PERMISSION;
        }
        if (this.args.length < 2) {
            return Result.WRONG_USAGE;
        }
        try {
            final int size = Integer.parseInt(this.args[0]);
            final String name = StringUtils.join(this.args, ' ', 1, this.args.length);
            if (size % 9 != 0 || size < 9 || size > 54) {
                this.send(ChatColor.RED + this.getMsg("Messages.errors.backpack-size-incorrect"));
                return Result.SUCCESS;
            }
            final ItemStack item = Backpack.createItem(size, name, false);
            this.p.getWorld().dropItem(this.p.getLocation(), item).setPickupDelay(0);
        } catch (NumberFormatException ex) {
            return Result.WRONG_USAGE;
        }
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).permission("craftz.makeBackpack");
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            addCompletions(options, (args.length == 0) ? "" : args[0], true, "9", "18", "27", "36", "45", "54");
        }
        return options;
    }
}
