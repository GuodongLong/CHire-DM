package DBConn;

import java.sql.CallableStatement;
import java.sql.ResultSet;

public interface IDBOperators {

	public ResultSet select(String sqlQueryStr);

	public boolean insert(String sqlInsertStr);

	public boolean update(String sqlUpdateStr);

	public boolean delete(String sqlDeleteStr);

	public boolean storedProcedure(String procedureStr);

	public boolean storedProcedure(CallableStatement procedure);

	public boolean execute(String sqlQueryStr);

}
