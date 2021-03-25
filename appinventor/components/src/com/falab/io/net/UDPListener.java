package com.falab.io.net;

public interface UDPListener {

	void onUDPPacket(byte[] data, int length);

}
