package com.falab.atp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.appinventor.components.runtime.ButtonBase;
import com.google.appinventor.components.runtime.CheckBox;
import com.google.appinventor.components.runtime.Label;
import com.google.appinventor.components.runtime.Spinner;

public final class ATPNode {
	public boolean init = false;
	public String ipv4 = "255.255.255.255";
	public InetAddress addr = null;
	public String mac = "001122334455";
	public String name = UUID.randomUUID().toString();
	public long elapsed = 0;
	public byte state = 'a';
	public String state_msg = "unkown";
	public int selected = 0;

	public long last_update_t;
	public final List<String> scenarios = new ArrayList<>();

	public Spinner view_Spinner;
	public Label view_Label;
	public CheckBox view_ChkBox;
	public Map<String, ButtonBase> view_buttons = new HashMap<>();

	public ATPNode() {

	}

	@Override
	public String toString() {
		return "[" + mac + "]\n[" + state_msg + "]\n" + name;
	}

}
