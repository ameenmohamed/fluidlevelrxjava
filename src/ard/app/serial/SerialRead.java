package ard.app.serial;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import java.time.LocalDateTime; // Import the LocalDateTime class
import java.time.format.DateTimeFormatter; // Import the DateTimeFormatter class

import com.fazecast.jSerialComm.SerialPort;

import ard.app.AppConstants;
import ard.app.SendHttp;

public class SerialRead {
	
	public static BufferedReader input;
	public static OutputStream output;
	/** Milliseconds to block while waiting for port open */
	public static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	public static final int DATA_RATE = 9600;
	static SerialPort ubxPort=null;
	static OutputStream outputStream;
	static Scanner scanStream;
	
	
	
	public void initialize() {
		
		boolean openedSuccessfully =false;
		SerialPort[] ports = SerialPort.getCommPorts();
		System.out.println("***** Available Ports:");
		for (int i = 0; i < ports.length; ++i){
			System.out.println(" [" + i + "] " + ports[i].getSystemPortName() + ": " + ports[i].getDescriptivePortName());		 
			if(ports[i].getDescriptivePortName().contains("USB")){
				ubxPort = ports[i];
				break;
			}
		}
		
		try {
			ubxPort.closePort();
			openedSuccessfully = ubxPort.openPort();
			
			System.out.println("port Name ...."+ubxPort.getSystemPortName()+" Status:"+openedSuccessfully);
			
		} catch (Exception e) {
			System.out.println("Failed Opening " + ubxPort.getSystemPortName() + ": " + ubxPort.getDescriptivePortName() + ": " + openedSuccessfully);
		}
		if(openedSuccessfully){
			System.out.println("Sucess port " + ubxPort.getSystemPortName() + ": " + ubxPort.getDescriptivePortName() + ": " + openedSuccessfully);
		}
		if(ubxPort !=null){
			ubxPort.setBaudRate(DATA_RATE);
			ubxPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
			InputStream instrm = ubxPort.getInputStream();
			if(instrm !=null){
				scanStream = new Scanner(instrm);
			}else {System.out.println("NO INPUT Stream from "+ubxPort.getSystemPortName());}
			 outputStream = ubxPort.getOutputStream();
			}else{
				System.out.println("ubxPort is NULL !!!");
			}
			if (!openedSuccessfully){
				//ubxPort.addDataListener(new SerialPortDataListener());
				return;
			}
		
		
	}
	public String closeConn(){
		StringBuffer sb = new StringBuffer();
		sb.append("port status:");
		sb.append(ubxPort.isOpen());
		if(ubxPort.isOpen()){
		sb.append("exec port close:");
		sb.append(ubxPort.closePort());
		}else{sb.append("port already close");}
		return sb.toString();
	}
	
	public static boolean isPortOpen(){
		if(ubxPort !=null){
		return ubxPort.isOpen();
		}else{
			return false;
		}
	}
	
	public void readSerialData(){
		ReadSerialThread sreader = new ReadSerialThread();
		Thread rdrThread = new Thread(sreader);
		rdrThread.start();
	}


}
class ReadSerialThread  implements Runnable  {
	static DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH:mm:ss");
	static DateTimeFormatter hrlyFile = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH");
	ArrayList<String> fluidLevels = new ArrayList<String>();
	
	public ReadSerialThread() {
	
	}
 
	public void run() {
		while(SerialRead.isPortOpen()){
			LocalDateTime myDateObjStart = LocalDateTime.now();
			String flHourNoext = myDateObjStart.format(hrlyFile);
			String fileNameHourly = flHourNoext+".txt";
			System.out.println("Starting fres .. creating filename "+fileNameHourly);
			
			while(SerialRead.scanStream.hasNextLine()){
				LocalDateTime myDateObj = LocalDateTime.now();
				String lineFeed = SerialRead.scanStream.nextLine();
					
				String timeNow = myDateObj.format(myFormatObj);
				String lineItem = timeNow+ ": "+lineFeed;
			
				if(isSameHour(flHourNoext,timeNow)) {
					if(fluidLevels.size() < AppConstants.listSize) {
						fluidLevels.add(lineItem);
						processLineItem(lineFeed);
					}else {
						try {
						//	System.out.println("Arraylist full writing..");
					
							FileUtils.writeLines(new File(AppConstants.FLUID_FILE_PATH+"/"+fileNameHourly), "UTF-8", fluidLevels,true);
							fluidLevels.clear();
						} catch (IOException e) {
							System.out.println("File write issue"+e.getMessage());
							e.printStackTrace();
						}
					}
					//appendfile
					
				}else {
					//create file
					try {
						fluidLevels.add(lineItem);
						System.out.println("writing all remaining records "+fluidLevels.size());
						FileUtils.writeLines(new File(AppConstants.FLUID_FILE_PATH+"/"+fileNameHourly), "UTF-8", fluidLevels,true);
						fluidLevels.clear();
						fileNameHourly = myDateObjStart.format(hrlyFile)+".txt";
						System.out.println("reset the fle t new hour file"+fileNameHourly);
						FileOutputStream s = FileUtils.openOutputStream(new File(AppConstants.FLUID_FILE_PATH+"/"+fileNameHourly));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			//	System.out.println(timeNow+ ": "+lineFeed);
			
				
				try {Thread.sleep(AppConstants.SLEEP_INTERVAL_SERIAL_READ);	} catch (InterruptedException e) {e.printStackTrace();	}
		
			}	
		}//wtrue
	}

	private void  processLineItem(String lineFeed) {
		Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
		if ( null != lineFeed) {
	      
	   
	    if(pattern.matcher(lineFeed).matches()) { ///if is number 
	    	int _val= Integer.parseInt(lineFeed.trim());
	    	
	    	if (_val > AppConstants.TANK_EMPTY_VALUE) {
	    		System.out.println("TANK IS EMPTY !!!!!!");
	    		SendHttp.sendPost("TANK IS EMPTY  Value recieved :"+lineFeed);
	    		//send update to web 	
	    	
	    	}else if(_val > AppConstants.TANK_ALERT_VALUE ) {
	    		SendHttp.sendPost("TANK needs Refilling  !...");
	    		System.out.println("Tank needs Refillling ..Value recieved :"+lineFeed);
	    		//send update to web 
	    		
	    	}else {
	    		System.out.println("ALL OK ...");
	    	}
	    }
	    }
	    
	    
		
	}

	private boolean isSameHour(String fileNameHourly, String timeNow) {
		String str1= "08-03-2020 03:37:09";
		String str2 = "08-03-2020-03";
	//	System.out.println(timeNow + "   " + fileNameHourly);
		if(timeNow.startsWith(fileNameHourly)) {
		//	System.out.println("Yes same hour");
			return true;
		}
		else
			return false;
	}
	
	
}

