package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.StructureGrowEvent;

public class NaturalWorldProtectionModule extends Module {
    public NaturalWorldProtectionModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBurn(final BlockBurnEvent event) {
        if (this.isWorld(event.getBlock().getWorld()) && !this.getConfig("config")
                .getBoolean("Config.world.world-changing.allow-burning")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockGrow(final BlockGrowEvent event) {
        if (this.isWorld(event.getBlock().getWorld()) && !this.getConfig("config")
                .getBoolean("Config.world.world-changing.allow-block-grow")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockSpread(final BlockSpreadEvent event) {
        if (this.isWorld(event.getBlock().getWorld()) && event.getBlock().getType() == Material.DIRT && !this.getConfig(
                "config").getBoolean("Config.world.world-changing.allow-grass-grow")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onStructureGrow(final StructureGrowEvent event) {
        if (this.isWorld(event.getWorld()) && !this.getConfig("config")
                .getBoolean("Config.world.world-changing.allow-tree-grow") && !event.isFromBonemeal()) {
            event.setCancelled(true);
        }
    }
}
