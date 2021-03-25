package com.falab.io.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class UDPClient implements Runnable {
	private static class UDPJob {
		private byte[] data;
		private InetAddress addr;
		private int port;
		private boolean broadcast;
	}

	public static final UDPClient INSTANCE;
	private static final BlockingQueue<UDPJob> queue = new ArrayBlockingQueue<>(16);

	static {
		INSTANCE = new UDPClient();
	}

	public static void send(byte[] data, InetAddress address, int port) {
		UDPClient.send(data, address, port, false);
	}

	public static void send(byte[] data, InetAddress address, int port, boolean broadcast) {
		if (data == null || data.length == 0) {
			return;
		}
		UDPJob m = new UDPJob();
		m.data = data;
		m.addr = address;
		m.port = port;
		m.broadcast = broadcast;
		UDPClient.queue.add(m);
	}

	public static void send(byte[] data, String address, int port) {
		try {
			UDPClient.send(data, InetAddress.getByName(address), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	private UDPClient() {
		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {
		UDPJob m = null;
		while (true) {
			try {
				m = UDPClient.queue.take();
				if (m.data != null && m.addr != null) {
					try {
						DatagramSocket dsoc = new DatagramSocket();
						dsoc.setBroadcast(m.broadcast);
						DatagramPacket dp = new DatagramPacket(m.data, m.data.length, m.addr, m.port);
						dsoc.send(dp);
						dsoc.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
