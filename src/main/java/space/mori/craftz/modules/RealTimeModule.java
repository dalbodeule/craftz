package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;

import java.util.Calendar;

public class RealTimeModule extends Module {
    public RealTimeModule(final CraftZ craftZ) {
        super(craftZ);
    }

    @Override
    public void onServerTick(final long tick) {
        if (this.getConfig("config").getBoolean("Config.world.real-time")) {
            int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) - 6;
            int m = Calendar.getInstance().get(Calendar.MINUTE);
            int t = (int) (h * 1000 + m * 16.666666666666668);
            this.world().setFullTime(t);
        }
    }
}
