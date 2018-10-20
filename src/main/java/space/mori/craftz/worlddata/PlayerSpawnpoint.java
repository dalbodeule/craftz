package space.mori.craftz.worlddata;

import space.mori.craftz.modules.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class PlayerSpawnpoint extends Spawnpoint {
    private final PlayerManager manager;
    private final String name;

    public PlayerSpawnpoint(@Nonnull PlayerManager manager, @Nonnull ConfigurationSection data) {
        super(manager.world(), data);
        this.manager = manager;
        this.name = data.getString("name");
    }

    public PlayerSpawnpoint(@Nonnull PlayerManager manager, String id, Location loc, String name) {
        super(id, loc);
        this.manager = manager;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void save() {
        this.save("Data.playerspawns");
    }

    @Override
    public void store(@Nonnull ConfigurationSection section) {
        super.store(section);
        section.set("name", this.name);
    }

    public void spawn(@Nonnull Player p) {
        p.teleport(this.getSafeLocation());
        p.sendMessage(ChatColor.YELLOW + this.manager.getMsg("Messages.spawned").replace("%s", this.name));
    }
}
