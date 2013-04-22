package Base;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class log {

	static Logger logger = Logger.getLogger(log.class.getName());

	private static boolean isInit = false;
	
	private static void init() {
		PropertyConfigurator.configure("src/log4j.properties");
		isInit = true;
	}
	
	/**
	 * write log
	 * @param str
	 */
	public static void write(String str)
	{
		if (!isInit)
		{
			init();
		}
		logger.info(str);
	}
	

	/**
	 * write log with the given type or priority
	 * @param message
	 * @param type
	 */
	public static void write(String message, logType type)
	{
		if (!isInit)
		{
			init();
		}
		
		if (type.equals(logType.info))
		{
			logger.info(message);
		}
		else if (type.equals(logType.error))
		{
			logger.error(message);
		}
		else if (type.equals(logType.fatal))
		{
			logger.fatal(message);
		}
		else
		{
			logger.error("Error log write " + type.toString() + " with message: " + message);
		}
	}
	
	/**
	 * priority of log
	 * @author user
	 *
	 */
	enum logType 
	{
		error,
		fatal,
		info
	}

	public static void main(String[] args) {

		log.write("hello zhenxing");
//		logger.error("error - Entering application.");
//		logger.fatal("fatal - Entering application.");
//		System.out.println("log information!");
//		logger.info("info - Exiting application.");
	}
}
