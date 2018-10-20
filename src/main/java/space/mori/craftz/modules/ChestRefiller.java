package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.StackParser;
import space.mori.craftz.worlddata.LootChest;
import space.mori.craftz.worlddata.WorldData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class ChestRefiller extends Module {
    private List<LootChest> chests = new ArrayList<>();

    public ChestRefiller(CraftZ craftZ) {
        super(craftZ);
    }

    @Nonnull
    public static String makeID(@Nonnull Location signLoc) {
        return "x" + signLoc.getBlockX() + "y" + signLoc.getBlockY() + "z" + signLoc.getBlockZ();
    }

    @Nonnull
    public static Optional<Location> findSign(@Nonnull Location chestLoc) {
        Location loc = chestLoc.clone();
        int y = loc.getBlockY();
        for (int i = 0; i < 256; ++i) {
            loc.setY(i);
            Block b = loc.getBlock();
            if (!(b.getState() instanceof Sign)) {
                continue;
            }
            Sign sign = (Sign) b.getState();
            String line3 = sign.getLine(2);
            String[] l3spl = line3.split(":");
            if (l3spl[0].equals(String.valueOf(y))) {
                return Optional.of(loc);
            }
        }
        return Optional.empty();
    }

    @Override
    public void onLoad(boolean configReload) {
        this.chests.clear();
        ConfigurationSection sec = WorldData.get().getConfigurationSection("Data.lootchests");
        if (sec == null) {
            return;
        }
        for (String signID : sec.getKeys(false)) {
            ConfigurationSection data = sec.getConfigurationSection(signID);
            if (data == null) {
                continue;
            }
            LootChest lootChest = new LootChest(this, data);
            this.chests.add(lootChest);
            if (this.getPropertyBoolean("despawn-on-startup", lootChest.getList())) {
                lootChest.startRefill(false);
            } else {
                lootChest.refill(false);
            }
        }
    }

    public int getChestCount() {
        return this.chests.size();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockBreak(final BlockBreakEvent event) {
        if (this.isWorld(event.getPlayer().getWorld()) && event.getBlock().getType() == Material.CHEST) {
            refill(event.getBlock().getState().getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(final InventoryCloseEvent event) {
        HumanEntity p = event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        if (this.isWorld(p.getWorld()) && holder instanceof Chest) {
            refill(((Chest) holder).getLocation());
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (this.isWorld(event.getPlayer().getWorld()) && event.getAction() == Action.LEFT_CLICK_BLOCK
                && event.getClickedBlock().getType() == Material.CHEST && this.getConfig("config")
                .getBoolean("Config.players.drop-lootchests-on-punch")) {
            refill(event.getClickedBlock().getLocation());
        }
    }

    private void refill(@Nonnull Location location) {
        findSign(location).ifPresent(
                signLoc -> this.getLootChest(signLoc).ifPresent(lootChest -> lootChest.startRefill(true)));
    }

    public Optional<LootChest> getLootChest(String signID) {
        for (final LootChest chest : this.chests) {
            if (chest.getID().equals(signID)) {
                return Optional.of(chest);
            }
        }
        return Optional.empty();
    }

    public Optional<LootChest> getLootChest(Location signLoc) {
        return this.getLootChest(makeID(signLoc));
    }

    public void addChest(String signID, String list, Location loc, String face) {
        LootChest lootChest = new LootChest(this, signID, list, loc, face);
        lootChest.save();
        this.chests.add(lootChest);
        lootChest.startRefill(false);
        Dynmap dynmap = this.getCraftZ().getDynmap();
        dynmap.createMarker(dynmap.SET_LOOT, "loot_" + signID, "Loot: " + list, loc, dynmap.ICON_LOOT);
    }

    public void removeChest(String signID) {
        WorldData.get().set("Data.lootchests." + signID, null);
        WorldData.save();
        this.getLootChest(signID).ifPresent(chest -> this.chests.remove(chest));
        Dynmap dynmap = this.getCraftZ().getDynmap();
        dynmap.removeMarker(dynmap.getMarker(dynmap.SET_LOOT, "loot_" + signID));
    }

    @Nonnull
    public Set<String> getLists() {
        ConfigurationSection sec = this.getConfig("loot").getConfigurationSection("Loot.lists");
        return sec == null ? Collections.emptySet() : sec.getKeys(false);
    }

    public int getPropertyInt(String name, String list) {
        FileConfiguration c = this.getConfig("loot");
        String ls = "Loot.lists-settings." + list + "." + name;
        return list != null && c.contains(ls) ? c.getInt(ls) : c.getInt("Loot.settings." + name);
    }

    public boolean getPropertyBoolean(String name, String list) {
        FileConfiguration c = this.getConfig("loot");
        String ls = "Loot.lists-settings." + list + "." + name;
        return list != null && c.contains(ls) ? c.getBoolean(ls) : c.getBoolean("Loot.settings." + name);
    }

    @Override
    public void onServerTick(final long tick) {
        this.chests.forEach(LootChest::onServerTick);
    }

    @Override
    public void onDynmapEnabled(Dynmap dynmap) {
        dynmap.clearSet(dynmap.SET_LOOT);
        if (!this.getConfig("config").getBoolean("Config.dynmap.show-lootchests")) {
            return;
        }
        for (LootChest chest : this.chests) {
            String id = "loot_" + chest.getID();
            String label = "Loot: " + chest.getList();
            Object icon = dynmap.createUserIcon(
                    "loot_" + chest.getList(), label, "loot_" + chest.getList(), dynmap.ICON_LOOT);
            Object m = dynmap.createMarker(dynmap.SET_LOOT, id, label, chest.getLocation(), icon);
            Location loc = chest.getLocation();
            List<String> items = chest.getLootDefinitions(false);
            StringBuilder s = new StringBuilder("<center>").append("<b>X</b>: ")
                    .append(loc.getBlockX())
                    .append(" &nbsp; <b>Y</b>: ")
                    .append(loc.getBlockY())
                    .append(" &nbsp; <b>Z</b>: ")
                    .append(loc.getBlockZ())
                    .append("</center><hr />");
            items.stream()
                    .flatMap(item -> StackParser.fromString(item, false).map(Stream::of).orElse(Stream.empty()))
                    .map(ItemStack::getType)
                    .filter(stack -> stack != Material.AIR)
                    .map(Dynmap::getItemImage)
                    .forEach(s::append);
            dynmap.setMarkerDescription(m, s.toString());
        }
    }
}
