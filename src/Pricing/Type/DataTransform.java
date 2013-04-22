package Pricing.Type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DataTransform {
	
	public static int MAX_TYPE_NO = 518;
//	public static String folder = "E:\\GoolgeDriver\\Google ‘∆∂À”≤≈Ã\\CoatesHire\\Pricing\\CustomerSegmentation\\";
	public static String folder = "/home/gulong/workspace/CoatesHire/data/pricing/";
	public static List<Row> lstRows = new ArrayList<Row>();
	
	public static Boolean isNormalized = false;
	public static Boolean isFiltered   = false;

	public static HashMap<String, Integer> subtypes = new HashMap<String, Integer>(1000);
	
	public static void main(String[] args) throws IOException
	{
		readData();
		normalizeData();
		writeCSV();
		writeWeka();
	}
	public static void readData() throws IOException
	{
		String inputFile = folder + "Customer_Type_Revenue.csv";
		
		FileReader fr = new FileReader(inputFile);
		BufferedReader br = new BufferedReader(fr);
		String header = br.readLine(); // 1st line is the table header
		String line   = null;
		Row currCust = null;
		int typeId = 0;
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
				currCust = new Row(custName, MAX_TYPE_NO);
				lstRows.add(currCust);
			} 
			String type = str[1];
			if (subtypes.get(type) == null)
			{
				subtypes.put(type, typeId++);
			}
			int itype = subtypes.get(type);
			if(itype > MAX_TYPE_NO)
			{
				System.out.println("itype " + itype + " exceed 200");
				break;
			}
			String revenue = str[2];
			double drevenue = Double.valueOf(revenue);
			currCust.add(drevenue, itype);
		}
		br.close();
		fr.close();

		System.out.println("read data success! total subtype is " + typeId);		
	}
	
	public static void normalizeData()
	{
		isNormalized = true;
		for(Row row : lstRows)
		{
			row.normalize();
		}
	}
	
	public static void writeCSV() throws IOException
	{
		String outputFile = folder + "data_fi(" + isFiltered + ")_norm(" + isNormalized + ").csv";
		
		FileWriter fw = new FileWriter(outputFile);
		fw.write("customer,");
		for(int i = 0; i < MAX_TYPE_NO; i++)
		{
			fw.write("type" + i + ",");
		}
		fw.write("\n");
		for(Row row : lstRows)
		{
			fw.write(row.toString());
		}
		fw.close();
		
		System.out.println("write data success!");
	}
	
	public static void writeWeka() throws IOException
	{
		String outputFile = folder + "wekadata_fi(" + isFiltered + ")_norm(" + isNormalized + ").arff";
		
		FileWriter fw = new FileWriter(outputFile);
		fw.write("@relation Customer_Revenue \n \n");
		for(int i = 0; i < MAX_TYPE_NO; i++)
		{
			fw.write("@attribute type" + i + " real \n");
		}
		fw.write("@data \n");
		for(Row row : lstRows)
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

}
