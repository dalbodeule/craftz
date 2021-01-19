package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.BlockChecker;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class SignModule extends Module {
    public SignModule(CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Player p = event.getPlayer();
        if (!this.isWorld(block.getWorld())) {
            return;
        }
        String[] lines = event.getLines();
        String noPerms = ChatColor.RED + this.getMsg("Messages.errors.not-enough-permissions");
        String success = ChatColor.GREEN + this.getMsg("Messages.successfully-created");
        ItemMeta meta = p.getInventory().getItemInMainHand().getItemMeta();

        if (meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.DARK_PURPLE + "Pre-written Sign / ")) {
            List<String> lore = meta.getLore();
            for (int i = 0; i < 4; ++i) {
                lines[i] = lore.get(i);
            }
        }

        if (!lines[0].equalsIgnoreCase("[CraftZ]")) {
            return;
        }
        if (lines[1].equals("")) {
            this.signNotComplete(p, block, "You have to define the sign type.");
        } else if (lines[1].equalsIgnoreCase("zombiespawn")) {
            if (p.hasPermission("craftz.buildZombieSpawn")) {
                if (lines[2].equals("")) {
                    this.signNotComplete(p, block, "Line 3 cannot be empty.");
                } else if (!lines[2].contains(":")) {
                    this.signNotComplete(p, block, "Line 3 must contain 2 values separated by a semicolon.");
                } else {
                    try {
                        final String[] spl = lines[2].split(":");
                        final int maxzIn = Integer.parseInt(spl[0]);
                        final int maxzRadius = Integer.parseInt(spl[1]);
                        final String type = lines[3].trim();
                        if (!type.isEmpty() && !this.getCraftZ().getEnemyDefinitions().contains(type)) {
                            this.signNotComplete(p, block,
                                    "The enemy type (line 4) does not exist. You can leave it empty to use the default type."
                            );
                        } else {
                            this.getCraftZ().getZombieSpawner().addSpawn(loc, maxzIn, maxzRadius, type);
                            p.sendMessage(success);
                        }
                    } catch (NumberFormatException ex) {
                        this.signNotComplete(
                                p, block, "One or both of the two values in line 3 are no valid integers.");
                    }
                }
            } else {
                p.sendMessage(noPerms);
            }
        } else if (lines[1].equalsIgnoreCase("playerspawn")) {
            if (p.hasPermission("craftz.buildPlayerSpawn")) {
                if (lines[2].equals("")) {
                    this.signNotComplete(p, block, "Line 3 cannot be empty: you have to give the spawn point a name.");
                } else {
                    this.getCraftZ().getPlayerManager().addSpawn(loc, lines[2]);
                    p.sendMessage(success);
                }
            } else {
                p.sendMessage(noPerms);
            }
        } else if (lines[1].equalsIgnoreCase("lootchest")) {
            if (!p.hasPermission("craftz.buildLootChest")) {
                p.sendMessage(noPerms);
                return;
            }
            if (lines[2].equals("")) {
                this.signNotComplete(p, block,
                        "Line 3 cannot be empty: please put the y-coordinate of the lootchest there (or use %c%)."
                );
                return;
            }
            int chestY;
            String[] l3spl = lines[2].split(":");
            String l3y = l3spl[0];
            if (l3y.equals("%c%")) {
                Block b = BlockChecker.getFirst(Material.CHEST, loc.getWorld(), loc.getBlockX(), loc.getBlockZ())
                        .orElse(null);
                if (b == null) {
                    this.signNotComplete(p, block, "No chest was found.");
                    return;
                }
                chestY = b.getY();
                lines[2] = lines[2].replace("%c%", String.valueOf(chestY));
            } else {
                try {
                    chestY = Integer.parseInt(l3y);
                } catch (NumberFormatException ex2) {
                    this.signNotComplete(p, block, "Line 3 contains neither a correct y coordinate nor %c%");
                    return;
                }
            }
            String l3f = (l3spl.length > 1) ? l3spl[1].toLowerCase() : "n";
            if (!l3f.equals("n") && !l3f.equals("s") && !l3f.equals("e") && !l3f.equals("w")) {
                p.sendMessage(ChatColor.RED + this.getMsg("Messages.errors.sign-facing-wrong"));
                block.breakNaturally();
            } else {
                String lootList = lines[3];
                if (!this.getCraftZ().getChestRefiller().getLists().contains(lootList)) {
                    this.signNotComplete(p, block, "The loot list '" + lootList + "' is not defined.");
                } else {
                    final Location cloc = loc.clone();
                    cloc.setY((double) chestY);
                    this.getCraftZ().getChestRefiller().addChest(ChestRefiller.makeID(loc), lootList, cloc, l3f);
                    p.sendMessage(success);
                }
            }
        }
    }

    private void signNotComplete(final Player p, final Block block, final String extendedMsg) {
        p.sendMessage(ChatColor.RED + this.getMsg("Messages.errors.sign-not-complete"));
        if (this.getConfig("config").getBoolean("Config.chat.extended-error-messages")) {
            p.sendMessage(ChatColor.RED + extendedMsg);
        }
        block.breakNaturally();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        final Player p = event.getPlayer();
        final Block block = event.getBlock();
        if (block.getType() != Material.LEGACY_SIGN_POST && block.getType() != Material.LEGACY_WALL_SIGN) {
            return;
        }
        final Sign sign = (Sign) block.getState();
        final String[] lines = sign.getLines();
        if (!lines[0].equalsIgnoreCase("[CraftZ]")) {
            return;
        }
        final Location signLoc = sign.getLocation();
        if (lines[1].equalsIgnoreCase("zombiespawn")) {
            if (p.hasPermission("craftz.buildZombieSpawn")) {
                this.getCraftZ().getZombieSpawner().removeSpawn(ZombieSpawner.makeID(signLoc));
                p.sendMessage(ChatColor.RED + this.getMsg("Messages.destroyed-sign"));
            } else {
                event.setCancelled(true);
                event.getPlayer()
                        .sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.errors.not-enough-permissions"));
            }
        } else if (lines[1].equalsIgnoreCase("playerspawn")) {
            if (event.getPlayer().hasPermission("craftz.buildPlayerSpawn")) {
                this.getCraftZ().getPlayerManager().removeSpawn(PlayerManager.makeSpawnID(signLoc));
                event.getPlayer().sendMessage(ChatColor.RED + this.getMsg("Messages.destroyed-sign"));
            } else {
                event.setCancelled(true);
                event.getPlayer()
                        .sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.errors.not-enough-permissions"));
            }
        } else if (lines[1].equalsIgnoreCase("lootchest")) {
            if (event.getPlayer().hasPermission("craftz.buildLootChest")) {
                this.getCraftZ().getChestRefiller().removeChest(ChestRefiller.makeID(signLoc));
                event.getPlayer().sendMessage(ChatColor.RED + this.getMsg("Messages.destroyed-sign"));
            } else {
                event.setCancelled(true);
                event.getPlayer()
                        .sendMessage(ChatColor.DARK_RED + this.getMsg("Messages.errors.not-enough-permissions"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (!this.isWorld(event.getPlayer().getWorld())) {
            return;
        }
        final Player p = event.getPlayer();
        final ItemStack item = event.getItem();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || item == null || event.getMaterial() != Material.LEGACY_SIGN
                || !item.hasItemMeta()) {
            return;
        }
        final ItemMeta meta = item.getItemMeta();
        if (meta.hasDisplayName() && meta.getDisplayName().startsWith(ChatColor.DARK_PURPLE + "Pre-written Sign / ")
                && item.getAmount() == 1 && p.getGameMode() != GameMode.CREATIVE) {
            item.setAmount(item.getAmount() + 1);
        }
    }
}
