package ThreadManagement;

import java.util.ArrayList;
import java.util.List;

public class ThreadMain implements ThreadCompleteListener{
	
	/**
	 * Maximal active thread number. 
	 */
	public int maxActiveThreadNum = 5;
	
	/**
	 * Total required thread number.
	 */
	public int maxCreatedThreadNum = 0;
	
	/**
	 * Total created thread number.
	 */
	public int createdThreadNum = 0;
	
	/**
	 * Current active thread number.
	 */
	public int activeThreadNum = 0;
	
	/**
	 * the name of thread class.
	 */
	public String className = "";
	
	/**
	 * The parameters for thread.
	 */
	public List<Parameter> parameters = null;
	
	public ThreadMain(int threadNum, String newClassName)
	{
		init(threadNum, 0, newClassName, null);
	}
	
	public ThreadMain(int threadNum, int maxActiveThNum, String newClassName)
	{
		init(threadNum, maxActiveThNum, newClassName, null);
	}
	
	public ThreadMain(int threadNum, int maxActiveThNum, String newClassName, List<Parameter> paras)
	{
		init(threadNum, maxActiveThNum, newClassName, paras);
	}

	public void init(int threadNum, int maxActiveThNum, String newClassName, List<Parameter> paras)
	{
		if (threadNum > 0)
		{
			this.maxCreatedThreadNum = threadNum;
		}
		if (maxActiveThNum > 0)
		{
			this.maxActiveThreadNum  = maxActiveThNum;
		}
		
		this.regClass(newClassName);
		
		if (paras != null)
		{
			parameters = new ArrayList<Parameter>();
			for(Parameter para: paras)
			{
				Parameter p = new Parameter(para);
				parameters.add(para);
			}
		}
	}
	/**
	 * execute the threads management by initial a set of threads, and the thread number should less than maxActiveThreadNum.
	 * The left threads will be created while one of active thread is completed.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public void execute()
	{
		int initThreadNum = Math.min(maxCreatedThreadNum, maxActiveThreadNum);
		
		for (int i = 0; i < initThreadNum; i++)
		{
			createSingleThread(parameters.get(i));
		}
	}
	
	/**
	 * Create single thread with the registered class
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public synchronized void createSingleThread(Parameter parameter)
	{
		try
		{
			Class theClass = Class.forName(className, true, this.getClass().getClassLoader());
			NotifyingThread thread1 = (NotifyingThread) theClass.newInstance();
			thread1.addListener(this); // add ourselves as a listener
			thread1.regParameter(parameter); // transfer the parameters into thread
			thread1.start();           // Start the Thread
			this.incrCreatedThreadNum();
			this.incrActiveThreadNum();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void regClass(String newClassName)
	{
		System.out.println("The registered thread class " + this.className + " is changed to new class " + newClassName);
		this.className = newClassName;
	}
	
	/**
	 * this function will be called while one thread is completing.
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws  
	 */
	@Override
	public synchronized void notifyOfThreadComplete(Thread thread){
		// TODO Auto-generated method stub
		this.decrActiveThreadNum();
		System.out.println("Finish thread " + thread.getId());
		if (this.getCreatedThreadNum() < this.maxCreatedThreadNum)
		{
			createSingleThread(this.parameters.get(this.getCreatedThreadNum()));
		}
		System.out.println("Total thread number: " + this.getCreatedThreadNum() + ", active thread number: "+ this.getActiveThreadNum());
	}
	

	private synchronized boolean incrCreatedThreadNum()
	{
		createdThreadNum++;
		return true;
	}

	private synchronized boolean incrActiveThreadNum()
	{
		activeThreadNum++;
		return true;
	}
	
	private synchronized boolean decrActiveThreadNum()
	{
		activeThreadNum--;
		return true;
	}
	
	public synchronized int getCreatedThreadNum()
	{
		return createdThreadNum;
	}

	public synchronized int getActiveThreadNum()
	{
		return activeThreadNum;
	}
}
