package com.falab.atp;

import java.net.InetAddress;

public interface UDPEventListener {
	void onPacket(InetAddress clientAddress, byte[] data, int length);
}
