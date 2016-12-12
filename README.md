# AppIoT RPi3 Demo Integration

This is a Demo Integration to describe the effort needed in order to integrate a sensor platform to AppIoT.
This Demo Integration use Java Gateway SDK.

Before starting with this demo, please make sure you have completed the following steps described in the following link: https://github.com/ApplicationPlatformForIoT/RPi3-Integration-Device. Once done, connect the Arduino Nano to the Rpi3 using a USB cable.

# Preparare workstation
Download and install oracle java: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

Make sure java.exe is in your path. https://www.java.com/en/download/help/path.xml

To edit source code, download and install eclipse: http://www.eclipse.org/downloads/packages/eclipse-ide-java-developers/mars2

Download and install Putty: http://www.putty.org/ (Used to access Linux Shell)

Download and install WinSCP: https://winscp.net/eng/download.php (Used to transfer files to RPi3)

Download Win32DiskImager: http://sourceforge.net/projects/win32diskimager/files/latest/download

# Prepare RPi3
- Download Raspian Lite: https://downloads.raspberrypi.org/raspbian_lite_latest and unpack the zip file. See chapter Quick route to skip ahead.
- Insert your SD Card and then run Win32DiskImager.exe. Make sure your SD Card drive is selected.  Select the '#.img' image file from the extracted zip file and then press write.  Once completed, insert your SD card into the RPi, attach an ethernet cable a monitor before you start up. Once started the IP address will be printed in the console.
In order to be able to connect over ssh you need to enable the ssh server on your rpi3. In the shell type sudo raspi-config. Follow the listed selections below to enable ssh:
- 7 Advanced Options 
- A4 Enable/Disable remote command line access to your pi using SSH 
- Enable SSH Server 
- Yes
Now you will be able to start a ssh session with yoyr rpi3.

If using a button and LED with your RPi3 (recommended to get feedback and control) follow the wiring diagram here: https://github.com/ApplicationPlatformForIoT/RPi3-Integration/blob/master/wiring.png

## Install AppIoT client and dependencies
- Startup WinSCP and connect to the IP address of the RPi. Default username and password for raspian is pi/raspberry.
- Transer files inside the install folder https://github.com/ApplicationPlatformForIoT/RPi3-Integration/tree/master/Install to the /home/pi/ folder of the RPi3.
- Startup Putty and connect to the IP address of the RPi. Default username and password for raspian is pi/raspberry.
- Make the Setup.sh script executable by using the following command: chmod 755 ./Setup.sh
- Install AppIoT client by executing the Setup script: ./Setup.sh. This will install dependencies and setup the home directory (/home/pi/SENSATION_HOME). Once completed, the RPi will reboot.



# Get the MAC Address
To get the MAC-Address of the bluetooth chip type the following command in the shell: cat /sys/class/bluetooth/hci0/device/hci0/address
Use the MAC Address to generate a QR-Code for the Deployment Application to use when registering the gateway.
There are several websites to use when generating a QR-Code, here is one http://www.qr-code-generator.com/ 

Enter the following as content of the QR-Code: G;MAC ADDRESS;10000
- G = Gateway
- MAC ADDRESS = The Mac Address you got from the RPi. But you need to add colon between the hex pairs like 12:AB:21:AD:BA:32
- 10000 = Hardware Type Id of the gateway type registered in AppIoT.
Valid content would be G;12:AB:21:AD:BA:32;10000

Once generated, you could print the QR-Code and put it on the outside of the RPi to easily scan when registering using the Deployment Application.

# AppIoT registration
# Gateway Type
Create a gateway type in AppIoT Settings -> Hardware Types -> Gateway Types -> Create
- Name: RPi3
- Type ID: 10000

A Default template for the gateway is automatically created and we do not need to do any changes to it.

# Register the gateway using the deployment application
! Currently this only works for Android, iOS example is being prepared.
Download the deployment application at https://play.google.com/store/apps/details?id=se.sigma.sensation
When starting the app, your can setup wifi settings that you later can send to a gateway when registering it. The app scans for available wifi networks just to let you skip entering the SSID by hand. Select a network to use or create one from scratch by clicking "Add custom network". Enter SSID (if creating from scratch) and password. 
Next you will be prompted to scan a qr code found in "the mail". For the Ericsson demo environment, use the following QR-code: https://github.com/JoakimHellberg/SENS-BY-SIGMA/blob/master/QR-Code-Setup-Deploy-App.png
Login to AppIoT using your AppIoT account.
Select the Device network to use (Click on your name at the top right corner to see the list of available device networks)

Before you continue, make sure that the bluetooth connection is up and running on the device. If you have mounted a button on your RPi3 as this wiring scheme (https://github.com/ApplicationPlatformForIoT/RPi3-Integration/blob/master/wiring.png) describes, then just push the button. Otherwise you need to ground pin 40 for a short moment, simulating a button press.

Navigate to a location and select "Register Gateway".
Click scan QR-Code and scan the QR-Code you created in the "Get the MAC Address" section above in the documentation.
If everything is setup properly, you should now be presented with a form where you name the gateway, select template etc. When done press Register. Now the deployment application will download the registration ticket and send it over bluetooth to the gateway together with the wifi settings configured when starting up the app.

# Register the device
With the device connected to the Rpi3 (using a USB Cable) you can now register the device using the deployment application. 
Once again, navigate to a location. Select "Register Device" and then "Scan QR-Code".
If everything is setup properly, you should now be presented with a form where you name the device, select template etc. At the bottom of the form you select what gateway to register the device to. You should see that the gateway you connected the device to is presented with an appendix looking something like "- signal (-45)". The -45 is the signal strength or QoS indicator. -45 is statically coded in this example. When done press Register.

Now browse to appiot administration UI to see data flowing in from the device.

