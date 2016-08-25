package com.ericsson.appiot.demo.integration.rpi3;

import java.util.logging.Logger;

import com.companyx.sensor.platformx.PlatformXListener;
import com.companyx.sensor.platformx.device.DeviceData;


import se.sigma.sensation.gateway.sdk.client.SensationClient;
import se.sigma.sensation.gateway.sdk.client.data.SensorMeasurement;

public class MeasurementHandler implements PlatformXListener {
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	public static final String PLATFORMX_TEMPERATURE_IDENTIFIER = "TEMP";
	public static final String PLATFORMX_HUMIDITY_IDENTIFIER = "HUM";
	
	private SensationClient client;
	public MeasurementHandler(SensationClient client) {
		this.client = client;
	}
	
	public void onData(DeviceData data) {
		String sensorType = data.getSensorType();
		if(sensorType != null) {
			
			int sensorHardwareTypeId = 0;
			
			if(sensorType.equals(PLATFORMX_TEMPERATURE_IDENTIFIER)) {
				sensorHardwareTypeId = AppIoTContract.SENSOR_TYPE_ID_TEMPERATURE;
			} else if(sensorType.equals(PLATFORMX_HUMIDITY_IDENTIFIER)) {
				sensorHardwareTypeId = AppIoTContract.SENSOR_TYPE_ID_HUMIDITY;
			} else {
				logger.warning("Unsupported sensor type detected: " + sensorType);
			}
			
			SensorMeasurement measurement = new SensorMeasurement();
			measurement.setSensorHardwareTypeId(sensorHardwareTypeId);
			measurement.setSerialNumber(data.getSerialNumber());
			measurement.setUnixTimestampUTC(System.currentTimeMillis());
			measurement.setValue(new double[] {data.getValue()});
			client.sendSensorMeasurement(measurement);
		}
	}
	
}
