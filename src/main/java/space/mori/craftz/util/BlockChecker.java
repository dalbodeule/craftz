package space.mori.craftz.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class BlockChecker {
    @Nonnull
    public static Optional<Block> getFirst(Material material, @Nonnull World world, int x, int z) {
        for (int y = 0; y < 256; ++y) {
            Location tempLoc = new Location(world, x, y, z);
            if (tempLoc.getBlock().getType() == material) {
                return Optional.ofNullable(tempLoc.getBlock());
            }
        }
        return Optional.empty();
    }

    @Nonnull
    public static Optional<Location> getSafeSpawnLocationOver(@Nonnull Location loc) {
        loc = loc.clone();
        for (int i = Math.max(loc.getBlockY(), 1); i < 256; ++i) {
            loc.setY(i);
            if (isSafe(loc) && isSafe(loc.clone().add(0, 1, 0)) && loc.clone()
                    .subtract(0, 1, 0)
                    .getBlock()
                    .getType()
                    .isSolid()) {
                return Optional.of(loc);
            }
        }
        return Optional.empty();
    }

    @Nonnull
    public static Optional<Location> getSafeSpawnLocationUnder(@Nonnull Location loc) {
        loc = loc.clone();
        for (int i = 255; i > 0; --i) {
            loc.setY(i);
            if (isSafe(loc) && isSafe(loc.clone().add(0, 1, 0)) && loc.clone()
                    .subtract(0, 1, 0)
                    .getBlock()
                    .getType()
                    .isSolid()) {
                return Optional.of(loc);
            }
        }
        return Optional.empty();
    }

    public static boolean isTree(@Nonnull Block block) {
        Location loc = block.getLocation();
        int below = countBlocksBelow(loc, Material.LEGACY_LOG, Material.LEGACY_LOG_2);
        int above = countBlocksAbove(loc, Material.LEGACY_LOG, Material.LEGACY_LOG_2);
        int logs = below + above;
        Location top = loc.clone().add(0.0, (double) above, 0.0);
        return logs > 2 && (isLeaves(top, BlockFace.UP) || (isLeaves(top, BlockFace.NORTH) && isLeaves(
                top, BlockFace.SOUTH) && isLeaves(top, BlockFace.EAST) && isLeaves(top, BlockFace.WEST)));
    }

    private static boolean isLeaves(@Nonnull Location loc, @Nonnull BlockFace face) {
        final Material t = loc.getBlock().getRelative(face).getType();
        return t == Material.LEGACY_LEAVES || t == Material.LEGACY_LEAVES_2;
    }

    public static boolean isSafe(@Nonnull Location loc) {
        return isSafe(loc.getBlock().getType());
    }

    public static boolean isSafe(@Nonnull Block block) {
        return isSafe(block.getType());
    }

    public static boolean isSafe(@Nonnull Material type) {
        return !type.isSolid() && type != Material.LAVA && type != Material.LEGACY_STATIONARY_LAVA;
    }

    public static int countBlocksBelow(@Nonnull Location loc, @Nonnull Material... types) {
        int amount = 0;
        loc = loc.clone();
        final List<Material> tlist = Arrays.asList(types);
        for (int i = loc.getBlockY() - 1; i >= 0; --i) {
            loc.setY(i);
            if (!tlist.contains(loc.getBlock().getType())) {
                break;
            }
            ++amount;
        }
        return amount;
    }

    public static int countBlocksAbove(@Nonnull Location loc, @Nonnull Material... types) {
        int amount = 0;
        loc = loc.clone();
        final List<Material> tlist = Arrays.asList(types);
        for (int i = loc.getBlockY() + 1; i < 256; ++i) {
            loc.setY(i);
            if (!tlist.contains(loc.getBlock().getType())) {
                break;
            }
            ++amount;
        }
        return amount;
    }
}
