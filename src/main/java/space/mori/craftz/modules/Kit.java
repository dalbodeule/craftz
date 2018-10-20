package space.mori.craftz.modules;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Kit {
    private final Kits kits;
    private final String name;
    private boolean isDefault;
    private String permission;
    private Map<String, ItemStack> items;

    public Kit(final Kits kits, final String name, final boolean isDefault, final String permission,
            final LinkedHashMap<String, ItemStack> items) {
        this.kits = kits;
        this.name = name;
        this.isDefault = isDefault;
        this.permission = permission;
        this.items = Collections.unmodifiableMap(items);
    }

    public String getName() {
        return this.name;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(final String permission) {
        this.permission = permission;
    }

    public Map<String, ItemStack> getItems() {
        return this.items;
    }

    public void setItems(final LinkedHashMap<String, ItemStack> items) {
        this.items = Collections.unmodifiableMap(items);
    }

    public void setItems(final PlayerInventory inventory) {
        final LinkedHashMap<String, ItemStack> items = new LinkedHashMap<String, ItemStack>();
        final ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; ++i) {
            if (contents[i] != null) {
                items.put(String.valueOf(i), contents[i]);
            }
        }
        if (inventory.getHelmet() != null) {
            items.put("helmet", inventory.getHelmet());
        }
        if (inventory.getChestplate() != null) {
            items.put("chestplate", inventory.getChestplate());
        }
        if (inventory.getLeggings() != null) {
            items.put("leggings", inventory.getLeggings());
        }
        if (inventory.getBoots() != null) {
            items.put("boots", inventory.getBoots());
        }
        this.setItems(items);
    }

    public boolean canUse(final Player p) {
        return this.permission == null || this.permission.isEmpty() || p.hasPermission(this.permission);
    }

    public void select(final Player p) {
        final PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); ++i) {
            final ItemStack stack = inv.getItem(i);
            if (this.kits.isSoulbound(stack)) {
                inv.setItem(i, null);
            }
        }
        if (this.kits.isSoulbound(inv.getHelmet())) {
            inv.setHelmet(null);
        }
        if (this.kits.isSoulbound(inv.getChestplate())) {
            inv.setChestplate(null);
        }
        if (this.kits.isSoulbound(inv.getLeggings())) {
            inv.setLeggings(null);
        }
        if (this.kits.isSoulbound(inv.getBoots())) {
            inv.setBoots(null);
        }
        this.give(p, true);
    }

    public void give(final Player p, final boolean soulbound) {
        final PlayerInventory inv = p.getInventory();
        for (final Map.Entry<String, ItemStack> entry : this.items.entrySet()) {
            final ItemStack item = entry.getValue().clone();
            this.setSlot(inv, soulbound ? this.kits.setSoulbound(item) : item, entry.getKey());
        }
    }

    protected void setSlot(final PlayerInventory inv, final ItemStack item, final String slot) {
        if (slot.equalsIgnoreCase("helmet") || slot.equalsIgnoreCase("helm")) {
            inv.setHelmet(item);
        } else if (slot.equalsIgnoreCase("chestplate") || slot.equalsIgnoreCase("chest")) {
            inv.setChestplate(item);
        } else if (slot.equalsIgnoreCase("leggings") || slot.equalsIgnoreCase("leggins")) {
            inv.setLeggings(item);
        } else if (slot.equalsIgnoreCase("boots")) {
            inv.setBoots(item);
        } else {
            try {
                inv.setItem(Integer.parseInt(slot), item);
            } catch (NumberFormatException ex) {}
        }
    }

    public void save() {
        final ConfigurationSection sec = this.kits.getConfig("kits").createSection("Kits.kits." + this.name);
        if (this.isDefault) {
            sec.set("default", true);
        }
        if (this.permission != null && !this.permission.isEmpty()) {
            sec.set("permission", this.permission);
        }
        sec.set("items", this.items);
        this.kits.saveConfig("kits");
    }

    public void delete() {
        this.kits.getConfig("kits").set("Kits.kits." + this.name, null);
        this.kits.saveConfig("kits");
    }
}
