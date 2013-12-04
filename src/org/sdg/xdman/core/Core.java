package org.sdg.xdman.core;

public class Core {
	
	public static void createProcess(String cmd) {
		try {
			Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void virusScan(String antivirTxt) {
		createProcess(antivirTxt);
	}

	public static void hungUp(String hungUpTxt) {
		createProcess(hungUpTxt);
	}

	public static void executeCommand(String cmdTxt) {
		createProcess(cmdTxt);
	}

}
