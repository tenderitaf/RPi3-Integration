package com.ericsson.appiot.demo.integration.rpi3;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.companyx.sensor.platformx.PlatformXManager;
import com.companyx.sensor.platformx.device.ArduinoDevice;

import se.sigma.sensation.gateway.sdk.client.Platform;
import se.sigma.sensation.gateway.sdk.client.PlatformInitialisationException;
import se.sigma.sensation.gateway.sdk.client.SensationClient;
import se.sigma.sensation.gateway.sdk.client.core.SensationClientProperties;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorDeleteResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorStatus;
import se.sigma.sensation.gateway.sdk.client.data.DataCollectorStatusCode;
import se.sigma.sensation.gateway.sdk.client.data.DiscoveredSensorCollection;
import se.sigma.sensation.gateway.sdk.client.data.ISensorMeasurement;
import se.sigma.sensation.gateway.sdk.client.data.NetworkSetting;
import se.sigma.sensation.gateway.sdk.client.data.NetworkSettingResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.RebootResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.RestartApplicationResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.SensorCollectionRegistrationResponseCode;
import se.sigma.sensation.gateway.sdk.client.data.UpdatePackage;
import se.sigma.sensation.gateway.sdk.client.data.UpdatePackageResponseCode;
import se.sigma.sensation.gateway.sdk.client.registry.SensorCollectionRegistration;
import se.sigma.sensation.gateway.sdk.client.registry.SensorCollectionRegistry;
import se.sigma.sensation.gateway.sdk.deployment.DeploymentApplicationConnector;
import se.sigma.sensation.gateway.sdk.deployment.bluetooth.JSR82Connector;

/**
 * @author Joakim Hellberg
 *
 */
public class RPi3Platform implements Platform {
	
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private static final String FIRMWARE_VERSION = "1.0";
	private static final String HARDWARE_VERSION = "1.0";
	private static final String SOFTWARE_VERSION = "1.0";

	private PlatformXManager manager;
	private SensationClient client;
	private DeploymentApplicationConnector bluetoothConnector;
	
	/**
	 * Initializes the platform.
	 * 
	 * @param client
	 *            - SensationClient to be used by the platform.
	 * @throws PlatformInitialisationException
	 *             - Indicates platform initialization failure.
	 */
	public void init(final SensationClient client) throws PlatformInitialisationException {
		this.client = client;
		this.manager = new PlatformXManager();
		bluetoothConnector = new JSR82Connector();
		manager.addListener(new MeasurementHandler(client));
	}

	/**
	 * Request from Sensation to report sensor collections in range of the data
	 * collector. The platform is expected to report sensor collections
	 * available.
	 * 
	 * @param correlationId
	 *            - The id of the request, use when answering.
	 * @see DiscoveredSensorCollection.
	 * @see SensationClient.reportDicoveredSensorCollection().
	 */
	public void reportDiscoveredSensorCollections(String correlationId) {
		
		// Perform scan of devices in range
		List<ArduinoDevice> devices = manager.getDevices(); 
		for(int i = 0; i < devices.size(); i++){
			ArduinoDevice device = devices.get(i);
			DiscoveredSensorCollection deviceDiscovered = new DiscoveredSensorCollection();
			
			// Set Device unique identifier e.g. serial number.
			// This is what you enter in Sensation when registering a new sensor collection.
			deviceDiscovered.setSerialNumber(device.getSerialNumber()); 
			
			// Set the time of when the device was last seen.
			deviceDiscovered.setLastObserved(System.currentTimeMillis());
			
			// Setting the signal strength makes Sensation able to find the best suitable gateway 
			// to register the device to. The gateway with best signal strength is put in top of the list. 
			deviceDiscovered.setSignalStrength(-45);
			client.reportDiscoveredSensorCollection(correlationId, deviceDiscovered);
		}
	}
	
	/**
	 * Called when a sensor collection has been registered. The Sensor
	 * Collection is already registered in the SensorCollectionRegistry. This
	 * call is for the platform to act if necessary.
	 * 
	 * @see SensorCollectionRegistry
	 * @param registration
	 *            - A reference to the entry in SensorCollectionRegistry.
	 * @return SensorCollectionRegistrationResponseCode
	 */
	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationCreated(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "called");
	

		ArduinoDevice registeredDevice = null;
		for(ArduinoDevice device : manager.getDevices()) {
			if(registration.getSerialNumber().equalsIgnoreCase(device.getSerialNumber())) {
				registeredDevice = device;
				break;
			}
		}
		
		if(registeredDevice != null) {			
			registeredDevice.Connect();			
			logger.log(Level.INFO, "Successfully registered sensor collection " + registration.getSerialNumber());
			return SensorCollectionRegistrationResponseCode.ADD_OK;
		}
		return SensorCollectionRegistrationResponseCode.UNABLE_TO_HANDLE_REGISTRATION_REQUEST;
	}

	/**
	 * Called when a sensor collection that is already registered has been
	 * updated. The Sensor Collection is already updated in the
	 * SensorCollectionRegistry. This call is for the platform to act if
	 * necessary.
	 * 
	 * @see SensorCollectionRegistry
	 * @param registration
	 *            - A reference to the entry in SensorCollectionRegistry.
	 * @return SensorCollectionRegistrationResponseCode - Indicating
	 */
	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationUpdated(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "called");
		registration.getSettings();
		return SensorCollectionRegistrationResponseCode.ADD_OK;
	}
	
	/**
	 * Called when a sensor collection that is already registered has been
	 * removed. The Sensor Collection is already removed in the
	 * SensorCollectionRegistry. This call is for the platform to act if
	 * necessary.
	 * 
	 * @see SensorCollectionRegistry
	 * @param registration
	 *            - A reference to the entry in SensorCollectionRegistry.
	 * @return SensorCollectionRegistrationResponseCode - Indicating
	 */
	public SensorCollectionRegistrationResponseCode sensorCollectionRegistrationDeleted(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "called");
		return SensorCollectionRegistrationResponseCode.DELETE_OK;
	}	
	
	/**
	 * A request to update the current status of a sensor collection
	 * registration.
	 * 
	 * @param regisration
	 *            - The registration to update containing registered values.
	 * 
	 * @return The updated SensorCollectionRegistration.
	 * @see SensorCollectionRegistration
	 */
	public SensorCollectionRegistration updateSensorCollectionStatus(SensorCollectionRegistration registration) {
		logger.log(Level.INFO, "updateSensorCollectionStatus called.");
		registration.setStatus(200);
		return registration;		
	}

	/**
	 * Called from Sensation requesting an update of the status of the data
	 * collector.
	 * 
	 * @return DataCollectorStatus populated with the current status of the data
	 *         collector.
	 */
	public DataCollectorStatus updateDataCollectorStatus() {
		DataCollectorStatus result = new DataCollectorStatus();
		result.setBluetoothMacAddress(bluetoothConnector.getBluetoothAddress());
		result.setHardwareTypeId(AppIoTContract.RPi3_HARDWARE_TYPE_ID);
		result.setFirmwareVersion(FIRMWARE_VERSION);
		result.setHardwareVersion(HARDWARE_VERSION);
		result.setSoftwareVersion(SOFTWARE_VERSION);
		result.setStatus(DataCollectorStatusCode.OK);
		result.setNetworkCards(manager.getNetworkCards());
		return result;
	}
	
	/**
	 * Called from Sensation requesting the system to reboot.
	 * 
	 * @return RebootResponseCode
	 */
	public RebootResponseCode reboot() {		
		logger.log(Level.INFO, "reboot called.");
		manager.reboot();
		return RebootResponseCode.OK;
	}

	/**
	 * Called from Sensation requesting the application to restart.
	 * 
	 * @return RestartApplicationResponseCode
	 */
	public RestartApplicationResponseCode restartApplication() {
		// Handle application restart.	
		logger.log(Level.INFO, "restartApplication called.");
		return RestartApplicationResponseCode.OK;
	}

	/**
	 * Called from Sensation requesting the data collector to add wifi setting.
	 * 
	 * @param networkSetting
	 *            the wifi setting to add.
	 * @return NetworkSettingResponseCode
	 */
	public NetworkSettingResponseCode addNetworkSetting(NetworkSetting networkSetting) {
		// Handle new network configuration settings.
		logger.log(Level.INFO, "addNetworkSettings called.");
		return NetworkSettingResponseCode.OK;
	}
	
	/**
	 * Called when Sensation distributes a FOTA update package for system
	 * update.
	 * 
	 * @param updatePackage
	 *            The update package.
	 * @return UpdatePackageResponseCode - Indicating result of update process.
	 * @see UpdatePackage
	 * @see UpdatePackageResponseCode
	 */
	public UpdatePackageResponseCode updateSystem(UpdatePackage updatePackage) {
		// Handle FOTA update of system / OS.
		logger.log(Level.INFO, "updateSystem called.");
		return UpdatePackageResponseCode.OK;
	}

	/**
	 * Called when Sensation distributes a FOTA update package for application
	 * update.
	 * 
	 * @param updatePackage
	 *            The update package.
	 * @return UpdatePackageResponseCode - Indicating result of update process.
	 * @see UpdatePackage
	 * @see UpdatePackageResponseCode
	 */
	public UpdatePackageResponseCode updateApplication(UpdatePackage updatePackage) {
		// Handle FOTA update of application.
		logger.log(Level.INFO, "updateApplication called.");
		return UpdatePackageResponseCode.OK;
	}
	
	/**
	 * Called when Sensation distributes a FOTA update package for sensor
	 * collection update.
	 * 
	 * @param registration
	 *            - The registered sensor collection to update.
	 * @param updatePackage
	 *            The update package.
	 * @return UpdatePackageResponseCode - Indicating result of update process.
	 * @see UpdatePackage
	 * @see UpdatePackageResponseCode
	 */
	public UpdatePackageResponseCode updateSensorCollection(SensorCollectionRegistration registration, UpdatePackage updatePackage) {
		logger.log(Level.INFO, "Update Sensor Collection called.");
		
		String serialNumber = registration.getSerialNumber();
		UpdatePackageResponseCode responseCode = UpdatePackageResponseCode.OK;
		ArduinoDevice device = manager.getDeviceBySerialNumber(serialNumber);
		if(device != null) {
			try {				
				int result = manager.flashDevice(device, updatePackage.getFile());
				if(result != 0) {
					logger.severe("Failed to flash device");
					responseCode = UpdatePackageResponseCode.FAILED_TO_APPLY;
				} else {
					logger.severe("Device successfully flashed!");
					device.Connect();
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Failed flash device", e);
				responseCode = UpdatePackageResponseCode.UNKNOWN_ERROR;
			}
		}
		return responseCode;
	}
	
	/**
	 * Called from Sensation requesting the data collector to be deleted.
	 * 
	 * @param forceDelete
	 *            - If true, ignore errors.
	 * @return DataCollectorDeleteResponseCode
	 */
	public DataCollectorDeleteResponseCode deleteDataCollector(boolean forceDelete) {
		logger.log(Level.INFO, "Delete Data Collector called.");
		return DataCollectorDeleteResponseCode.OK;
	}

	/**
	 * Called from Sensation when using custom commands.
	 * 
	 * @param correlationId
	 *            - Id to identify this request, use id when responding back to
	 *            Sensation.
	 * @param actorId
	 *            - The id of the microservice in Sensation.
	 * @param payloadType
	 *            - Indicates what kind of payload.
	 * @param payload
	 *            - The payload handle.
	 */
	public void handleCustomCommand(String correlationId, String actorId, String payloadType, String payload) {
		logger.log(Level.INFO, "handle Custom Command called.");
		logger.log(Level.INFO, "Payload type: " + payloadType);
		logger.log(Level.INFO, "Payload: " + payload);		
		client.sendCustomCommandResponse(correlationId, actorId, payloadType, payload);
	}

	/**
	 * Called when measurements are sent to Sensation telling if sensing the
	 * measurements was successful or not.
	 * 
	 * @param measurementsSent
	 *            - A list of measurements with acknowledge parameter set.
	 */
	public void acknowledgeMeasurementsSent(List<ISensorMeasurement> measurementsSent) {

		
	}

	/**
	 * Called from Sensation when settings for a data collector is updated.
	 * @param properties @see {@link SensationClientProperties}
	 */
	public void updateDataCollectorSettings(SensationClientProperties properties) {
		// TODO: Handle updated gateway settings		
	}
}
