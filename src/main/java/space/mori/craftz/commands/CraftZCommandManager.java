package space.mori.craftz.commands;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.*;

public final class CraftZCommandManager extends Module implements CommandExecutor, TabCompleter {
    private Map<String, CraftZCommand> commands = new LinkedHashMap<>();
    private CraftZCommand def;

    public CraftZCommandManager(CraftZ craftZ) {
        super(craftZ);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length > 0 && this.hasCommand(args[0])) {
            return this.getCommandExecutor(args[0])
                    .onCommand(sender, cmd, args[0].toLowerCase(), Arrays.copyOfRange(args, 1, args.length));
        }
        return this.def == null || this.def.onCommand(sender, cmd, label, args);
    }

    public void registerCommand(CraftZCommand commandExecutor, String cmd, String... aliases) {
        boolean cv = this.commands.containsValue(commandExecutor) || this.def == commandExecutor;
        this.commands.put(cmd.toLowerCase(), commandExecutor);
        for (String alias : aliases) {
            this.commands.put(alias.toLowerCase(), commandExecutor);
        }
        if (!cv) {
            this.getCraftZ().addModule(commandExecutor);
        }
    }

    public CraftZCommand getDefault() {
        return this.def;
    }

    public void setDefault(CraftZCommand def) {
        this.def = def;
        if (!this.commands.containsValue(def)) {
            this.getCraftZ().addModule(def);
        }
    }

    public Set<String> getCommands(boolean aliases) {
        Set<String> cmds = this.commands.keySet();
        if (aliases) {
            return cmds;
        }
        List<CraftZCommand> mappings = new ArrayList<>();
        Iterator<String> it = cmds.iterator();
        while (it.hasNext()) {
            CraftZCommand mapping = this.commands.get(it.next());
            if (!mappings.contains(mapping)) {
                mappings.add(mapping);
            } else {
                it.remove();
            }
        }
        return cmds;
    }

    public CraftZCommand getCommandExecutor(String command) {
        return this.commands.get(command.toLowerCase());
    }

    public boolean hasCommand(String command) {
        return this.commands.containsKey(command.toLowerCase());
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            String arg = args.length == 0 ? "" : args[0];
            List<String> options = new ArrayList<>();
            Set<String> cmds = this.getCommands(true);
            for (String label : cmds) {
                CraftZCommand cmd = this.getCommandExecutor(label);
                if (label.toLowerCase().startsWith(arg)
                        && cmd.canExecute(sender).result() == CraftZCommand.Result.SUCCESS) {
                    options.add(label);
                }
            }
            return options;
        }
        String label2 = args[0];
        CraftZCommand cmd2 = this.getCommandExecutor(label2);
        if (cmd2 != null && cmd2.canExecute(sender).result() == CraftZCommand.Result.SUCCESS) {
            return cmd2.onTabComplete(sender, command, label2, Arrays.copyOfRange(args, 1, args.length));
        }
        return Collections.emptyList();
    }
}
