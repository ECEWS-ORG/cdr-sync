/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openmrs.module.cdrsync.api.dao;

/**
 *
 * @author Nwokoma
 */

import java.sql.DriverManager;
import java.sql.*;
import java.util.Properties;
import java.util.logging.Level;
import org.openmrs.util.OpenmrsUtil;
import org.openmrs.api.context.Context;

/**
 * @author Nwokoma
 */
public class Database {
	
	//static DBConnection openmrsConn = Utils.getNmrsConnectionDetails();
	
	public static Connection conn = null;
	
	//public static Connection [] connPool = new Connection[10];
	//public static DBConnection openmrsConn2 = null;
	
	public static ConnectionPool connectionPool;
	
	public static void initConnection() {
		try {
			System.out.println("initing connection");
			Properties props;
			/*
			props = OpenmrsUtil.getRuntimeProperties("openmrs");
			if (props == null) {
				props = OpenmrsUtil.getRuntimeProperties("openmrs-standalone");
				
			}
			*/
			props = Context.getRuntimeProperties();
			
			String username = props.getProperty("connection.username");
			String password = props.getProperty("connection.password");
			String connectionUrl = props.getProperty("connection.url");
			
			connectionPool = new ConnectionPool("com.mysql.jdbc.Driver", connectionUrl, username, password, 1, 1, true);
			
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void closeConnection() {
		if (connectionPool != null) {
			connectionPool.closeAllConnections();
		}
	}
	
	public static void cleanUp(ResultSet rs, Statement stmt, Connection con) {
		try {
			Database.connectionPool.free(con);
			stmt.close();
			if (rs != null)
				rs.close();
			
		}
		catch (Exception ex) {
			handleException(ex);
		}
	}
	
	public static void handleException(Exception ex) {
		ex.printStackTrace();
	}
}
