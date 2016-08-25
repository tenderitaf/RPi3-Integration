package com.companyx.sensor.platformx.device;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataListener;

public class SerialListener implements SerialDataListener {
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 
	
	private ArduinoDevice device;
	public SerialListener(ArduinoDevice device) {
		this.device = device;
	}
	
	public void dataReceived(SerialDataEvent event) {
       	try {
			String msg = new String(event.getData().getBytes(), "UTF-8");
			StringTokenizer stRows = new StringTokenizer(msg, "\n");
			while(stRows.hasMoreTokens()) {
				String row = stRows.nextToken();
				boolean validMeasurement = false;
				if(row.indexOf(";") != -1) {
					DeviceData deviceData = new DeviceData();
					StringTokenizer stDatas = new StringTokenizer(row, ";");
					while(stDatas.hasMoreTokens()) {
						String data = stDatas.nextToken();						
						if(data.indexOf(':') != -1) {
							String dataType = data.substring(0, data.indexOf(':'));
							String valuestr = data.substring(data.indexOf(':')+1);							
							deviceData.setSensorType(dataType);
							if(dataType.equals("ID")) {
								deviceData.setSerialNumber(valuestr);
								validMeasurement = true;
							} else {
								try {
									double dataValue = Double.parseDouble(valuestr);
									deviceData.setValue(dataValue);
									validMeasurement = true;
								} catch (NumberFormatException e) {}
							}
						}	
					}
					if(validMeasurement) {
						this.device.newMeasurement(deviceData);
					}
				} 
			}
       	} catch (UnsupportedEncodingException e) {
			logger.log(Level.SEVERE, "UTF-8 not supported.", e);
		}
	}
}
