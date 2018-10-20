package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.ChunkLoadEvent;

public class ChunkModule extends Module {
    public ChunkModule(CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (this.isWorld(event.getWorld()) && !this.getConfig("config")
                .getBoolean("Config.world.world-changing.allow-new-chunks") && event.isNewChunk()) {
            event.getChunk().unload(false);
        }
    }
}
