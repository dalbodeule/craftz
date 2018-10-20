package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class RemoveItemsCommand extends CraftZCommand {
    public RemoveItemsCommand(CraftZ craftZ) {
        super(craftZ, "{cmd}");
    }

    @Override
    public Result execute() {
        if (!this.hasPerm("craftz.remitems") && !this.hasPerm("craftz.removeitems")) {
            return Result.NO_PERMISSION;
        }
        AtomicInteger removed = new AtomicInteger(0);
        List<Entity> entities = this.world().getEntities();
        entities.stream().filter(entity -> entity.getType() == EntityType.DROPPED_ITEM).forEach(entity -> {
            entity.remove();
            removed.incrementAndGet();
        });
        this.send(this.getCraftZ().getPrefix() + " " + ChatColor.GREEN + this.getMsg("Messages.cmd.removed-items")
                .replace("%i", String.valueOf(ChatColor.AQUA) + removed.intValue() + ChatColor.GREEN));
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).permission("craftz.remitems", "craftz.removeitems");
    }

    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias,
            final String[] args) {
        return Collections.emptyList();
    }
}
