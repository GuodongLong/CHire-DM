package ThreadManagement;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class NotifyingThread extends Thread {

	  private final Set<ThreadCompleteListener> listeners = new CopyOnWriteArraySet<ThreadCompleteListener>();
	  protected Parameter parameter = null;
	  
	  public final void addListener(final ThreadCompleteListener listener) 
	  {
	      listeners.add(listener);
	  }
	  
	  public final void removeListener(final ThreadCompleteListener listener) 
	  {
	      listeners.remove(listener);
	  }
	  
	  public final void regParameter(Parameter para)
	  {
		  parameter = new Parameter(para);
	  }
	  
	  private final void notifyListeners() 
	  {
	      for (ThreadCompleteListener listener : listeners) 
	      {
	          listener.notifyOfThreadComplete(this);
	      }
	  }
	  
	  @Override
	  public final void run() 
	  {
	      try 
	      {
	          doRun();
	      } 
	      finally 
	      {
	          notifyListeners();
	      }
	  }
	  
	  public abstract void doRun();
}
