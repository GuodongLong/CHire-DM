package Pricing.Type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

public class AnalyzeResult {

	public static double MIN_DOUBLE = 1e-75;
	
//	public static String folder = "E:\\GoolgeDriver\\Google ‘∆∂À”≤≈Ã\\CoatesHire\\Pricing\\CustomerSegmentation\\";
	public static String folder = "/home/gulong/workspace/CoatesHire/data/pricing/";
	public static Instances data;
	public static AbstractClusterer cluster;
	public static List<String> customer_list;
	public static HashMap<Integer, String> subtypes;
	public static String algName = "";
	public static String clusterNum = "50";
	
	public static void main(String[] args) throws Exception
	{
		customer_list = loadCustomerCodeList(folder + "Customer_Type_Revenue.csv");
		data          = loadClusterDataFromFile(folder + "wekadata_fi(false)_norm(true).arff");
		subtypes      = loadSubtypeList(folder + "pricing_clustering_rate_subtype_list.csv");
		// cluster string
		cluster       = buildSimpleKMeans(data);
//		cluster       = buildEM(data);
		

	    ClusterEvaluation eval = new ClusterEvaluation();
	    eval.setClusterer(cluster);
	    eval.evaluateClusterer(data);
		FileWriter fw_cluster = new FileWriter(folder + "cluster_result_" + algName + "_" + clusterNum + ".txt");
		fw_cluster.write(eval.clusterResultsToString());
		fw_cluster.close();
		
		// get customer list
		String custList = getCustomerList(data, customer_list);
//		System.out.println(custList);
		FileWriter fw_cust = new FileWriter(folder + "clustered_customer" + algName + "_" + clusterNum +  ".csv");
		fw_cust.write(custList);
		fw_cust.close();
		
		// get product list
		String prodList = getProductList(data, customer_list);
//		System.out.println(prodList);
		FileWriter fw_prod = new FileWriter(folder + "clustered_product" + algName + "_" + clusterNum +  ".csv");
		fw_prod.write(prodList);
		fw_prod.close();
	}

	public static List<String> loadCustomerCodeList(String fileName) throws Exception
	{
		List<String> cust_List = new ArrayList<String>();
		
		FileReader fr = new FileReader(fileName);
		BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		String currCust = "";
		while((line = br.readLine()) != null)
		{
			if (line.length() < 2)
			{
				continue;
			}
			String[] str = line.split(",");
			String customer = str[0];
			if (!customer.equalsIgnoreCase(currCust))
			{
				currCust = customer;
				cust_List.add(customer);
			}
		}
		
		br.close();
		fr.close();
		System.out.println("Load customer num : " + cust_List.size());
		return cust_List;
	}
	
	public static AbstractClusterer buildSimpleKMeans(Instances data) throws Exception
	{
		algName = "KMeans";
		
		String[] options = new String[4];
		options[0] = "-N";
		options[1] =  clusterNum;
		options[2] = "-M";
		options[3] = "10000";
		
		
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
		
//		ClusterEvaluation eval = new ClusterEvaluation();
		
		AbstractClusterer cluster = new EM();
//		((EM) cluster).setOptions(options);
		cluster.buildClusterer(data);
		System.out.println(cluster.toString());
		
//		eval.setClusterer(clusterer)
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
			sb.append(clusterId + "," + cust_list.get(idx++) + "," + instance.toString() + "\n");
		}
		
		return sb.toString();
	}
	
	public static String getProductList(Instances data, List<String> prod_list) throws Exception
	{
		HashMap<String, String> hs = new HashMap<String, String>(); 
		for(Instance instance : data)
		{
			int clusterId = cluster.clusterInstance(instance);
			for(int i = 0; i < instance.numAttributes(); i++)
			{
				double value = instance.value(i);
				if (value > MIN_DOUBLE)
				{
					String key = clusterId + "," + i;
					hs.put(key, key);
				}
			}
		}
		

		StringBuilder sb = new StringBuilder();
		sb.append("cluster_id,product_type\n");
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
	
	public static HashMap<Integer, String> loadSubtypeList(String fileName) throws Exception
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
			int key = Integer.valueOf(str[0]);
			String value = str[1];
			hs.put(key, value);
		}
		br.close();
		fr.close();
		return hs;
	}
}
