package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.modules.Kit;
import space.mori.craftz.modules.Kits;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public final class KitsAdminCommand extends CraftZCommand {
    public KitsAdminCommand(final CraftZ craftZ) {
        super(craftZ, "{cmd} <kit> create | edit | permission <perm>/- | setdefault | delete");
    }

    @Override
    public Result execute() {
        if (!this.isPlayer) {
            return Result.MUST_BE_PLAYER;
        }
        if (this.args.length < 2) {
            return Result.WRONG_USAGE;
        }
        if (!this.hasPerm("craftz.kitsadmin")) {
            return Result.NO_PERMISSION;
        }
        Kits kits = this.getCraftZ().getKits();
        String kitname = this.args[0];
        Kit kit = kits.match(kitname);
        String action = this.args[1];
        if (action.equalsIgnoreCase("create")) {
            if (kit != null) {
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-already-exists")
                        .replace("%k", kitname));
            } else {
                kit = new Kit(kits, kitname, false, null, new LinkedHashMap<>());
                kits.addKit(kit);
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-created").replace("%k", kitname));
            }
        } else if (action.equalsIgnoreCase("edit")) {
            if (kit == null) {
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-not-found").replace("%k", kitname));
            } else if (kits.isEditing(this.p)) {
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-already-editing")
                        .replace("%k", kits.getEditingSession(this.p).kit.getName()));
            } else {
                kits.startEditing(this.p, kit);
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-editing").replace("%k", kitname));
            }
        } else if (action.equalsIgnoreCase("permission")) {
            if (this.args.length < 3) {
                return Result.WRONG_USAGE;
            }
            if (kit == null) {
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-not-found").replace("%k", kitname));
            } else {
                final String perm = this.args[2].trim();
                final boolean noperm = perm.isEmpty() || perm.equals("-") || perm.equals("/") || perm.equals(".");
                kit.setPermission(noperm ? null : perm);
                kit.save();
                this.send(ChatColor.AQUA + this.getMsg("Messages.cmd.kitsadmin.kit-edited").replace("%k", kitname));
            }
        } else if (action.equalsIgnoreCase("setdefault")) {
            if (kit == null) {
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-not-found").replace("%k", kitname));
            } else {
                kits.setDefault(kit);
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-edited").replace("%k", kitname));
            }
        } else if (action.equalsIgnoreCase("delete")) {
            if (kit == null) {
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-not-found").replace("%k", kitname));
            } else {
                kits.removeKit(kit);
                this.send(this.getMsg("Messages.cmd.kitsadmin.kit-deleted").replace("%k", kitname));
            }
        }
        return Result.SUCCESS;
    }

    @Override
    public CanExecute canExecute(final CommandSender sender) {
        return CanExecute.on(sender).player().permission("craftz.kitsadmin");
    }

    public List<String> onTabComplete(final CommandSender sender, final Command cmd, final String label,
            final String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length <= 1) {
            addCompletions(options, (args.length < 1) ? "" : args[0], true, Stringifier.KIT,
                    this.getCraftZ().getKits().toCollection()
            );
        } else if (args.length == 2) {
            addCompletions(options, args[1], true, "create", "edit", "permission", "setdefault", "delete");
        }
        return options;
    }
}
