package ThreadManagement;

public class TestMain {


	public static void main(String[] args)
	{
		ThreadMain tm = new ThreadMain(10, 5, ThreadTemplate.class.getName());
		tm.execute();
	}
}
