package space.mori.craftz;


import space.mori.craftz.modules.Dynmap;
import space.mori.craftz.worlddata.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class Module implements Listener {
    private final CraftZ craftZ;

    public Module(CraftZ craftZ) {
        this.craftZ = craftZ;
    }

    public final CraftZ getCraftZ() {
        return craftZ;
    }

    public final FileConfiguration getConfig(String config) {
        return ConfigManager.getConfig(config);
    }

    public final void saveConfig(String config) {
        ConfigManager.saveConfig(config);
    }

    public final String getMsg(String path) {
        return this.craftZ.getMsg(path);
    }

    public final World world() {
        return this.craftZ.world();
    }

    public final boolean isWorld(World world) {
        return this.craftZ.isWorld(world);
    }

    public final boolean isWorld(String worldName) {
        return this.craftZ.isWorld(worldName);
    }

    public PlayerData getData(Player p) {
        return this.craftZ.getPlayerManager().getData(p);
    }

    public PlayerData getData(UUID id) {
        return this.craftZ.getPlayerManager().getData(id);
    }

    protected final void reduceInHand(Player p) {
        if (p.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null) {
            return;
        }
        if (hand.getAmount() == 1) {
            p.getInventory().setItemInMainHand(null);
        } else {
            hand.setAmount(hand.getAmount() - 1);
        }
    }

    protected final boolean isSurvival(Player p) {
        return this.isSurvival(p.getGameMode());
    }

    protected final boolean isSurvival(GameMode gm) {
        return gm == GameMode.SURVIVAL || gm == GameMode.ADVENTURE;
    }

    public void onLoad(boolean configReload) {
    }

    public void onDisable() {
    }

    public void onServerTick(long tick) {
    }

    public void onPlayerTick(Player p, long tick) {
    }

    public void onDynmapEnabled(Dynmap dynmap) {
    }

    public int getNumberAllowed(Inventory inv, ItemStack item) {
        return -1;
    }
}

