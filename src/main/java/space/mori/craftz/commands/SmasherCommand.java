package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.util.ItemRenamer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public final class SmasherCommand extends CraftZCommand {
    public SmasherCommand(final CraftZ craftZ) {
        super(craftZ, "{cmd}");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        if (!this.hasPerm("craftz.smasher")) {
            return Result.NO_PERMISSION;
        }
        this.p.getInventory()
                .addItem(
                        ItemRenamer.on(new ItemStack(Material.STICK)).setName(ChatColor.GOLD + "Zombie Smasher").get());
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).player().permission("craftz.smasher");
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias,
            final String[] args) {
        return Collections.emptyList();
    }
}
