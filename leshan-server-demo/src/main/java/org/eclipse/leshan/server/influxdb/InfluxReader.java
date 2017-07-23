package org.eclipse.leshan.server.influxdb;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.db.influxdb.Configuration;
import com.db.influxdb.DataReader;
import com.db.influxdb.Query;
import com.db.influxdb.ResultSet;
import com.db.influxdb.ResultSet.Result;
import com.db.influxdb.ResultSet.Result.Series;
import com.db.influxdb.*;

public class InfluxReader {

	void readPost(){
		Configuration configuration = new Configuration("localhost","8086", "root", "root", "mydb");
		Query query = new Query();
		ResultSet result = null;
		ResultSet result2 = null;
		
		query.setMeasurement("TestSampleMeasurement");
		query.setDuration("1d");
		
		DataReader reaxer2 = new DataReader();
		reaxer2.setConfiguration(configuration);
		
		reaxer2.setQuery(query);
		try {
			result2= reaxer2.getResult();
					}
		catch (IOException | URISyntaxException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		DataReader reader = new DataReader(query, configuration);
		try {
			result = reader.getResult();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Result> tempResult = result.getResults();
		
		List <Series> tempSeries = tempResult.get(0).getSeries(); //to get the series
		
		
		for (Series series : tempSeries) {
			
			
			
			System.out.println("Series Name: " +series);
			System.out.println("Series Name: " +series.getName());
		
			System.out.println(series.getColumns());
			
			for (List<String> templs : series.getValues()) {
				
				System.out.println(templs.toString());
			}
						
		}
	}
	
	
}
