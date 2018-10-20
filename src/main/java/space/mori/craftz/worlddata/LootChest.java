package space.mori.craftz.worlddata;

import space.mori.craftz.CraftZ;
import space.mori.craftz.modules.ChestRefiller;
import space.mori.craftz.util.EntityChecker;
import space.mori.craftz.util.ItemRenamer;
import space.mori.craftz.util.StackParser;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LootChest extends WorldDataObject {
    private final ChestRefiller refiller;
    private final String list;
    private final Location loc;
    private final String face;
    private int refillCountdown;
    private int despawnCountdown;

    public LootChest(ChestRefiller refiller, @Nonnull ConfigurationSection data) {
        this(refiller, data.getName(), data.getString("list"),
                new Location(refiller.world(), data.getInt("coords.x"), data.getInt("coords.y"),
                        data.getInt("coords.z")
                ), data.getString("face")
        );
    }

    public LootChest(ChestRefiller refiller, String id, String list, Location loc, @Nullable String face) {
        super(id);
        this.refiller = refiller;
        this.list = list;
        this.loc = loc;
        this.face = face == null ? "n" : face.toLowerCase();
    }

    public String getList() {
        return this.list;
    }

    @Nonnull
    public Location getLocation() {
        return this.loc.clone();
    }

    public String getFace() {
        return this.face;
    }

    @Nonnull
    public BlockFace getBlockFace() {
        switch (this.face) {
            case "s":
                return BlockFace.SOUTH;
            case "e":
                return BlockFace.EAST;
            case "w":
                return BlockFace.WEST;
            default:
                return BlockFace.NORTH;
        }
    }

    public void save() {
        this.save("Data.lootchests");
    }

    @Override
    public void store(@Nonnull ConfigurationSection section) {
        section.set("coords.x", this.loc.getBlockX());
        section.set("coords.y", this.loc.getBlockY());
        section.set("coords.z", this.loc.getBlockZ());
        section.set("face", this.face);
        section.set("list", this.list);
    }

    @Nonnull
    public List<String> getLootDefinitions(boolean multiplied) {
        List<String> defs = this.refiller.getConfig("loot").getStringList("Loot.lists." + this.list);
        if (!multiplied || defs == null) {
            return Collections.emptyList();
        }
        List<String> ndefs = new ArrayList<>();
        for (String str : defs) {
            int count = 1;
            String itm = str;
            Pattern pattern = Pattern.compile("^(\\d)x");
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                count = Integer.parseInt(matcher.group(1));
                itm = str.substring(matcher.end());
            }
            for (int a = 0; a < count; ++a) {
                ndefs.add(itm);
            }
        }
        return ndefs;
    }

    public void refill(boolean placeChest) {
        Block block = this.loc.getBlock();
        double mpv = this.refiller.getPropertyInt("max-player-vicinity", this.list);
        if ((mpv > 0.0 && EntityChecker.areEntitiesNearby(this.loc, mpv, EntityType.PLAYER, 1)) || (!placeChest
                && block.getType() != Material.CHEST)) {
            this.startRefill(false);
            return;
        }
        block.setType(Material.CHEST);
        Chest chest = (Chest) block.getState();
        Inventory inv = chest.getInventory();
        BlockFace face = this.getBlockFace();
        ((org.bukkit.material.Chest) chest.getData()).setFacingDirection(face);
        for (ItemStack stack : inv) {
            if (stack != null && stack.getType() != Material.AIR) {
                if (this.refiller.getPropertyBoolean("despawn", this.list)) {
                    this.despawnCountdown = this.refiller.getPropertyInt("time-before-despawn", this.list) * 20;
                }
                return;
            }
        }
        List<String> defs = this.getLootDefinitions(true);
        if (!defs.isEmpty()) {
            int min = this.refiller.getPropertyInt("min-stacks-filled", this.list);
            int max = this.refiller.getPropertyInt("max-stacks-filled", this.list);
            for (int i = 0, n = min + (max > min ? CraftZ.RANDOM.nextInt(max - min) : 0); i < n; ++i) {
                StackParser.fromString(defs.get(CraftZ.RANDOM.nextInt(defs.size())), false)
                        .ifPresent(stack2 -> chest.getInventory().addItem(stack2));
            }
            ItemRenamer.on(chest).setSpecificNames(ItemRenamer.DEFAULT_MAP);
        }
        if (this.refiller.getPropertyBoolean("despawn", this.list)) {
            this.despawnCountdown = this.refiller.getPropertyInt("time-before-despawn", this.list) * 20;
        }
    }

    public void startRefill(boolean drop) {
        this.refillCountdown = this.refiller.getPropertyInt("time-before-refill", this.list) * 20;
        Block block = this.loc.getBlock();
        try {
            BlockState bs = block.getState();
            if (bs instanceof Chest && !drop) {
                ((Chest) bs).getInventory().clear();
            }
        } catch (NullPointerException ex) {}
        block.setType(Material.AIR);
    }

    public void onServerTick() {
        if (this.despawnCountdown > 0) {
            if (--this.despawnCountdown <= 0) {
                this.startRefill(this.refiller.getPropertyBoolean("drop-on-despawn", this.list));
            }
        } else if (this.refillCountdown > 0 && --this.refillCountdown <= 0) {
            this.refill(true);
        }
    }
}
