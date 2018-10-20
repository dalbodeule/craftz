package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.EntityChecker;
import space.mori.craftz.util.StackParser;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class DeadPlayers extends Module {
    public static List<Material> WEAPONS = Arrays.asList(Material.WOOD_SWORD, Material.WOOD_AXE, Material.STONE_SWORD,
            Material.STONE_AXE, Material.BOW, Material.IRON_SWORD, Material.IRON_AXE, Material.GOLD_SWORD,
            Material.GOLD_AXE, Material.DIAMOND_SWORD, Material.DIAMOND_AXE
    );

    public DeadPlayers(final CraftZ craftZ) {
        super(craftZ);
    }

    public static ItemStack getHandItem(@Nonnull Collection<ItemStack> items) {
        ItemStack hand = null;
        int handIndex = 0;
        for (ItemStack item : items) {
            if (item == null) {
                continue;
            }
            int index = DeadPlayers.WEAPONS.indexOf(item.getType());
            if (hand != null && index <= handIndex) {
                continue;
            }
            hand = item;
            handIndex = index;
        }
        return hand;
    }

    public ZombieVillager create(@Nonnull Player p) {
        PlayerInventory inv = p.getInventory();
        List<ItemStack> inventory = Arrays.stream(inv.getContents())
                .filter(stack -> stack != null && stack.getType() != Material.AIR && !this.getCraftZ()
                        .getKits()
                        .isSoulbound(stack))
                .collect(Collectors.toList());

        ZombieVillager zombie = p.getWorld().spawn(p.getLocation(), ZombieVillager.class);
        zombie.setBaby(false);
        zombie.setCanPickupItems(false);
        zombie.setRemoveWhenFarAway(false);
        zombie.setCustomName(p.getName());
        zombie.setCustomNameVisible(true);
        zombie.getEquipment().setArmorContents(inv.getArmorContents());
        zombie.getEquipment().setItemInMainHand(getHandItem(inventory));
        zombie.getEquipment().setItemInMainHandDropChance(0.0f);
        zombie.setMetadata("inventory", new FixedMetadataValue(this.getCraftZ(), inventory));
        return zombie;
    }

    @Nonnull
    public List<ItemStack> getInventory(final Zombie zombie) {
        List<ItemStack> inventory = new ArrayList<>();
        if (!zombie.hasMetadata("inventory")) {
            return inventory;
        }
        List<MetadataValue> metaList = zombie.getMetadata("inventory");
        for (MetadataValue meta : metaList) {
            if (meta.getOwningPlugin().getName().equals(this.getCraftZ().getName()) && meta.value() instanceof List) {
                List<? extends ItemStack> value = (List<? extends ItemStack>) meta.value();
                inventory.addAll(value);
            }
        }
        Arrays.stream(zombie.getEquipment().getArmorContents()).filter(Objects::nonNull).forEach(inventory::add);

        return inventory;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (this.isWorld(p.getWorld()) && this.getConfig("config").getBoolean("Config.players.spawn-death-zombie")) {
            this.create(p);
            event.getDrops().clear();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        if (!this.isWorld(event.getEntity().getWorld())) {
            return;
        }
        LivingEntity entity = event.getEntity();
        List<ItemStack> drops = event.getDrops();
        if (!this.getCraftZ().isEnemy(entity)) {
            return;
        }
        drops.clear();
        List<ItemStack> inventory = entity instanceof Zombie
                                    ? this.getInventory((Zombie) entity)
                                    : Collections.emptyList();
        if (!inventory.isEmpty()) {
            drops.addAll(inventory);
        } else {
            MetadataValue meta = EntityChecker.getMeta(entity, "enemyType");
            if (meta == null) {
                return;
            }
            ConfigurationSection sec = this.getCraftZ().getEnemyDefinition(meta.asString());
            if (sec == null || !sec.contains("drops")) {
                return;
            }
            ConfigurationSection dropsSec = sec.getConfigurationSection("drops");
            for (String itemString : dropsSec.getKeys(false)) {
                StackParser.fromString(itemString, true).ifPresent(item -> {
                    if (CraftZ.RANDOM.nextDouble() <= dropsSec.getDouble(itemString)) {
                        drops.add(item);
                    }
                });
            }
        }
    }
}
