package com.falab.atp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ButtonBase;
import com.google.appinventor.components.runtime.CheckBox;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.Spinner;
import com.google.appinventor.components.runtime.Switch;
import com.google.appinventor.components.runtime.VerticalArrangement;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.YailList;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

@DesignerComponent(version = 1, category = ComponentCategory.EXTENSION, nonVisible = true, description = "FaLAB UDP Extension", iconName = "aiwebres/fa-lab.png")
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET," + "android.permission.WRITE_EXTERNAL_STORAGE,"
		+ "android.permission.READ_EXTERNAL_STORAGE," + "android.permission.CHANGE_WIFI_MULTICAST_STATE,"
		+ "android.permission.ACCESS_WIFI_STATE," + "android.permission.ACCESS_NETWORK_STATE")
public final class ATP extends AndroidNonvisibleComponent implements ViewListener.Button<ATPNode>, UDPJob.UDPEvent {

	private final ComponentContainer mContainer;

	private final Context mContext;
	private final Handler mHandler = new Handler();
	private final Random mRandom = new Random();
	private final Set<String> mIPs = new HashSet<>();
	private final Map<String, ATPNode> mNodes = new HashMap<>();
	private Notifier mNotifier = null;
	private RUDP mRUDP = null;
	private HVArrangement mLayout = null;

	public ATP(ComponentContainer container) {
		super(container.$form());
		mContainer = container;
		mContext = container.$context();
		mRUDP = new RUDP();
	}

	private void _layout_init() {
		ViewGroup mRoot = (ViewGroup) mLayout.getView();

		/**
		 * HVArrangement : FrameLayout[frameContainer;ViewGroup] ->
		 * LinearLayout[viewLayout;LinearLayout]
		 */
		/* Clean Layout */
		mRoot = (ViewGroup) mRoot.getChildAt(0);
		mRoot.removeAllViews();

		VerticalArrangement itemRoot;
		HVArrangement line;
		HVArrangement wrap;
		LayoutParams lp;
		ButtonBase btn;
		boolean isStart = true;

		/* Body */
		for (ATPNode node : mNodes.values()) {
			itemRoot = new VerticalArrangement(mLayout);
			itemRoot.Width(Component.LENGTH_FILL_PARENT);
			itemRoot.Height(Component.LENGTH_PREFERRED);
			itemRoot.AlignHorizontal(ComponentConstants.GRAVITY_CENTER_HORIZONTAL);

			if (isStart) {
				wrap = _view_add_arrangement(itemRoot);
				wrap.WidthPercent(98);
				wrap.Height(1);
				wrap.BackgroundColor(Component.COLOR_WHITE);
				lp = _view_get_lp(wrap);
				lp.topMargin = 10;
				lp.bottomMargin = 10;
				_view_set_lp(wrap, lp);
				isStart = false;
			}

			line = _view_add_arrangement(itemRoot, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL);
			line.Width(Component.LENGTH_FILL_PARENT);
			line.Height(Component.LENGTH_PREFERRED);

			lp = _view_get_lp(line);
			lp.topMargin = 15;
			lp.leftMargin = 10;
			lp.rightMargin = 10;
			lp.bottomMargin = 15;
			_view_set_lp(line, lp);

			wrap = _view_add_arrangement(line);
			wrap.WidthPercent(70);
			wrap.AlignHorizontal(ComponentConstants.GRAVITY_LEFT);
			wrap.AlignVertical(ComponentConstants.GRAVITY_BOTTOM);
			node.view_Label = _view_add_label(wrap, node.name);
			node.view_Label.FontSize(24.0f);
			node.view_Label.Width(Component.LENGTH_FILL_PARENT);

			wrap = _view_add_arrangement(line);
			wrap.WidthPercent(30);
			wrap.AlignHorizontal(ComponentConstants.GRAVITY_CENTER_HORIZONTAL);
			wrap.AlignVertical(ComponentConstants.GRAVITY_BOTTOM);
			node.view_ChkBox = _view_add_checkbox(wrap);
			node.view_ChkBox.Enabled(false);
			node.view_ChkBox.Checked(false);

			node.view_Spinner = _view_add_spinner(itemRoot);
			node.view_Spinner.ElementsFromString("시나리오 선택");
			node.view_Spinner.SelectionIndex(1);
			node.view_Spinner.WidthPercent(98);
			node.view_Spinner.Height(Component.LENGTH_PREFERRED);
			lp = _view_get_lp(node.view_Spinner);
			lp.topMargin = 15;
			lp.bottomMargin = 15;
			_view_set_lp(node.view_Spinner, lp);

			line = _view_add_arrangement(itemRoot, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL);
			line.Width(Component.LENGTH_FILL_PARENT);
			line.Height(Component.LENGTH_PREFERRED);
			lp = _view_get_lp(line);
			lp.topMargin = 15;
			lp.bottomMargin = 15;
			_view_set_lp(line, lp);

			/* Button 1 */
			btn = _view_add_button(line, "btn1", "불러오기", node, ATP.this);
			lp = _view_get_lp(btn);
			lp.leftMargin = 5;
			lp.rightMargin = 5;
			_view_set_lp(btn, lp);

			/* Button 2 */
			btn = _view_add_button(line, "btn2", "준비", node, ATP.this);
			lp = _view_get_lp(btn);
			lp.leftMargin = 5;
			lp.rightMargin = 5;
			_view_set_lp(btn, lp);

			/* Button 3 */
			btn = _view_add_button(line, "btn3", "시작", node, ATP.this);
			lp = _view_get_lp(btn);
			lp.leftMargin = 5;
			lp.rightMargin = 5;
			_view_set_lp(btn, lp);

			/* Button 4 */
			btn = _view_add_button(line, "btn4", "정지", node, ATP.this);
			lp = _view_get_lp(btn);
			lp.leftMargin = 5;
			lp.rightMargin = 5;
			_view_set_lp(btn, lp);

			wrap = _view_add_arrangement(itemRoot);
			wrap.WidthPercent(98);
			wrap.Height(1);
			wrap.BackgroundColor(Component.COLOR_WHITE);
			lp = _view_get_lp(wrap);
			lp.topMargin = 15;
			lp.bottomMargin = 15;
			_view_set_lp(wrap, lp);

			node.init = true;
		}
	}

	public HVArrangement _view_add_arrangement(ComponentContainer container) {
		return _view_add_arrangement(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL);
	}

	public HVArrangement _view_add_arrangement(ComponentContainer container, int orientation) {
		HVArrangement ar = new HVArrangement(container, orientation, ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
		return ar;
	}

	private ButtonBase _view_add_button(ComponentContainer container, final String btn_id, String text,
			final ATPNode node, final ViewListener.Button<ATPNode> listener) {
		ButtonBase btn = new ButtonBase(container) {

			@Override
			public void click() {
				listener.click(this, btn_id, node);
			}

		};
		btn.Text(text);
		btn.Enabled(false);
		node.view_buttons.put(btn_id, btn);
		return btn;
	}

	private CheckBox _view_add_checkbox(ComponentContainer container) {
		CheckBox cb = new CheckBox(container);
		return cb;
	}

	private Label _view_add_label(ComponentContainer container, String text) {
		Label lb = new Label(container);
		lb.Text(text);
		return lb;
	}

	private Spinner _view_add_spinner(ComponentContainer container) {
		AndroidViewComponent ss = new AndroidViewComponent(container) {

			@Override
			public View getView() {
				// TODO Auto-generated method stub
				return null;
			}

		};

		Spinner sp = new Spinner(container);
		return sp;
	}

	private Switch _view_add_switch(ComponentContainer container) {
		Switch sw = new Switch(container);
		return sw;
	}

	public LayoutParams _view_get_lp(final AndroidViewComponent c) {
		View v = c.getView();
		return (LayoutParams) v.getLayoutParams();
	}

	public void _view_set_lp(final AndroidViewComponent c, LayoutParams lp) {
		c.getView().setLayoutParams(lp);
	}

	@Override
	public void click(ButtonBase btn, String btn_id, ATPNode node) {
		if (btn_id == "btn1") {
			node.scenarios.clear();
			mNotifier.ShowProgressDialog("불러오기", "시나리오를 불러옵니다.");

			UDPJob udpjob = new UDPJob(this);
			udpjob.send_data = (OpCode.OPCODE_SCENARIO_GET + "0;").getBytes();
			udpjob.target_addr = node.addr;
			mRUDP.send(OpCode.OPCODE_SCENARIO_GET, udpjob);
		} else if (btn_id == "btn2") {
			int select = node.view_Spinner.SelectionIndex() - 2;
			if (select < 0) {
				mNotifier.ShowTextDialog("시나리오가 선택되지 않았습니다.", "에러", false);
			} else {
				mNotifier.ShowProgressDialog("불러오기", "시나리오를 불러옵니다.");
				UDPJob udpjob = new UDPJob(this);
				udpjob.send_data = (OpCode.OPCODE_SCENARIO_LOAD + node.view_Spinner.Selection() + ";").getBytes();
				udpjob.target_addr = node.addr;
				mRUDP.send(OpCode.OPCODE_SCENARIO_LOAD, udpjob);
			}
		} else if (btn_id == "btn3") {
			int select = node.view_Spinner.SelectionIndex() - 2;
			if (select < 0) {
				mNotifier.ShowTextDialog("시나리오가 선택되지 않았습니다.", "에러", false);
			} else {
				UDPJob udpjob = new UDPJob(this);
				udpjob.send_data = (OpCode.OPCODE_START).getBytes();
				udpjob.target_addr = node.addr;
				udpjob.with_offset = true;
				mRUDP.send(OpCode.OPCODE_START, udpjob);
			}
		} else if (btn_id == "btn4") {

			UDPJob udpjob = new UDPJob(this);
			udpjob.send_data = (OpCode.OPCODE_STOP + ";").getBytes();
			udpjob.target_addr = node.addr;
			mRUDP.send(OpCode.OPCODE_STOP, udpjob);

		}
	}

	@SimpleFunction
	public void Func_findNode() throws YailRuntimeError, UnknownHostException {
		if (mLayout == null) {
			throw new YailRuntimeError("Not Init", "[IGN]");
		}
		mIPs.clear();
		mNotifier.ShowProgressDialog("노드를 찾고있습니다.", "알림");

		UDPJob udpjob = new UDPJob(this);
		udpjob.send_data = (OpCode.OPCODE_FIND + ";").getBytes();
		udpjob.target_addr = InetAddress.getByName("255.255.255.255");
		udpjob.single_response = false;
		udpjob.send_broadcast = true;
		udpjob.max_retries = 2;

		mRUDP.send(OpCode.OPCODE_FIND, udpjob);
	}

	@SimpleFunction
	public void Func_init(HVArrangement layout, Notifier notifier) throws YailRuntimeError {
		if (mLayout != null) {
			mNotifier.DismissProgressDialog();
			return;
		}
		if (!(layout instanceof VerticalArrangement)) {
			throw new YailRuntimeError("VerticalArrangement is required..", "[IGN]");
		}
		mLayout = layout;
		mNotifier = notifier;

	}

	@SimpleFunction
	public void Func_StartAllNode() throws YailRuntimeError, UnknownHostException {
		if (mLayout == null) {
			throw new YailRuntimeError("Not Init", "[IGN]");
		}
		UDPJob udpjob = new UDPJob(this);
		udpjob.send_data = (OpCode.OPCODE_START).getBytes();
		udpjob.target_addr = InetAddress.getByName("255.255.255.255");
		udpjob.single_response = false;
		udpjob.with_offset = true;
		udpjob.send_broadcast = true;
		udpjob.max_retries = 3;
		mRUDP.send(OpCode.OPCODE_START, udpjob);

	}

	@SimpleFunction
	public void Func_StopAllNode() throws YailRuntimeError, UnknownHostException {
		if (mLayout == null) {
			throw new YailRuntimeError("Not Init", "[IGN]");
		}
		UDPJob udpjob = new UDPJob(this);
		udpjob.send_data = (OpCode.OPCODE_STOP + ";").getBytes();
		udpjob.target_addr = InetAddress.getByName("255.255.255.255");
		udpjob.single_response = false;
		udpjob.send_broadcast = true;
		udpjob.max_retries = 3;
		mRUDP.send(OpCode.OPCODE_STOP, udpjob);

	}

	@SimpleFunction
	public void Func_updateNode() throws YailRuntimeError, UnknownHostException {
		if (mLayout == null) {
			throw new YailRuntimeError("Not Init", "[IGN]");
		}

		UDPJob udpjob = new UDPJob(this);
		udpjob.send_data = (OpCode.OPCODE_INFO + ";").getBytes();
		udpjob.target_addr = InetAddress.getByName("255.255.255.255");
		udpjob.single_response = false;
		udpjob.send_broadcast = true;
		udpjob.max_retries = 1;

		mRUDP.send(OpCode.OPCODE_INFO, udpjob);

	}

	@Override
	public void response(String opCode, long elapsed, String ipv4, final byte[] data) {
		if (opCode.equals(OpCode.OPCODE_FIND)) {
			if (data == null) {
				mNodes.clear();
				for (String ip : mIPs) {
					try {
						ATPNode node = new ATPNode();
						node.ipv4 = ip;
						node.addr = InetAddress.getByName(node.ipv4);

						UDPJob udpjob = new UDPJob(this);
						udpjob.send_data = (OpCode.OPCODE_GET_DATA + "atpname;").getBytes();
						udpjob.target_addr = node.addr;
						mRUDP.send(OpCode.OPCODE_GET_DATA, udpjob);
						mNodes.put(ip, node);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						_layout_init();
						mNotifier.DismissProgressDialog();
					}
				}, RUDP.UDP_READ_TIMEOUT);
			} else {
				mIPs.add(ipv4);
			}
		} else if (opCode.equals(OpCode.OPCODE_INFO)) {
			if (data != null && data.length == 1) {
				final ATPNode node = mNodes.get(ipv4);
				if (node != null && node.init) {
					node.elapsed = elapsed;
					node.state = data[0];
					if (node.view_Label != null && node.view_buttons.size() == 4) {
						mHandler.post(new Runnable() {
							@Override
							public void run() {

								node.view_Label.Text(node.name + " [" + String.valueOf(node.elapsed) + "]");

								switch (node.state) {
								case 'a': // STATE_INIT
									node.view_Spinner.getView().setEnabled(true);
									node.view_buttons.get("btn1").Enabled(true);
									node.view_buttons.get("btn2").Enabled(true);
									node.view_buttons.get("btn3").Enabled(false);
									node.view_buttons.get("btn4").Enabled(false);
									break;
								case 'b': // STATE_REQUEST_SCENARIO
									node.view_Spinner.getView().setEnabled(false);
									node.view_buttons.get("btn1").Enabled(false);
									node.view_buttons.get("btn2").Enabled(false);
									node.view_buttons.get("btn3").Enabled(false);
									node.view_buttons.get("btn4").Enabled(false);
									break;
								case 'c': // STATE_READY
									node.view_Spinner.getView().setEnabled(false);
									node.view_buttons.get("btn1").Enabled(false);
									node.view_buttons.get("btn2").Enabled(false);
									node.view_buttons.get("btn3").Enabled(true);
									node.view_buttons.get("btn4").Enabled(false);
									break;
								case 'd': // STATE_REQUEST_PLAY
									node.view_Spinner.getView().setEnabled(false);
									node.view_buttons.get("btn1").Enabled(false);
									node.view_buttons.get("btn2").Enabled(false);
									node.view_buttons.get("btn3").Enabled(false);
									node.view_buttons.get("btn4").Enabled(false);
									break;
								case 'e': // STATE_PLAY
									node.view_Spinner.getView().setEnabled(false);
									node.view_buttons.get("btn1").Enabled(false);
									node.view_buttons.get("btn2").Enabled(false);
									node.view_buttons.get("btn3").Enabled(false);
									node.view_buttons.get("btn4").Enabled(true);
									break;
								}

							}
						});
					}
				}
			}

		} else if (opCode.equals(OpCode.OPCODE_GET_DATA)) {
			if (data != null && data.length != 0) {
				ATPNode node = mNodes.get(ipv4);
				if (node != null) {
					String strData = new String(data);
					String[] kv = strData.split("=", 2);
					if (kv.length == 2) {
						if (kv[0].equals("atpname")) {
							node.elapsed = elapsed;
							node.name = kv[1];
						}
					}
				}
			}
		} else if (opCode.equals(OpCode.OPCODE_SCENARIO_GET)) {
			if (data != null) {
				if (data.length == 0) {
					// No more data;
					final ATPNode node = mNodes.get(ipv4);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Spinner sp = node.view_Spinner;
							String[] rr = new String[node.scenarios.size() + 1];
							rr[0] = "시나리오 선택";
							for (int i = 0; i < node.scenarios.size(); i++) {
								rr[i + 1] = node.scenarios.get(i);
							}
							sp.Elements(YailList.makeList(rr));
							mNotifier.DismissProgressDialog();
						}
					});
				} else {
					ATPNode node = mNodes.get(ipv4);
					String scenarios = new String(data);
					String[] tmp = scenarios.split(",");
					for (String scenario : tmp) {
						node.scenarios.add(scenario);
					}
					UDPJob udpjob = new UDPJob(this);
					udpjob.send_data = (OpCode.OPCODE_SCENARIO_GET + String.valueOf(node.scenarios.size()) + ";")
							.getBytes();
					udpjob.target_addr = node.addr;
					mRUDP.send(OpCode.OPCODE_SCENARIO_GET, udpjob);
				}
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mNotifier.DismissProgressDialog();
					}
				});
			}
		} else if (opCode.equals(OpCode.OPCODE_SCENARIO_LOAD)) {
			if (data != null) {
				if (data.length == 0) {
					// No more data;
					final ATPNode node = mNodes.get(ipv4);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mNotifier.DismissProgressDialog();
							node.view_ChkBox.Checked(true);
						}
					});
				} else {
					final ATPNode node = mNodes.get(ipv4);
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mNotifier.DismissProgressDialog();
							mNotifier.ShowTextDialog(new String(data), "알림", false);
						}
					});
				}
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mNotifier.DismissProgressDialog();
						mNotifier.ShowTextDialog("시간 초과", "알림", false);
					}
				});
			}
		}
	}

//	private enum UDP_PACKET_TARGET {
//		REFUSE, FIND_NODE, INFO_NODE, GET_SCENARIO_NODE, LOAD_SCENARIO_NODE, SET_CONFIG_NODE;
//	}
//	private UDP_PACKET_TARGET target = UDP_PACKET_TARGET.REFUSE;
//	private boolean target_finished = false;
//
//	@SimpleFunction(description = "Find Nodes")
//	public synchronized void findNodes(final int timeout) {
//		nodes.clear();
//		target = UDP_PACKET_TARGET.FIND_NODE;
//		UDPHelper.send(UDPOpCode.INFO_NODE, "255.255.255.255", UDP_MOSI, true);
//		handler.postDelayed(new Runnable() {
//			@Override
//			public synchronized void run() {
//				target = UDP_PACKET_TARGET.REFUSE;
//				onFindNode(YailList.makeList(nodes));
//			}
//		}, timeout);
//	}
//
//	@SimpleFunction(description = "Get Scenarios")
//	public synchronized void findScenarios(final int index, final int timeout) {
//		// SelectionIndex is not zero-base
//		final ATPNode node = getNodeByIndex(index - 1);
//		if (node == null) {
//			return;
//		}
//		if (node.isScenarioSeeked) {
//			onScenarioFound(YailList.makeList(node.scenarios), node.selected);
//		} else {
//			node.scenarios.clear();
//
//			target = UDP_PACKET_TARGET.GET_SCENARIO_NODE;
//			target_finished = false;
//			String data = "[g" + String.valueOf(node.scenarios.size()) + "]";
//			UDPHelper.send(data.getBytes(), node.ipv4, UDP_MOSI, false);
//			handler.postDelayed(new Runnable() {
//				@Override
//				public synchronized void run() {
//					target = UDP_PACKET_TARGET.REFUSE;
//					if (!target_finished) {
//						node.isScenarioSeeked = true;
//						onScenarioFound(YailList.makeList(node.scenarios), node.selected);
//					}
//				}
//			}, timeout);
//		}
//	}
//
//	public String getItem(int index) throws YailRuntimeError {
//		if (index < 0 || index >= lists.size()) {
//			throw new YailRuntimeError("IndexOutOfBoundsException", "[IGN]");
//		}
//
//		return "";
//	}
//
//	public ATPNode getNodeByIndex(int index) {
//		if (index >= nodes.size()) {
//			return null;
//		}
//		return nodes.get(index);
//	}
//
//	public ATPNode getNodeByIp(String ipv4) {
//		if (ipv4 == null) {
//			return null;
//		}
//		for (ATPNode node : nodes) {
//			if (node.ipv4.equals(ipv4)) {
//				return node;
//			}
//		}
//		return null;
//	}
//
//	@SimpleFunction(description = "Load Scenario")
//	public synchronized void loadScenario(final int n_index, final int s_index, final int timeout) {
//		// SelectionIndex is not zero-base
//		final ATPNode node = getNodeByIndex(n_index - 1);
//		if (node == null) {
//			return;
//		}
//
//		if (node.isScenarioSeeked) {
//			String scenario = node.scenarios.get(s_index - 1);
//
//			if (scenario == null) {
//				return;
//			}
//
//			target = UDP_PACKET_TARGET.LOAD_SCENARIO_NODE;
//			target_finished = false;
//			String data = "[l" + scenario + "]";
//			UDPHelper.send(data.getBytes(), node.ipv4, UDP_MOSI, false);
//			handler.postDelayed(new Runnable() {
//				@Override
//				public synchronized void run() {
//					target = UDP_PACKET_TARGET.REFUSE;
//					if (!target_finished) {
//						onScenarioLoad(false, "Timeout");
//					}
//				}
//			}, timeout);
//		} else {
//			handler.post(new Runnable() {
//				@Override
//				public synchronized void run() {
//					onScenarioLoad(false, "Scenarios is not loaded");
//				}
//			});
//		}
//	}
//
//	@SimpleEvent(description = "User tapped and released the button.")
//	public void onConfigSet(boolean success, String message) {
//		EventDispatcher.dispatchEvent(this, "onConfigSet", success, message);
//	}
//
//	@SimpleEvent(description = "User tapped and released the button.")
//	public void onFindNode(YailList list) {
//		EventDispatcher.dispatchEvent(this, "onFindNode", list);
//	}
//
//	@Override
//	public void onPacket(InetAddress clientAddress, byte[] data, int length) {
//		if (target == UDP_PACKET_TARGET.REFUSE || data.length < 2) {
//			return;
//		}
//
//		if (target == UDP_PACKET_TARGET.FIND_NODE) {
//			if (data[0] == 'i' && length > 14) {
//				ATPNode node = new ATPNode();
//				node.mac = new String(data, 2, 12).toUpperCase();
//				node.name = new String(data, 14, length - 14);
//				node.ipv4 = clientAddress.getHostAddress();
//				node.state = data[1];
//				if (data[1] == 'a') { // ready
//					node.state_msg = "대기중";
//				} else if (data[1] == 'b') { // loaded
//					node.state_msg = "준비됨";
//				} else if (data[1] == 'c') { // playing
//					node.state_msg = "작동중";
//				} else if (data[1] == 'd') { // paused
//					// Not Supported
//					node.state_msg = "paused";
//				} else {
//					return;
//				}
//				nodes.add(node);
//			}
//		} else if (target == UDP_PACKET_TARGET.INFO_NODE) {
//			if (data[0] == 'i' && length > 14) {
//				ATPNode node = getNodeByIp(clientAddress.getHostAddress());
//				node.state = data[1];
//				if (node != null) {
//					if (data[1] == 'a') { // ready
//						node.state_msg = "대기중";
//					} else if (data[1] == 'b') { // loaded
//						node.state_msg = "준비됨";
//					} else if (data[1] == 'c') { // playing
//						node.state_msg = "작동중";
//					} else if (data[1] == 'd') { // paused
//						// Not Supported
//						node.state_msg = "paused";
//					} else {
//						return;
//					}
//				}
//			}
//		} else if (target == UDP_PACKET_TARGET.LOAD_SCENARIO_NODE) {
//			if (data[0] == '[' && data[1] == 'l') {
//				final ATPNode node = getNodeByIp(clientAddress.getHostAddress());
//				if (node != null) {
//					if (data[2] == ']') {
//						// end
//						target_finished = true;
//						handler.post(new Runnable() {
//							@Override
//							public synchronized void run() {
//								target = UDP_PACKET_TARGET.REFUSE;
//								onScenarioLoad(true, "Success");
//
//							}
//						});
//					} else {
//						final String message = new String(data, 2, length - 3);
//						target_finished = true;
//						handler.post(new Runnable() {
//							@Override
//							public synchronized void run() {
//								target = UDP_PACKET_TARGET.REFUSE;
//								onScenarioLoad(false, message);
//
//							}
//						});
//					}
//				}
//			}
//		} else if (target == UDP_PACKET_TARGET.GET_SCENARIO_NODE) {
//			if (data[0] == '[' && data[1] == 'g') {
//				final ATPNode node = getNodeByIp(clientAddress.getHostAddress());
//				if (node != null) {
//					if (data[2] == ']') {
//						// end
//						target_finished = true;
//						handler.post(new Runnable() {
//							@Override
//							public synchronized void run() {
//								target = UDP_PACKET_TARGET.REFUSE;
//								node.isScenarioSeeked = true;
//								onScenarioFound(YailList.makeList(node.scenarios), node.selected);
//							}
//						});
//					} else {
//						String scenarios = new String(data, 2, length - 3);
//						String[] tmp = scenarios.split(",");
//						for (String scenario : tmp) {
//							node.scenarios.add(scenario);
//						}
//						String dsata = "[g" + String.valueOf(node.scenarios.size()) + "]";
//						UDPHelper.send(dsata.getBytes(), node.ipv4, UDP_MOSI, false);
//					}
//				}
//			}
//		} else if (target == UDP_PACKET_TARGET.SET_CONFIG_NODE) {
//			if (data[0] == '[' && data[1] == 's') {
//				final ATPNode node = getNodeByIp(clientAddress.getHostAddress());
//				if (node != null) {
//					if (data[2] == ']') {
//						// end
//						target_finished = true;
//						handler.post(new Runnable() {
//							@Override
//							public synchronized void run() {
//								target = UDP_PACKET_TARGET.REFUSE;
//								onConfigSet(true, "Success");
//							}
//						});
//					} else {
//						final String message = new String(data, 2, length - 3);
//						target_finished = true;
//						handler.post(new Runnable() {
//							@Override
//							public synchronized void run() {
//								target = UDP_PACKET_TARGET.REFUSE;
//								onConfigSet(false, message);
//							}
//						});
//					}
//				}
//			}
//		}
//	}
//
//	@SimpleEvent(description = "User tapped and released the button.")
//	public void onScenarioFound(YailList scenarios, int selected) {
//		EventDispatcher.dispatchEvent(this, "onScenarioFound", scenarios, selected);
//	}
//
//	@SimpleEvent(description = "User tapped and released the button.")
//	public void onScenarioLoad(boolean success, String message) {
//		EventDispatcher.dispatchEvent(this, "onScenarioLoad", success, message);
//	}
//
//	@SimpleFunction(description = "Set Node Config")
//	public synchronized void setNodeConfig(final int n_index, String key, String value, int timeout) {
//		if (key == null || value == null) {
//			return;
//		}
//		// SelectionIndex is not zero-base
//		final ATPNode node = getNodeByIndex(n_index - 1);
//		if (node == null) {
//			return;
//		}
//
//		key = key.trim();
//		value = value.trim();
//
//		if (key.length() == 0 || value.length() == 0) {
//			return;
//		}
//
//		target = UDP_PACKET_TARGET.SET_CONFIG_NODE;
//		target_finished = false;
//		String data = "[s" + key + "=" + value + "]";
//		UDPHelper.send(data.getBytes(), node.ipv4, UDP_MOSI, false);
//		handler.postDelayed(new Runnable() {
//			@Override
//			public synchronized void run() {
//				target = UDP_PACKET_TARGET.REFUSE;
//				if (!target_finished) {
//					onConfigSet(false, "Timeout");
//				}
//			}
//		}, timeout);
//	}
//
//	@SimpleFunction(description = "Find Nodes")
//	public synchronized void startNodes() {
//		UDPHelper.send(UDPOpCode.START_NODE, "255.255.255.255", UDP_MOSI, true);
//	}

}
