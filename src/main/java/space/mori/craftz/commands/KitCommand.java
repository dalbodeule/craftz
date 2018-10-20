package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.modules.Kit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class KitCommand extends CraftZCommand {
    public KitCommand(final CraftZ craftZ) {
        super(craftZ, "{cmd} <kit>");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        if (this.args.length < 1) {
            return Result.WRONG_USAGE;
        }
        if (!this.getCraftZ().getPlayerManager().isInsideOfLobby(this.p)) {
            this.send(ChatColor.RED + this.getMsg("Messages.errors.not-in-lobby"));
        } else {
            final String kitname = this.args[0];
            final Kit kit = this.getCraftZ().getKits().match(kitname);
            if (kit != null && kit.canUse(this.p)) {
                kit.select(this.p);
            }
        }
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).player();
    }

    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label,
            final String[] args) {
        final List<String> options = new ArrayList<>();
        if (!(sender instanceof Player)) {
            return options;
        }
        CraftZCommand.addCompletions(options, (args.length < 1) ? "" : args[0], true, Stringifier.KIT,
                this.getCraftZ().getKits().getAvailableKits((Player) sender)
        );
        return options;
    }
}
