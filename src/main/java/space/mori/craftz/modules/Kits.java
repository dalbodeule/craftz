package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.ItemRenamer;
import space.mori.craftz.util.KitEditingSession;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class Kits extends Module {
    private Map<String, Kit> kits;
    private Map<UUID, KitEditingSession> editingSessions;

    public Kits(final CraftZ craftZ) {
        super(craftZ);
        this.kits = new HashMap<>();
        this.editingSessions = new HashMap<>();
    }

    @Override
    public void onLoad(final boolean configReload) {
        this.kits.clear();
        final ConfigurationSection kits = this.getConfig("kits").getConfigurationSection("Kits.kits");
        if (kits != null) {
            for (final String name : kits.getKeys(false)) {
                final ConfigurationSection sec = kits.getConfigurationSection(name);
                final ConfigurationSection itemsSec = sec.getConfigurationSection("items");
                final LinkedHashMap<String, ItemStack> items = new LinkedHashMap<String, ItemStack>();
                if (itemsSec != null) {
                    for (final String slot : itemsSec.getKeys(false)) {
                        items.put(slot, itemsSec.getItemStack(slot));
                    }
                }
                this.kits.put(name, new Kit(this, name, sec.getBoolean("default"), sec.getString("permission"), items));
            }
        }
    }

    @Override
    public void onDisable() {
        for (final Player p : this.getCraftZ().getServer().getOnlinePlayers()) {
            if (this.isEditing(p)) {
                this.stopEditing(p, false, false);
            }
        }
    }

    public void addKit(final Kit kit) {
        this.kits.put(kit.getName(), kit);
        kit.save();
    }

    public void removeKit(final Kit kit) {
        final Iterator<KitEditingSession> it = this.editingSessions.values().iterator();
        while (it.hasNext()) {
            final KitEditingSession session = it.next();
            if (session.kit == kit) {
                session.stop(false);
                it.remove();
            }
        }
        this.kits.remove(kit.getName());
        kit.delete();
    }

    public void setDefault(final Kit defaultKit) {
        for (final Kit kit : this.kits.values()) {
            kit.setDefault(false);
            kit.save();
        }
        if (defaultKit != null) {
            defaultKit.setDefault(true);
            defaultKit.save();
        }
    }

    public Kit getDefaultKit() {
        Kit kit = null;
        for (final Map.Entry<String, Kit> entry : this.kits.entrySet()) {
            if (kit == null) {
                kit = entry.getValue();
            } else {
                if (entry.getValue().isDefault()) {
                    return entry.getValue();
                }
            }
        }
        return kit;
    }

    public Collection<Kit> toCollection() {
        return this.kits.values();
    }

    public List<Kit> getAvailableKits(final Player p) {
        final List<Kit> available = new ArrayList<>();
        for (final Map.Entry<String, Kit> entry : this.kits.entrySet()) {
            final Kit kit = entry.getValue();
            if (kit.canUse(p)) {
                available.add(kit);
            }
        }
        return available;
    }

    public Kit get(final String name) {
        return this.kits.get(name);
    }

    public Kit match(final String name) {
        return this.kits.get(name.toLowerCase());
    }

    public String getSoulboundLabel() {
        return ChatColor.DARK_PURPLE + this.getCraftZ().getPrefix() + " " + ChatColor.LIGHT_PURPLE + this.getConfig(
                "kits").getString("Kits.settings.soulbound-label");
    }

    public boolean isSoulbound(final ItemStack stack) {
        if (stack == null || !stack.hasItemMeta()) {
            return false;
        }
        final ItemMeta meta = stack.getItemMeta();
        if (!meta.hasLore()) {
            return false;
        }
        final List<String> lore = meta.getLore();
        return !lore.isEmpty() && lore.get(0).equals(this.getSoulboundLabel());
    }

    public ItemStack setSoulbound(final ItemStack stack) {
        return (stack.hasItemMeta() && stack.getItemMeta().hasLore())
               ? stack
               : ItemRenamer.on(stack).setLore(this.getSoulboundLabel()).get();
    }

    public KitEditingSession startEditing(final Player p, final Kit kit) {
        if (this.isEditing(p)) {
            return null;
        }
        final KitEditingSession session = KitEditingSession.start(p, kit);
        this.editingSessions.put(p.getUniqueId(), session);
        return session;
    }

    public void stopEditing(final Player p, final boolean message, final boolean save) {
        if (!this.isEditing(p)) {
            return;
        }
        final KitEditingSession session = this.getEditingSession(p);
        session.stop(save);
        this.editingSessions.remove(p.getUniqueId());
        if (message) {
            p.sendMessage(ChatColor.AQUA + this.getMsg(
                    "Messages.cmd.kitsadmin." + (save ? "kit-edited" : "kit-editing-cancelled"))
                    .replace("%k", session.kit.getName()));
        }
    }

    public boolean isEditing(final Player p) {
        return this.editingSessions.containsKey(p.getUniqueId());
    }

    public KitEditingSession getEditingSession(final Player p) {
        return this.editingSessions.get(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Player p = event.getPlayer();
        if (this.isEditing(p)) {
            final String msg = event.getMessage();
            if (msg.equalsIgnoreCase("done")) {
                this.stopEditing(p, true, true);
                event.setCancelled(true);
            } else if (msg.equalsIgnoreCase("cancel")) {
                this.stopEditing(p, true, false);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Item item = event.getItemDrop();
        if (this.isSoulbound(item.getItemStack())) {
            item.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player p = event.getEntity();
        if (this.isWorld(p.getWorld())) {
            event.getDrops().removeIf(stack -> stack != null && this.isSoulbound(stack));
        }
    }
}
