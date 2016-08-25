package com.companyx.sensor.platformx.device;

import java.util.logging.Logger;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class ArduinoDevice {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private DeviceListener listener;
	private final Serial serial; 
	private String serialNumber;
	private String device;
	private int baudRate;
	
	public Logger getLogger() {
		return logger;
	}

	public String getDevice() {
		return device;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public DeviceListener getListener() {
		return listener;
	}

	public Serial getSerial() {
		return serial;
	}

	public ArduinoDevice(String device, int baudrate) {
		this.device = device;
		this.baudRate = baudrate;
		serial = SerialFactory.createInstance();
	}	
	
	public void Connect() throws SerialPortException {
		serial.open(getDevice(), getBaudRate());
		serial.addListener(new SerialListener(this));		
	}
	
	protected void newMeasurement(DeviceData data) {
		// set serial number of the device if not yet set.
		if(getSerialNumber() == null) {
			setSerialNumber(data.getSerialNumber());
		}
		
		if(listener != null) {
			listener.onData(data);
		}
	}
	
	public void setListener(DeviceListener listener) {
		this.listener = listener;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}	
}
