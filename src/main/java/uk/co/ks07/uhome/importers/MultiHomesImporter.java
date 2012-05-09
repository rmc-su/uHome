package uk.co.ks07.uhome.importers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.World;
import uk.co.ks07.uhome.HomeList;
import uk.co.ks07.uhome.uHome;

public class MultiHomesImporter implements HomeImporter {
    public static final String filename = "multihome_homes.txt";
    public static final String oldFilename = "multihome_homes.txt.old";

    private final uHome uH;
    private final File importFrom;

    public MultiHomesImporter(uHome uH) {
        this.uH = uH;
        this.importFrom = new File(uH.getDataFolder(), filename);
    }

    public boolean canImport() {
        return this.importFrom.isFile();
    }

    public void tryImport(HomeList homeList) {
        this.uH.getLogger().info("Trying to import MultiHomes homes from " + filename);

        BufferedReader file = null;
        int notImported = 0;
        int lineCount = 0;
        String line;
        String[] split;
        String owner;
        String homeName;
        Location loc;

        try {
            file = new BufferedReader(new FileReader(this.importFrom));

            while ((line = file.readLine()) != null) {
                lineCount++;

                if (line.isEmpty() || line.startsWith("#")) {
                    notImported++;
                    continue;
                }

                split = line.split(";");

                if (split.length == 7 || split.length == 8) {
                    // <username>;<x>;<y>;<z>;<pitch>;<yaw>;<world>;
                    // <username>;<x>;<y>;<z>;<pitch>;<yaw>;<world>;<name>
                    owner = split[0];

                    try {
                        World homeWorld = this.uH.getServer().getWorld(split[6]);

                        if (homeWorld == null) {
                            notImported++;
                            this.uH.getLogger().warning("Could not find world named " + split[6] + " on line number " + lineCount + ", skipping.");
                            continue;
                        }

                        loc = new Location(homeWorld, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[5]), Float.parseFloat(split[4]));
                    } catch (NumberFormatException nfe) {
                        notImported++;
                        this.uH.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                        continue;
                    }

                    if (split.length == 8) {
                        homeName = split[7];
                    } else {
                        homeName = uHome.DEFAULT_HOME;
                    }

                    homeList.adminAddHome(loc, owner, homeName, this.uH.getLogger());
                } else {
                    notImported++;
                    this.uH.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                    continue;
                }
            }
        } catch (FileNotFoundException ex) {
            this.uH.getLogger().log(Level.WARNING, "MultiHome Import Exception", ex);
        } catch (IOException ex) {
            this.uH.getLogger().log(Level.WARNING, "MultiHome Import Exception", ex);
        } finally {
            try {
                this.importFrom.renameTo(new File(this.uH.getDataFolder(), oldFilename));

                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
                this.uH.getLogger().log(Level.WARNING, "MultiHome Import Exception (on close)", ex);
            }

            this.uH.getLogger().info("Imported " + (lineCount - notImported) + " homes.");
        }
    }

}
