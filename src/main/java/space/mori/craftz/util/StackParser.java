package space.mori.craftz.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import space.mori.craftz.worlddata.Backpack;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StackParser {
    public static Optional<ItemStack> fromString(String string, boolean withAmount) {
        Material mat = Material.AIR;
        short data = 0;
        int amount = 1;
        String itemName = string;
        Pattern pattern = Pattern.compile("^([0-9])x");
        Matcher matcher = pattern.matcher(string);
        if (matcher.find()) {
            if (withAmount) {
                amount = Integer.parseInt(matcher.group(1));
            }
            itemName = string.substring(matcher.end());
        }
        if (itemName.startsWith("'")) {
            itemName = itemName.substring(1);
        }
        if (itemName.endsWith("'")) {
            itemName = itemName.substring(0, itemName.length() - 1);
        }
        if (itemName.startsWith("<") && itemName.endsWith(">")) {
            return getCustomItem(itemName.substring(1, itemName.length() - 1), amount);
        }
        /* if (itemName.contains(":")) {
            try {
                final Material mat2 = Material.matchMaterial(itemName.split(":")[0]);
                mat = ((mat2 != null) ? mat2 : Material.getMaterial(Integer.parseInt(itemName.split(":")[0])));
                data = Short.parseShort(itemName.split(":")[1]);
            } catch (Exception ex) {}
            if (mat == null) {
                CraftZ.severe("There is no item with name '" + itemName.split(":")[0]
                        + "'! Please check the configuration files.");
            }
        } else {
            try {
                final Material mat2 = Material.matchMaterial(itemName);
                mat = ((mat2 != null) ? mat2 : Material.getMaterial(Integer.parseInt(itemName)));
            } catch (Exception ex2) {}
            if (mat == null) {
                CraftZ.severe("There is no item with name '" + itemName + "'! Please check the configuration files.");
            }
        } */
        return (mat == null) ? Optional.empty() : Optional.of(new ItemStack(mat, amount, data));
    }

    public static String toString(ItemStack stack, boolean withAmount) {
        if (stack == null) {
            return "air";
        }
        boolean a = withAmount && stack.getAmount() > 1;
        return (a ? stack.getAmount() + "x'" : "") + stack.getType().name().toLowerCase() + (a ? "'" : "") + (
                stack.getDurability() != 0 ? ":" + stack.getDurability() : "");
    }

    public static Optional<ItemStack> getCustomItem(@Nonnull String itemName, int amount) {
        String[] spl = itemName.split(":");
        if (spl[0].equalsIgnoreCase("backpack")) {
            int size = 9;
            String title = "Standard Backpack";
            if (spl.length > 1) {
                try {
                    size = Integer.parseInt(spl[1]);
                } catch (NumberFormatException ex) {}
            }
            if (spl.length > 2) {
                title = spl[2];
            }
            return Optional.ofNullable(Backpack.createItem(size, title, false));
        }
        return Optional.empty();
    }

    public static boolean compare(ItemStack stack, String string, boolean withAmount) {
        ItemStack other = fromString(string, withAmount).orElse(null);
        if (other == null) {
            return stack == null;
        }
        boolean a = stack.getType() == other.getType() && stack.getData().equals(other.getData());
        if (withAmount) {
            return a && stack.getAmount() == other.getAmount();
        }
        return a;
    }

    public static boolean compare(Material type, short durability, String string) {
        return fromString(string, false).map(other -> other.getType() == type && other.getDurability() == durability)
                .orElse(false);
    }

    public static boolean compare(@Nonnull Block block, String string) {
        return compare(block.getType(), block.getData(), string);
    }
}
