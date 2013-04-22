package Pricing.Rate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import weka.clusterers.AbstractClusterer;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.EM;
import weka.clusterers.SimpleKMeans;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class AnalyzeRateResult {

	public static double MIN_DOUBLE = 1e-75;
	
//	public static String folder = "E:\\GoolgeDriver\\Google ‘∆∂À”≤≈Ã\\CoatesHire\\Pricing\\CustomerSegmentation\\";
	public static String folder = "E:\\don\\CoatesHire\\data\\pricing\\";
//	public static String folder = "/home/gulong/workspace/CoatesHire/data/pricing/";
	public static Instances data;
	public static AbstractClusterer cluster;
	public static List<String> customer_list;
	public static HashMap<Integer, String> subtypes;
	public static String algName;
	public static int clusterNum = 0;
	
	public static void main(String[] args) throws Exception
	{
		long start = System.currentTimeMillis();
		System.out.println(start);
		customer_list = loadCustomerCodeList();
		data          = loadClusterDataFromFile(folder + "wekaRate_final.arff");
//		data          = loadClusterDataFromFile(folder + "cpu.arff");
		subtypes      = loadSubtypeList(folder + "pricing_clustering_rate_subtype_list.csv");
		// cluster string
		cluster       = buildSimpleKMeans(data);
//		cluster       = buildEM(data);
				
	    ClusterEvaluation eval = new ClusterEvaluation();
	    eval.setClusterer(cluster);
	    eval.evaluateClusterer(data);
		FileWriter fw_cluster = new FileWriter(folder + "cluster_result_" + algName + ".txt");
		fw_cluster.write(eval.clusterResultsToString());
		fw_cluster.close();
		
		// get customer list
		String custList = getCustomerList(data, customer_list);
//		System.out.println(custList);
		FileWriter fw_cust = new FileWriter(folder + "clustered_customer_" + algName + ".csv");
		fw_cust.write(custList);
		fw_cust.close();
		
		// get product list
		String prodList = getProductList(data, customer_list);
//		System.out.println(prodList);
		FileWriter fw_prod = new FileWriter(folder + "clustered_product_" + algName + ".csv");
		fw_prod.write(prodList);
		fw_prod.close();
		
		long end = System.currentTimeMillis();
		long dur = (end - start)/(1000);
		System.out.println("Spending time is " + dur + " seconds! \n");
	}

	public static List<String> loadCustomerCodeList() throws Exception
	{
		List<String> cust_List = new ArrayList<String>();
		
		String fileName = folder + "rate_final.csv";
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		while((line = br.readLine()) != null)
		{
			if (line.length() < 1)
			{
				continue;
			}
			String[] str = line.split(",");
			cust_List.add(str[0]);
		}
		
		br.close();
		fr.close();
		
		return cust_List;
	}
	
	public static AbstractClusterer buildSimpleKMeans(Instances data) throws Exception
	{
		String[] options = new String[4];
		options[0] = "-N";
		options[1] = "30";
		options[2] = "-M";
		options[3] = "10000";
		algName = "KMeans"+options[1];
		
		AbstractClusterer cluster = new SimpleKMeans();
		((SimpleKMeans) cluster).setOptions(options);
		cluster.buildClusterer(data);
		return cluster;
	}
	
	public static AbstractClusterer buildEM(Instances data) throws Exception
	{
		algName = "EM";
//		String[] options = new String[4];
//		options[0] = "-N";
//		options[1] = "9";
//		options[2] = "-M";
//		options[3] = "10000";
		
		AbstractClusterer cluster = new EM();
//		((EM) cluster).setOptions(options);
		
		cluster.buildClusterer(data);
		
		return cluster;
	}
	
	public static String getCustomerList(Instances data, List<String> cust_list) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		sb.append("cluster_id,customer_code\n");
		int idx = 0;
		for(Instance instance : data)
		{
			int clusterId = cluster.clusterInstance(instance);
			int cnt = 0;
			sb.append(clusterId + "," + cust_list.get(idx++));
			for(int i = 0; i < instance.numAttributes(); i++)
			{
				int subtype_id = i / 5;
				int rate_id = i % 5;
				String rate_str;
				switch(rate_id)
				{
					case 0: rate_str = "d"; break;
					case 1: rate_str = "w"; break;
					case 2: rate_str = "f"; break;
					case 3: rate_str = "m"; break;
					case 4: rate_str = "M"; break;
					default: rate_str = "E"; break;
				}
				double value = instance.value(i);
				if (value <= 0)
				{
					continue;
				}
				cnt++;
				sb.append("," + subtypes.get(subtype_id) + " " + rate_str + " " + value);
			}
			if (cnt == 0)
			{
				sb.append("," + instance.toString());
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	
	public static String getProductList(Instances data, List<String> prod_list) throws Exception
	{
		HashMap<String, String> hs = new HashMap<String, String>(); 
		for(Instance instance : data)
		{
			int clusterId = cluster.clusterInstance(instance);
			for(int i = 0; i < instance.numAttributes()/5; i++) // One subtype occupies 5 attributes (daily, weekly, fortnight, monthly, monthly2).
			{
				double value = instance.value(i*5);
				if (value > MIN_DOUBLE)
				{
					String key = clusterId + "," + i + "," + subtypes.get(i).toString();
					if (subtypes.get(i) == null)
					{
						System.out.print("ERROR on subtypes id " + i);
					}
					hs.put(key, key);
				}
			}
		}
		

		StringBuilder sb = new StringBuilder();
		sb.append("cluster_id,subtype_id,subtype_name\n");
		Iterator it = hs.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        sb.append(pairs.getKey() + "\n");
	    }
		
		return sb.toString();
	}
	

	/**
	 * Load data from file.
	 * @param fileName
	 * @throws DataFileReadError 
	 * @throws DataFileNotFind 
	 * @throws DataSourceLoadError 
	 * @throws TimerException 
	 * @throws Exception, IllegalEnumValue 
	 */
	public static Instances loadClusterDataFromFile(String fileName) throws Exception
	{
		System.out.println("Begin load data from file " + fileName);
		
		/* Validate file */
		File file = new File(fileName);
		if (!file.exists() || !file.isFile())
		{
			System.out.println("File " + fileName + " is not exist or is not a real file.");
			throw new Exception("File not be found");
		}
		
		/* Get instances */
		Instances instances = null;
		try {
			instances = DataSource.read(fileName);//dataSource.getDataSet(0);
//			instances.setClassIndex(instances.numAttributes() - 1);
		} catch (Exception e) {
			System.out.println("The " + fileName + " cannot be loaded correctly." + e.toString());
			e.printStackTrace();
			throw new Exception("Load file error");
		}
		
		System.out.println("Total " + instances.size() + " samples were loaded!");
		
		return instances;
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
