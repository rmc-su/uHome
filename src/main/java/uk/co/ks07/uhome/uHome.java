package uk.co.ks07.uhome;

import uk.co.ks07.uhome.storage.ConnectionManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.co.ks07.uhome.griefcraft.Updater;
import uk.co.ks07.uhome.metrics.Metrics;
import uk.co.ks07.uhome.metrics.UHomePlotter;
import uk.co.ks07.uhome.locale.LocaleManager;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;

public class uHome extends JavaPlugin {

    private HomeList homeList;
    public String name;
    public String version;
    private Updater updater;
    public PluginManager pm;
    public static final String DEFAULT_HOME = "home";

    public Economy economy;

    // MultipleHomes import regex.
    private static final Pattern filenamePattern = Pattern.compile("^home_(\\d+)\\.txt$");

    @Override
    public void onDisable() {
        ConnectionManager.closeConnection(this.getLogger());
    }

    @Override
    public void onEnable() {
        this.pm = getServer().getPluginManager();
        this.name = this.getDescription().getName();
        this.version = this.getDescription().getVersion();

        this.getLogger().setLevel(Level.INFO);

        try {
            this.getConfig().options().copyDefaults(true);
            HomeConfig.initialize(this, this.getConfig(), getDataFolder(), this.getLogger());
            this.saveConfig();
        } catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Could not load config!", ex);
        }

        if (HomeConfig.enableEcon) {
            if (getServer().getPluginManager().getPlugin("Vault") != null) {
                RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
                if (rsp != null) {
                    economy = rsp.getProvider();
                    this.getLogger().info("Connected to " + economy.getName() + " for economy support.");
                } else {
                    this.getLogger().warning("Vault could not find any economy plugin to connect to. Please install one or disable economy.");
                    HomeConfig.enableEcon = false;
                }
            } else {
                this.getLogger().warning("Coult not find Vault plugin, but economy is enabled. Please install Vault or disable economy.");
                HomeConfig.enableEcon = false;
            }
        }

        libCheck();
        boolean needImport = convertOldDB(getDataFolder());
        if (!sqlCheck()) {
            return;
        }

        homeList = new HomeList(this, needImport, this.getLogger());
        File cbHomes = new File(this.getDataFolder(), "commandbook_homes.csv");
        if (cbHomes.isFile()) {
            this.getLogger().info("Trying to import CommandBook homes from commandbook_homes.csv.");
            this.importCommandBook(cbHomes);
        }

        File multihomeHomes = new File(this.getDataFolder(), "multihome_homes.txt");
        if (multihomeHomes.isFile()) {
            this.getLogger().info("Trying to import MultiHome homes from multihome_homes.txt.");
            this.importMultiHome(multihomeHomes);
        }

        File multiplehomesHomes = new File(this.getDataFolder(), "multiplehomes_homes");
        if (multiplehomesHomes.isDirectory()) {
            File[] importFrom = multiplehomesHomes.listFiles();

            if (importFrom.length > 0) {
                this.getLogger().info("Trying to import MultipleHomes homes from multiplehomes_homes directory.");
                this.importMultipleHomes(importFrom);
            }
        }

        File homespawnplusHomes = new File(this.getDataFolder(), "HomeSpawnPlus.db");
        if (homespawnplusHomes.isFile()) {
            this.getLogger().info("Trying to import HomeSpawnPlus homes from HomeSpawnPlus.db.");
            this.importHomeSpawnPlus(homespawnplusHomes);
        }

        File customLocale = new File(this.getDataFolder(), "customlocale.properties");

        if (!customLocale.exists()) {
            writeResource(this.getResource("customlocale.properties"), customLocale);
        }

        LocaleManager.init(customLocale, this.getLogger());

        try {
            this.getCommand("sethome").setExecutor(new SetHomeCommand(this, homeList));
            this.getCommand("home").setExecutor(new HomeCommand(this, homeList));
        } catch (NullPointerException npe) {
            this.getLogger().severe("Could not load uHome - another plugin is trying to use /home or /sethome!");
            this.getPluginLoader().disablePlugin(this);
            return;
        }

        this.beginMetrics();

        this.pm.registerEvents(new UHomeListener(this, this.homeList), this);

        // We must defer registration of defaults. See BUKKIT-932
        if (HomeConfig.enableDefaultPerms) {
            this.getServer().getScheduler().scheduleSyncDelayedTask(this, new SetupTask(this), 1L);
        }
    }

    private void libCheck() {
        if (HomeConfig.downloadLibs) {
            updater = new Updater();
            try {
                updater.check();
                updater.update();
            } catch (Exception e) {
                this.getLogger().warning("Failed to update libs.");
            }
        }
    }

    private boolean convertOldDB(File df) {
        File oldDatabase = new File(df, "homes.db");
        File newDatabase = new File(df, "uhomes.db");
        if (!newDatabase.exists() && oldDatabase.exists()) {
            // Create new database file.
            updateFiles(newDatabase);
            oldDatabase.renameTo(new File(df, "homes.db.old"));

            // Return true if importing is required (sqlite only).
            if (!HomeConfig.usemySQL) {
                return true;
            }
        } else if (newDatabase.exists() && oldDatabase.exists()) {
            // We no longer need this file since uhomes.db exists
            oldDatabase.renameTo(new File(df, "homes.db.old"));
        }
        return false;
    }

    private boolean sqlCheck() {
        Connection conn = ConnectionManager.initialize(this.getLogger());
        if (conn == null) {
            this.getLogger().severe("Could not establish SQL connection.");
            pm.disablePlugin(this);
            return false;
        }
        return true;
    }

    private void updateFiles(File newDatabase) {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        if (newDatabase.exists()) {
            newDatabase.delete();
        }
        try {
            newDatabase.createNewFile();
        } catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Could not create new database file", ex);
        }
    }

    // Thanks to xZise for original code.
    public static void writeResource(InputStream fromResource, File toFile) {
        FileOutputStream to = null;
        try {
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fromResource.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead);
            }
        } catch (IOException ex) {
        } finally {
            if (fromResource != null) {
                try {
                    fromResource.close();
                } catch (IOException e) {
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private void beginMetrics() {
        try {
            Metrics metrics = new Metrics(this);

            Metrics.Graph homesGraph = metrics.createGraph("Home Count");

            // Plot the total amount of protections
            homesGraph.addPlotter(new UHomePlotter("Total Homes", this.homeList) {

                @Override
                public int getValue() {
                    return this.homeList.getTotalWarps();
                }

            });

            Metrics.Graph limitGraph = metrics.createGraph("Active Limits");

            // Plot the number of dynamic home limits registered
            limitGraph.addPlotter(new Metrics.Plotter("Registered Home Limits") {

                @Override
                public int getValue() {
                    return HomeConfig.permLimits.size();
                }

            });
            // Plot the number of dynamic home invite limits registered
            limitGraph.addPlotter(new Metrics.Plotter("Registered Invite Limits") {

                @Override
                public int getValue() {
                    if (HomeConfig.enableInvite) {
                        return HomeConfig.permInvLimits.size();
                    } else {
                        return 0;
                    }
                }

            });

            if (metrics.start()) {
                this.getLogger().info("Sending anonymous usage statistics to https://mcstats.org/.");
            }
        } catch (IOException e ) {
            this.getLogger().log(Level.WARNING, "Failed to connect to plugin metrics.", e);
        }
    }

    private void importCommandBook(File csv) {
        BufferedReader file = null;
        int notImported = 0;
        int lineCount = 0;
        String line;
        String[] split;
        String owner;
        String homeName;
        Location loc;

        try {
            file = new BufferedReader(new FileReader(csv));

            while ((line = file.readLine()) != null) {
                lineCount++;
                split = line.replaceAll("\"", "").split(",");

                if (split.length != 8) {
                    notImported++;
                    this.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                    continue;
                } else {
                    // name, world, owner, X, Y, Z, pitch, yaw
                    owner = split[2];
                    homeName = split[0];
                    try {
                        World homeWorld = getServer().getWorld(split[1]);

                        if (homeWorld == null) {
                            notImported++;
                            this.getLogger().warning("Could not find world named " + split[1] + " on line number " + lineCount + ", skipping.");
                            continue;
                        }

                        loc = new Location(homeWorld, Double.parseDouble(split[3]), Double.parseDouble(split[4]), Double.parseDouble(split[5]), Float.parseFloat(split[7]), Float.parseFloat(split[6]));
                    } catch (NumberFormatException nfe) {
                        notImported++;
                        this.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                        continue;
                    }

                    this.homeList.adminAddHome(loc, owner, homeName, this.getLogger());
                }
            }
        } catch (FileNotFoundException ex) {
            this.getLogger().log(Level.WARNING, "CommandBook Import Exception", ex);
        } catch (IOException ex) {
            this.getLogger().log(Level.WARNING, "CommandBook Import Exception", ex);
        } finally {
            try {
                csv.renameTo(new File(this.getDataFolder(), "commandbook_homes.csv.old"));

                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
                this.getLogger().log(Level.WARNING, "CommandBook Import Exception (on close)", ex);
            }

            this.getLogger().info("Imported " + (lineCount - notImported) + " homes.");
        }
    }

    private void importMultiHome(File csv) {
        BufferedReader file = null;
        int notImported = 0;
        int lineCount = 0;
        String line;
        String[] split;
        String owner;
        String homeName;
        Location loc;

        try {
            file = new BufferedReader(new FileReader(csv));

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
                        World homeWorld = getServer().getWorld(split[6]);

                        if (homeWorld == null) {
                            notImported++;
                            this.getLogger().warning("Could not find world named " + split[6] + " on line number " + lineCount + ", skipping.");
                            continue;
                        }

                        loc = new Location(homeWorld, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[5]), Float.parseFloat(split[4]));
                    } catch (NumberFormatException nfe) {
                        notImported++;
                        this.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                        continue;
                    }

                    if (split.length == 8) {
                        homeName = split[7];
                    } else {
                        homeName = DEFAULT_HOME;
                    }

                    this.homeList.adminAddHome(loc, owner, homeName, this.getLogger());
                } else {
                    notImported++;
                    this.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                    continue;
                }
            }
        } catch (FileNotFoundException ex) {
            this.getLogger().log(Level.WARNING, "MultiHome Import Exception", ex);
        } catch (IOException ex) {
            this.getLogger().log(Level.WARNING, "MultiHome Import Exception", ex);
        } finally {
            try {
                csv.renameTo(new File(this.getDataFolder(), "multihome_homes.txt.old"));

                if (file != null) {
                    file.close();
                }
            } catch (IOException ex) {
                this.getLogger().log(Level.WARNING, "MultiHome Import Exception (on close)", ex);
            }

            this.getLogger().info("Imported " + (lineCount - notImported) + " homes.");
        }
    }

    private void importMultipleHomes(File[] importFrom) {
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
                                    World homeWorld = getServer().getWorld(split[5]);

                                    if (homeWorld == null) {
                                        notImported++;
                                        this.getLogger().warning("Could not find world named " + split[6] + " on line number " + lineCount + ", skipping.");
                                        continue;
                                    }

                                    loc = new Location(homeWorld, Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), Float.parseFloat(split[4]), Float.parseFloat(split[3]));
                                } catch (NumberFormatException nfe) {
                                    notImported++;
                                    this.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                                    continue;
                                }

                                this.homeList.adminAddHome(loc, owner, homeName, this.getLogger());
                            } else {
                                notImported++;
                                this.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                                continue;
                            }
                        } else {
                            notImported++;
                            this.getLogger().warning("Failed to parse line number " + lineCount + ", skipping.");
                            continue;
                        }
                    }
                } catch (FileNotFoundException ex) {
                    this.getLogger().log(Level.WARNING, "MultipleHomes Import Exception", ex);
                } catch (IOException ex) {
                    this.getLogger().log(Level.WARNING, "MultipleHomes Import Exception", ex);
                } finally {
                    try {
                        homeFile.renameTo(new File(this.getDataFolder(), homeFile.getName() + ".old"));

                        if (file != null) {
                            file.close();
                        }
                    } catch (IOException ex) {
                        this.getLogger().log(Level.WARNING, "MultipleHomes Import Exception (on close)", ex);
                    }

                    this.getLogger().info("Imported " + (lineCount - notImported) + " homes.");
                }
            }
        }
    }

    private void importHomeSpawnPlus(File db) {
        Connection sqliteconn = null;
        Statement slstatement = null;
        ResultSet slset = null;
        Statement invslstatement;
        ResultSet invslset;

        try {
            Class.forName("org.sqlite.JDBC");
            sqliteconn = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
            sqliteconn.setAutoCommit(false);
            slstatement = sqliteconn.createStatement();
            slset = slstatement.executeQuery("SELECT * FROM hsp_home");

            int imported = 0;
            while (slset.next()) {
                int id = slset.getInt("id");
                String owner = slset.getString("player_name");
                String homeName = slset.getString("name");

                if (homeName == null) {
                    homeName = uHome.DEFAULT_HOME;
                }

                World world = this.getServer().getWorld(slset.getString("world"));

                if (world == null) {
                    this.getLogger().log(Level.WARNING, "Could not import " + owner + "'s home " + homeName + ". World not loaded!");
                    continue;
                }

                Location loc = new Location(world, slset.getDouble("x"), slset.getDouble("y"), slset.getDouble("z"), slset.getFloat("yaw"), slset.getFloat("pitch"));
                this.homeList.adminAddHome(loc, owner, homeName, this.getLogger());
                imported++;

                if (HomeConfig.enableInvite) {
                    invslstatement = sqliteconn.createStatement();
                    invslset = invslstatement.executeQuery("SELECT invited_player FROM hsp_homeinvite WHERE home_id = " + Integer.toString(id));
                    while (invslset.next()) {
                        this.homeList.invitePlayer(owner, invslset.getString("invited_player"), homeName);
                    }
                }
            }

            this.getLogger().info("Imported " + Integer.toString(imported) + " homes.");
        } catch (Exception ex) {
            this.getLogger().log(Level.WARNING, "HomeSpawnPlus Import Exception", ex);
        } finally {
            try {
                if (slstatement != null) {
                    slstatement.close();
                }
                if (slset != null) {
                    slset.close();
                }
                if (sqliteconn != null) {
                    sqliteconn.close();
                }
            } catch (Exception e) {
                this.getLogger().log(Level.WARNING, "HomeSpawnPlus Import Exception (on close)", e);
            } finally {
                db.renameTo(new File(this.getDataFolder(), db.getName() + ".old"));
            }
        }
    }

    public HomeList getHomeList() {
        return this.homeList;
    }

    private class SetupTask implements Runnable {
        private final uHome plugin;

        public SetupTask(uHome plugin) {
            this.plugin = plugin;
        }

        @Override
        public void run() {
            plugin.getLogger().info("Giving default permissions to players.");
            plugin.pm.getPermission("uhome.own").setDefault(PermissionDefault.TRUE);
            plugin.pm.getPermission("uhome.admin").setDefault(PermissionDefault.OP);
            plugin.pm.getPermission("uhome.bypass").setDefault(PermissionDefault.OP);
        }
    }
}
