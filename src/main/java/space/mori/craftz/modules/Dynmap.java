package space.mori.craftz.modules;

import space.mori.craftz.CraftZ;
import space.mori.craftz.Module;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.*;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Dynmap extends Module {
    private static Map<Material, String> itemImageMap = new HashMap<>();

    static {
        Dynmap.itemImageMap.put(Material.LEATHER_HELMET, "leather-cap");
        Dynmap.itemImageMap.put(Material.LEATHER_CHESTPLATE, "leather-tunic");
        Dynmap.itemImageMap.put(Material.LEATHER_LEGGINGS, "leather-pants");
        Dynmap.itemImageMap.put(Material.WEB, "web-block");
        Dynmap.itemImageMap.put(Material.WOOD_SWORD, "wooden-sword");
        Dynmap.itemImageMap.put(Material.WOOD_PICKAXE, "wooden-pickaxe");
        Dynmap.itemImageMap.put(Material.WOOD_AXE, "wooden-axe");
        Dynmap.itemImageMap.put(Material.WOOD_HOE, "wooden-hoe");
        Dynmap.itemImageMap.put(Material.WOOD_SPADE, "wooden-shovel");
        Dynmap.itemImageMap.put(Material.MUSHROOM_SOUP, "mushroom-stew");
        Dynmap.itemImageMap.put(Material.CARROT_ITEM, "carrot");
        Dynmap.itemImageMap.put(Material.WORKBENCH, "crafting-table");
    }

    public Object ICON_LOOT;
    public Object ICON_PLAYERSPAWN;
    public Object SET_LOOT;
    public Object SET_PLAYERSPAWNS;
    public Object SET_WORLDBORDER;
    private DynmapCommonAPI api;

    public Dynmap(CraftZ craftZ) {
        super(craftZ);
    }

    @Nonnull
    public static String getItemImage(Material material) {
        String n = Dynmap.itemImageMap.containsKey(material)
                   ? Dynmap.itemImageMap.get(material)
                   : material.name().toLowerCase().replace('_', '-');
        String url = "http://www.minecraftinformation.com/images/" + n + ".png";
        String rn = material.name().toLowerCase().replace('_', ' ');
        String onerror = "this.parentNode.replaceChild(document.createTextNode(\"[" + rn + "]\"), this)";
        return "<img src='" + url + "' onerror='" + onerror + "' style='width: 32px;' />";
    }

    @Override
    public void onLoad(boolean configReload) {
        if (configReload) {
            this.getCraftZ().getModules().forEach(m -> m.onDynmapEnabled(this));
        } else if (Bukkit.getPluginManager().isPluginEnabled("dynmap")) {
            this.apiReady(Bukkit.getPluginManager().getPlugin("dynmap"));
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("dynmap")) {
            this.apiReady(event.getPlugin());
        }
    }

    private void apiReady(Plugin plugin) {
        this.api = (DynmapCommonAPI) plugin;
        CraftZ.info("Successfully hooked into Dynmap.");
        this.ICON_LOOT = this.createIcon("loot", "Loot", this.getCraftZ().getResource("icon_loot.png"));
        this.ICON_PLAYERSPAWN = this.createIcon(
                "playerspawn", "Spawn", this.getCraftZ().getResource("icon_playerspawn.png"));
        this.SET_LOOT = this.createSet("loot", "Loot");
        this.SET_PLAYERSPAWNS = this.createSet("playerspawns", "Spawns");
        this.SET_WORLDBORDER = this.createSet("worldborder", "World Border");
        this.getCraftZ().getModules().forEach(m -> m.onDynmapEnabled(this));
    }

    public boolean hasAccess() {
        return this.api != null && this.api.markerAPIInitialized();
    }

    public Object createSet(String id, String label) {
        if (!this.hasAccess()) {
            return null;
        }
        id = "craftz_" + id;
        label = this.getCraftZ().getPrefix() + " " + label;
        MarkerAPI mapi = this.api.getMarkerAPI();
        MarkerSet set = mapi.getMarkerSet(id);
        return (set != null) ? set : mapi.createMarkerSet(id, label, null, false);
    }

    public Object createIcon(String id, String label, InputStream pngStream) {
        return this.createIcon(id, label, pngStream, false);
    }

    public Object createIcon(String id, String label, InputStream pngStream, boolean deleteExisting) {
        if (!this.hasAccess()) {
            return null;
        }
        MarkerAPI mapi = this.api.getMarkerAPI();
        id = "craftz_" + id;
        label = this.getCraftZ().getPrefix() + " " + label;
        MarkerIcon icon = mapi.getMarkerIcon(id);
        if (icon != null && deleteExisting) {
            icon.deleteIcon();
        }
        return (!deleteExisting && icon != null) ? icon : mapi.createMarkerIcon(id, label, pngStream);
    }

    public Object createUserIcon(String id, String label, String name, Object defaultIconHandle) {
        File f = new File(this.getCraftZ().getDataFolder(), "mapicons/" + name + ".png");
        try {
            return this.createIcon(id, label, new FileInputStream(f), true);
        } catch (FileNotFoundException ex) {
            InputStream cstream = this.getCraftZ().getResource("icon_" + name + ".png");
            if (cstream != null) {
                return this.createIcon(id, label, cstream);
            }
            return defaultIconHandle;
        }
    }

    public void unpackIcons(String... icons) {
        File dir = new File(this.getCraftZ().getDataFolder(), "mapicons");
        dir.mkdirs();
        for (String icon : icons) {
            try {
                InputStream in = this.getCraftZ().getResource("icon_" + icon + ".png");
                if (in == null) {
                    CraftZ.severe("Default icon '" + icon + "' ('icon_" + icon + ".png') not found!");
                } else {
                    File f = new File(dir, icon + ".png");
                    if (!f.exists()) {
                        OutputStream out = new FileOutputStream(f);
                        byte[] buffer = new byte[4096];
                        int readBytes;
                        while ((readBytes = in.read(buffer)) > 0) {
                            out.write(buffer, 0, readBytes);
                        }
                        in.close();
                        out.flush();
                        out.close();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public Object createMarker(Object setHandle, String id, String label, Location loc, Object iconHandle) {
        if (!this.hasAccess()) {
            return null;
        }
        MarkerSet set = (MarkerSet) setHandle;
        MarkerIcon icon = (MarkerIcon) iconHandle;
        id = "craftz_" + id;
        label = this.getCraftZ().getPrefix() + " " + label;
        Marker marker = set.findMarker(id);
        return (marker != null)
               ? marker
               : set.createMarker(id, label, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), icon, false);
    }

    public void removeMarker(Object markerHandle) {
        if (!this.hasAccess()) {
            return;
        }
        Marker m = (Marker) markerHandle;
        if (m != null) {
            m.deleteMarker();
        }
    }

    public Object getMarker(Object setHandle, String id) {
        if (!this.hasAccess()) {
            return null;
        }
        MarkerSet set = (MarkerSet) setHandle;
        id = "craftz_" + id;
        return set.findMarker(id);
    }

    public void setMarkerDescription(Object markerHandle, String s) {
        if (!this.hasAccess()) {
            return;
        }
        Marker m = (Marker) markerHandle;
        if (m != null) {
            m.setDescription(s);
        }
    }

    public Object createCircleMarker(Object setHandle, String id, String label, int weight, double opacity, int color,
            Location loc, double xr, double zr) {
        if (!this.hasAccess() || loc == null) {
            return null;
        }
        MarkerSet set = (MarkerSet) setHandle;
        id = "craftz_" + id;
        label = this.getCraftZ().getPrefix() + " " + label;
        CircleMarker marker = set.findCircleMarker(id);
        if (marker == null) {
            marker = set.createCircleMarker(
                    id, label, false, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), xr, zr, false);
        }
        marker.setLineStyle(weight, opacity, color);
        marker.setFillStyle(0.0, 0);
        return marker;
    }

    public void removeCircleMarker(Object setHandle, String id) {
        if (!this.hasAccess()) {
            return;
        }
        MarkerSet set = (MarkerSet) setHandle;
        id = "craftz_" + id;
        CircleMarker m = set.findCircleMarker(id);
        if (m != null) {
            m.deleteMarker();
        }
    }

    public void clearSet(Object setHandle) {
        if (!this.hasAccess()) {
            return;
        }
        MarkerSet set = (MarkerSet) setHandle;
        for (Marker m : set.getMarkers()) {
            m.deleteMarker();
        }
    }
}
