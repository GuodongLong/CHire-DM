package DBConn;

import java.sql.ResultSet;
import java.sql.SQLException;

import Base.log;

public class Test {

	// public void select() {
	// String sql = "SELECT * FROM algorithm;";
	// try {
	// Connection con = new SimpleConnetion().getConnection();
	// PreparedStatement stm = con.prepareStatement(sql);
	//
	// ResultSet rs = stm.executeQuery();
	// while (rs.next()) {
	// log.write(rs.getString("description"));
	// }
	// stm.close();
	// con.close();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public void insert() {
	// String sql =
	// "insert algorithm (description,input,output,name,parameter)  values(?,?,?,?,?);";
	//
	// try {
	// Connection con = new SimpleConnetion().getConnection();
	// PreparedStatement stm = con.prepareStatement(sql);
	//
	// stm.setString(1, "desc");
	// stm.setString(2, "input");
	// stm.setString(3, "output");
	// stm.setString(4, "name");
	// stm.setString(5, "parameter");
	// stm.execute();
	//
	// stm.close();
	// con.close();
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	//
	// }
	// }

	public static void main(String[] args) {

		IDBOperators dbOperator = new DataBaseOperators();
		// test select
		String sqlQueryStr = "SELECT * FROM algorithm;";
		ResultSet rs = dbOperator.select(sqlQueryStr);
		try {
			while (rs.next()) {
				log.write(rs.getString("description"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// test insert
		String sqlInsertStr = "insert algorithm (description,input,output,name,parameter)  "
				+ "values('Insert from script','inputScript','outputScript','Script','parameterScript');";
		boolean insertStatus = dbOperator.insert(sqlInsertStr);
		if (insertStatus)
			log.write("Successfully insert!");
		else
			log.write("Insert Error!");

		// test update
		String sqlUpdateStr = "update algorithm set description = 'update from script'"
				+ "where description = 'Insert from script';";
		boolean updateStatus = dbOperator.update(sqlUpdateStr);
		if (updateStatus)
			log.write("Successfully update!");
		else
			log.write("Update Error!");

		// test delete
		String sqlDeleteStr = "delete from algorithm where description = 'update from script';";
		boolean deleteStatus = dbOperator.delete(sqlDeleteStr);
		if (deleteStatus)
			log.write("Successfully delete!");
		else
			log.write("Delete Error!");

	}

}
