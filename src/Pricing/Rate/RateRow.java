package Pricing.Rate;

public class RateRow {

	public String custName;
	public int rowSize = 0;
	public double[][] lstRate;
	public double subtype_weight;
	public double avg_daily_rate;
	public double avg_weekly_rate;
	

	public RateRow(String name, int size)
	{
		this.custName = name;
		this.rowSize = size;
		this.lstRate = new double[size][RateTransform.MAX_DUR_NUM + RateTransform.MAX_ATTR_EXT];
		for(int i = 0; i < size; i++)
		{
			for(int j = 0; j < RateTransform.MAX_DUR_NUM + RateTransform.MAX_ATTR_EXT; j++)
			{
				lstRate[i][j] = 0;
			}
		}
	}
	
	public void add(double revenue, int rowIdx, int colIdx)
	{
		lstRate[rowIdx][colIdx] = revenue;
	}
	
//	public void normalize()
//	{
//		double cnt = 0;
//		for(int i = 0; i < lstRevenue.length; i++)
//		{
//			cnt += lstRevenue[i];
//		}
//		for(int i = 0; i < lstRevenue.length; i++)
//		{
//			lstRevenue[i] /= cnt; 
//		}
//	}
	
	public static int MAX_INT = 100000000;
	public void adjustRate()
	{
		for(int i = 0; i < this.rowSize; i++)
		{
			double weight = lstRate[i][RateTransform.MAX_DUR_NUM];
			double min_rate = MAX_INT;
			for(int j = 0; j < RateTransform.MAX_DUR_NUM; j++)
			{
				double currRate = lstRate[i][j];
				if (currRate == 0) // fill in average rate
				{
					if (j == 0) // daily_rate
					{
						double avg_rate = lstRate[i][RateTransform.MAX_DUR_NUM + 1];
						lstRate[i][j] = weight * Math.min(avg_rate, min_rate);
					}
					else  // weekly_rate and above duration
					{
						double avg_rate = lstRate[i][RateTransform.MAX_DUR_NUM + 2];
						lstRate[i][j] = weight * Math.min(avg_rate, min_rate);
					}
				}
				else // real rate
				{
					min_rate = Math.min(currRate, min_rate);
					lstRate[i][j] = weight * currRate;
				}
			}
		}
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(custName);
		sb.append(",");
		for(int i = 0; i < this.rowSize; i++)
		{
			for (int j = 0; j < RateTransform.MAX_DUR_NUM; j++)
			{
				sb.append(String.valueOf(lstRate[i][j]));
				sb.append(",");
			}
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public String toWekaString()
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < this.rowSize; i++)
		{
			for (int j = 0; j < RateTransform.MAX_DUR_NUM; j++)
			{
				double rev = lstRate[i][j];
				if (rev == 0)
				{
					continue;
				}
				int attrIdx = i * RateTransform.MAX_DUR_NUM + j;
				sb.append(attrIdx + " " + rev + ",");
			}
		}
	    if (lstRate.length == 0)
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
