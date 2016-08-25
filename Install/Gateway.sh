#!/bin/bash
SENSATION_HOME=/home/pi/SENSATION_HOME
export SENSATION_HOME

# Ensure bluetooth is up
sudo invoke-rc.d bluetooth restart
sudo hciconfig hci0 up
sudo hciconfig hci0 piscan

java -Djava.library.path=/usr/local/lib -jar /home/pi/SENSATION_HOME/RPiDemoGateway-Complete-0.9-jar-with-dependencies.jar
