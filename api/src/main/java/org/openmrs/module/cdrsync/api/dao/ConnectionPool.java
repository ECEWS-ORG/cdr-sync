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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Nwokoma
 */
public class ConnectionPool implements Runnable {
	
	private String driver, url, username, password;
	
	private int maxConnections;
	
	private boolean waitIfBusy;
	
	public Vector<Connection> availableConnections, busyConnections;
	
	private boolean connectionPending = false;
	
	private static HikariDataSource dataSource;
	
	public ConnectionPool(String driver, String url, String username, String password, int initialConnections,
	    int maxConnections, boolean waitIfBusy) throws SQLException {
		initializeDataSource(driver, url, username, password);
		
	}
	
	private void initializeDataSource(String driver, String url, String username, String password) {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(url);
		config.setUsername(username);
		config.setPassword(password);
		config.setDriverClassName(driver);
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		dataSource = new HikariDataSource(config);
	}
	
	public synchronized Connection getConnection() throws SQLException {
		return dataSource.getConnection();
		
	}
	
	@Override
	public void run() {
		try {
			Connection connection = makeNewConnection();
			//Connection connection = getConnection();
			synchronized (this) {
				availableConnections.addElement(connection);
				connectionPending = false;
				notifyAll();
			}
		}
		catch (Exception e) { // SQLException or OutOfMemory
			// Give up on new connection and wait for existing one
			// to free up.
			e.printStackTrace();
		}
	}
	
	/**
	 * // Method explicitly makes a new connection. Called in // the foreground when initializing
	 * the ConnectionPool, // and called in the background when running.
	 */
	private Connection makeNewConnection() throws SQLException {
		try {
			
			// Connection connection = ds.getConnection();
			
			return (DriverManager.getConnection(url + "", username, password));
		}
		catch (Exception cnfe) {
			// Simplify try/catch blocks of people using this by
			// throwing only one exception type.
			cnfe.printStackTrace();
			throw new SQLException("ConnectionPool:: SQLException encountered:: " + cnfe.getMessage());
		}
	}
	
	/**
	 * Method to free the Connections
	 */
	public synchronized void free(Connection connection) {
		try {
			if (connection != null) {
				connection.close();
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void closeAllConnections() {
		dataSource.close();
		
	}
	
	private void closeConnections(Vector<Connection> connections) {
		try {
			for (int i = 0; i < connections.size(); i++) {
				Connection connection = connections.elementAt(i);
				if (!connection.isClosed()) {
					connection.close();
				}
			}
		}
		catch (SQLException sqle) {
			sqle.printStackTrace();
			// Ignore errors; garbage collect anyhow
		}
	}
	
	public synchronized String toString() {
		return ("ConnectionPool(" + url + "," + username + ")" + ", available=" + availableConnections.size() + ", busy="
		        + busyConnections.size() + ", max=" + maxConnections);
	}
	
}
