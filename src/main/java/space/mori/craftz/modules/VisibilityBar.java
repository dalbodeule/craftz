package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class VisibilityBar extends Module {
    public VisibilityBar(CraftZ craftZ) {
        super(craftZ);
    }

    public void updateVisibility(Player p) {
        if (!this.getConfig("config").getBoolean("Config.players.enable-visibility-bar")) {
            return;
        }
        float visibility = 0.32f;
        final boolean mov = this.getCraftZ().getPlayerManager().isMoving(p);
        if (!mov) {
            visibility -= 0.25f;
        }
        if (p.isSneaking()) {
            visibility -= mov ? 0.15f : 0.3f;
        }
        if (p.isSprinting()) {
            visibility = 0.6f;
        }
        if (p.isInsideVehicle()) {
            visibility = mov ? 1.0f : visibility * 4.0f;
        }
        if (p.getLocation().getBlock().getType() != Material.AIR) {
            visibility -= 0.15f;
        }
        if (p.isSleeping()) {
            visibility /= 4.0f;
        }
        p.setExp(visibility > 0.0f ? visibility : 0.0f);
    }

    public float getVisibility(final Player p) {
        return this.getConfig("config").getBoolean("Config.players.enable-visibility-bar") ? p.getExp() : 0.6f;
    }

    @Override
    public void onPlayerTick(final Player p, final long tick) {
        this.updateVisibility(p);
    }
}
