package com.companyx.sensor.platformx;

import java.io.File;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.companyx.sensor.platformx.device.ArduinoDevice;
import com.companyx.sensor.platformx.device.DeviceData;
import com.companyx.sensor.platformx.device.DeviceListener;
import com.pi4j.io.serial.SerialPortException;

import se.sigma.sensation.gateway.sdk.client.data.NetworkCard;
import se.sigma.sensation.gateway.sdk.client.platform.ConnectivitySettings;

public class PlatformXManager {

	private final Logger logger = Logger.getLogger(this.getClass().getName());

	private PlatformXListener platformListener;

	private List<ArduinoDevice> devices = new Vector<ArduinoDevice>();

	private SimpleLinuxManager linuxManager;

	public PlatformXManager() {
		this.linuxManager = new SimpleLinuxManager();
		ArduinoDevice device = new ArduinoDevice("/dev/ttyUSB0", 9600);
		device.setListener(new MyDeviceListener());
		devices.add(device);
		try {
			device.Connect();
		} catch (SerialPortException spe) {
			logger.warning(
					"Could not connect to device /dev/ttyUSB0. "
					+ "Please make sure the Arduino device is connected and restart application.");
		}
	}

	public List<ArduinoDevice> getDevices() {
		return devices;
	}

	public ArduinoDevice getDeviceBySerialNumber(String serialNumber) {
		for (ArduinoDevice arduino : getDevices()) {
			if (arduino.getSerialNumber().equals(serialNumber)) {
				return arduino;
			}
		}
		return null;
	}

	public List<NetworkCard> getNetworkCards() {
		return linuxManager.getNetworkCards();
	}

	public ConnectivitySettings getConnectivitySettings(String adapterName) {
		return linuxManager.getConnectivitySettings(adapterName);
	}

	public int flashDevice(ArduinoDevice device, File file) {

		String command1 = "/usr/bin/ard-reset-arduino " + device.getDevice();

		String command2 = "/usr/share/arduino/hardware/tools/avr/../avrdude" + " -q -V -D -p atmega328p"
				+ " -C /usr/share/arduino/hardware/tools/avr/../avrdude.conf" + " -c arduino" + " -b 57600" + " -P "
				+ device.getDevice() + " -U flash:w:" + file.getAbsoluteFile() + ":i";

		logger.info("Flashing file + " + file.getAbsoluteFile());

		int exitCode = 0;
		try {

			exitCode = linuxManager.executeBash(command1);
			if (exitCode != 0) {
				logger.severe("Failed to reset device " + device.getDevice());
				return exitCode;
			}

			exitCode = linuxManager.executeBash(command2);
			if (exitCode != 0) {
				logger.severe("Failed to flash device " + device.getDevice());
			}
			logger.fine("Device " + device.getSerialNumber() + " successfully flashed");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "flash device " + device.getSerialNumber(), e);
		}
		return exitCode;
	}

	public int reboot() {
		return linuxManager.reboot();
	}

	public void addListener(PlatformXListener platformListener) {
		this.platformListener = platformListener;
	}

	// Simply forward device data to platform listener.
	private class MyDeviceListener implements DeviceListener {
		public void onData(DeviceData data) {
			if (platformListener != null) {
				platformListener.onData(data);
			}
		}
	}
}
