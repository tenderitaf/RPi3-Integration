package com.ericsson.appiot.demo.integration.rpi3;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import se.sigma.sensation.dto.RegistrationTicket;
import se.sigma.sensation.gateway.sdk.client.SensationClient;
import se.sigma.sensation.gateway.sdk.client.data.RegistrationResponseCode;
import se.sigma.sensation.gateway.sdk.deployment.DeploymentApplicationListener;
import se.sigma.sensation.gateway.sdk.deployment.DeploymentApplicationManager;
import se.sigma.sensation.gateway.sdk.deployment.bluetooth.JSR82Connector;

public class Gateway {
	private final Logger logger = Logger.getLogger(this.getClass().getName()); 

	private RPi3Platform platform;
	private SensationClient sensationClient;
	private DeploymentApplicationManager deploymentApplicationManager;
	
	public static void main(String[] args) {
		Gateway gateway = new Gateway();
		gateway.start();
	}
	
	private void start() {
		logger.log(Level.INFO, "RPi3 Gateway starting up.");

		// Setup platform and startup sensation client
		platform = new RPi3Platform();		
		sensationClient = new SensationClient(platform); 
		sensationClient.start();
		
		// Define GPIO pins for button and led.
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalInput buttonPin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, PinPullResistance.PULL_UP);
		final GpioPinDigitalOutput ledPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29);
		
		// Setup deployment application manager to listen on bluetooth
		deploymentApplicationManager = new DeploymentApplicationManager(
				new MyDeploymentApplicationListener(ledPin), 
				new JSR82Connector() , 
				platform);
		
		
		// If button is pressed, startup deployment application manager.
		// This result in a bluetooth socket to be established.
		buttonPin.addListener(new GpioPinListenerDigital() {
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
            	if(event.getState().isHigh()) {
            		deploymentApplicationManager.start();
                	logger.log(Level.INFO, "Starting deployment interface.");
            	} 
            }
        });		

		// If not yet registered, just start without requiring the user to press the button.
		if(!sensationClient.isRegistered()) {
			deploymentApplicationManager.start();
		}
		
		while(true) {
			try {Thread.sleep(10000);}
			catch(InterruptedException e) {}
		}
	}	
	


	
	private class MyDeploymentApplicationListener implements DeploymentApplicationListener, Runnable {

		private GpioPinDigitalOutput ledPin;
		Thread t;
		boolean stop = false;
		boolean connected = false;
		boolean running = false;
		public MyDeploymentApplicationListener(GpioPinDigitalOutput ledPin) {
			this.ledPin = ledPin;
		}
		
		public void onClientConnected() {
			connected = true;
		}

		public void onRegistrationTicket(RegistrationTicket registrationTicket) {
			RegistrationResponseCode responseCode = sensationClient.register(registrationTicket);
			deploymentApplicationManager.sendGatewayAcknowledge(responseCode);
			deploymentApplicationManager.stop();
		}

		public void onStart() {
			if(!running) {
				stop = false;
				t = new Thread(this);
				t.start();
				ledPin.low();
			}
		}

		public void onStop() {
			running = false;
			stop = true;
			connected = false;
			ledPin.low();

			if(!sensationClient.isRegistered()) {
				deploymentApplicationManager.start();
			}			
		}

		public void onWaiting() {
			connected = false;
		}

		public void run() {
			running = true;
			while(!stop) {
				try {				
					ledPin.high();
					Thread.sleep(1000);
					if(!connected) {
						ledPin.low();
						Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
				}
			}
			running = false;
		}

		@Override
		public void onClientDisConnected() {
			// TODO Auto-generated method stub
			
		}		
	}
}
