package com.companyx.sensor.platformx;

import com.companyx.sensor.platformx.device.DeviceData;

public interface PlatformXListener {
	void onData(DeviceData data);
}
