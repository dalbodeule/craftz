package space.mori.craftz.util;

import space.mori.craftz.modules.Kit;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class KitEditingSession {
    public final UUID playerID;
    public final Kit kit;
    public ItemStack[] invContents;
    public ItemStack[] armorContents;
    public GameMode gameMode;

    public KitEditingSession(Player p, Kit kit) {
        this(p.getUniqueId(), kit, p.getInventory().getContents(), p.getInventory().getArmorContents(),
                p.getGameMode()
        );
    }

    public KitEditingSession(UUID playerID, Kit kit, ItemStack[] invContents, ItemStack[] armorContents,
            GameMode gameMode) {
        this.playerID = playerID;
        this.kit = kit;
        this.invContents = invContents;
        this.armorContents = armorContents;
        this.gameMode = gameMode;
    }

    public static KitEditingSession start(Player p, Kit kit) {
        KitEditingSession session = new KitEditingSession(p, kit);
        p.setGameMode(GameMode.CREATIVE);
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setArmorContents(new ItemStack[4]);
        kit.give(p, false);
        return session;
    }

    public boolean stop(boolean save) {
        Player p = Bukkit.getPlayer(this.playerID);
        if (p == null) {
            return false;
        }
        PlayerInventory inv = p.getInventory();
        if (save) {
            this.kit.setItems(inv);
            this.kit.save();
        }
        inv.setContents(this.invContents);
        inv.setArmorContents(this.armorContents);
        p.setGameMode(this.gameMode);
        return true;
    }
}
