package ThreadManagement;

import java.util.HashMap;
import java.util.Map.Entry;

public class Parameter {

	/**
	 * K -- parameter name, e.g. input_file
	 * V -- parameter value, e.g. E:\\input.txt
	 */
	private HashMap<String, String> hsParas = new HashMap<String, String>();
	
	public Parameter()
	{
		
	}
	
	public Parameter(Parameter para)
	{
		HashMap<String, String> lstParas = para.hsParas;

		hsParas = new HashMap<String, String>();
		for (Entry<String, String> entry : lstParas.entrySet())
		{
			hsParas.put(entry.getKey(), entry.getValue());
		}
	}
	
	public Parameter(HashMap<String, String> paras)
	{
		for (Entry<String, String> entry : paras.entrySet())
		{
			hsParas.put(entry.getKey(), entry.getValue());
		}
	}
	
	
	public void addParameter(String key, String value)
	{
		hsParas.put(key, value);
	}
	
	public String get(String key)
	{
		return hsParas.get(key);
	}
}
