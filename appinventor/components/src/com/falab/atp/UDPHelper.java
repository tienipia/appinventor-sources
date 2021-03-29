package com.falab.atp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public final class UDPHelper {

	private static class Client implements Runnable {

		private final BlockingQueue<UDPJob> queue = new ArrayBlockingQueue<>(16);

		@Override
		public void run() {
			UDPJob m = null;
			while (true) {
				try {
					m = queue.take();
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

		private void send(byte[] data, InetAddress address, int port, boolean broadcast) {
			if (data == null || data.length == 0) {
				return;
			}
			UDPJob m = new UDPJob();
			m.data = data;
			m.addr = address;
			m.port = port;
			m.broadcast = broadcast;
			queue.add(m);
		}

	}

	private static class Server implements Runnable {

		private int port;
		private UDPEventListener listener;

		private Server(int port, UDPEventListener listener) {
			this.port = port;
			this.listener = listener;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[UDPHelper.UDP_SERVER_MAX_LENGTH];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			DatagramSocket socket = null;
			while (true) {
				// Auto-Reconnect.
				try {
					socket = new DatagramSocket(port);
					socket.setSoTimeout(0);
					while (true) {
						socket.receive(packet);

						InetAddress clientAddress = packet.getAddress();
						listener.onPacket(clientAddress, buffer, packet.getLength());
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (socket != null) {
						socket.close();
					}
				}
				try {
					Thread.sleep(UDPHelper.UDP_SERVER_RECONNECT);
				} catch (Exception e) {

				}
			}
		}

	}

	private static class UDPJob {
		private byte[] data;
		private InetAddress addr;
		private int port;
		private boolean broadcast;
	}

	private static final UDPHelper _inst;

	static {
		_inst = new UDPHelper();
	}

	private static final int UDP_SERVER_RECONNECT = 3000;
	private static final int UDP_SERVER_MAX_LENGTH = 64;

	public static void send(byte[] data, InetAddress ip, int port, boolean broadcast) {
		UDPHelper._inst.client.send(data, ip, port, broadcast);
	}

	public static void send(byte[] data, String ip, int port) {
		UDPHelper.send(data, ip, port, false);
	}

	public static void send(byte[] data, String ip, int port, boolean broadcast) {

		try {
			UDPHelper.send(data, InetAddress.getByName(ip), port, broadcast);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public static void startServer(int port, UDPEventListener udp_listener) {
		if (port < 1000 || port > 65535) {
			return;
		}
		if (udp_listener == null) {
			return;
		}
		UDPHelper._inst.listen(port, udp_listener);
	}

	private final Map<Integer, Server> servers = new HashMap<>();

	private final Client client;

	private UDPHelper() {
		client = new Client();
		new Thread(client).start();
	}

	private synchronized void listen(final int port, final UDPEventListener udp_listener) {
		Server server = servers.get(port);
		if (server == null) {
			server = new Server(port, udp_listener);
			new Thread(server).start();
		}
	}

}
