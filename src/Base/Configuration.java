package Base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

	private Properties propertie;
	private FileInputStream inputFile;
	private FileOutputStream outputFile;

	public Configuration() {
		propertie = new Properties();
	}

	public Configuration(String filePath) {
		propertie = new Properties();
		try {
			inputFile = new FileInputStream(filePath);
			propertie.load(inputFile);
			inputFile.close();
		} catch (FileNotFoundException ex) {
			System.out.println("读取属性文件--->失败！- 原因：文件路径错误或者文件不存在");
			ex.printStackTrace();
		} catch (IOException ex) {
			System.out.println("装载文件--->失败!");
			ex.printStackTrace();
		}
	}// end ReadConfigInfo(...)

	public String getValue(String key) {
		if (propertie.containsKey(key)) {
			String value = propertie.getProperty(key);// 得到某一属性的值
			return value;
		} else
			return "";
	}// end getValue(...)

	public String getValue(String fileName, String key) {
		try {
			String value = "";
			inputFile = new FileInputStream(fileName);
			propertie.load(inputFile);
			inputFile.close();
			if (propertie.containsKey(key)) {
				value = propertie.getProperty(key);
				return value;
			} else
				return value;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "";
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}// end getValue(...)

	public void clear() {
		propertie.clear();
	}// end clear();

	public void setValue(String key, String value) {
		propertie.setProperty(key, value);
	}// end setValue(...)

	public void saveFile(String fileName, String description) {
		try {
			outputFile = new FileOutputStream(fileName);
			propertie.store(outputFile, description);
			outputFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}// end saveFile(...)

	public static void main(String[] args) {
		Configuration rc = new Configuration("src/config.properties");// 相对路径

		String ip = rc.getValue("ip");// 以下读取properties文件的值
		String database = rc.getValue("database");
		String username = rc.getValue("username");
		String password = rc.getValue("password");
		String endpoint = rc.getValue("endpoint");

		// rc.modifyFile(ip,database,username,password);

		System.out.println("ip = " + ip);// 以下输出properties读出的值
		System.out.println("ip's length = " + ip.length());
		System.out.println("database = " + database);
		System.out.println("username = " + username);
		System.out.println("password = " + password);
		System.out.println("password = " + endpoint);

	}// end main()

	public void modifyFile(String ip, String database, String username,
			String password) {

		Configuration rc_back = new Configuration("src/config_back.properties");// 相对路径

		rc_back.setValue("ip", ip);
		rc_back.setValue("database", database);
		rc_back.setValue("username", username);
		rc_back.setValue("password", password);

		rc_back.saveFile("src/config_back.properties", "back");
	}

}// end class ReadConfigInfo

