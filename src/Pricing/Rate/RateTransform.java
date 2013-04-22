package Pricing.Rate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RateTransform {
	
	public static int MAX_TYPE_NO = 509;
	public static int MAX_DUR_NUM = 5;
	public static int MAX_ATTR_EXT = 3;
//	public static String folder = "E:\\GoolgeDriver\\Google ‘∆∂À”≤≈Ã\\CoatesHire\\Pricing\\CustomerSegmentation\\";
	public static String folder = "/home/gulong/workspace/CoatesHire/data/pricing/";
	public static List<RateRow> lstRows = new ArrayList<RateRow>();
	
	public static Boolean isNormalized = false;
	public static Boolean isFiltered   = false;
	
	public static void main(String[] args) throws IOException
	{
		readData();
		adjustRate();
		writeCSV();
		writeWeka();
	}
	public static void readData() throws IOException
	{
		String inputFile = folder + "pricing_clustering_rate.csv";
		
		FileReader fr = new FileReader(inputFile);
		BufferedReader br = new BufferedReader(fr);
		String header = br.readLine();
		String line   = null;
		RateRow currCust = null;
		while((line = br.readLine()) != null)
		{
			if (line.length() < 2)
			{
				continue;
			}
			String[] str = line.split(",");
			String custName = str[0];
			if (currCust == null || !currCust.custName.equalsIgnoreCase(custName))// a new customer
			{
				currCust = new RateRow(custName, MAX_TYPE_NO);
				lstRows.add(currCust);
			} 
			String type = str[1];
			int itype = Integer.valueOf(type) - 1;
			if(itype >= MAX_TYPE_NO)
			{
				System.out.println("itype " + itype + " exceed " + MAX_TYPE_NO);
				break;
			}
			for(int col = 0; col < MAX_DUR_NUM + MAX_ATTR_EXT; col++)
			{
				String rate = str[2 + col];
				double val = Double.valueOf(rate);
				currCust.add(val, itype, col);
			}
		}
		br.close();
		fr.close();

		System.out.println("read data success!");		
	}
	
	public static void adjustRate()
	{
		isNormalized = true;
		for(RateRow row : lstRows)
		{
			row.adjustRate();
		}
	}
	
	public static void writeCSV() throws IOException
	{
		String outputFile = folder + "rate_final.csv";
		
		FileWriter fw = new FileWriter(outputFile);
		fw.write("customer,");
		for(int i = 0; i < MAX_TYPE_NO; i++)
		{
			for(int j = 0; j < MAX_DUR_NUM; j++)
			{
				fw.write("type" + i + "rate_" + j + ",");
			}
		}
		fw.write("\n");
		for(RateRow row : lstRows)
		{
			fw.write(row.toString());
		}
		fw.close();
		
		System.out.println("write data success!");
	}
	
	public static void writeWeka() throws IOException
	{
		HashMap<Integer, String> subtypes = loadSubtypeList(folder + "pricing_clustering_rate_subtype_list.csv");
		String outputFile = folder + "wekaRate_final.arff";
		
		FileWriter fw = new FileWriter(outputFile);
		fw.write("@relation Customer_Revenue \n \n");
		for(int i = 0; i < MAX_TYPE_NO; i++)
		{
			for(int j = 0; j < MAX_DUR_NUM; j++)
			{
				fw.write("@attribute type" + i + "_rate" + j + " real \n");
			}
		}
		fw.write("@data \n");
		for(RateRow row : lstRows)
		{
			String content = row.toWekaString();
			if (content != null)
			{
				fw.write("{");
				fw.write(content);
				fw.write("}\n");
			}
		}
		fw.close();
		
		System.out.println("write data success!");
	}
	
	public static HashMap<Integer, String> loadSubtypeList(String fileName) throws IOException
	{
		HashMap<Integer, String> hs = new HashMap<Integer, String>();
		File file = new File(fileName);
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		while((line = br.readLine()) != null)
		{
			if (line.length() < 2)
			{
				continue;
			}
			String[] str = line.split(",");
			int key = Integer.valueOf(str[0]) - 1;
			String value = str[1];
			hs.put(key, value);
		}
		br.close();
		fr.close();
		return hs;
	}

}
