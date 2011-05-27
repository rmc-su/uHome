package me.taylorkelly.myhome;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WarpDataSource {

    public final static String DATABASE = "jdbc:sqlite:homes-warps.db";
    private final static String HOME_TABLE = "CREATE TABLE IF NOT EXISTS `homeTable` (" + "`id` INTEGER PRIMARY KEY," + "`name` varchar(32) NOT NULL DEFAULT 'Player',"
            + "`world` varchar(32) NOT NULL DEFAULT '0'," + "`x` DOUBLE NOT NULL DEFAULT '0'," + "`y` tinyint NOT NULL DEFAULT '0',"
            + "`z` DOUBLE NOT NULL DEFAULT '0'," + "`yaw` smallint NOT NULL DEFAULT '0'," + "`pitch` smallint NOT NULL DEFAULT '0',"
            + "`publicAll` boolean NOT NULL DEFAULT '0'," + "`permissions` varchar(150) NOT NULL DEFAULT '',"
            + "`welcomeMessage` varchar(100) NOT NULL DEFAULT ''" + ");";

    public static void initialize() {
    	if (!tableExists()) {
    		createTable();
    	}
    	dbTblCheck();
    }

    public static HashMap<String, Home> getMap() {
    	HashMap<String, Home> ret = new HashMap<String, Home>();
    	Statement statement = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		statement = conn.createStatement();
    		set = statement.executeQuery("SELECT * FROM homeTable");
    		int size = 0;
    		while (set.next()) {
    			size++;
    			int index = set.getInt("id");
    			String name = set.getString("name");
    			String world = set.getString("world");
    			double x = set.getDouble("x");
    			int y = set.getInt("y");
    			double z = set.getDouble("z");
    			int yaw = set.getInt("yaw");
    			int pitch = set.getInt("pitch");
    			boolean publicAll = set.getBoolean("publicAll");
    			String permissions = set.getString("permissions");
    			String welcomeMessage = set.getString("welcomeMessage");
    			Home warp = new Home(index, name, world, x, y, z, yaw, pitch, publicAll, permissions, welcomeMessage);
    			ret.put(name, warp);
    		}
    		HomeLogger.info("[MYHOME]: " + size + " homes loaded");
    	} catch (SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Home Load Exception");
    	} finally {
    		try {
    			if (statement != null) {
    				statement.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("[MYHOME]: Home Load Exception (on close)");
    		}
    	}
    	return ret;
    }

    private static boolean tableExists() {
    	ResultSet rs = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		DatabaseMetaData dbm = conn.getMetaData();
    		rs = dbm.getTables(null, null, "homeTable", null);
    		if (!rs.next()) {
    			return false;
    		}
    		return true;
    	} catch (SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Table Check Exception", ex);
    		return false;
    	} finally {
    		try {
    			if (rs != null) {
    				rs.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("[MYHOME]: Table Check SQL Exception (on closing)");
    		}
    	}
    }

    private static void createTable() {
    	Statement st = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		st = conn.createStatement();
    		st.executeUpdate(HOME_TABLE);
    		conn.commit();
    	} catch (SQLException e) {
    		HomeLogger.severe("[MYHOME]: Create Table Exception", e);
    	} finally {
    		try {
    			if (st != null) {
    				st.close();
    			}
    		} catch (SQLException e) {
    			HomeLogger.severe("[MYHOME]: Could not create the table (on close)");
    		}
    	}
    }

    public static void addWarp(Home warp) {
    	PreparedStatement ps = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		ps = conn.prepareStatement("INSERT INTO homeTable (id, name, world, x, y, z, yaw, pitch, publicAll, permissions, welcomeMessage) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
    		ps.setInt(1, warp.index);
    		ps.setString(2, warp.name);
    		ps.setString(3, warp.world);
    		ps.setDouble(4, warp.x);
    		ps.setInt(5, warp.y);
    		ps.setDouble(6, warp.z);
    		ps.setInt(7, warp.yaw);
    		ps.setInt(8, warp.pitch);
    		ps.setBoolean(9, warp.publicAll);
    		ps.setString(10, warp.permissionsString());
    		ps.setString(11, warp.welcomeMessage);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Home Insert Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("[MYHOME]: Home Insert Exception (on close)", ex);
    		}
    	}
    }

    public static void deleteWarp(Home warp) {
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();

    		ps = conn.prepareStatement("DELETE FROM homeTable WHERE id = ?");
    		ps.setInt(1, warp.index);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Home Delete Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("[MYHOME]: Home Delete Exception (on close)", ex);
    		}
    	}
    }

    public static void publicizeWarp(Home warp, boolean publicAll) {
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE homeTable SET publicAll = ? WHERE id = ?");
    		ps.setBoolean(1, publicAll);
    		ps.setInt(2, warp.index);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Home Publicize Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("[MYHOME]: Home Publicize Exception (on close)", ex);
    		}
    	}
    }

    public static void updatePermissions(Home warp) {
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE homeTable SET permissions = ? WHERE id = ?");
    		ps.setString(1, warp.permissionsString());
    		ps.setInt(2, warp.index);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Home Permissions Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("[MYHOME]: Home Permissions Exception (on close)", ex);
    		}
    	}
    }

    public static void moveWarp(Home warp) {
    	PreparedStatement ps = null;
    	ResultSet set = null;
    	
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		ps = conn.prepareStatement("UPDATE homeTable SET x = ?, y = ?, z = ?, world = ?, yaw = ?, pitch = ? WHERE id = ?");
    		ps.setDouble(1, warp.x);
    		ps.setInt(2, warp.y);
    		ps.setDouble(3, warp.z);
    		ps.setString(4, warp.world);
    		ps.setInt(5, warp.yaw);
    		ps.setDouble(6, warp.pitch);
    		ps.setInt(7, warp.index);
    		ps.executeUpdate();
    		conn.commit();
    	} catch (SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Home Move Exception", ex);
    	} finally {
    		try {
    			if (ps != null) {
    				ps.close();
    			}
    			if (set != null) {
    				set.close();
    			}
    		} catch (SQLException ex) {
    			HomeLogger.severe("[MYHOME]: Home Move Exception (on close)", ex);
    		}
    	}
    }

    public static void dbTblCheck() {
    	// Add future modifications to the table structure here
    	
    }

    public static void updateDB(String test, String sql) {
    	// Use same sql for both mysql/sqlite
    	updateDB(test, sql, sql);
    }

    public static void updateDB(String test, String sqlite, String mysql) {
    	// Allowing for differences in the SQL statements for mysql/sqlite.
    	try {
    		Connection conn = ConnectionManager.getConnection();
    		Statement statement = conn.createStatement();
    		statement.executeQuery(test);
    		statement.close();
    	} catch(SQLException ex) {
    		HomeLogger.info("[MYHOME]: Updating database");
    		// Failed the test so we need to execute the updates
    		try {
    			String[] query;
    			if (HomeSettings.usemySQL) {
    				query = mysql.split(";");
    			} else { 
    				query = sqlite.split(";");
    			}

    			Connection conn = ConnectionManager.getConnection();
    			Statement sqlst = conn.createStatement();
    			for (String qry : query) {
    				sqlst.executeUpdate(qry);
    			}
    			conn.commit();
    			sqlst.close();
    		} catch (SQLException exc) {
    			HomeLogger.severe("[MYHOME]: Failed to update the database to the new version - ", exc);
    			ex.printStackTrace();
    		}	
    	}
    }

    public static void updateFieldType(String field, String type) {
    	try {
    		if (!HomeSettings.usemySQL) return;
    		HomeLogger.info("[MYHOME]: Updating database");
    		
    		Connection conn = ConnectionManager.getConnection();
    		DatabaseMetaData meta = conn.getMetaData();

    		ResultSet colRS = null;
    		colRS = meta.getColumns(null, null, "homeTable", null);
    		while (colRS.next()) {
    			String colName = colRS.getString("COLUMN_NAME");
    			String colType = colRS.getString("TYPE_NAME");
    			
    			if (colName.equals(field) && !colType.equals(type))
    			{
    				Statement stm = conn.createStatement();
    				stm.executeUpdate("ALTER TABLE homeTable MODIFY " + field + " " + type + "; ");
    				conn.commit();
    				stm.close();
    				break;
    			}
    		}
    		colRS.close();
    	} catch(SQLException ex) {
    		HomeLogger.severe("[MYHOME]: Failed to update the database to the new version - ", ex);
    		ex.printStackTrace();
    	}
    }
}
