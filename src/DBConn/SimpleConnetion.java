package DBConn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import Base.Configuration;

public class SimpleConnetion {

	private static String m_dbtype;
	private static String m_database;
	private static String m_ip;
	private static String m_port;
	private static String m_user;
	private static String m_password;

	public static void init() {
		Configuration cfg = new Configuration("src/config.properties");// Ïà¶ÔÂ·¾¶

		m_dbtype = cfg.getValue("dbtype");
		
		m_user = cfg.getValue("username_" + m_dbtype);
		m_password = cfg.getValue("password_" + m_dbtype);
		m_database = cfg.getValue("database_" + m_dbtype);
		m_ip = cfg.getValue("ip_" + m_dbtype);
		m_port = cfg.getValue("port_" + m_dbtype);
	}

	private static Connection con = null;
	public static Connection getConnection() {
		try {
			if (con == null)
			{
				SimpleConnetion.init();
				String m_url = "jdbc:" + m_dbtype + "://" + m_ip + ":" + m_port + "/" + m_database
					+ "?useunicode=true&characterEncoding=utf8";
				con = DriverManager.getConnection(m_url, m_user, m_password);

			}
			return con;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
