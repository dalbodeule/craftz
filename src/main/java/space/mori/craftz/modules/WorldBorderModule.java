package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WorldBorderModule extends Module {
    public WorldBorderModule(final CraftZ craftZ) {
        super(craftZ);
    }

    public boolean isEnabled() {
        return this.getConfig("config").getBoolean("Config.world.world-border.enable");
    }

    public void setEnabled(final boolean enable) {
        this.getConfig("config").set("Config.world.world-border.enable", enable);
        this.saveConfig("config");
    }

    public String getShape() {
        return this.getConfig("config").getString("Config.world.world-border.shape");
    }

    public void setShape(final String shape) {
        this.getConfig("config").set("Config.world.world-border.shape", shape);
        this.saveConfig("config");
    }

    public double getRadius() {
        return this.getConfig("config").getDouble("Config.world.world-border.radius");
    }

    public void setRadius(final double radius) {
        this.getConfig("config").set("Config.world.world-border.radius", radius);
        this.saveConfig("config");
    }

    public double getX() {
        return this.getConfig("config").getDouble("Config.world.world-border.x");
    }

    public void setX(final double x) {
        this.getConfig("config").set("Config.world.world-border.x", x);
        this.saveConfig("config");
    }

    public double getZ() {
        return this.getConfig("config").getDouble("Config.world.world-border.z");
    }

    public void setZ(final double z) {
        this.getConfig("config").set("Config.world.world-border.z", z);
        this.saveConfig("config");
    }

    public void setLocation(final double x, final double z) {
        this.getConfig("config").set("Config.world.world-border.x", x);
        this.getConfig("config").set("Config.world.world-border.z", z);
        this.saveConfig("config");
    }

    public double getRate() {
        return this.getConfig("config").getDouble("Config.world.world-border.rate");
    }

    public void setRate(final double rate) {
        this.getConfig("config").set("Config.world.world-border.rate", rate);
        this.saveConfig("config");
    }

    public double getWorldBorderDistance(final Location ploc) {
        double radius = this.getRadius();
        String shape = this.getShape();
        Location loc = new Location(this.world(), this.getX(), ploc.getY(), this.getZ());
        if (!ploc.getWorld().getName().equals(loc.getWorld().getName())) {
            return 0.0;
        }
        double dist;
        if (shape.equalsIgnoreCase("square") || shape.equalsIgnoreCase("rect")) {
            double x = loc.getX();
            double z = loc.getZ();
            double px = ploc.getX();
            double pz = ploc.getZ();
            double dx = Math.max(Math.max(x - radius - px, 0.0), px - (x + radius));
            double dy = Math.max(Math.max(z - radius - pz, 0.0), pz - (z + radius));
            dist = Math.sqrt(dx * dx + dy * dy);
        } else {
            dist = ploc.distance(loc) - radius;
        }
        return dist < 0.0 ? 0.0 : dist;
    }

    public double getWorldBorderDamage(final Location ploc) {
        return this.getWorldBorderDistance(ploc) * this.getRate();
    }

    @Override
    public void onPlayerTick(final Player p, final long tick) {
        if (tick % 30L != 0L || !this.isSurvival(p) || !this.isEnabled()) {
            return;
        }
        double dmg = this.getWorldBorderDamage(p.getLocation());
        if (!(dmg > 0.0)) {
            return;
        }
        if (tick % 200L == 0L) {
            p.sendMessage(this.getCraftZ().getPrefix() + " " + this.getMsg("Messages.out-of-world"));
        }
        p.damage(dmg);
        p.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 20, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 80, 1));
    }
}
