package com.companyx.sensor.platformx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sigma.sensation.dto.NetworkSetting;
import se.sigma.sensation.gateway.sdk.client.data.NetworkCard;
import se.sigma.sensation.gateway.sdk.client.platform.ConnectivitySettings;

public class SimpleLinuxManager {

	private final Logger logger = Logger.getLogger(this.getClass().getName()); 
	
	public List<NetworkCard> getNetworkCards() {
    	List<NetworkCard> result = new Vector<NetworkCard>(); 
		try {
			// Ethernet
			ConnectivitySettings eth0 = getConnectivitySettings("eth0");
			if(eth0 != null) {
				NetworkCard cardeth0 = new NetworkCard();
				cardeth0.setName("eth0");
				cardeth0.setiPv4Address(eth0.getIpAddress());
				cardeth0.setiPv4Subnet(eth0.getSubnet());
				cardeth0.setiPv4Gateway(eth0.getGateway());
				cardeth0.setStatus("200");
				result.add(cardeth0);
			}
			
			// Wi-Fi
			ConnectivitySettings wlan0 = getConnectivitySettings("wlan0");
			if(wlan0 != null) {
				NetworkCard cardwlan0 = new NetworkCard();
				cardwlan0.setName("wlan0");
				cardwlan0.setiPv4Address(wlan0.getIpAddress());
				cardwlan0.setiPv4Subnet(wlan0.getSubnet());
				cardwlan0.setiPv4Gateway(wlan0.getGateway());
				cardwlan0.setStatus("200");
				result.add(cardwlan0);
			}
         } catch (Exception e) {
        	 logger.log(Level.WARNING, "Failed to list get network cards.", e);
         }
         return result;
     }
	
	
   /**
     * Calls OS ifconfig to parse out IP address assigned to adapter.
	 * @param networkAdapter The adapter to query.
	 * @return IP Address assigned.
	 */
	public ConnectivitySettings getConnectivitySettings(String networkAdapter) {
		ConnectivitySettings result = null; 
 	
		try {
			
			String[] cmd = {
					"/bin/sh",
					"-c",
					"ifconfig " + networkAdapter + " | grep inet"
					};			

			logger.fine("executing command on OS: " + cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			int exitCode = p.exitValue();
			logger.fine("ifconfig process exited with status code: " + exitCode);

			if(exitCode != 0) {
		        BufferedReader output = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		        String info = output.readLine();
				logger.severe("Failed to execute ifconfig: " + info);
				return result;
			}

			BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        String info = output.readLine();
	        logger.fine("ifconfig result for " + networkAdapter + ": " + info);
	        
	        result = new ConnectivitySettings();
	        StringTokenizer st = new StringTokenizer(info);
	        
	        while(st.hasMoreElements()) {
	        	String infoPart = st.nextToken();
	        	if(infoPart.contains(":")) {
	        		StringTokenizer st2 = new StringTokenizer(infoPart, ":");
	        		String key = st2.nextToken();
	        		String value = st2.nextToken();
	        		 
	        		if(key.equalsIgnoreCase("addr")) {
	        			result.setIpAddress(value);
	        		} else if(key.equalsIgnoreCase("Mask")) {
	        			result.setSubnet(value);
	        		} else {
	        			result.setGateway(value);
	        		}
	        	}
	        }
	     
	     } catch (Exception e) {
	       	 logger.log(Level.WARNING, "Failed to parse network connectivity info for " + networkAdapter + ". " + e.getMessage(), e);
	     }
	     return result;
	 }

	public int executeBash(String command) throws IOException, InterruptedException {
		String[] cmd = {"/bin/sh", "-c", command};	
		
		int exitCode = 0;
		logger.fine("executing bash script on OS: " + command);
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		exitCode = p.exitValue();
		logger.fine("Linux process exited with status code: " + exitCode);

		if(exitCode != 0) {
	        BufferedReader output = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String info = "";
			while((info = output.readLine()) != null) {
				logger.log(Level.SEVERE, info);
			}
			logger.severe("Failed to execute script: " + info);
		}

		BufferedReader output = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String info = "";
		while((info = output.readLine()) != null) {
			logger.log(Level.INFO, info);
		}
        logger.fine("Bash script successfully executed");
        return exitCode;
	}
	
	public int reboot() {	
		try {
			executeBash("reboot");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to reboot data collector.", e);
			return 500;
		}
		return 200;
	}
	
	public int addNetworkSetting(NetworkSetting networkSettings) {
		int exitCode = 0;
		
		String ssid = networkSettings.getSsid();
		String psk = networkSettings.getEncryptionKey();
		
		String[] rows = new String[] {"network={", "\tssid=\"" + ssid + "\"", "\tpsk=\"" + psk + "\"", "}\n" };
		try {
			for(String row : rows) {
				exitCode = executeBash("sudo echo '" + row + "' >> /etc/wpa_supplicant/wpa_supplicant.conf");	
			}
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to add wifi settings.", e);
		} 
		
		return exitCode;
	}	
	
	public int restartWifi() {
		int exitCode = 0;
		try {
			exitCode = executeBash("sudo ifdown wlan0");
			exitCode = executeBash("sudo ifup wlan0");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed to add wifi settings.", e);
		}
		return exitCode;
	}
}
