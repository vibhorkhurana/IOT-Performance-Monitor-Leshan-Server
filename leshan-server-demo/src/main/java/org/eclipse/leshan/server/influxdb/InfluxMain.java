package org.eclipse.leshan.server.influxdb;

import java.util.*;

import com.db.influxdb.DataWriter;
import com.db.influxdb.Utilities;

public class InfluxMain {

	String hostAddr,username, password, dbName, measurementName;
	int port;
	Map<String, String> tags; 
	Map<String, Object> fields;
	private final Random m_random= new Random();
	InfluxPost infPost;
	DataWriter writer;
	int attempts =1;
	Utilities ob = new Utilities();
	
	
	public InfluxMain(String hostAddr2, int hostPort, String hostUsername, String hostPassword, String dbName2,
			String endpoint) {
		this.hostAddr = hostAddr2;
		this.port = hostPort;
		this.username = hostUsername;
		this.password = hostPassword;
		this.dbName = dbName2;
		this.measurementName = endpoint;

	}


	void PostData() throws Exception{
		
//		hostAddr = "localhost";
//		port = 8086;
//		username="root";
//		password="root";
//		dbName="myNewTest31";
//		measurementName="newInfluxTest";
		
//		tags = new HashMap<>();
//		fields = new HashMap<>();
//		tags.put("language", "java");
//		tags.put("user", "vibhor");
//		m_random.nextInt(10);
//		fields.put("random1",m_random.nextInt(10));
//		fields.put("random2",m_random.nextInt(20));
	
		infPost = new InfluxPost(hostAddr, port, username, password, dbName, measurementName, tags, fields);
		writer = infPost.connectDb();
		
		try {
			infPost.PostData(writer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
			System.out.println("DB doesnot exists..Creating DB");
			
			throw e;
//			writer = infPost.connectDb();
//			try {
//				System.out.println("here2");
//				infPost.PostData(writer, tags, fields);
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				System.out.println("here3");
//				e1.printStackTrace();
//			}
		}
		
		}
	
//	public static void main(String[] args) {
//	
//		InfluxMain infMain = new InfluxMain();
//		try {
//			infMain.PostData();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("Trying Again");
//			try {
//				infMain.PostData();
//				System.out.println("Successfully Created");
//
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//		}
//		
//	}
}
