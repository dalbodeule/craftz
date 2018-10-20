package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherModule extends Module {
    public WeatherModule(CraftZ craftZ) {
        super(craftZ);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (this.isWorld(event.getWorld()) && !this.getConfig("config")
                .getBoolean("Config.world.weather.allow-weather-changing")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onThunderChange(ThunderChangeEvent event) {
        if (this.isWorld(event.getWorld()) && !this.getConfig("config")
                .getBoolean("Config.world.weather.allow-weather-changing")) {
            event.setCancelled(true);
        }
    }
}
