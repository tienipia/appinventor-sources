package com.falab.atp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RUDP implements Runnable {

	private static class UDPServer implements Runnable {

		private RUDP listener;

		private UDPServer(RUDP listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[RUDP.UDP_SERVER_MAX_LENGTH];
			DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
			DatagramSocket socket = null;
			while (true) {
				// Auto-Reconnect.
				try {
					socket = new DatagramSocket(RUDP.UDP_MISO);
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
					Thread.sleep(RUDP.UDP_SERVER_RECONNECT);
				} catch (Exception e) {

				}
			}
		}

	}

	public static final int UDP_MOSI = 3333;
	public static final int UDP_MISO = 3334;

	public static final int UDP_READ_TIMEOUT = 1500;
	public static final int UDP_RESEND_INTERVAL = 300;

	public static final int UDP_SERVER_RECONNECT = 3000;
	public static final int UDP_SERVER_MAX_LENGTH = 64;

	// ip
	private final Map<String, UDPJob> registred_queues = new HashMap<String, UDPJob>();
	private final Map<String, UDPJob> registred_jobs = new HashMap<String, UDPJob>();

	private final ExecutorService executors = Executors.newSingleThreadExecutor();
	private final UDPServer _server;

	public RUDP() {
		_server = new UDPServer(this);
		new Thread(_server).start();
		new Thread(this).start();
	}

	public void onPacket(InetAddress clientAddress, final byte[] data, final int length) {
		if (length >= 3 && data[length - 1] == OpCode.OPCODE_EOL_CHAR) {
			String opCode = new String(data, 0, 2);
			UDPJob j = registred_jobs.get(opCode);

			if (j != null && j.valid) {
				if (j.single_response) {
					j.valid = false;
				}

				j.func.response(opCode, System.currentTimeMillis() - j.t_init, clientAddress.getHostAddress(),
						Arrays.copyOfRange(data, 2, length - 1));
			}
		}
	}

	private synchronized void register_job() {
		registred_jobs.putAll(registred_queues);
		registred_queues.clear();
	}

	@Override
	public void run() {
		long current_t;
		List<String> jobNames = new ArrayList<>();
		while (true) {
			jobNames.clear();
			register_job();
			for (Entry<String, UDPJob> e : registred_jobs.entrySet()) {
				UDPJob j = e.getValue();
				if (j.valid) {
					current_t = System.currentTimeMillis();
					if (current_t > j.t_init + RUDP.UDP_READ_TIMEOUT) {
						j.valid = false;
						j.func.response(e.getKey(), 0, null, null);
					} else if (current_t > (j.t_last + RUDP.UDP_RESEND_INTERVAL) && j.cnt < j.max_retries) {
						if (j.with_offset) {
							String offsetStr = new String(j.send_data);
							offsetStr = offsetStr + String.valueOf(RUDP.UDP_RESEND_INTERVAL * j.cnt) + ";";
							udp_send(offsetStr.getBytes(), j.target_addr, j.send_broadcast);
						} else {
							udp_send(j.send_data, j.target_addr, j.send_broadcast);
						}
						j.t_last = current_t;
						j.cnt++;

					}
				} else {
					jobNames.add(e.getKey());
				}
			}

			for (String jobName : jobNames) {
				registred_jobs.remove(jobName);
			}

			try {
				Thread.sleep(25);
			} catch (Exception e) {

			}
		}

	}

	public synchronized void send(String jobName, UDPJob job) {
		registred_queues.put(jobName, job);
	}

	private void udp_send(final byte[] data, final InetAddress addr, final boolean broadcast) {
		executors.submit(new Runnable() {
			@Override
			public void run() {
				try {
					DatagramSocket dsoc = new DatagramSocket();
					DatagramPacket dp = new DatagramPacket(data, data.length, addr, RUDP.UDP_MOSI);
					dsoc.setBroadcast(broadcast);
					dsoc.send(dp);
					dsoc.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
	}
}
