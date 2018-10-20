package space.mori.craftz.worlddata;

import javax.annotation.Nonnull;

public class PlayerData {
    public int thirst;
    public int zombiesKilled;
    public int playersKilled;
    public int minutesSurvived;
    public boolean bleeding;
    public boolean poisoned;
    public boolean bonesBroken;

    public PlayerData(final int thirst, final int zombiesKilled, final int playersKilled, final int minutesSurvived,
            final boolean bleeding, final boolean bonesBroken, final boolean poisoned) {
        this.thirst = thirst;
        this.zombiesKilled = zombiesKilled;
        this.playersKilled = playersKilled;
        this.minutesSurvived = minutesSurvived;
        this.bleeding = bleeding;
        this.bonesBroken = bonesBroken;
        this.poisoned = poisoned;
    }

    @Nonnull
    public static PlayerData fromString(@Nonnull String s) {
        String[] spl = s.split("\\|");
        int thirst = spl.length > 0 ? Integer.valueOf(spl[0]) : 20;
        int zombiesKilled = spl.length > 1 ? Integer.valueOf(spl[1]) : 0;
        int playersKilled = spl.length > 2 ? Integer.valueOf(spl[2]) : 0;
        int minutesSurvived = spl.length > 3 ? Integer.valueOf(spl[3]) : 0;
        boolean bleeding = spl.length > 4 && spl[4].equals("1");
        boolean bonesBroken = spl.length > 5 && spl[5].equals("1");
        boolean poisoned = spl.length > 6 && spl[6].equals("1");
        return new PlayerData(thirst, zombiesKilled, playersKilled, minutesSurvived, bleeding, bonesBroken, poisoned);
    }

    @Override
    public String toString() {
        return this.thirst + "|" + this.zombiesKilled + "|" + this.playersKilled + "|" + this.minutesSurvived + "|" + (
                this.bleeding
                ? "1"
                : "0") + "|" + (this.poisoned ? "1" : "0") + "|" + (this.bonesBroken ? "1" : "0");
    }
}
