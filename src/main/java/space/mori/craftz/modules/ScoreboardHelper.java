package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import space.mori.craftz.worlddata.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ScoreboardHelper extends Module {
    private static final String OBJECTIVE = "stats";
    private static final String OBJECTIVE_DISPLAY = "Stats";
    private ScoreboardManager manager;
    private Map<UUID, Scoreboard> boards = new HashMap<>();

    public ScoreboardHelper(CraftZ craftZ) {
        super(craftZ);
    }

    @Override
    public void onLoad(boolean configReload) {
        if (configReload) {
            return;
        }
        this.manager = this.getCraftZ().getServer().getScoreboardManager();
    }

    public void addPlayer(Player p) {
        Scoreboard board = this.manager.getNewScoreboard();
        Objective stats = board.registerNewObjective("stats", "dummy");
        stats.setDisplayName("Stats");
        Statistic.prepare(stats);
        this.boards.put(p.getUniqueId(), board);
    }

    public void removePlayer(UUID id) {
        if (this.boards.containsKey(id)) {
            this.boards.get(id).clearSlot(DisplaySlot.SIDEBAR);
        }
        this.boards.remove(id);
    }

    @Override
    public void onServerTick(long tick) {
        if (tick % 10L != 0L) {
            return;
        }
        Iterator<Map.Entry<UUID, Scoreboard>> it = this.boards.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Scoreboard> entry = it.next();
            UUID id = entry.getKey();
            Scoreboard board = entry.getValue();
            Player p = PlayerManager.p(id);
            if (p == null) {
                board.clearSlot(DisplaySlot.SIDEBAR);
                it.remove();
            } else {
                if (!this.getCraftZ().getPlayerManager().hasPlayer(p)) {
                    continue;
                }
                PlayerData data = this.getData(p);
                Objective stats = board.getObjective("stats");
                Statistic.apply(stats, p, data);
                if (this.getConfig("config").getBoolean("Config.players.use-scoreboard-for-stats")) {
                    if (stats.getDisplaySlot() != DisplaySlot.SIDEBAR) {
                        stats.setDisplaySlot(DisplaySlot.SIDEBAR);
                    }
                    if (p.getScoreboard() == board) {
                        continue;
                    }
                    p.setScoreboard(board);
                } else {
                    if (board.getObjective(DisplaySlot.SIDEBAR) != stats) {
                        continue;
                    }
                    board.clearSlot(DisplaySlot.SIDEBAR);
                }
            }
        }
    }

    public enum Statistic {
        BLOODLEVEL("Blood level") {
            @Override
            public int computeScore(final Player p, final PlayerData data) {
                return (int) (p.getHealth() * 600.0);
            }
        },
        ZOMBIES_KILLED("Zombies killed") {
            @Override
            public int computeScore(final Player p, final PlayerData data) {
                return data.zombiesKilled;
            }
        },
        PLAYERS_KILLED("Players killed") {
            @Override
            public int computeScore(final Player p, final PlayerData data) {
                return data.playersKilled;
            }
        },
        MINUTES_SURVIVED("Minutes survived") {
            @Override
            public int computeScore(final Player p, final PlayerData data) {
                return data.minutesSurvived;
            }
        };

        public final String label;

        Statistic(final String label) {
            this.label = label;
        }

        public static void prepare(final Objective objective) {
            for (final Statistic stat : values()) {
                objective.getScore(stat.label).setScore(0);
            }
        }

        public static void apply(final Objective objective, final Player p, final PlayerData data) {
            for (final Statistic stat : values()) {
                objective.getScore(stat.label).setScore(stat.computeScore(p, data));
            }
        }

        public abstract int computeScore(final Player p0, final PlayerData p1);
    }
}
