/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *     
 * Change Log:
 * Vibhor 28/07/2017 Added Influx DB support for Project Implementation
 *******************************************************************************/
package org.eclipse.leshan.server.demo.servlet;

import java.awt.Point;
import java.awt.Window.Type;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.demo.servlet.json.RegistrationSerializer;
import org.eclipse.leshan.server.demo.servlet.log.CoapMessage;
import org.eclipse.leshan.server.demo.servlet.log.CoapMessageListener;
import org.eclipse.leshan.server.demo.servlet.log.CoapMessageTracer;
import org.eclipse.leshan.server.demo.utils.EventSource;
import org.eclipse.leshan.server.demo.utils.EventSourceServlet;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.Pong;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


class valueObject{
	int id;
	float value;

	public valueObject(int id, float value) {
		this.id = id;
		this.value = value;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public float getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
}

public class EventServlet extends EventSourceServlet {

	private static final String HOST_ADDR = "localhost";
	
	private static final int HOST_PORT = 8086;
	
	private static final String HOST_USERNAME = "root";
	
	private static final String HOST_PASSWORD = "root";
	
	private static final String DB_NAME = "dataghost";
	
    private static final String EVENT_DEREGISTRATION = "DEREGISTRATION";

    private static final String EVENT_UPDATED = "UPDATED";

    private static final String EVENT_REGISTRATION = "REGISTRATION";

    private static final String EVENT_NOTIFICATION = "NOTIFICATION";

    private static final String EVENT_COAP_LOG = "COAPLOG";

    private static final String QUERY_PARAM_ENDPOINT = "ep";
    
    private static final String RETENTION_POLICY = "two_hours";

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);

    private final Gson gson;
    
    private String url;
	
	private String measurement;
	private String writeLine;
	
    private StringBuilder value;
    private StringBuilder tags;
    
    
    
    private InfluxDB influxDB;

	InfluxDB.LogLevel logLevel;
	InfluxDB.ConsistencyLevel consistencyLevel;
	org.influxdb.dto.Point p1;

    
    private final CoapMessageTracer coapMessageTracer;

    private Set<LeshanEventSource> eventSources = Collections
            .newSetFromMap(new ConcurrentHashMap<LeshanEventSource, Boolean>());

    
    private final RegistrationListener registrationListener = new RegistrationListener() {

        @Override
        public void registered(Registration registration, Registration previousReg,
                Collection<Observation> previousObsersations) {
            String jReg = EventServlet.this.gson.toJson(registration);
            
            sendEvent(EVENT_REGISTRATION, jReg, registration.getEndpoint());
            
        }

        @Override
        public void updated(RegistrationUpdate update, Registration updatedRegistration,
                Registration previousRegistration) {
            String jReg = EventServlet.this.gson.toJson(updatedRegistration);
            sendEvent(EVENT_UPDATED, jReg, updatedRegistration.getEndpoint());
        }

        @Override
        public void unregistered(Registration registration, Collection<Observation> observations, boolean expired,
                Registration newReg) {
            String jReg = EventServlet.this.gson.toJson(registration);
            sendEvent(EVENT_DEREGISTRATION, jReg, registration.getEndpoint());
        }
    };

    private final ObservationListener observationListener = new ObservationListener() {

    	
        @Override
        public void cancelled(Observation observation) {
        }

        
        @Override
        public void onResponse(Observation observation, Registration registration, ObserveResponse response) {
   
        	float temperature;
        	value = new StringBuilder();
        	tags = new StringBuilder();
        	
        	if (LOG.isDebugEnabled()) {
                LOG.debug("Received notification from [{}] containing value [{}]", observation.getPath(),
                        response.getContent().toString());
            }

            if (registration != null) {
                String data = new StringBuilder("{\"ep\":\"").append(registration.getEndpoint()).append("\",\"res\":\"")
                        .append(observation.getPath().toString()).append("\",\"val\":")
                        .append(gson.toJson(response.getContent())).append("}").toString();
                String tempJson = gson.toJson(response.getContent());
                valueObject vO = new Gson().fromJson(tempJson, valueObject.class);
                
                measurement = registration.getEndpoint().toString();
                
                System.out.println("IT came here....Came here");
                if((observation.getPath().toString().split("/")[1]).equals("3349"))
                {
                	System.out.println("Came here");
                	
                	tags.append("Object=Device");
                }
                
                if((observation.getPath().toString().split("/")[3]).equals("5851"))
                {
                	
                	System.out.println(vO.getValue()/1000);
                	float cpu_load = (float)vO.getValue()/1000;
                	
                	value.append("Instance=").append(observation.getPath().toString().split("/")[2]);
                	value.append(",");
                	value.append("Resource=").append("CpuLoad");
                	value.append(",");
                	value.append("Value=").append(cpu_load);
                	
                	System.out.println("Value field is "+value);
                	
                	p1 = org.influxdb.dto.Point.measurement(measurement).addField("Instance", observation.getPath().toString().split("/")[2])
                														.addField("Resource", "CpuLoad")
                														.addField("Value",cpu_load)
                														.tag("Object", "Device")
                														.build();
                }
                else
                    if((observation.getPath().toString().split("/")[3]).equals("5850"))
                    {
                    	
                    	temperature = (float)vO.getValue()/1000;
                    	//fields.put("Value",temperature );
                    	value.append("Instance=").append(observation.getPath().toString().split("/")[2]);
                    	value.append(",");
                    	value.append("Resource=").append("SystemTemperature");
                    	value.append(",");
                    	value.append("Value=").append(temperature);
                    	
                    	System.out.println("Value field is "+value);
                    
                    	p1 = org.influxdb.dto.Point.measurement(measurement).addField("Instance", observation.getPath().toString().split("/")[2])
								   .addField("Resource", "SystemTemperature")
								   .addField("Value", temperature)
								   .tag("Object", "Device")
								   .build();
                    
                    }
                    else
                        if((observation.getPath().toString().split("/")[3]).equals("5852"))
                        {
                        	float avg_load1 = (float)vO.getValue()/100;
                        
                        	p1 = org.influxdb.dto.Point.measurement(measurement).addField("Instance", observation.getPath().toString().split("/")[2])
    								   .addField("Resource", "AvgLoad1Min")
    								   .addField("Value", avg_load1)
    								   .tag("Object", "Device")
    								   .build();
                        
                        }
                        else
                            if((observation.getPath().toString().split("/")[3]).equals("5853"))
                            {
                            	float avg_load1 = (float)vO.getValue()/100;
                            
                            	p1 = org.influxdb.dto.Point.measurement(measurement).addField("Instance", observation.getPath().toString().split("/")[2])
        								   .addField("Resource", "AvgLoad5Min")
        								   .addField("Value", avg_load1)
        								   .tag("Object", "Device")
        								   .build();
                            
                            }
                            else
                                if((observation.getPath().toString().split("/")[3]).equals("5854"))
                                {
                                	float avg_load1 = (float)vO.getValue()/100;
                                
                                	p1 = org.influxdb.dto.Point.measurement(measurement).addField("Instance", observation.getPath().toString().split("/")[2])
            								   .addField("Resource", "AvgLoad15Min")
            								   .addField("Value", avg_load1)
            								   .tag("Object", "Device")
            								   .build();
                                
                                }
                
                
                
                url = "http://".concat(HOST_ADDR+":"+HOST_PORT);
                
                influxDB = InfluxDBFactory.connect(url,"root","root");
                
                influxDB.setRetentionPolicy(RETENTION_POLICY);
                              
                if(!influxDB.databaseExists(DB_NAME))
                	influxDB.createDatabase(DB_NAME);
                
                writeLine = measurement+","+tags+" "+value;
                
                System.out.println(writeLine);
                
                influxDB.setLogLevel(InfluxDB.LogLevel.BASIC);
                
                //influxDB.write(DB_NAME,RETENTION_POLICY,InfluxDB.ConsistencyLevel.ONE,writeLine);
                influxDB.write(DB_NAME,RETENTION_POLICY,p1);
                
                sendEvent(EVENT_NOTIFICATION, data, registration.getEndpoint());
            }
    }
        

        @Override
        public void onError(Observation observation, Registration registration, Exception error) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Unable to handle notification of [%s:%s]", observation.getRegistrationId(),
                        observation.getPath()), error);
            }
        }

        @Override
        public void newObservation(Observation observation, Registration registration) {
        }
    };

    public EventServlet(LeshanServer server, int securePort) {
        server.getRegistrationService().addListener(this.registrationListener);
        server.getObservationService().addListener(this.observationListener);

        // add an interceptor to each endpoint to trace all CoAP messages
        coapMessageTracer = new CoapMessageTracer(server.getRegistrationService());
        for (Endpoint endpoint : server.getCoapServer().getEndpoints()) {
            endpoint.addInterceptor(coapMessageTracer);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Registration.class, new RegistrationSerializer(securePort));
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeSerializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
    }

    private synchronized void sendEvent(String event, String data, String endpoint) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching {} event from endpoint {}", event, endpoint);
        }

        for (LeshanEventSource eventSource : eventSources) {
            if (eventSource.getEndpoint() == null || eventSource.getEndpoint().equals(endpoint)) {
                eventSource.sentEvent(event, data);
            }
        }
    }

    class ClientCoapListener implements CoapMessageListener {

        private final String endpoint;

        ClientCoapListener(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void trace(CoapMessage message) {
            String coapLog = EventServlet.this.gson.toJson(message);
            sendEvent(EVENT_COAP_LOG, coapLog, endpoint);
        }

    }

    private void cleanCoapListener(String endpoint) {
        // remove the listener if there is no more eventSources for this endpoint
        for (LeshanEventSource eventSource : eventSources) {
            if (eventSource.getEndpoint() == null || eventSource.getEndpoint().equals(endpoint)) {
                return;
            }
        }
        coapMessageTracer.removeListener(endpoint);
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest req) {
        String endpoint = req.getParameter(QUERY_PARAM_ENDPOINT);
        return new LeshanEventSource(endpoint);
    }

    private class LeshanEventSource implements EventSource {

        private String endpoint;
        private Emitter emitter;

        public LeshanEventSource(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void onOpen(Emitter emitter) throws IOException {
            this.emitter = emitter;
            eventSources.add(this);
            if (endpoint != null) {
                coapMessageTracer.addListener(endpoint, new ClientCoapListener(endpoint));
            }
        }

        @Override
        public void onClose() {
            cleanCoapListener(endpoint);
            eventSources.remove(this);
        }

        public void sentEvent(String event, String data) {
            try {
                emitter.event(event, data);
            } catch (IOException e) {
                onClose();
            }
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
