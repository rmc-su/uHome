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

public class CommandBookImporter implements HomeImporter {
    public static final String filename = "commandbook_homes.csv";
    public static final String oldFilename = "commandbook_homes.csv.old";

    private final uHome uH;
    private final File importFrom;

    public CommandBookImporter(uHome uH) {
        this.uH = uH;
        this.importFrom = new File(uH.getDataFolder(), filename);
    }
    
    public boolean canImport() {
        return this.importFrom.isFile();
    }

    public void tryImport(HomeList homeList) {
        this.uH.getLogger().info("Trying to import CommandBook homes from " + filename);

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
                split = line.replaceAll("\"", "").split(",");

                if (split.length != 8) {
                    notImported++;
                    this.uH.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                    continue;
                } else {
                    // name, world, owner, X, Y, Z, pitch, yaw
                    owner = split[2];
                    homeName = split[0];
                    try {
                        World homeWorld = this.uH.getServer().getWorld(split[1]);

                        if (homeWorld == null) {
                            notImported++;
                            this.uH.getLogger().warning("Could not find world named " + split[1] + " on line number " + lineCount + ", skipping.");
                            continue;
                        }

                        loc = new Location(homeWorld, Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]), Float.parseFloat(split[7]), Float.parseFloat(split[6]));
                    } catch (NumberFormatException nfe) {
                        notImported++;
                        this.uH.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                        continue;
                    }

                    homeList.adminAddHome(loc, owner, homeName, this.uH.getLogger());
                }
            }
        } catch (FileNotFoundException ex) {
            this.uH.getLogger().log(Level.WARNING, "CommandBook Import Exception", ex);
        } catch (IOException ex) {
            this.uH.getLogger().log(Level.WARNING, "CommandBook Import Exception", ex);
        } finally {
            try {
                this.importFrom.renameTo(new File(this.uH.getDataFolder(), oldFilename));

                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
                this.uH.getLogger().log(Level.WARNING, "CommandBook Import Exception (on close)", ex);
            }

            this.uH.getLogger().info("Imported " + (lineCount - notImported) + " homes.");
        }
    }

    private void importCommandBook(File csv) {
        
    }
}
