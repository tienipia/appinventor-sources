package com.falab.atp;

import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.ComponentConstants;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.runtime.AndroidNonvisibleComponent;
import com.google.appinventor.components.runtime.AndroidViewComponent;
import com.google.appinventor.components.runtime.ButtonBase;
import com.google.appinventor.components.runtime.CheckBox;
import com.google.appinventor.components.runtime.Component;
import com.google.appinventor.components.runtime.ComponentContainer;
import com.google.appinventor.components.runtime.EventDispatcher;
import com.google.appinventor.components.runtime.HVArrangement;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.Notifier;
import com.google.appinventor.components.runtime.Switch;
import com.google.appinventor.components.runtime.VerticalArrangement;
import com.google.appinventor.components.runtime.errors.YailRuntimeError;

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
public final class ATP extends AndroidNonvisibleComponent implements UDPEventListener, ViewListener.Button<ATPNode> {

	private final ComponentContainer mContainer;
	private final Context mContext;
	private final Handler mHandler = new Handler();
	private final List<ATPNode> mNodes = new LinkedList<>();
	private final List<String> mJobQueue = new LinkedList<>();

	private HVArrangement mLayout = null;
	private Notifier mNotifier = null;

	private int UDP_TIMEOUT;
	private int UDP_RETRY;
	private int UDP_INTERVAL;
	private int UDP_MOSI;
	private int UDP_MISO;

	public ATP(ComponentContainer container) {
		super(container.$form());
		mContainer = container;
		mContext = container.$context();
	}

	private synchronized String _job_create() {
		String job_id = UUID.randomUUID().toString();
		mJobQueue.add(job_id);
		return job_id;
	}

	private synchronized boolean _job_finish(String job_id) {
		return mJobQueue.remove(job_id);
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
		for (ATPNode node : mNodes) {
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

			line = _view_add_arrangement(itemRoot, ComponentConstants.LAYOUT_ORIENTATION_HORIZONTAL);
			line.Width(Component.LENGTH_FILL_PARENT);
			line.Height(Component.LENGTH_PREFERRED);
			lp = _view_get_lp(line);
			lp.topMargin = 15;
			lp.bottomMargin = 15;
			_view_set_lp(line, lp);

			/* Button 1 */
			btn = _view_add_button(line, "btn1", "이름 변경", node, ATP.this);
			lp = _view_get_lp(btn);
			lp.leftMargin = 5;
			lp.rightMargin = 5;
			_view_set_lp(btn, lp);

			/* Button 2 */
			btn = _view_add_button(line, "btn2", "이름 변경", node, ATP.this);
			lp = _view_get_lp(btn);
			lp.leftMargin = 5;
			lp.rightMargin = 5;
			_view_set_lp(btn, lp);

			/* Button 3 */
			btn = _view_add_button(line, "btn3", "이름 변경", node, ATP.this);
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
		}
	}

	public HVArrangement _view_add_arrangement(ComponentContainer container) {
		return _view_add_arrangement(container, ComponentConstants.LAYOUT_ORIENTATION_VERTICAL);
	}

	public HVArrangement _view_add_arrangement(ComponentContainer container, int orientation) {
		HVArrangement ar = new HVArrangement(container, orientation, ComponentConstants.NONSCROLLABLE_ARRANGEMENT);
		return ar;
	}

	private ButtonBase _view_add_button(ComponentContainer container, final String id, String text, final ATPNode node,
			final ViewListener.Button<ATPNode> listener) {
		ButtonBase btn = new ButtonBase(container) {

			@Override
			public void click() {
				listener.click(id, node);
			}

		};
		btn.Text(text);
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
	public void click(String id, ATPNode obj) {
		mNotifier.ShowTextDialog(obj.name, id, false);
	}

	@SimpleFunction
	public void Func_findNode(HVArrangement layout) throws YailRuntimeError {
		if (mLayout == null) {
			throw new YailRuntimeError("Not Init", "[IGN]");
		}
		mNotifier.ShowProgressDialog("노드를 찾고있습니다.", "알림");
		final String job_id = _job_create();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (_job_finish(job_id)) {
					Func_findNode_evt();
				}
			}
		}, UDP_TIMEOUT);
	}

	@SimpleEvent
	public void Func_findNode_evt() {
		_layout_init();
		mNotifier.DismissProgressDialog();
		EventDispatcher.dispatchEvent(this, "Func_findNode_evt");
	}

	@SimpleFunction
	public void Func_init(HVArrangement layout, Notifier notifier) throws YailRuntimeError {
		if (mLayout != null) {
			mNodes.clear();
			mJobQueue.clear();
			mNotifier.DismissProgressDialog();
			return;
		}
		if (!(layout instanceof VerticalArrangement)) {
			throw new YailRuntimeError("VerticalArrangement is required..", "[IGN]");
		}
		mLayout = layout;
		mNotifier = notifier;
		UDPHelper.startServer(UDP_MISO, this);
	}

	@SimpleFunction
	public void Func_test() throws YailRuntimeError {
		mNodes.clear();
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		mNodes.add(new ATPNode());
		_layout_init();
	}

	@Override
	public void onPacket(InetAddress clientAddress, byte[] data, int length) {
		// TODO Auto-generated method stub

	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "500", alwaysSend = true)
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
	public void UDP_INTERVAL(int i) {
		if (i >= 0) {
			UDP_INTERVAL = i;
		}
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "3334", alwaysSend = true)
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
	public void UDP_MISO(int i) {
		if (i >= 1000) {
			UDP_MISO = i;
		}
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "3333", alwaysSend = true)
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
	public void UDP_MOSI(int i) {
		if (i >= 1000) {
			UDP_MOSI = i;
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

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "3", alwaysSend = true)
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
	public void UDP_RETRY(int i) {
		if (i >= 0) {
			UDP_RETRY = i;
		}
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_INTEGER, defaultValue = "3000", alwaysSend = true)
	@SimpleProperty(category = PropertyCategory.BEHAVIOR, userVisible = false)
	public void UDP_TIMEOUT(int i) {
		UDP_TIMEOUT = i;
	}
}
