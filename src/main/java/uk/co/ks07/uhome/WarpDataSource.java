package uk.co.ks07.uhome;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.util.HashMap;

public class WarpDataSource {
    public final static String sqlitedb = "/uhomes.db";
    public final static String mhsqlitedb = "/homes.db.old";
    private final static String TABLE_NAME = "uhomeTable";
    private final static String HOME_TABLE = "CREATE TABLE IF NOT EXISTS `uhomeTable` ("
    	    + "`id` INTEGER PRIMARY KEY,"
            + "`owner` varchar(32) NOT NULL DEFAULT 'Player',"
    	    + "`name` varchar(32) NOT NULL DEFAULT 'home',"
            + "`world` varchar(32) NOT NULL DEFAULT '0'," 
            + "`x` DOUBLE NOT NULL DEFAULT '0'," 
            + "`y` DOUBLE NOT NULL DEFAULT '0',"
            + "`z` DOUBLE NOT NULL DEFAULT '0'," 
            + "`yaw` smallint NOT NULL DEFAULT '0'," 
            + "`pitch` smallint NOT NULL DEFAULT '0',"
            + "UNIQUE (`owner`,`name`)"
            + ");";

    public static void initialize(boolean needImport) {
        TableStatus status = tableExists();
    	if (status == TableStatus.NONE_EXIST) {
            // No tables exist, so create them.
            createTable();
            // Will only be true if using SQLite, so the table wouldn't exist (new file).
            if (needImport) {
                importMyHome();
            }
    	} else if (status == TableStatus.OLD_ONLY) {
            // Need to update the schema.
            HomeLogger.info("Updating database to new format.");
            // Updates MyHome table to current format, including rename. MySQL only.
            boolean tableChanged = dbTblCheck();
            // Update <v1.3a format to patched format. MySQL only.
            if (!tableChanged) {
                patchMySQLTable();
            }
            // SQLite only, copy data into renamed new table. Not needed for MySQL.
            importOldTable();
        } else {
            // Tables are fine.
            HomeLogger.info("Database is up-to-date.");
        }
    }

    private static void patchMySQLTable() {
        if (HomeConfig.usemySQL) {
            String sqlOne = "ALTER TABLE `homeTable` DROP INDEX `owner`";
            String sqlTwo = "ALTER TABLE `homeTable` RENAME TO `"+TABLE_NAME+"`, ADD UNIQUE INDEX `owner` (`owner` ASC, `name` ASC)";

            Statement ps = null;
            try {
                    Connection conn = ConnectionManager.getConnection();
                    ps = conn.createStatement();
                    ps.executeUpdate(sqlOne);
                    conn.commit();

                    ps = conn.createStatement();
                    ps.executeUpdate(sqlTwo);
                    conn.commit();
            } catch (SQLException ex) {
                    HomeLogger.severe("Table Update Exception", ex);
            } finally {
                    try {
                            if (ps != null) {
                                    ps.close();
                            }
                    } catch (SQLException ex) {
                            HomeLogger.severe("Table Update Exception (on close)", ex);
                    }
            }
        }
    }

    private static void importOldTable() {
        if (!HomeConfig.usemySQL) {
            // Create the new table.
            createTable();
            Statement ps = null;
            try {
                    Connection conn = ConnectionManager.getConnection();
                    ps = conn.createStatement();
                    ps.executeUpdate("INSERT INTO "+TABLE_NAME+" SELECT * FROM homeTable");
                    conn.commit();

                    ps = conn.createStatement();
                    ps.executeUpdate("DROP TABLE homeTable");
                    conn.commit();
            } catch (SQLException ex) {
                    HomeLogger.severe("Home Import Exception", ex);
            } finally {
                    try {
                            if (ps != null) {
                                    ps.close();
                            }
                    } catch (SQLException ex) {
                            HomeLogger.severe("Home Import Exception (on close)", ex);
                    }
            }
        }
    }

    public static HashMap<String, HashMap<String, Home>> getMap() {
    	HashMap<String, HashMap<String, Home>> ret = new HashMap<String, HashMap<String, Home>>();
    	Statement statement = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		statement = conn.createStatement();
    		set = statement.executeQuery("SELECT * FROM "+TABLE_NAME);
    		int size = 0;
    		while (set.next()) {
    			size++;
    			int index = set.getInt("id");
                        String name = set.getString("name");
    			String owner = set.getString("owner");
    			String world = set.getString("world");
    			double x = set.getDouble("x");
    			double y = set.getDouble("y");
    			double z = set.getDouble("z");
    			int yaw = set.getInt("yaw");
    			int pitch = set.getInt("pitch");
    			Home warp = new Home(index, owner, name, world, x, y, z, yaw, pitch);
                        if (ret.containsKey(owner)) {
                            ret.get(owner).put(name, warp);
                        } else {
                            HashMap<String, Home> ownerWarps = new HashMap<String, Home>();
                            ownerWarps.put(name, warp);
                            ret.put(owner, ownerWarps);
                        }
    		}
    		HomeLogger.info(size + " homes loaded");
    	} catch (SQLException ex) {
    		HomeLogger.severe("Home Load Exception");
    	} finally {
    		try {
    			if (statement != null) {
    				statement.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("Home Load Exception (on close)");
    		}
    	}
    	return ret;
    }

    private static TableStatus tableExists() {
    	ResultSet rs = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		DatabaseMetaData dbm = conn.getMetaData();
    		rs = dbm.getTables(null, null, TABLE_NAME, null);
    		if (!rs.next()) {
                        rs = dbm.getTables(null, null, "homeTable", null);
                        if (!rs.next()) {
                                return TableStatus.NONE_EXIST;
                        } else {
                                return TableStatus.OLD_ONLY;
                        }
    		}
    		
    		return TableStatus.UP_TO_DATE;
    	} catch (SQLException ex) {
    		HomeLogger.severe("Table Check Exception", ex);
    		return TableStatus.NONE_EXIST;
    	} finally {
    		try {
    			if (rs != null) {
    				rs.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("Table Check SQL Exception (on closing)");
    		}
    	}
    }



    private static void createTable() {
    	Statement st = null;
    	try {
    		HomeLogger.info("Creating Database...");
    		Connection conn = ConnectionManager.getConnection();
    		st = conn.createStatement();
    		st.executeUpdate(HOME_TABLE);
    		conn.commit();

    		if(HomeConfig.usemySQL){ 
    			// We need to set auto increment on SQL.
    			String sql = "ALTER TABLE `homeTable` CHANGE `id` `id` INT NOT NULL AUTO_INCREMENT ";
    			HomeLogger.info("Modifying database for MySQL support");
    			st = conn.createStatement();
    			st.executeUpdate(sql);
    			conn.commit();

    			// Check for old uhomes.db and import to mysql
    			File sqlitefile = new File(HomeConfig.dataDir.getAbsolutePath() + sqlitedb);
    			if (!sqlitefile.exists()) {
    				HomeLogger.info("Could not find old " + sqlitedb);
    				return;
    			} else {
    				HomeLogger.info("Trying to import homes from uhomes.db");
    				Class.forName("org.sqlite.JDBC");
    				Connection sqliteconn = DriverManager.getConnection("jdbc:sqlite:" + HomeConfig.dataDir.getAbsolutePath() + sqlitedb);
    				sqliteconn.setAutoCommit(false);
    				Statement slstatement = sqliteconn.createStatement();
    				ResultSet slset = slstatement.executeQuery("SELECT * FROM "+TABLE_NAME);

    				int size = 0;
    				while (slset.next()) {
    					size++;
    					int index = slset.getInt("id");
                                        String name = slset.getString("name");
    					String owner = slset.getString("owner");
    					String world = slset.getString("world");
    					double x = slset.getDouble("x");
    					double y = slset.getInt("y");
    					double z = slset.getDouble("z");
    					int yaw = slset.getInt("yaw");
    					int pitch = slset.getInt("pitch");
    					Home warp = new Home(index, owner, name, world, x, y, z, yaw, pitch);
    					addWarp(warp);
    				}
    				HomeLogger.info("Imported " + size + " homes from " + sqlitedb);
    				HomeLogger.info("Renaming " + sqlitedb + " to " +sqlitedb + ".old");
    				if (!sqlitefile.renameTo(new File(HomeConfig.dataDir.getAbsolutePath(), sqlitedb + ".old"))) {
    					HomeLogger.warning("Failed to rename " + sqlitedb + "! Please rename this manually!");
    				}

    				if (slstatement != null) {
    					slstatement.close();
    				}
    				if (slset != null) {
    					slset.close();
    				}

    				if (sqliteconn != null) {
    					sqliteconn.close();
    				}
    			}
    		}
    	} catch (SQLException e) {
    		HomeLogger.severe("Create Table Exception", e);
    	} catch (ClassNotFoundException e) {
    		HomeLogger.severe("You need the SQLite library.", e);
    	} finally {
    		try {
    			if (st != null) {
    				st.close();
    			}
    		} catch (SQLException e) {
    			HomeLogger.severe("Could not create the table (on close)");
    		}
    	}
    }

    public static void addWarp(Home warp) {
    	PreparedStatement ps = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		ps = conn.prepareStatement("INSERT INTO "+TABLE_NAME+" (id, name, owner, world, x, y, z, yaw, pitch) VALUES (?,?,?,?,?,?,?,?,?)");
    		ps.setInt(1, warp.index);
                ps.setString(2, warp.name);
    		ps.setString(3, warp.owner);
    		ps.setString(4, warp.world);
    		ps.setDouble(5, warp.x);
    		ps.setDouble(6, warp.y);
    		ps.setDouble(7, warp.z);
    		ps.setInt(8, warp.yaw);
    		ps.setInt(9, warp.pitch);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("Home Insert Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("Home Insert Exception (on close)", ex);
    		}
    	}
    }

    public static void deleteWarp(Home warp) {
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		ps = conn.prepareStatement("DELETE FROM "+TABLE_NAME+" WHERE id = ?");
    		ps.setInt(1, warp.index);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("Home Delete Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("Home Delete Exception (on close)", ex);
    		}
    	}
    }

    public static void moveWarp(Home warp) {
    	PreparedStatement ps = null;
    	ResultSet set = null;

    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE "+TABLE_NAME+" SET x = ?, y = ?, z = ?, world = ?, yaw = ?, pitch = ? WHERE id = ?");
    		ps.setDouble(1, warp.x);
    		ps.setDouble(2, warp.y);
    		ps.setDouble(3, warp.z);
    		ps.setString(4, warp.world);
    		ps.setInt(5, warp.yaw);
    		ps.setDouble(6, warp.pitch);
    		ps.setInt(7, warp.index);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("Home Move Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("Home Move Exception (on close)", ex);
    		}
    	}
    }

    public static boolean dbTblCheck() {
        // SQLite does not support field renaming or deletion, so we can't alter the table this way.
        if (HomeConfig.usemySQL) {
            String test = "SELECT `owner` FROM `homeTable`";
            String sql = "ALTER TABLE `homeTable` RENAME TO `"+TABLE_NAME+"`, CHANGE COLUMN `name` `owner` VARCHAR(32) NOT NULL DEFAULT 'Player', ADD COLUMN `name` VARCHAR(32) NOT NULL DEFAULT 'home', DROP COLUMN `publicAll`, DROP COLUMN `permissions`, DROP COLUMN `welcomeMessage`, ADD UNIQUE INDEX `owner` (`owner` ASC, `name` ASC)";
            return updateDB(test, sql);
        }
        // No changes.
        return false;
    }

    private static void importMyHome() {
    	try {
    		if(!HomeConfig.usemySQL){
    			// Check for old homes.db and import to new db. Assume home name as 'home'
    			File sqlitefile = new File(HomeConfig.dataDir.getAbsolutePath() + mhsqlitedb);
    			if (!sqlitefile.exists()) {
    				HomeLogger.info("Could not find " + mhsqlitedb);
    				return;
    			} else {
    				HomeLogger.info("Trying to import homes from homes.db.old");
    				Class.forName("org.sqlite.JDBC");
    				Connection sqliteconn = DriverManager.getConnection("jdbc:sqlite:" + HomeConfig.dataDir.getAbsolutePath() + mhsqlitedb);
    				sqliteconn.setAutoCommit(false);
    				Statement slstatement = sqliteconn.createStatement();
    				ResultSet slset = slstatement.executeQuery("SELECT * FROM homeTable");

    				int size = 0;
    				while (slset.next()) {
    					size++;
    					int index = slset.getInt("id");
                                        String owner = slset.getString("name");
    					String world = slset.getString("world");
    					double x = slset.getDouble("x");
    					double y = slset.getInt("y");
    					double z = slset.getDouble("z");
    					int yaw = slset.getInt("yaw");
    					int pitch = slset.getInt("pitch");
    					Home warp = new Home(index, owner, "home", world, x, y, z, yaw, pitch);
    					addWarp(warp);
    				}
    				HomeLogger.info("Imported " + size + " homes from " + mhsqlitedb);

    				if (slstatement != null) {
    					slstatement.close();
    				}
    				if (slset != null) {
    					slset.close();
    				}

    				if (sqliteconn != null) {
    					sqliteconn.close();
    				}
    			}
    		}
    	} catch (SQLException e) {
    		HomeLogger.severe("MyHome Import Exception", e);
    	} catch (ClassNotFoundException e) {
    		HomeLogger.severe("You need the SQLite library.", e);
    	}
    }

    public static boolean updateDB(String test, String sql) {
    	// Use same sql for both mysql/sqlite
    	return updateDB(test, sql, sql);
    }

    public static boolean updateDB(String test, String sqlite, String mysql) {
    	// Allowing for differences in the SQL statements for mysql/sqlite.
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		Statement statement = conn.createStatement();
    		statement.executeQuery(test);
    		statement.close();
                // No changes made, return false.
                HomeLogger.info("DB test passed, no changes made.");
                return false;
    	} catch(SQLException ex) {
    		HomeLogger.info("Backing up database for update.");
    		// Failed the test so we need to execute the updates
    		try {
                        Connection conn = ConnectionManager.getConnection();
                        Statement bkpStatement = conn.createStatement();
                        bkpStatement.executeUpdate("DROP TABLE IF EXISTS "+TABLE_NAME+"Backup");
                        bkpStatement.close();
                        HomeLogger.info("Updating database.");
                        bkpStatement = conn.createStatement();
                        bkpStatement.executeUpdate("CREATE TABLE "+TABLE_NAME+"Backup SELECT * FROM "+TABLE_NAME);
                        bkpStatement.close();

    			String[] query;
    			if (HomeConfig.usemySQL) {
    				query = mysql.split(";");
    			} else { 
    				query = sqlite.split(";");
    			}

    			Statement sqlst = conn.createStatement();
    			for (String qry : query) {
    				sqlst.executeUpdate(qry);
    			}
    			conn.commit();
    			sqlst.close();
                        // Table modified, return true.
                        HomeLogger.info("DB was updated.");
                        return true;
    		} catch (SQLException exc) {
    			HomeLogger.severe("Failed to update the database to the new version - ", exc);
    			ex.printStackTrace();
                        return false;
    		}	
    	}
    }

    public static void updateFieldType(String field, String type) {
    	try {
    		// SQLite uses dynamic field typing so we dont need to process these.  
    		if (!HomeConfig.usemySQL) return;

    		HomeLogger.info("Updating database");

    		Connection conn = ConnectionManager.getConnection();
    		DatabaseMetaData meta = conn.getMetaData();

    		ResultSet colRS = null;
    		colRS = meta.getColumns(null, null, TABLE_NAME, null);
    		while (colRS.next()) {
    			String colName = colRS.getString("COLUMN_NAME");
    			String colType = colRS.getString("TYPE_NAME");

    			if (colName.equals(field) && !colType.equals(type))
    			{
    				Statement stm = conn.createStatement();
    				stm.executeUpdate("ALTER TABLE "+TABLE_NAME+" MODIFY " + field + " " + type + "; ");
    				conn.commit();
    				stm.close();
    				break;
    			}
    		}
    		colRS.close();
    	} catch(SQLException ex) {
    		HomeLogger.severe("Failed to update the database to the new version - ", ex);
    		ex.printStackTrace();
    	}
    }

    private static enum TableStatus {
        NONE_EXIST,
        OLD_ONLY,
        UP_TO_DATE;
    }
}
