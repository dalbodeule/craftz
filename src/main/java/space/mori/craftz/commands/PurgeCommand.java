package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public final class PurgeCommand extends CraftZCommand {
    public PurgeCommand(CraftZ craftZ) {
        super(craftZ, "{cmd}");
    }

    @Override
    public Result execute() {
        if (this.hasPerm("craftz.purge")) {
            World w = this.world();
            List<Entity> ents = w.getEntities();
            int n = 0;
            for (Entity ent : ents) {
                if (this.getCraftZ().isEnemy(ent)) {
                    Location loc = ent.getLocation();
                    IntStream.range(0, 10).forEach(ya -> {
                        IntStream.range(0, 9).forEach(i -> w.playEffect(loc, Effect.SMOKE, i));
                        loc.add(0, 0.2, 0);
                    });
                    ent.remove();
                    ++n;
                }
            }
            this.send(this.getCraftZ().getPrefix() + " " + ChatColor.GREEN + this.getMsg("Messages.cmd.purged")
                    .replace("%z", String.valueOf(ChatColor.AQUA) + n + ChatColor.GREEN));
            return Result.SUCCESS;
        }
        return Result.NO_PERMISSION;
    }

    @Override
    public CanExecute canExecute(CommandSender sender) {
        return CanExecute.on(sender).permission("craftz.purge");
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
