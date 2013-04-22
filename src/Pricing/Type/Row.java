package Pricing.Type;

import java.util.ArrayList;
import java.util.List;

public class Row {

	public String custName;
	public double[] lstRevenue;
	
	public Row(String name, int size)
	{
		this.custName = name;
		this.lstRevenue = new double[size];
		for(int i = 0; i < size; i++)
		{
			lstRevenue[i] = 0;
		}
	}
	
	public void add(double revenue, int idx)
	{
		lstRevenue[idx] = revenue;
	}
	
	public void normalize()
	{
		double cnt = 0;
		for(int i = 0; i < lstRevenue.length; i++)
		{
			cnt += lstRevenue[i];
		}
		for(int i = 0; i < lstRevenue.length; i++)
		{
			lstRevenue[i] /= cnt; 
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(custName);
		sb.append(",");
		for(int i = 0; i < lstRevenue.length; i++)
		{
			sb.append(String.valueOf(lstRevenue[i]));
			sb.append(",");
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public String toWekaString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < lstRevenue.length; i++)
		{
			double rev = lstRevenue[i];
			if (rev == 0)
			{
				continue;
			}
			sb.append(i + " " + rev + ",");
		}
	    if (lstRevenue.length == 0)
	    {
	    	System.out.println("lstRevenue.length==0");
	    	return "";
	    }
		String str = sb.toString();
	    if (str.length() == 0)
	    {
	    	System.out.println("str.length==0" + str);
	    	return null;
	    }
		str = str.substring(0, str.length() - 1);
		return str;
	}
}
