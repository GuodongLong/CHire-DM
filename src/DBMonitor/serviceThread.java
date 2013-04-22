package DBMonitor;

import ThreadManagement.ThreadMain;
import ThreadManagement.ThreadTemplate;
import DataSplit.SplitMain;

public class serviceThread extends Thread {

	public static boolean stopFlag = false;
	public static long    waitMillSec = 100 * 1000; // 100s
	
	@Override
	public final void run() 
	{
		
		while(!stopFlag)
		{
			if (!checkRenewFlag())
			{
				try {
					wait(waitMillSec);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			
		}
	}
	
	public boolean checkRenewFlag()
	{
		boolean isRenewed = false;
		
		String strSQL = "SELECT * FROM data_extraction_history WHERE isNew=1";
		String retVal = "";//DBFunc.getString(strSQL);
		if (retVal.equalsIgnoreCase("1"))
		{
			isRenewed = true;
		}
		
		return isRenewed;
	}
	
	public void execute()
	{
		// split data
		SplitMain spm = new SplitMain();
		if (spm.split("", ""))
		{
			System.out.println("Split data file is error! Please check it! ");
			return;
		}
		
		// call thread to consume the splitted data.
		ThreadMain tm = new ThreadMain(10, 5, ThreadTemplate.class.getName());
		tm.execute();
	}

}
