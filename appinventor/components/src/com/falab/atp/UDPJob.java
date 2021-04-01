package com.falab.atp;

import java.net.InetAddress;

public class UDPJob {
	public interface UDPEvent {

		void response(String jobName, long elapsed, String ipv4, byte[] data);
	}

	public enum UDPEventType {
		TIMEOUT, DATA;
	}

	public byte[] send_data = null;
	public boolean send_broadcast = false;
	public InetAddress target_addr;

	public int cnt = 0;
	public int max_retries = 3;
	public final long t_init;
	public long t_last = 0;
	public long t_elapsed = 0;

	public final UDPEvent func;
	public boolean single_response = true;

	public UDPJob(UDPEvent func) {
		t_init = System.currentTimeMillis();
		this.func = func;
	}

}
