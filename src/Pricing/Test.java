package Pricing;

import java.util.ArrayList;

public class Test {

	public static void main(String[] args)
	{
	}
	
	public Test()
	{
		Node node = new Node("A");
	}
	
	public class Node
	{
		String name;
		ArrayList<Link> links = new ArrayList<Link>();
		public Node(String str)
		{
			name = str;
		}
	}
	
	public class Link
	{
		Node node1, node2;
		double rate;
	}
	
	public class Tree
	{
		ArrayList<Link> links = new ArrayList();
		ArrayList<Node> nodes = new ArrayList();
		
	}
	
	public class Forest
	{
		ArrayList<Tree> trees = new ArrayList();
	}
}
