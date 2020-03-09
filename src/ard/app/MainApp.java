package ard.app;

import ard.app.serial.SerialRead;

public class MainApp {
	
	public static void main(String[] args) {
	//	System.out.println("BisMillah");
		SerialRead srlRead = new SerialRead();
		srlRead.initialize();
		srlRead.readSerialData();
		HandleFile.startSysWatchHandleFile();
	}

}
