package org.eclipse.leshan.server.influxdb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//import javax.security.auth.login.Configuration;
import com.db.influxdb.Configuration;
import com.db.influxdb.DataWriter;
import com.db.influxdb.Utilities;

public class InfluxPost {

	//private int port;
	private String hostAddr, ports ,username, password, dbName, measurementName;
	private Map<String, String> tags; 
	private Map<String, Object> fields;
	Configuration configuration;
	Utilities ob = new Utilities();
	
	public InfluxPost(String hostAddr, int port, String username, String password, String dbName,
			String measurementName,Map<String, String> tags,Map<String, Object> fields ){
		this.hostAddr = hostAddr;
		this.ports = String.valueOf(port);
		this.username = username;
		this.password = password;
		this.dbName = dbName;
		this.measurementName = measurementName;
		this.tags = tags;
		this.fields = fields;
	}
	
	

	public void PostData(DataWriter writer) throws Exception{
		writer.setMeasurement(measurementName);
		writer.setTimeUnit(TimeUnit.SECONDS);
		writer.setTime(System.currentTimeMillis()/1000);
		tags = new HashMap<>();
		tags.put("Object", "Device");
		writer.setTags(tags);
		writer.setFields(fields);
		
	//	setParameter(writer, tags, fields);

		try {
			writer.writeData();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			System.out.println("Creating Db");
			try {
				ob.createDatabase(configuration);
				System.out.println("Successfully Created");
				writer.writeData();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				throw e1;
				//e1.printStackTrace();
			}
			//e.printStackTrace();
		}
		
	}



	/**
	 * @param writer
	 * @param tags
	 * @param fields
	 */
	private void setParameter(DataWriter writer, Map<String, String> tags, Map<String, Object> fields) {

	}

	/**
	 * @return
	 * This function will be used to Connect to the database and returns the writer object.
	 */
	public DataWriter connectDb() {
		configuration = new Configuration(hostAddr,ports,username,password,dbName);
		
		DataWriter writer = null;
		try {
			writer = new DataWriter(configuration);
		} catch (Exception e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
		}
		return writer;
	}
	
}
