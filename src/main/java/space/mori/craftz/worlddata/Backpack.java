package space.mori.craftz.worlddata;

import space.mori.craftz.util.ItemRenamer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Backpack extends WorldDataObject {
    public static final String DEFAULT_TITLE = "Standard Backpack";
    public static final String LORE_PREFIX = "" + ChatColor.RESET + ChatColor.GRAY;

    private final String title;
    private final Inventory inventory;
    private final ItemStack item;

    public Backpack(@Nonnull final ConfigurationSection data) {
        this(data.getName(), data.getInt("size"), data.getString("title"));
        ConfigurationSection itemssec = data.getConfigurationSection("items");
        if (itemssec == null) {
            return;
        }
        for (String slot : itemssec.getKeys(false)) {
            try {
                int slotNum = Integer.parseInt(slot);
                ItemStack stack = itemssec.getItemStack(slot);
                this.inventory.setItem(slotNum, stack);
            } catch (Exception ex) {}
        }
    }

    public Backpack(String id, int size, String title) {
        super(id);
        this.title = title;
        this.item = createItem(size, title, id);
        this.inventory = Bukkit.createInventory(null, size, title);
    }

    @Nonnull
    public static Optional<Backpack> create(@Nullable ItemStack stack) {
        if (!isBackpack(stack)) {
            return Optional.empty();
        }
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        String id = lore.get(2);
        if (id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
        int size = Integer.parseInt(ChatColor.stripColor(lore.get(1)).replace("Size: ", ""));
        String name = meta.hasDisplayName() ? ChatColor.stripColor(meta.getDisplayName()) : "Standard Backpack";
        return Optional.of(new Backpack(id, size, name));
    }

    public static ItemStack createItem(int size, String title, boolean withId) {
        return createItem(size, title, withId ? UUID.randomUUID().toString() : "");
    }

    private static ItemStack createItem(int size, String title, String id) {
        return ItemRenamer.on(new ItemStack(Material.CHEST))
                .setName(ChatColor.RESET + title)
                .setLore(LORE_PREFIX + "Backpack", LORE_PREFIX + "Size: " + size, id)
                .get();
    }

    public static boolean isBackpack(@Nullable ItemStack stack) {
        if (stack == null) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || meta.hasLocalizedName()) {
            return false;
        }
        List<String> lore = meta.getLore();
        return lore != null && lore.size() >= 3 && lore.get(0).contains("Backpack");
    }

    public void save() {
        this.save("Data.backpacks");
    }

    @Override
    public void store(@Nonnull ConfigurationSection section) {
        section.set("title", this.title);
        section.set("size", this.inventory.getSize());
        section.set("items", this.toMap());
    }

    public String getTitle() {
        return this.title;
    }

    @Nonnull
    public ItemStack getItem() {
        return this.item.clone();
    }

    public boolean is(ItemStack stack) {
        if (!isBackpack(stack)) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.getLore();
        String id = lore.get(2);
        return id.equals(this.getID());
    }

    @Nonnull
    public Inventory getInventory() {
        return this.inventory;
    }

    public LinkedHashMap<Integer, ItemStack> toMap() {
        LinkedHashMap<Integer, ItemStack> map = new LinkedHashMap<>();
        for (int i = this.inventory.getSize() - 1; i >= 0; i--) {
            ItemStack stack = this.inventory.getItem(i);
            if (stack != null) {
                map.put(i, stack);
            }
        }
        return map;
    }
}
