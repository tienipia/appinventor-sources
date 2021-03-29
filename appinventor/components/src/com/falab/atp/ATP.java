package com.falab.atp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.util.YailList;

import android.os.Handler;

@DesignerComponent(version = 1, category = ComponentCategory.EXTENSION, nonVisible = true, description = "FaLAB UDP Extension", iconName = "aiwebres/fa-lab.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET," + "android.permission.WRITE_EXTERNAL_STORAGE,"
		+ "android.permission.READ_EXTERNAL_STORAGE," + "android.permission.CHANGE_WIFI_MULTICAST_STATE,"
		+ "android.permission.ACCESS_WIFI_STATE," + "android.permission.ACCESS_NETWORK_STATE")
public final class ATP extends AndroidNonvisibleComponent implements UDPEventListener {

	private enum UDP_PACKET_TARGET {
		REFUSE, FIND_NODE, INFO_NODE, GET_SCENARIO_NODE, LOAD_SCENARIO_NODE, SET_CONFIG_NODE;
	}

	private UDP_PACKET_TARGET target = UDP_PACKET_TARGET.REFUSE;
	private boolean target_finished = false;

	private final ComponentContainer container;

	private final List<ATPNode> nodes = new ArrayList<>();

	private final Handler handler = new Handler();
	private final int UDP_MOSI = 3333;
	private final int UDP_MISO = 3334;

	public ATP(ComponentContainer container) {
		super(container.$form());
		this.container = container;
		UDPHelper.startServer(UDP_MISO, this);
	}

	@SimpleFunction(description = "Find Nodes")
	public synchronized void findNodes(final int timeout) {
		nodes.clear();
		target = UDP_PACKET_TARGET.FIND_NODE;
		UDPHelper.send(UDPOpCode.INFO_NODE, "255.255.255.255", UDP_MOSI, true);
		handler.postDelayed(new Runnable() {
			@Override
			public synchronized void run() {
				target = UDP_PACKET_TARGET.REFUSE;
				onFindNode(YailList.makeList(nodes));
			}
		}, timeout);
	}

	@SimpleFunction(description = "Get Scenarios")
	public synchronized void findScenarios(final int index, final int timeout) {
		// SelectionIndex is not zero-base
		final ATPNode node = getNodeByIndex(index - 1);
		if (node == null) {
			return;
		}
		if (node.isScenarioSeeked) {
			onScenarioFound(YailList.makeList(node.scenarios), node.selected);
		} else {
			node.scenarios.clear();

			target = UDP_PACKET_TARGET.GET_SCENARIO_NODE;
			target_finished = false;
			String data = "[g" + String.valueOf(node.scenarios.size()) + "]";
			UDPHelper.send(data.getBytes(), node.ipv4, UDP_MOSI, false);
			handler.postDelayed(new Runnable() {
				@Override
				public synchronized void run() {
					target = UDP_PACKET_TARGET.REFUSE;
					if (!target_finished) {
						node.isScenarioSeeked = true;
						onScenarioFound(YailList.makeList(node.scenarios), node.selected);
					}
				}
			}, timeout);
		}
	}

	public ATPNode getNodeByIndex(int index) {
		if (index >= nodes.size()) {
			return null;
		}
		return nodes.get(index);
	}

	public ATPNode getNodeByIp(String ipv4) {
		if (ipv4 == null) {
			return null;
		}
		for (ATPNode node : nodes) {
			if (node.ipv4.equals(ipv4)) {
				return node;
			}
		}
		return null;
	}

	@SimpleFunction(description = "Load Scenario")
	public synchronized void loadScenario(final int n_index, final int s_index, final int timeout) {
		// SelectionIndex is not zero-base
		final ATPNode node = getNodeByIndex(n_index - 1);
		if (node == null) {
			return;
		}

		if (node.isScenarioSeeked) {
			String scenario = node.scenarios.get(s_index - 1);

			if (scenario == null) {
				return;
			}

			target = UDP_PACKET_TARGET.LOAD_SCENARIO_NODE;
			target_finished = false;
			String data = "[l" + scenario + "]";
			UDPHelper.send(data.getBytes(), node.ipv4, UDP_MOSI, false);
			handler.postDelayed(new Runnable() {
				@Override
				public synchronized void run() {
					target = UDP_PACKET_TARGET.REFUSE;
					if (!target_finished) {
						onScenarioLoad(false, "Timeout");
					}
				}
			}, timeout);
		} else {
			handler.post(new Runnable() {
				@Override
				public synchronized void run() {
					onScenarioLoad(false, "Scenarios is not loaded");
				}
			});
		}
	}

	@SimpleEvent(description = "User tapped and released the button.")
	public void onConfigSet(boolean success, String message) {
		EventDispatcher.dispatchEvent(this, "onConfigSet", success, message);
	}

	@SimpleEvent(description = "User tapped and released the button.")
	public void onFindNode(YailList list) {
		EventDispatcher.dispatchEvent(this, "onFindNode", list);
	}

	@Override
	public void onPacket(InetAddress clientAddress, byte[] data, int length) {
		if (target == UDP_PACKET_TARGET.REFUSE || data.length < 2) {
			return;
		}

		if (target == UDP_PACKET_TARGET.FIND_NODE) {
			if (data[0] == 'i' && length > 14) {
				ATPNode node = new ATPNode();
				node.mac = new String(data, 2, 12).toUpperCase();
				node.name = new String(data, 14, length - 14);
				node.ipv4 = clientAddress.getHostAddress();
				node.state = data[1];
				if (data[1] == 'a') { // ready
					node.state_msg = "대기중";
				} else if (data[1] == 'b') { // loaded
					node.state_msg = "준비됨";
				} else if (data[1] == 'c') { // playing
					node.state_msg = "작동중";
				} else if (data[1] == 'd') { // paused
					// Not Supported
					node.state_msg = "paused";
				} else {
					return;
				}
				nodes.add(node);
			}
		} else if (target == UDP_PACKET_TARGET.INFO_NODE) {
			if (data[0] == 'i' && length > 14) {
				ATPNode node = getNodeByIp(clientAddress.getHostAddress());
				node.state = data[1];
				if (node != null) {
					if (data[1] == 'a') { // ready
						node.state_msg = "대기중";
					} else if (data[1] == 'b') { // loaded
						node.state_msg = "준비됨";
					} else if (data[1] == 'c') { // playing
						node.state_msg = "작동중";
					} else if (data[1] == 'd') { // paused
						// Not Supported
						node.state_msg = "paused";
					} else {
						return;
					}
				}
			}
		} else if (target == UDP_PACKET_TARGET.LOAD_SCENARIO_NODE) {
			if (data[0] == '[' && data[1] == 'l') {
				final ATPNode node = getNodeByIp(clientAddress.getHostAddress());
				if (node != null) {
					if (data[2] == ']') {
						// end
						target_finished = true;
						handler.post(new Runnable() {
							@Override
							public synchronized void run() {
								target = UDP_PACKET_TARGET.REFUSE;
								onScenarioLoad(true, "Success");

							}
						});
					} else {
						final String message = new String(data, 2, length - 3);
						target_finished = true;
						handler.post(new Runnable() {
							@Override
							public synchronized void run() {
								target = UDP_PACKET_TARGET.REFUSE;
								onScenarioLoad(false, message);

							}
						});
					}
				}
			}
		} else if (target == UDP_PACKET_TARGET.GET_SCENARIO_NODE) {
			if (data[0] == '[' && data[1] == 'g') {
				final ATPNode node = getNodeByIp(clientAddress.getHostAddress());
				if (node != null) {
					if (data[2] == ']') {
						// end
						target_finished = true;
						handler.post(new Runnable() {
							@Override
							public synchronized void run() {
								target = UDP_PACKET_TARGET.REFUSE;
								node.isScenarioSeeked = true;
								onScenarioFound(YailList.makeList(node.scenarios), node.selected);
							}
						});
					} else {
						String scenarios = new String(data, 2, length - 3);
						String[] tmp = scenarios.split(",");
						for (String scenario : tmp) {
							node.scenarios.add(scenario);
						}
						String dsata = "[g" + String.valueOf(node.scenarios.size()) + "]";
						UDPHelper.send(dsata.getBytes(), node.ipv4, UDP_MOSI, false);
					}
				}
			}
		} else if (target == UDP_PACKET_TARGET.SET_CONFIG_NODE) {
			if (data[0] == '[' && data[1] == 's') {
				final ATPNode node = getNodeByIp(clientAddress.getHostAddress());
				if (node != null) {
					if (data[2] == ']') {
						// end
						target_finished = true;
						handler.post(new Runnable() {
							@Override
							public synchronized void run() {
								target = UDP_PACKET_TARGET.REFUSE;
								onConfigSet(true, "Success");
							}
						});
					} else {
						final String message = new String(data, 2, length - 3);
						target_finished = true;
						handler.post(new Runnable() {
							@Override
							public synchronized void run() {
								target = UDP_PACKET_TARGET.REFUSE;
								onConfigSet(false, message);
							}
						});
					}
				}
			}
		}
	}

	@SimpleEvent(description = "User tapped and released the button.")
	public void onScenarioFound(YailList scenarios, int selected) {
		EventDispatcher.dispatchEvent(this, "onScenarioFound", scenarios, selected);
	}

	@SimpleEvent(description = "User tapped and released the button.")
	public void onScenarioLoad(boolean success, String message) {
		EventDispatcher.dispatchEvent(this, "onScenarioLoad", success, message);
	}

	@SimpleFunction(description = "Set Node Config")
	public synchronized void setNodeConfig(final int n_index, String key, String value, int timeout) {
		if (key == null || value == null) {
			return;
		}
		// SelectionIndex is not zero-base
		final ATPNode node = getNodeByIndex(n_index - 1);
		if (node == null) {
			return;
		}

		key = key.trim();
		value = value.trim();

		if (key.length() == 0 || value.length() == 0) {
			return;
		}

		target = UDP_PACKET_TARGET.SET_CONFIG_NODE;
		target_finished = false;
		String data = "[s" + key + "=" + value + "]";
		UDPHelper.send(data.getBytes(), node.ipv4, UDP_MOSI, false);
		handler.postDelayed(new Runnable() {
			@Override
			public synchronized void run() {
				target = UDP_PACKET_TARGET.REFUSE;
				if (!target_finished) {
					onConfigSet(false, "Timeout");
				}
			}
		}, timeout);
	}

	@SimpleFunction(description = "Find Nodes")
	public synchronized void startNodes() {
		UDPHelper.send(UDPOpCode.START_NODE, "255.255.255.255", UDP_MOSI, true);
	}
}
