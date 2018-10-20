package space.mori.craftz.util;

import space.mori.craftz.ConfigManager;
import space.mori.craftz.CraftZ;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Rewarder {
    public static final DecimalFormat DEFAULT_FORMAT = new DecimalFormat(
            "0.##", new DecimalFormatSymbols(Locale.ENGLISH));
    public static Economy economy = null;

    public static boolean setup() {
        try {
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager()
                    .getRegistration(Economy.class);
            if (economyProvider != null) {
                Rewarder.economy = economyProvider.getProvider();
            }
        } catch (NoClassDefFoundError noClassDefFoundError) {}
        return Rewarder.economy != null;
    }

    public static void give(OfflinePlayer player, double amount) {
        if (Rewarder.economy == null || amount == 0.0) {
            return;
        }
        if (amount > 0.0) {
            Rewarder.economy.depositPlayer(player, amount);
        } else {
            Rewarder.economy.withdrawPlayer(player, -amount);
        }
    }

    public static String formatMoney(double money) {
        if (Rewarder.economy == null) {
            return Rewarder.DEFAULT_FORMAT.format(money);
        }
        return Rewarder.economy.format(money);
    }

    public enum RewardType {
        KILL_ZOMBIE("KILL_ZOMBIE", 0, "Config.players.rewards.amount-kill-zombie", "Messages.rewards.message"),
        KILL_PLAYER("KILL_PLAYER", 1, "Config.players.rewards.amount-kill-player", "Messages.rewards.message"),
        HEAL_PLAYER("HEAL_PLAYER", 2, "Config.players.rewards.amount-heal-player", "Messages.rewards.message");

        public final String configEntry;
        public final String messageEntry;

        RewardType(final String s, final int n, final String configEntry, final String messageEntry) {
            this.configEntry = configEntry;
            this.messageEntry = messageEntry;
        }

        public double getReward() {
            return ConfigManager.getConfig("config").getDouble(this.configEntry);
        }

        public boolean getNotificationsEnabled() {
            return ConfigManager.getConfig("config").getBoolean("Config.players.rewards.enable-notifications");
        }

        public String getNotification() {
            return CraftZ.getInstance().getMsg(this.messageEntry);
        }

        public String formatNotification() {
            return this.getNotification().replace("%m", Rewarder.formatMoney(this.getReward()));
        }

        public void reward(OfflinePlayer player) {
            Rewarder.give(player, this.getReward());
            Player p = player.getPlayer();
            if (p != null && this.getNotificationsEnabled()) {
                p.sendMessage(ChatColor.GOLD + this.formatNotification());
            }
        }
    }
}
