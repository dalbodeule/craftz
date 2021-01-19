package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.util.ItemRenamer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SignCommand extends CraftZCommand {
    public SignCommand(final CraftZ craftZ) {
        super(craftZ, "{cmd} <line2> <line3> <line4>");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        if (!this.hasPerm("craftz.sign")) {
            return Result.NO_PERMISSION;
        }
        if (this.args.length > 0) {
            String line2 = this.args[0];
            String line3 = this.args.length > 1 ? this.args[1] : "";
            String line4 = this.args.length > 2 ? this.args[2] : "";
            String desc = "Unknown";
            if (line2.equalsIgnoreCase("lootchest")) {
                desc = "Loot '" + line4 + "'";
            } else if (line2.equalsIgnoreCase("playerspawn")) {
                desc = "Player Spawn '" + line3 + "'";
            } else if (line2.equalsIgnoreCase("zombiespawn")) {
                desc = "Zombie Spawn" + (line4.equals("") ? "" : (" '" + line4 + "'"));
            }
            this.p.getInventory()
                    .addItem(ItemRenamer.on(new ItemStack(Material.OAK_SIGN))
                            .setName(ChatColor.DARK_PURPLE + "Pre-written Sign / " + desc)
                            .setLore("[CraftZ]", line2, line3, line4)
                            .get());
            return Result.SUCCESS;
        }
        return Result.WRONG_USAGE;
    }

    @Override
    public CanExecute canExecute(CommandSender sender) {
        return CanExecute.on(sender).player().permission("craftz.sign");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            CraftZCommand.addCompletions(
                    options, args.length == 0 ? "" : args[0], true, "lootchest", "playerspawn", "zombiespawn");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("lootchest")) {
            CraftZCommand.addCompletions(options, args[2], true, this.getCraftZ().getChestRefiller().getLists());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("zombiespawn")) {
            CraftZCommand.addCompletions(options, args[2], true, this.getCraftZ().getEnemyDefinitions());
        }
        return options;
    }
}
