package com.falab.io.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public final class UDPServer implements Runnable {

	private static final int UDP_SERVER_PORT = 3334;
	private static final int UDP_SERVER_RECONNECT = 3000;
	private static final int UDP_SERVER_MAX_LENGTH = 32;
	public static final UDPServer INSTNACE;

	static {
		INSTNACE = new UDPServer();
	}

	public static void setListener(UDPListener l) {
		UDPServer.INSTNACE._listener = l;
	}

	private final Thread _thread;
	private UDPListener _listener = null;

	private UDPServer() {
		_thread = new Thread(this);
		_thread.start();
	}

	@Override
	public void run() {
		byte[] buffer = new byte[UDPServer.UDP_SERVER_MAX_LENGTH];
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		DatagramSocket socket = null;
		while (true) {
			// Auto-Reconnect.
			try {
				socket = new DatagramSocket(UDPServer.UDP_SERVER_PORT);
				socket.setSoTimeout(0);
				while (true) {
					socket.receive(packet);
					if (_listener != null) {
						_listener.onUDPPacket(buffer, packet.getLength());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (socket != null) {
					socket.close();
				}
			}
			try {
				Thread.sleep(UDPServer.UDP_SERVER_RECONNECT);
			} catch (Exception e) {

			}
		}
	}
}
