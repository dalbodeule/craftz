package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.EntityChecker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class FireplaceModule extends Module {
    private static Vector[] fireplaceRotations;

    static {
        FireplaceModule.fireplaceRotations = new Vector[] {
                new Vector(0, 0, 1), new Vector(1, 0, 1), new Vector(1, 0, 0), new Vector(1, 0, -1)
        };
    }

    public FireplaceModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (this.isWorld(event.getPlayer().getWorld())) {
            final Player p = event.getPlayer();
            final Block block = event.getClickedBlock();
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (event.getMaterial() == Material.LEGACY_LOG
                    || event.getMaterial() == Material.LEGACY_LOG_2) && this.getConfig("config")
                    .getBoolean("Config.players.campfires.enable")) {
                if (!block.getType().isTransparent() && block.getType().isSolid() && block.getType() != Material.CHEST
                        && block.getRelative(BlockFace.UP).getType() == Material.AIR
                        && event.getBlockFace() == BlockFace.UP) {
                    this.reduceInHand(p);
                    final Location loc = block.getLocation();
                    final Location standLoc = loc.clone().add(0.5, -0.3, 0.5);
                    final int campfireTicks = this.getConfig("config").getInt("Config.players.campfires.tick-duration");
                    final int lightAfter = FireplaceModule.fireplaceRotations.length * 4;
                    for (int i = 0; i < FireplaceModule.fireplaceRotations.length; ++i) {
                        final int delay = i * 4;
                        this.constructFireplaceStand(standLoc, FireplaceModule.fireplaceRotations[i], delay,
                                lightAfter - delay, campfireTicks
                        );
                    }
                    this.constructFireplaceTorch(loc.add(0.0, 1.0, 0.0), lightAfter, campfireTicks);
                    p.sendMessage(this.getMsg("Messages.placed-fireplace"));
                    event.setCancelled(true);
                } else {
                    p.sendMessage(this.getMsg("Messages.cannot-place-fireplace"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Block block = event.getBlock();
        final Material type = block.getType();
        final Player p = event.getPlayer();
        if (this.isWorld(p.getWorld()) && (type == Material.LEGACY_LOG || type == Material.LEGACY_LOG_2) && this.getConfig("config")
                .getBoolean("Config.players.campfires.enable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(final EntityDamageEvent event) {
        final Entity entity = event.getEntity();
        final EntityType type = event.getEntityType();
        final MetadataValue isFireplace;
        if (this.isWorld(entity.getWorld()) && type == EntityType.ARMOR_STAND
                && (isFireplace = EntityChecker.getMeta(entity, "isFireplace")) != null && isFireplace.asBoolean()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player p = event.getPlayer();
        final Item item = event.getItemDrop();
        if (this.isWorld(item.getWorld())) {
            new BukkitRunnable() {
                public void run() {
                    if (item.isDead()) {
                        this.cancel();
                        return;
                    }
                    final List<Entity> ents = EntityChecker.getNearbyEntities(item, 2.0);
                    for (final Entity ent : ents) {
                        final MetadataValue meta;
                        if (ent instanceof ArmorStand && (meta = EntityChecker.getMeta(ent, "isFireplace")) != null
                                && meta.asBoolean()) {
                            final ItemStack result = item.getItemStack();
                            item.remove();
                            final Material type = result.getType();
                            switch (type) {
                                case CHICKEN: {
                                    result.setType(Material.COOKED_CHICKEN);
                                    break;
                                }
                                case BEEF: {
                                    result.setType(Material.COOKED_BEEF);
                                    break;
                                }
                                case COD: {
                                    result.setType(Material.COOKED_COD);
                                    break;
                                }
                                case SALMON: {
                                    result.setType(Material.COOKED_SALMON);
                                    break;
                                }
                                case PORKCHOP: {
                                    result.setType(Material.COOKED_PORKCHOP);
                                    break;
                                }
                                case POTATO: {
                                    result.setType(Material.BAKED_POTATO);
                                    break;
                                }
                            }
                            this.cancel();
                            if (p.isOnline()) {
                                p.getWorld().dropItem(p.getLocation(), result).setPickupDelay(0);
                                break;
                            }
                            p.getWorld().dropItem(item.getLocation(), result).setPickupDelay(0);
                            break;
                        }
                    }
                }
            }.runTaskTimer(this.getCraftZ(), 10L, 10L);
        }
    }

    public void constructFireplaceStand(final Location loc, final Vector rotation, final int delay,
            final int lightAfter, final int fireTicks) {
        Bukkit.getScheduler().runTaskLater(this.getCraftZ(), () -> {
            final ArmorStand stand = (ArmorStand) loc.getWorld()
                    .spawnEntity(loc.setDirection(rotation), EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setBasePlate(false);
            stand.setMetadata("isFireplace", new FixedMetadataValue(FireplaceModule.this.getCraftZ(), true));
            Bukkit.getScheduler().runTaskLater(FireplaceModule.this.getCraftZ(), () -> {
                stand.setFireTicks(fireTicks);
                Bukkit.getScheduler().runTaskLater(FireplaceModule.this.getCraftZ(), stand::remove, (long) fireTicks);
            }, (long) lightAfter);
        }, delay);
    }

    public void constructFireplaceTorch(final Location loc, final int delay, final int fireTicks) {
        Bukkit.getScheduler().runTaskLater(this.getCraftZ(), () -> {
            final Block torch = loc.getBlock();
            torch.setType(Material.TORCH);
            torch.setMetadata("isFireplace", new FixedMetadataValue(FireplaceModule.this.getCraftZ(), true));
            Bukkit.getScheduler()
                    .scheduleSyncDelayedTask(FireplaceModule.this.getCraftZ(), () -> torch.setType(Material.AIR),
                            fireTicks
                    );
        }, delay);
    }
}
