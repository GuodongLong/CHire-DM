package DBConn;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseOperators implements IDBOperators {

	@Override
	public boolean insert(String sqlInsertStr) {
		try {

			Connection con = SimpleConnetion.getConnection();
			PreparedStatement stm = con.prepareStatement(sqlInsertStr);
			stm.execute();
			stm.close();
//			con.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean update(String sqlUpdateStr) {
		try {

			Connection con = SimpleConnetion.getConnection();
			PreparedStatement stm = con.prepareStatement(sqlUpdateStr);
			int status = stm.executeUpdate();
			stm.close();
//			con.close();
			if (status > 0)
				return true;
			else
				return false;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean delete(String sqlDeleteStr) {
		try {

			Connection con = SimpleConnetion.getConnection();
			PreparedStatement stm = con.prepareStatement(sqlDeleteStr);
			stm.execute();
			stm.close();
//			con.close();
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public ResultSet select(String sqlQueryStr) {
		try {
			Connection con = SimpleConnetion.getConnection();
			PreparedStatement stm = con.prepareStatement(sqlQueryStr);
			ResultSet rs = stm.executeQuery();
			return rs;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	

	@Override
	public boolean execute(String sqlQueryStr) {
		try {
			Connection con = SimpleConnetion.getConnection();
			PreparedStatement stm = con.prepareStatement(sqlQueryStr);
			boolean b = stm.execute();
			return b;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean storedProcedure(String procedureStr) {
		try {
			Connection con = SimpleConnetion.getConnection();
			CallableStatement calls = con.prepareCall(procedureStr);
			calls.execute();
			// boolean status = calls.getBoolean("status");
			// return status;
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	 
	@Override
	public boolean storedProcedure(CallableStatement procedure) {
		try {
			procedure.execute();
			// boolean status = calls.getBoolean("status");
			// return status;
			return true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

}
