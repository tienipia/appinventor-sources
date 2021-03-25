package com.falab.io.net;

import java.io.IOException;
import java.net.InetAddress;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

@DesignerComponent(version = 1, category = ComponentCategory.CONNECTIVITY, nonVisible = true, description = "FaLAB UDP Extension", iconName = "aiwebres/packet.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET," + "android.permission.WRITE_EXTERNAL_STORAGE,"
		+ "android.permission.READ_EXTERNAL_STORAGE," + "android.permission.CHANGE_WIFI_MULTICAST_STATE,"
		+ "android.permission.ACCESS_WIFI_STATE," + "android.permission.ACCESS_NETWORK_STATE")
public final class UDP extends AndroidNonvisibleComponent implements UDPListener {

	private final ComponentContainer container;

	public UDP(ComponentContainer container) {
		super(container.$form());
		this.container = container;

		UDPServer.setListener(this);
	}

	@SimpleFunction(description = "Broadcast UDP Packet")
	public void broadcast(String message, int port) {
		if (message != null) {
			try {
				UDPClient.send(message.getBytes(), getBroadcastAddress(), port, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private InetAddress getBroadcastAddress() throws IOException {
		WifiManager wifi = (WifiManager) container.$context().getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcp = wifi.getDhcpInfo();

		int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
		byte[] quads = new byte[4];
		for (int k = 0; k < 4; k++) {
			quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
		}
		return InetAddress.getByAddress(quads);
	}

	@SimpleFunction(description = "Check WiFi is connected")
	public boolean isWifiConnected() {
		try {
			WifiManager wifiMgr = (WifiManager) container.$context().getSystemService(Context.WIFI_SERVICE);
			if (wifiMgr.isWifiEnabled()) {
				WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
				if (wifiInfo.getNetworkId() == -1) {
					return false; // Not connected to an access point
				}
				return true; // Connected to an access point
			} else {
				return false; // Wi-Fi adapter is OFF
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onUDPPacket(byte[] data, int length) {
		if (length > 1) {
			if (data[0] == 0) {
				container.$form().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						receive("[Blob]");
					}
				});
			} else {
				final String text = new String(data, 1, length).trim();
				container.$form().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						receive(text);
					}
				});
			}
		}

	}

	@SimpleEvent(description = "Receive UDP Data")
	public void receive(String text) {
		EventDispatcher.dispatchEvent(this, "receive", text);
	}

	@SimpleFunction(description = "Send UDP Packet")
	public void send(String message, String ip, int port) {
		if (message != null && ip != null) {
			UDPClient.send(message.getBytes(), ip, port);
		}
	}
}
