package uk.co.ks07.uhome.storage;

import java.util.HashMap;
import java.util.HashSet;
import uk.co.ks07.uhome.Home;

public class WarpData {
    public HashMap<String, HashMap<String, Home>> homeMap;
    public HashMap<String, HashSet<Home>> inviteMap;

    public WarpData() {
        this.homeMap = new HashMap<String, HashMap<String, Home>>();
        this.inviteMap = new HashMap<String, HashSet<Home>>();
    }

    public WarpData(HashMap<String, HashMap<String, Home>> homeMap, HashMap<String, HashSet<Home>> inviteMap) {
        this.homeMap = homeMap;
        this.inviteMap = inviteMap;
    }
}