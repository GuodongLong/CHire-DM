package Call;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ThreadManagement.Parameter;
import ThreadManagement.ThreadMain;

import Base.Configuration;
import Base.log;
import DBConn.DataBaseOperators;
import DBConn.IDBOperators;

public class CallMain {
	
	public static int min_customer_hire_number = 1000;
	public static int max_active_thread_number = 5;
	
	public static void main(String[] args)
	{
		Configuration cfg = new Configuration("src/config.properties");
		min_customer_hire_number = Integer.valueOf(cfg.getValue("min_customer_hire_number"));
		max_active_thread_number = Integer.valueOf(cfg.getValue("max_active_thread_number"));
		
		List<Parameter> paras = getBigCustomers("", min_customer_hire_number);

		log.write("min_customer_hire_number=" + min_customer_hire_number);
		log.write("max_active_thread_number=" + max_active_thread_number);
		
		/* clear the old patterns; */
		clearPatterns();
		
		/* start multi-thread for mining the pattern */
		ThreadMain tm = new ThreadMain(paras.size(), max_active_thread_number, ThreadCall.class.getName(), paras);
		tm.execute();
	}
	
	/**
	 * Clear the hire patterns.
	 */
	public static void clearPatterns()
	{
		IDBOperators dbOperator = new DataBaseOperators();
		String sqlQueryStr = "truncate table hp_patterns;";
		dbOperator.execute(sqlQueryStr);
	}
	
	
	/**
	 * Get all big customers who have numerous hire transaction.
	 * @param inputPath
	 * @param min_num
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public static List<Parameter> getBigCustomers(String inputPath, int min_num)
	{
		List<Parameter> lstParas = new ArrayList<Parameter>();
		IDBOperators dbOperator = new DataBaseOperators();
		String sqlQueryStr = "select * from ( " +
				"select branch_code, customer_code, count(*) as cnt from hp_trans_hist group by branch_code, customer_code) as b " + 
				"where b.customer_code not like '%CASH%' AND b.cnt > " + min_num + " order by cnt desc";
		ResultSet rs = dbOperator.select(sqlQueryStr);
		try {
			while (rs.next()) {
				String customer_code = rs.getString("customer_code");
				String branch_code =  rs.getString("branch_code");
				String cnt = rs.getString("cnt");
				Parameter parameter = new Parameter();
				parameter.addParameter("customer_code", customer_code.toUpperCase());
				parameter.addParameter("branch_code", branch_code.toUpperCase());
//				log.write("customer_code=" + customer_code+ ",branch_code=" + branch_code + ",cnt=" + cnt);
				lstParas.add(parameter);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		log.write("Total " + lstParas.size() + " big customers are selected!");
		
		return lstParas;
	}
}
