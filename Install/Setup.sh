#!/bin/bash

# Install required libraries
sudo apt-get update
sudo apt-get install -y arduino-mk bluez bluez-tools bluez-hcidump libncurses5-dev libncursesw5-dev libbluetooth-dev

# Clear caches
sudo rm -rf /var/lib/apt/lists/*
sudo rm -rf /var/cache/apt/*
sudo rm -rf /var/cache/debconf/*

# Disable bluetooth pnat support as there seems to be a bug which stops proper operation with pnat enabled. 
# Full details can be found here: https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=690749
sudo sed -i '2i\
DisablePlugins = pnat\
' /etc/bluetooth/main.conf

# Run bluetoothd in compatibility mode to provide deprecated command line interfaces
sudo sed -i 's,ExecStart=/usr/lib/bluetooth/bluetoothd,ExecStart=/usr/lib/bluetooth/bluetoothd -C,' /lib/systemd/system/bluetooth.service

sudo hciconfig hci0 noauth
sudo systemctl daemon-reload
sudo invoke-rc.d bluetooth restart
sudo hciconfig hci0 up
sudo sdptool add SP
sudo hciconfig hci0 piscan

# Install oracle jdk
sudo mkdir /opt/jdk
sudo wget --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/8u65-b17/jdk-8u65-linux-arm32-vfp-hflt.tar.gz"
sudo tar -zxf jdk-8u65-linux-arm32-vfp-hflt.tar.gz -C /opt/jdk
sudo rm ./jdk-8u65-linux-arm32-vfp-hflt.tar.gz
sudo update-alternatives --install /usr/bin/java java /opt/jdk/jdk1.8.0_65/bin/java 100
sudo update-alternatives --install /usr/bin/javac javac /opt/jdk/jdk1.8.0_65/bin/javac 100

# Setup Sensation home folder
mkdir /home/pi/SENSATION_HOME
mkdir /home/pi/SENSATION_HOME/logs
mv sensation-client.properties /home/pi/SENSATION_HOME/sensation-client.properties
mv ticket.json /home/pi/SENSATION_HOME/ticket.json
mv Gateway.sh /home/pi/SENSATION_HOME/Gateway.sh
mv RPiDemoGateway-Complete-0.9-jar-with-dependencies.jar /home/pi/SENSATION_HOME/RPiDemoGateway-Complete-0.9-jar-with-dependencies.jar

# make start script executable
chmod 755 /home/pi/SENSATION_HOME/Gateway.sh

# Make start script run at boot
sudo sed -i '19i\
sudo /home/pi/SENSATION_HOME/Gateway.sh &
' /etc/rc.local


# Expand sd card partition
# sudo raspi-config --expand-rootfs

sudo reboot