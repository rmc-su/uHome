package uk.co.ks07.dynmap;

import java.util.HashSet;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import uk.co.ks07.uhome.HomeList;

public class DynmapHomes {
    final DynmapCommonAPI dynmapAPI;
    final MarkerAPI markerAPI;
    
    public DynmapHomes(Plugin dynmap) {
        dynmapAPI = (DynmapCommonAPI) dynmap;
        markerAPI = dynmapAPI.getMarkerAPI();
    }
    
    public void beginMapping(HomeList homeList) {
        MarkerSet homeMarkers = markerAPI.createMarkerSet("uhome_homes", "Player Homes", null, false);
        homeMarkers.
    }
}
