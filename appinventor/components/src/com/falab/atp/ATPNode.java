package com.falab.atp;

import java.util.ArrayList;
import java.util.List;

public final class ATPNode {

	public String ipv4 = "255.255.255.255";
	public String mac = "001122334455";
	public String name = "dummy";
	public byte state = 'a';
	public String state_msg = "unkown";

	public boolean isScenarioSeeked = false;
	public int selected = 0;
	final List<String> scenarios = new ArrayList<>();

	public ATPNode() {

	}

	@Override
	public String toString() {

		return "[" + mac + "]\n[" + state_msg + "]\n" + name;
	}

}
