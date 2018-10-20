package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.util.ItemRenamer;
import space.mori.craftz.util.Rewarder;
import space.mori.craftz.worlddata.PlayerData;
import space.mori.craftz.worlddata.PlayerSpawnpoint;
import space.mori.craftz.worlddata.WorldData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class PlayerManager extends Module {
    private List<PlayerSpawnpoint> spawns = new ArrayList<>();
    private Map<UUID, PlayerData> players = new HashMap<>();
    private Map<UUID, Integer> movingPlayers = new HashMap<>();
    private Map<UUID, Long> lastDeaths = new HashMap<>();

    public PlayerManager(final CraftZ craftZ) {
        super(craftZ);
    }

    public static Player p(final UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    public static String makeSpawnID(final Location signLoc) {
        return "x" + signLoc.getBlockX() + "y" + signLoc.getBlockY() + "z" + signLoc.getBlockZ();
    }

    public static SortedSet<Map.Entry<String, Integer>> sortHighscores(final Map<String, Integer> scoresMap) {
        SortedSet<Map.Entry<String, Integer>> scores = new TreeSet<>((e1, e2) -> {
            int res = e2.getValue().compareTo(e1.getValue());
            return res != 0 ? res : 1;
        });
        scores.addAll(scoresMap.entrySet());
        return scores;
    }

    @Override
    public void onLoad(final boolean configReload) {
        this.spawns.clear();
        final ConfigurationSection sec = WorldData.get().getConfigurationSection("Data.playerspawns");
        if (sec != null) {
            for (String entry : sec.getKeys(false)) {
                ConfigurationSection data = sec.getConfigurationSection(entry);
                PlayerSpawnpoint spawn = new PlayerSpawnpoint(this, data);
                this.spawns.add(spawn);
            }
        }
        for (final Player p : this.world().getPlayers()) {
            this.joinPlayer(p);
        }
    }

    @Override
    public void onDisable() {
        this.saveAllPlayers();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (this.isWorld(p.getWorld())) {
            this.joinPlayer(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChanged(final PlayerChangedWorldEvent event) {
        Player p = event.getPlayer();
        World w = p.getWorld();
        World f = event.getFrom();
        if (this.isWorld(f)) {
            this.savePlayer(p);
        } else if (this.isWorld(w)) {
            this.joinPlayer(p);
        }
    }

    public void joinPlayer(final Player p) {
        if (this.existsInConfig(p)) {
            this.loadPlayer(p, false, null);
        } else {
            boolean reset = this.getConfig("config").getBoolean("Config.players.reset-in-lobby");
            if (reset || p.getHealth() == 0.0) {
                p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
            }
            if (reset) {
                p.setFoodLevel(20);
                p.getInventory().clear();
                p.getInventory().setArmorContents(new ItemStack[4]);
            }
            p.teleport(this.getLobby());
            Kit kit = this.getCraftZ().getKits().getDefaultKit();
            if (kit != null) {
                kit.select(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        Player p = event.getPlayer();
        if (this.isWorld(p.getWorld())) {
            this.savePlayer(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(final PlayerKickEvent event) {
        Player p = event.getPlayer();
        if (this.isWorld(p.getWorld()) && !event.getReason().startsWith(this.getCraftZ().getPrefix())) {
            this.savePlayer(p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        Player p = event.getEntity();
        if (!this.isWorld(p.getWorld())) {
            return;
        }
        FileConfiguration config = this.getConfig("config");
        Player killer = p.getKiller();
        if (killer != null) {
            final PlayerData data = this.getData(killer);
            ++data.playersKilled;
            if (config.getBoolean("Config.players.send-kill-stat-messages")) {
                killer.sendMessage(ChatColor.GOLD + this.getMsg("Messages.killed.player")
                        .replaceAll("%p", p.getDisplayName())
                        .replaceAll("%k", String.valueOf(this.getData(killer).playersKilled)));
            }
            Rewarder.RewardType.KILL_PLAYER.reward(killer);
        }
        final String kickMsg = (this.getCraftZ().getPrefix() + " " + this.getMsg("Messages.died")).replace(
                "%z", "" + this.getData(p).zombiesKilled)
                .replace("%p", "" + this.getData(p).playersKilled)
                .replace("%m", "" + this.getData(p).minutesSurvived);
        this.resetPlayer(p);
        this.setLastDeath(p, System.currentTimeMillis());
        if (config.getBoolean("Config.players.kick-on-death") && !p.hasPermission("craftz.bypassKick")) {
            p.kickPlayer(kickMsg);
        } else {
            p.sendMessage(ChatColor.GREEN + kickMsg);
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
            p.setFoodLevel(20);
            Bukkit.getScheduler().runTask(this.getCraftZ(), () -> {
                p.getInventory().clear();
                p.getInventory().setArmorContents(new ItemStack[4]);
                p.setVelocity(new Vector());
                p.teleport(PlayerManager.this.getLobby());
                final Kit kit = PlayerManager.this.getCraftZ().getKits().getDefaultKit();
                if (kit != null) {
                    kit.select(p);
                }
            });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(final EntityDamageEvent event) {
        EntityType type = event.getEntityType();
        if (type != EntityType.PLAYER) {
            return;
        }
        Player p = (Player) event.getEntity();
        if (this.isInsideOfLobby(p) || (this.isWorld(p.getWorld()) && !this.isPlaying(p))) {
            event.setCancelled(true);
        }
    }

    public boolean hasPlayer(final Player p) {
        return this.hasPlayer(p.getUniqueId());
    }

    public boolean hasPlayer(final UUID uuid) {
        return this.players.containsKey(uuid);
    }

    public void savePlayer(final Player p) {
        if (this.hasPlayer(p)) {
            WorldData.get().set("Data.players." + p.getUniqueId(), this.getData(p).toString());
            WorldData.save();
        }
    }

    public void saveAllPlayers() {
        for (Player p : this.world().getPlayers()) {
            if (hasPlayer(p)) {
                WorldData.get().set("Data.players." + p.getUniqueId(), this.getData(p).toString());
            }
        }
        WorldData.save();
    }

    public void loadPlayer(final Player p, final boolean forceRespawn, PlayerSpawnpoint spawnpoint) {
        if (this.hasPlayer(p) && !forceRespawn) {
            return;
        }
        final FileConfiguration config = this.getConfig("config");
        int invulnTime;
        if (this.existsInConfig(p) && !forceRespawn) {
            this.putPlayer(p, false);
            invulnTime = (int) (config.getDouble("Config.players.invulnerability.on-return") * 20.0);
        } else {
            if (spawnpoint == null && (spawnpoint = this.randomSpawn().orElse(null)) == null) {
                p.sendMessage(ChatColor.RED + this.getMsg("Messages.errors.no-player-spawns"));
                return;
            }
            this.putPlayer(p, true);
            this.savePlayer(p);
            if (config.getBoolean("Config.players.clear-inventory-on-spawn")) {
                final PlayerInventory inv = p.getInventory();
                final Kits kits = this.getCraftZ().getKits();
                for (int i = 0; i < inv.getSize(); ++i) {
                    final ItemStack stack = inv.getItem(i);
                    if (!kits.isSoulbound(stack)) {
                        inv.setItem(i, null);
                    }
                }
                if (!kits.isSoulbound(inv.getHelmet())) {
                    inv.setHelmet(null);
                }
                if (!kits.isSoulbound(inv.getChestplate())) {
                    inv.setChestplate(null);
                }
                if (!kits.isSoulbound(inv.getLeggings())) {
                    inv.setLeggings(null);
                }
                if (!kits.isSoulbound(inv.getBoots())) {
                    inv.setBoots(null);
                }
            }
            p.setHealth(p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue());
            p.setFoodLevel(20);
            spawnpoint.spawn(p);
            invulnTime = (int) (config.getDouble("Config.players.invulnerability.on-spawn") * 20.0);
        }
        if (config.getBoolean("Config.players.medical.thirst.enable")) {
            p.setLevel(this.players.get(p.getUniqueId()).thirst);
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, invulnTime, 1000));
        this.getCraftZ().getScoreboardHelper().addPlayer(p);
    }

    private void putPlayer(final Player p, final boolean defaults) {
        if (defaults) {
            this.players.put(p.getUniqueId(), new PlayerData(20, 0, 0, 0, false, false, false));
        } else {
            final String s = WorldData.get().getString("Data.players." + p.getUniqueId());
            this.players.put(p.getUniqueId(), PlayerData.fromString(s));
        }
    }

    public void resetPlayer(final Player p) {
        if (this.hasPlayer(p)) {
            final PlayerData data = this.getData(p);
            this.addToHighscores(p, data);
        }
        WorldData.get().set("Data.players." + p.getUniqueId(), null);
        WorldData.save();
        this.getCraftZ().getScoreboardHelper().removePlayer(p.getUniqueId());
        this.players.remove(p.getUniqueId());
    }

    public Optional<PlayerSpawnpoint> getSpawnpoint(final String signID) {
        return this.spawns.stream().filter(spawn -> spawn.getID().equals(signID)).findFirst();
    }

    public Optional<PlayerSpawnpoint> getSpawnpoint(final Location signLoc) {
        return this.getSpawnpoint(makeSpawnID(signLoc));
    }

    public void addSpawn(final Location signLoc, final String name) {
        String id = makeSpawnID(signLoc);
        PlayerSpawnpoint spawn = new PlayerSpawnpoint(this, id, signLoc, name);
        this.spawns.add(spawn);
        spawn.save();
        Dynmap dynmap = this.getCraftZ().getDynmap();
        dynmap.createMarker(
                dynmap.SET_PLAYERSPAWNS, "playerspawn_" + id, "Spawn: " + name, signLoc, dynmap.ICON_PLAYERSPAWN);
    }

    public void removeSpawn(final String signID) {
        WorldData.get().set("Data.playerspawns." + signID, null);
        WorldData.save();
        this.getSpawnpoint(signID).ifPresent(spawn -> this.spawns.remove(spawn));
        Dynmap dynmap = this.getCraftZ().getDynmap();
        dynmap.removeMarker(dynmap.getMarker(dynmap.SET_PLAYERSPAWNS, "playerspawn_" + signID));
    }

    public List<PlayerSpawnpoint> getSpawns() {
        return Collections.unmodifiableList(this.spawns);
    }

    public Optional<PlayerSpawnpoint> matchSpawn(final String name) {
        return this.spawns.stream().filter(spawn -> spawn.getName().equalsIgnoreCase(name.trim())).findFirst();
    }

    public Optional<PlayerSpawnpoint> randomSpawn() {
        if (this.spawns.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(this.spawns.get(CraftZ.RANDOM.nextInt(this.spawns.size())));
    }

    public int getSpawnCount() {
        return this.spawns.size();
    }

    public int getRespawnCountdown(final Player player) {
        if (!this.lastDeaths.containsKey(player.getUniqueId()) || player.hasPermission("craftz.instantRespawn")) {
            return 0;
        }
        int countdown = this.getConfig("config").getInt("Config.players.respawn-countdown");
        return (int) (countdown * 1000 - (System.currentTimeMillis() - this.lastDeaths.get(player.getUniqueId())));
    }

    public void setLastDeath(final Player p, final long timestamp) {
        this.lastDeaths.put(p.getUniqueId(), timestamp);
    }

    @Override
    public PlayerData getData(final UUID p) {
        if (!this.players.containsKey(p)) {
            this.loadPlayer(p(p), false, null);
        }
        return this.players.get(p);
    }

    @Override
    public PlayerData getData(final Player p) {
        if (!this.players.containsKey(p.getUniqueId())) {
            this.loadPlayer(p, false, null);
        }
        return this.players.get(p.getUniqueId());
    }

    @Override
    public void onServerTick(final long tick) {
        final Iterator<Map.Entry<UUID, PlayerData>> it = this.players.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, PlayerData> entry = it.next();
            UUID id = entry.getKey();
            PlayerData data = entry.getValue();
            Player p = p(id);
            if (!this.isPlaying(id)) {
                if (p != null) {
                    this.savePlayer(p);
                }
                this.getCraftZ().getScoreboardHelper().removePlayer(id);
                it.remove();
            } else {
                for (final Module m : this.getCraftZ().getModules()) {
                    if (m != this) {
                        m.onPlayerTick(p, tick);
                    }
                }
                if (this.isSurvival(p) && tick % 1200L == 0L) {
                    ++data.minutesSurvived;
                }
                if (tick % 10L != 0L) {
                    continue;
                }
                ItemRenamer.on(p).setSpecificNames(ItemRenamer.DEFAULT_MAP);
            }
        }

        Iterator<Map.Entry<UUID, Integer>> it2 = this.movingPlayers.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<UUID, Integer> entry2 = it2.next();
            int v = entry2.getValue() + 1;
            entry2.setValue(v);
            if (v > 8) {
                it2.remove();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player p = event.getPlayer();
        if (this.isWorld(p.getWorld())) {
            final double distance = event.getFrom().distance(event.getTo());
            if (distance > 0.0) {
                this.movingPlayers.put(p.getUniqueId(), 0);
            }
        }
    }

    public boolean isMoving(final Player p) {
        return this.movingPlayers.containsKey(p.getUniqueId());
    }

    public boolean isLobby(final Location loc) {
        final Location lobby = this.getLobby();
        final int radius = this.getConfig("config").getInt("Config.lobby.radius");
        return loc.getWorld().getName().equals(lobby.getWorld().getName()) && lobby.distance(loc) <= radius;
    }

    public boolean isInsideOfLobby(final Player p) {
        return this.isLobby(p.getLocation());
    }

    public Location getLobby() {
        final World cw = this.world();
        if (cw == null) {
            return null;
        }
        Location lobby = cw.getSpawnLocation();
        ConfigurationSection sec = this.getConfig("config").getConfigurationSection("Config.lobby");
        String ws = sec.getString("world");
        World w = (ws == null) ? null : Bukkit.getWorld(ws);
        if (w != null) {
            lobby.setWorld(w);
        }
        lobby.setX(sec.getDouble("x"));
        lobby.setY(sec.getDouble("y"));
        lobby.setZ(sec.getDouble("z"));
        lobby.setYaw((float) sec.getDouble("yaw"));
        lobby.setPitch((float) sec.getDouble("pitch"));
        return lobby;
    }

    public void setLobby(final Location loc, final double radius) {
        ConfigurationSection sec = this.getConfig("config").getConfigurationSection("Config.lobby");
        sec.set("world", loc.getWorld().getName());
        sec.set("x", (Math.round(loc.getX() * 100.0) / 100.0));
        sec.set("y", (Math.round(loc.getY() * 100.0) / 100.0));
        sec.set("z", (Math.round(loc.getZ() * 100.0) / 100.0));
        sec.set("yaw", (Math.round(loc.getYaw() * 100.0f) / 100.0f));
        sec.set("pitch", (Math.round(loc.getPitch() * 100.0f) / 100.0f));
        sec.set("radius", radius);
        this.saveConfig("config");
    }

    public boolean existsInConfig(final Player p) {
        return WorldData.get().contains("Data.players." + p.getUniqueId());
    }

    public boolean existsInWorld(final Player p) {
        return this.players.containsKey(p.getUniqueId());
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public Optional<Player> randomPlayer() {
        final List<Player> players = this.world().getPlayers();
        if (players.isEmpty()) {
            return Optional.empty();
        }
        Collections.shuffle(players);
        return players.stream().filter(chosen -> !this.isInsideOfLobby(chosen)).findFirst();
    }

    public boolean isPlaying(Player p) {
        return this.players.containsKey(p.getUniqueId()) && this.isWorld(p.getWorld()) && !this.isInsideOfLobby(p);
    }

    public boolean isPlaying(UUID id) {
        Player p = p(id);
        return p != null && this.players.containsKey(id) && this.isWorld(p.getWorld()) && !this.isInsideOfLobby(p);
    }

    public Map<String, Integer> getHighscores(String category) {
        LinkedHashMap<String, Integer> scores = new LinkedHashMap<>();
        ConfigurationSection sec = this.getConfig("highscores").getConfigurationSection("Highscores." + category);
        if (sec != null) {
            for (String player : sec.getKeys(false)) {
                scores.put(player, sec.getInt(player));
            }
        }
        return scores;
    }

    public void addToHighscores(final Player p, final PlayerData data) {
        this.addToHighscores(p, data.minutesSurvived, "minutes-survived");
        this.addToHighscores(p, data.zombiesKilled, "zombies-killed");
        this.addToHighscores(p, data.playersKilled, "players-killed");
    }

    public void addToHighscores(final Player p, final int v, final String category) {
        Map<String, Integer> scores = this.getHighscores(category);
        SortedSet<Map.Entry<String, Integer>> scoresSorted = sortHighscores(scores);
        if (scores.containsKey(p.getName())) {
            int score = scores.get(p.getName());
            if (v < score) {
                return;
            }
        }
        final Map.Entry<String, Integer> scoresLast = scores.isEmpty() ? null : scoresSorted.last();
        if (scores.size() < 10 || scoresLast.getValue() < v) {
            scores.put(p.getName(), v);
            scores.remove(scoresLast);
        }
        if (scores.size() > 10) {
            scores.remove(scoresLast);
        }
        this.getConfig("highscores").createSection("Highscores." + category, scores);
        this.saveConfig("highscores");
    }

    @Override
    public void onDynmapEnabled(final Dynmap dynmap) {
        FileConfiguration config = this.getConfig("config");
        dynmap.clearSet(dynmap.SET_WORLDBORDER);
        if (config.getBoolean("Config.dynmap.show-worldborder") && config.getBoolean(
                "Config.world.world-border.enable")) {
            double r = config.getDouble("Config.world.world-border.radius");
            dynmap.createCircleMarker(
                    dynmap.SET_WORLDBORDER, "worldborder", "World Border", 6, 0.4, 15606306, this.getLobby(), r, r);
        }
        dynmap.clearSet(dynmap.SET_PLAYERSPAWNS);
        if (config.getBoolean("Config.dynmap.show-playerspawns")) {
            for (final PlayerSpawnpoint spawn : this.spawns) {
                final String id = "playerspawn_" + spawn.getID();
                final String label = "Spawn: " + spawn.getName();
                dynmap.createMarker(dynmap.SET_PLAYERSPAWNS, id, label, spawn.getLocation(), dynmap.ICON_PLAYERSPAWN);
            }
        }
    }
}
