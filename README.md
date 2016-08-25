# AppIoT RPi3 Demo Integration

This is a Demo Integration to describe the effort needed in order to integrate a sensor platform to AppIoT.
This Demo Integration use Java Gateway SDK.

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

If using a button and LED with your RPi3 (recommended to get feed back and control) follow the wiring diagram here: https://github.com/JoakimHellberg/AppIoT-RPi3-Integration/blob/master/wiring.png 

## Install AppIoT client and dependencies
- Startup WinSCP and connect to the IP address of the RPi. Default username and password for raspian is pi/raspberry.
- Transer files inside the install folder https://github.com/JoakimHellberg/AppIoT-RPi3-Integration-Start/tree/master/Install to the /home/pi/ folder of the RPi3.
- Startup Putty and connect to the IP address of the RPi. Default username and password for raspian is pi/raspberry.
- Make the Setup.sh script executable by using the following command: chmod 755 ./Setup.sh
- Install AppIoT client by executing the Setup script: ./Setup.sh. This will install dependencies and setup the home directory (/home/pi/SENSATION_HOME). Once completed, the RPi will reboot.

## Quick route
In order to get the RPi up and running some preparations are required. All these preparations are provided in the following image: https://appiothackathon.blob.core.windows.net/rpi3/2016-04-07-AppIoT-RPi3DemoGateway.zip. Unpack the zip file to extract the .img file.
Insert your SD Card and then run Win32DiskImager.exe. Make sure your SD Card drive is selected. Select the  'AppIoT-RPi3DemoIntegration.img' image file from the extracted zip file and then press write.

# Get the MAC Address
When the RPi is prepared and rebooted, the RPi will launch the Demo Gateway. In the console the demo gateway will print the MACAddress of the bluetooth chip. Use the MAC Address to generate a QR-Code for the Deployment Application to use when registering the gateway.
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
