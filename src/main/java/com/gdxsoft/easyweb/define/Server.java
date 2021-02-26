package com.gdxsoft.easyweb.define;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.gdxsoft.easyweb.utils.UPath;


public class Server {

	public static boolean checkServer() {
		Connection con = createConnection();
		if (con == null)
			return false;
		try {
			con.getMetaData();
			con.close();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	private static Connection createConnection() {
		String dbPassword=UPath.getSystemDbPassword();
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			Connection conn = DriverManager.getConnection(
					"jdbc:hsqldb:hsql://localhost/", "sa", dbPassword);
			return conn;
		} catch (ClassNotFoundException e) {
			return null;
		} catch (SQLException e) {
			return null;
		}
	}

	public static void shutdown()   {
		Connection con = createConnection();
		Statement st;
		try {
			st = con.createStatement();
			st.execute("SHUTDOWN");
			con.close(); // if there are no other open connection
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void start(){
		if(Server.checkServer()){
			return;
		}
		final String db = UPath.getSystemDbPath();
		Thread server = new Thread() {
            public void run() {
                String[] args = { "-database", db,
                    "-no_system_exit", "true" };
                org.hsqldb.Server.main(args);
            }
        };
        server.run();

	}

	public static void main(String[] args){
		if(!Server.checkServer()){
			start();
		}
	}
}  

