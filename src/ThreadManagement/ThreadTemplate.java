package ThreadManagement;

import java.util.Random;

public class ThreadTemplate extends NotifyingThread {

	@Override
	public void doRun() {
		// TODO Auto-generated method stub
		for (int i = 0; i < 3; i++)
		{
			int t = new Random(10).nextInt();
			if (t <= 0){ t = 1;}
			t = t * 1000;
			try {
				sleep(t);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
	}

	
}
