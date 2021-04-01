package com.falab.atp;

public final class OpCode {
	public static final byte[] FIND_NODE = "!f".getBytes();
	public static final byte[] INFO_NODE = "!i".getBytes();
	public static final byte[] START_NODE = "(s)".getBytes();
	public static final byte[] STOP_NODE = "(x)".getBytes();

	public static final byte[] SET_CONFIG_NODE = "[s".getBytes();
	public static final byte[] SET_OFFSET_NODE = "[o".getBytes();
	public static final byte[] GET_SCENARIO_NODE = "[g".getBytes();
	public static final byte[] LOAD_SCENARIO_NODE = "[l".getBytes();

	/**
	 * Not Supported
	 */
	public static final byte[] WRITE_SCENARIO_NODE = "[w".getBytes();
}
