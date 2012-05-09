package uk.co.ks07.uhome.importers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.World;
import uk.co.ks07.uhome.HomeList;
import uk.co.ks07.uhome.uHome;

public class MultipleHomesImporter implements HomeImporter {
    public static final String directory = "multiplehomes_homes";
    public static final String oldDirectory = "multiplehomes_homes_old";
    public static final String oldFilenameSuffix = ".old";

    private static final Pattern filenamePattern = Pattern.compile("^home_(\\d+)\\.txt$");

    private final uHome uH;
    private final File mhHomesDirectory;
    private File[] importFrom;
    
    public MultipleHomesImporter(uHome uH) {
        this.uH = uH;
        this.mhHomesDirectory = new File(uH.getDataFolder(), directory);
    }

    public boolean canImport() {
        if (mhHomesDirectory.isDirectory()) {
            this.importFrom = mhHomesDirectory.listFiles();
            return importFrom.length > 0;
        } else {
            return false;
        }
    }

    public void tryImport(HomeList homeList) {
        this.uH.getLogger().info("Trying to import MultipleHomes homes from " + directory);

        BufferedReader file = null;
        int notImported = 0;
        int lineCount = 0;
        String line;
        String[] split;
        String owner;
        String homeName;
        Location loc;

        for (File homeFile : importFrom) {
            Matcher match = filenamePattern.matcher(homeFile.getName());
            
            // Ignore files that don't appear to be from MultipleHomes.
            if (match.matches()) {
                // homeName is a number, and will be the same for all homes in each file
                homeName = match.group(0);

                try {
                    file = new BufferedReader(new FileReader(homeFile));

                    while ((line = file.readLine()) != null) {
                        lineCount++;

                        if (line.isEmpty() || line.startsWith("#")) {
                            notImported++;
                            continue;
                        }
                        // ~<username>:<x>_<y>_<z>_<pitch>_<yaw>_<world>

                        split = line.split(":");

                        if (split.length == 2) {
                            // Remove the leading "~"
                            owner = split[0].substring(1);

                            split = split[1].split("_");

                            if (split.length == 6) {
                                try {
                                    World homeWorld = this.uH.getServer().getWorld(split[5]);

                                    if (homeWorld == null) {
                                        notImported++;
                                        this.uH.getLogger().warning("Could not find world named " + split[6] + " on line number " + lineCount + ", skipping.");
                                        continue;
                                    }

                                    loc = new Location(homeWorld, Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Float.parseFloat(split[4]), Float.parseFloat(split[3]));
                                } catch (NumberFormatException nfe) {
                                    notImported++;
                                    this.uH.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                                    continue;
                                }

                                homeList.adminAddHome(loc, owner, homeName, this.uH.getLogger());
                            } else {
                                notImported++;
                                this.uH.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                                continue;
                            }
                        } else {
                            notImported++;
                            this.uH.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                            continue;
                        }
                    }
                } catch (FileNotFoundException ex) {
                    this.uH.getLogger().log(Level.WARNING, "MultipleHomes Import Exception", ex);
                } catch (IOException ex) {
                    this.uH.getLogger().log(Level.WARNING, "MultipleHomes Import Exception", ex);
                } finally {
                    try {
                        homeFile.renameTo(new File(this.uH.getDataFolder(), homeFile.getName() + oldFilenameSuffix));

                        if (file != null) {
                            file.close();
                        }
                    } catch (IOException ex) {
                        this.uH.getLogger().log(Level.WARNING, "MultipleHomes Import Exception (on close)", ex);
                    }

                    this.uH.getLogger().info("Imported " + (lineCount - notImported) + " homes.");
                }
            }
        }
    }

}
