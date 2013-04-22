package Call;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import ThreadManagement.NotifyingThread;

import Base.Configuration;
import Base.log;
import DBConn.DataBaseOperators;
import DBConn.IDBOperators;

/**
 * A thread class response to finish one customer's mining task.
 * @author user
 *
 */
public class ThreadCall extends NotifyingThread {
	
	@Override
	public void doRun()
	{
		Configuration cfg = new Configuration("src/config.properties");
		int minSup = Integer.valueOf(cfg.getValue("minSup"));
		int maxLength = Integer.valueOf(cfg.getValue("maxLength"));
		int maxGap = Integer.valueOf(cfg.getValue("maxGap"));
		int upperBoundSup = Integer.valueOf(cfg.getValue("upper_bound_sup"));
//		String customerCode = "BLUE2000";
//		String branchCode = "HAST";
		 String customerCode = this.parameter.get("customer_code");//"THIE9516";
		 String branchCode = this.parameter.get("branch_code");//"WONT";
		 
		/* get input data file */
		String inputPath = "E:\\don\\CoatesHire\\alg\\hire_pattern_"
				+ branchCode + "_" + customerCode + "_input.csv";
//		log.write("Start to Prepare Input" + inputPath);
		try {
			getInput(inputPath, customerCode, branchCode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* prepare output file */
		String outputPath = "E:\\don\\CoatesHire\\alg\\hire_pattern_"
				+branchCode + "_" + customerCode + "_" + minSup + "_"
				+ maxLength + "_" + maxGap + "_" + upperBoundSup
				+ "_output.csv";

		/* prepare configure file */
		String confPath = "E:\\don\\CoatesHire\\alg\\config.txt";
//		log.write("Start to Prepare Conf");
		try {
			buildConf(confPath, minSup, maxLength, maxGap, upperBoundSup);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * call algorithm: input file, output file, configure file
		 */
//		log.write("Start to Mine Pattern");
		callAlgorithm("E:\\don\\CoatesHire\\alg\\SerialPattern.exe", inputPath,
				outputPath, confPath);

		/* output to database */
		try {
			outputToDatabase(outputPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.write("finish the thread and output " + outputPath);

	}

	/**
	 * 
	 CREATE TABLE `hp_patterns` ( `id` bigint(20) NOT NULL auto_increment,
	 * `branch` varchar(50), `customer` varchar(50), `length` int, `fleet1`
	 * varchar(200), `fleet2` varchar(200), `fleet3` varchar(200), `frequency`
	 * int, `accuracy` float, PRIMARY KEY (`id`) ) ENGINE=InnoDB
	 * AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;
	 * 
	 * @param resultPath
	 * @throws IOException
	 */
	public void outputToDatabase(String resultPath) throws IOException {
		IDBOperators dbOperator = new DataBaseOperators();
		String tableName = "hp_patterns";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(resultPath), "utf8"));
		String[] headers = br.readLine().split(",");
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] values = line.split(",");
			String branchCode = values[0];
			String customerCode = values[1];
			String pattern_length = values[2];
			String fleet1 = values[3];
			String fleet2 = values[4];
			String fleet3 = values[5];
			String support = values[6];
			String accuracy = values[7];
			String sqlInsertStr = "insert "
					+ tableName
					+ " (branch, customer, length, fleet1, fleet2, fleet3, frequency, accuracy)  "
					+ "values('" + branchCode + "','" + customerCode + "','"
					+ pattern_length + "','" + fleet1 + "','" + fleet2 + "','"
					+ fleet3 + "','" + support + "','" + accuracy + "');";
			boolean insertStatus = dbOperator.insert(sqlInsertStr);
//			if (insertStatus)
//				log.write("Successfully insert!");
//			else
//				log.write("Insert Error!");
//			log.write(line);
//			log.write(sqlInsertStr);
		}
		br.close();
	}

	public void getInput(String inputPath, String customerCode,
			String branchCode) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(inputPath), "utf8"));
		IDBOperators dbOperator = new DataBaseOperators();
		String sqlQueryStr = "select * from hp_trans_hist where branch_code='"
				+ branchCode + "' and customer_code = '" + customerCode + "' order by start_date asc";
		ResultSet rs = dbOperator.select(sqlQueryStr);
		try {
			while (rs.next()) {
				String line = rs.getString("branch_code").toUpperCase() + ","
						+ rs.getString("customer_code").toUpperCase() + ","
						+ rs.getString("start_date") + ","
						+ rs.getString("date_gap") + ","
						+ rs.getString("model").replace(',', ' ') + ","
						+ rs.getString("fld_Desc").replace(',', ' ') + ","
						+ rs.getString("category_desc").replace(',', ' ')
						+ "\n";
				bw.write(line);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		bw.flush();
		bw.close();
	}

	// public static void buildConf(String confPath, int minSup, int maxLength,
	// int maxGap, int upperBoundSup) throws IOException {
	// Configuration cfg = new Configuration();
	// cfg.setValue("minSup", String.valueOf(minSup));
	// cfg.setValue("maxLength", String.valueOf(maxLength));
	// cfg.setValue("maxGap", String.valueOf(maxGap));
	// cfg.setValue("upper_bound_sup", String.valueOf(upperBoundSup));
	// cfg.saveFile(confPath, "Config for Hire Pattern Mining");
	// }

	public void buildConf(String confPath, int minSup, int maxLength,
			int maxGap, int upperBoundSup) throws IOException {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(confPath), "utf8"));
		bw.write("#1\n");
		bw.write("#2\n");
		bw.write("minSup=" + minSup + "\n");
		bw.write("maxLength=" + maxLength + "\n");
		bw.write("maxGap=" + maxGap + "\n");
		bw.write("upper_bound_sup=" + upperBoundSup + "\n");
		bw.close();
	}

	public void callAlgorithm(String exepath, String inputPath,
			String outputPath, String confPath) {
		try {
			String line;
			// Process p = Runtime.getRuntime().exec("cmd /c dir");
			Process p = Runtime.getRuntime().exec(
					exepath + " " + inputPath + " " + outputPath + " "
							+ confPath);
			BufferedReader brInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			BufferedReader brError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));
			while ((line = brInput.readLine()) != null) {
				System.out.println(line);
			}
			brInput.close();
			while ((line = brError.readLine()) != null) {
				System.out.println(line);
			}
			brError.close();
			p.waitFor();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}
