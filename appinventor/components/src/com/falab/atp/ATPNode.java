package com.falab.atp;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.appinventor.components.runtime.CheckBox;
import com.google.appinventor.components.runtime.Label;

public final class ATPNode {
	public String ipv4 = "255.255.255.255";
	public String mac = "001122334455";
	public String name = UUID.randomUUID().toString();
	public byte state = 'a';
	public String state_msg = "unkown";

	public int selected = 0;

	public long last_update_t;
	public final List<String> scenarios = new ArrayList<>();

	public Label view_Label;
	public CheckBox view_ChkBox;

	public ATPNode() {

	}

	@Override
	public String toString() {
		return "[" + mac + "]\n[" + state_msg + "]\n" + name;
	}

}
