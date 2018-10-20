package space.mori.craftz.util;

import space.mori.craftz.ConfigManager;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ItemRenamer {
    public static Map<String, String> DEFAULT_MAP;
    private List<ItemStack> stacks = new ArrayList<>();

    private ItemRenamer(final ItemStack stack, final ItemStack... stacks) {
        this.stacks.add(stack);
        if (stacks != null) {
            this.stacks.addAll(Arrays.asList(stacks));
        }
    }

    private ItemRenamer(final List<ItemStack> stacks) {
        this.stacks.addAll(stacks);
    }

    private ItemRenamer(final Inventory inv) {
        this(new ArrayList<>(Arrays.asList(inv.getContents())));
    }

    public static ItemRenamer on(final ItemStack stack) {
        return new ItemRenamer(stack);
    }

    public static ItemRenamer on(final ItemStack stack, final ItemStack... stacks) {
        return new ItemRenamer(stack, stacks);
    }

    public static ItemRenamer on(final Inventory inv) {
        return new ItemRenamer(inv);
    }

    public static ItemRenamer on(final InventoryHolder invHolder) {
        return on(invHolder.getInventory());
    }

    public static String getName(final ItemStack input, final Map<String, String> entries) {
        if (input == null || entries == null) {
            return "";
        }
        for (final Map.Entry<String, String> entry : entries.entrySet()) {
            final ItemStack stack = StackParser.fromString(entry.getKey(), false).orElse(null);
            if (input.isSimilar(stack)) {
                return entry.getValue();
            }
        }
        return "";
    }

    public static void reloadDefaultNameMap() {
        ItemRenamer.DEFAULT_MAP = toStringMap(ConfigManager.getConfig("config")
                .getConfigurationSection("Config.change-item-names.names")
                .getValues(false));
    }

    public static Map<String, String> toStringMap(final Map<?, ?> map) {
        final Map<String, String> smap = new HashMap<String, String>();
        for (final Map.Entry<?, ?> entry : map.entrySet()) {
            smap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return smap;
    }

    public ItemRenamer setName(final String name) {
        for (final ItemStack stack : this.stacks) {
            final ItemMeta meta = stack.getItemMeta();
            meta.setDisplayName(name);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemRenamer setLore(final List<String> lore) {
        for (final ItemStack stack : this.stacks) {
            final ItemMeta meta = stack.getItemMeta();
            meta.setLore(lore);
            stack.setItemMeta(meta);
        }
        return this;
    }

    public ItemRenamer setLore(final String... lore) {
        return this.setLore(Arrays.asList(lore));
    }

    public ItemRenamer copyFrom(final ItemStack sample) {
        for (final ItemStack stack : this.stacks) {
            stack.setType(sample.getType());
            stack.setAmount(sample.getAmount());
            stack.setDurability(sample.getDurability());
            stack.setItemMeta(sample.getItemMeta());
        }
        return this;
    }

    public ItemRenamer setSpecificNames(final Map<String, String> map) {
        for (final ItemStack stack : this.stacks) {
            final String name = getName(stack, map);
            if (name != null && !name.equals("")) {
                final ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(name);
                stack.setItemMeta(meta);
            }
        }
        return this;
    }

    public ItemStack get() {
        return this.stacks.isEmpty() ? null : this.stacks.get(0);
    }

    public List<ItemStack> getAll() {
        return Collections.unmodifiableList(this.stacks);
    }
}
