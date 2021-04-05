package com.falab.atp;

public final class OpCode {
	public static final byte OPCODE_EOL_CHAR = ';';
	public static final String OPCODE_FIND = "!F";
	public static final String OPCODE_INFO = "!I";
	public static final String OPCODE_SCENARIO_GET = "!C";
	public static final String OPCODE_SCENARIO_LOAD = "!L";

	public static final String OPCODE_START = "!S";
	public static final String OPCODE_STOP = "!X";
	public static final String OPCODE_GET_DATA = "!G";
	public static final String OPCODE_SET_DATA = "!D";

	public static final byte STATE_INIT = 'a';
	public static final byte STATE_REQUEST_SCENARIO = 'b';
	public static final byte STATE_READY = 'c';
	public static final byte STATE_REQUEST_PLAY = 'd';
	public static final byte STATE_PLAY = 'e';

}
