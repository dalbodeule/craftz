package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.modules.Kit;
import space.mori.craftz.worlddata.PlayerSpawnpoint;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public abstract class CraftZCommand extends Module implements CommandExecutor, TabCompleter {
    protected final String usage;
    protected CommandSender sender;
    protected boolean isPlayer;
    protected Player p;
    protected String[] args;

    public CraftZCommand(CraftZ craftZ, final String usage) {
        super(craftZ);
        this.usage = usage;
    }

    protected static <T> List<String> addCompletions(List<String> list, String arg, boolean ignoreCase,
            Stringifier<T> stringifier, Stream<T> possible) {
        String argProcessed = ignoreCase ? arg.toLowerCase() : arg;
        possible.map(stringifier::stringify)
                .map(s -> ignoreCase ? s.toLowerCase() : s)
                .filter(s -> s.startsWith(argProcessed))
                .forEach(list::add);
        return list;
    }


    @SafeVarargs
    protected static <T> List<String> addCompletions(List<String> list, String arg, boolean ignoreCase,
            Stringifier<T> stringifier, T... possible) {
        return addCompletions(list, arg, ignoreCase, stringifier, Arrays.stream(possible));
    }

    protected static <T> List<String> addCompletions(List<String> list, String arg, boolean ignoreCase,
            Stringifier<T> stringifier, Collection<T> possible) {
        return addCompletions(list, arg, ignoreCase, stringifier, possible.stream());
    }

    protected static List<String> addCompletions(List<String> list, String arg, boolean ignoreCase,
            String... possible) {
        return addCompletions(list, arg, ignoreCase, Stringifier.STRING, Arrays.stream(possible));
    }

    protected static List<String> addCompletions(List<String> list, String arg, boolean ignoreCase,
            Collection<String> possible) {
        return addCompletions(list, arg, ignoreCase, Stringifier.STRING, possible.stream());
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.sender = sender;
        this.isPlayer = sender instanceof Player;
        this.p = this.isPlayer ? (Player) sender : null;
        this.args = args;
        Result result = this.execute();
        switch (result) {
            case NO_PERMISSION: {
                this.send(ChatColor.RED + this.getMsg("Messages.errors.not-enough-permissions"));
                break;
            }
            case MUST_BE_PLAYER: {
                this.send(ChatColor.RED + this.getMsg("Messages.errors.must-be-player"));
                break;
            }
            case WRONG_USAGE: {
                this.send(ChatColor.RED + this.getMsg("Messages.errors.wrong-usage"));
                this.send(String.valueOf(ChatColor.RED) + ChatColor.ITALIC + this.getUsage(label));
                break;
            }
        }
        return true;
    }

    public abstract Result execute();

    protected boolean hasPerm(String perm) {
        return this.sender.hasPermission(perm);
    }

    protected void send(Object msg) {
        this.send(msg, this.sender);
    }

    protected void send(Object msg, CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.valueOf(msg)));
    }

    public final String getUsage(String label) {
        return "/craftz " + this.usage.replace("{cmd}", label);
    }

    public CanExecute canExecute(CommandSender sender) {
        return CanExecute.on(sender);
    }

    public enum Result {
        SUCCESS,
        NO_PERMISSION,
        MUST_BE_PLAYER,
        WRONG_USAGE
    }

    protected interface Stringifier<T> {
        Stringifier<String> STRING = t -> t;
        Stringifier<Kit> KIT = Kit::getName;
        Stringifier<PlayerSpawnpoint> PLAYERSPAWN = PlayerSpawnpoint::getName;

        String stringify(final T p0);
    }

    public static final class CanExecute {
        private CommandSender sender;
        private boolean player;
        private Set<String> permissions = new HashSet<>();

        private CanExecute(final CommandSender sender) {
            this.sender = sender;
        }

        @Nonnull
        public static CanExecute on(final CommandSender sender) {
            return new CanExecute(sender);
        }

        public CanExecute player() {
            this.player = true;
            return this;
        }

        public CanExecute permission(String... possiblePerms) {
            this.permissions.addAll(Arrays.asList(possiblePerms));
            return this;
        }

        public Result result() {
            if (this.player && !(this.sender instanceof Player)) {
                return Result.MUST_BE_PLAYER;
            }
            boolean ok = this.permissions.stream().allMatch(perm -> this.sender.hasPermission(perm));
            if (ok) {
                return Result.SUCCESS;
            } else {
                return Result.NO_PERMISSION;
            }
        }
    }
}
