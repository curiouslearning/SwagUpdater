/*
 * The MIT License (MIT)

Copyright (c) 2014 The The Global Literacy Project

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class SwagUpdater {

    private static String username, password;
    private static ServerConnect server;
    private static String userCredentialsFile = "userCredentials.txt";
    private static String cmd = "";
    
	public static void main(String[] args) {
		
		
    	
    	Util util = new Util();
    	
    	//restart the server
       executeCommand("adb kill-server");
       executeCommand("adb start-server");
       
    	if(util.isWindows())
    		cmd = "cmd /c ";
		
		//Check credentials before doing any processing
        server = new ServerConnect();
        
        if(!readFromFile(userCredentialsFile))
        	getUserCredentials();
        
        //Connect to the tablet
        connectToDevices();
        
        //Update the serial number
        //updateSerialNumber();
        
        //Update the label
        
        //Update the Applock
        updateAppLock();
        
        System.out.println("Done");
	}
	
	
	
	public static void updateAppLock()
	{
		
    	String adbAndSerial = "adb ";
    	
		//push the db files to the tablet
		executeCommand(adbAndSerial + " push gr_pref.xml /sdcard/");
		executeCommand(adbAndSerial + " push applock.db /sdcard/");
		executeCommand(adbAndSerial + " com.morrison.applocklite_preferences.xml /sdcard/");
		

    	//Get the user info for com.morrison.applocklite
    	String packageListing = adbAndSerial + " shell \"cat /sdcard/dataOutput.txt\"";
    	
    	String[] resultSet = readCommandResponse(executeCommand(packageListing)).split("\n");
    	String morrisonString = "";
    	for(String result : resultSet)
    	{
    		if(result.contains("com.morrison.applocklite"))
    		{
				morrisonString = result;
				break;
    		}
    	}
    	System.out.println("morrisonString: " + morrisonString);
    	String userIdOfMorrison = morrisonString.split(" ")[1];

    	try{
    	
    		String content = new Scanner(new File("morrisonInstaller.sh")).useDelimiter("\\Z").next().replace("~~~", userIdOfMorrison).replace("\r", "");
    		Util util = new Util();
    		util.writeToFile("morrisonInstallerComplete.sh", content);
    		executeCommand(adbAndSerial + " push morrisonInstallerComplete.sh /sdcard/");
    		executeCommand(adbAndSerial + "shell \"cat /sdcard/morrisonInstallerComplete.sh | sh\"");
    	}
    	catch(IOException e){System.out.println("Error Reading morrisonInstaller File" + e);}
	}
	
    private static String readCommandResponse(BufferedReader reader)
    {
    	String commandResponse = "";
    	String line;
    	if(reader == null)
    		return "";
    	try
    	{
    		commandResponse = reader.readLine();
    		
    		while((line = reader.readLine()) != null)
    		commandResponse += "\n" + line;
    	}
    	catch(IOException e)
    	{
    		System.out.println("IO Exception when reading command response: " + e);
    	}
    	
    	return commandResponse;
    }
	
	private static void connectToDevices()
	{
    	List<String> ipAddresses;
    	
		ipAddresses = getIpAddresses();
    	
		//Connect to the wireless addresses
		for(String ipAddress : ipAddresses)
		{
			executeCommand("adb connect " + ipAddress);	
		}
	}
	
	private static void updateSerialNumber()
	{
		//Use the current timestamp as the new serial number
		String newSerialNumber = "s" + System.currentTimeMillis();
		writeToFile("iSerial", newSerialNumber);
		
		//executeCommand("dos2unix updateSerial.sh");
		executeCommand("adb push updateSerial.sh /sdcard/");
		executeCommand("adb push iSerial /sdcard/");
		executeCommand("adb shell \"cat /sdcard/updateSerial.sh | sh\"");
		
	}
	
    private static List<String> getIpAddresses()
    {
    	List<String> ipAddresses = new LinkedList<String>();
    	
		System.out.println("Please enter the IP address of the tabet in the "
				+ "form of xxx.xxx.xxx.xxx and then hit Enter\n"
				+ "After you have entered in all IP's that you want, type in 'done' and hit 'Enter'");
		
		String userInput = "";
		while(!(userInput = readUserInput()).trim().toLowerCase().equals("done"))
		{
			if(Util.validateIp(userInput))
			{
				ipAddresses.add(userInput);
				System.out.println("Address Saved.");
				break;
			}
			else
				System.out.println("That is not a valid IP address");
		}
		
		return ipAddresses;
    }
	
	private static void getUserCredentials()
	{
		while(true)
        {
            System.out.println("Please enter your username");
            username = readUserInput();
            System.out.println("Please Enter your password");
            password = readUserInput();
            
            System.out.println("Checking...");
            if(!server.checkCredentials(username, password))
                System.out.println("Your credentials don't match.\nPlease try again.");
            else
            {
            	server.saveCredentials(username, password);
            	String usernameAndPassword = username + "\n" + password;
            	writeToFile(userCredentialsFile, usernameAndPassword);
                System.out.println("Your credentials match!");
                break;
            }
        }
	}
	
	private static BufferedReader executeCommand(String command)
    {
    	command = cmd + command;
    	BufferedReader reader = null;
    	
    	try 
    	{
	        Process p = Runtime.getRuntime().exec(command); 
	        Thread.sleep(1000);
	       
	        reader =new BufferedReader(
	            new InputStreamReader(p.getInputStream())
	        );
    	}
    	catch (Exception e)
    	{
    		System.out.println("Exception when trying to execute command: " + e);
    	}

    	return reader;
    }
	
    private static String readUserInput()
    {
        try {
            return new BufferedReader(new InputStreamReader(System.in)).readLine();
        }
        catch (IOException e){}
        
        //If try fails
        return "-1";
    }
    
	public static boolean writeToFile(String fileName, String dataToWrite)
	{
		try (FileWriter fr = new FileWriter(new File(fileName)))
		{
			BufferedWriter writer = new BufferedWriter(fr);
			
			writer.write(dataToWrite);
			
			writer.close();
			return true;
		}
		catch(IOException e)
		{
			System.out.println("File Exception: " + e);
			return false;
		}
	}
	
	private static boolean readFromFile(String file)
	{
		try 
        {
			
			//check if file exists
			if(!(new File(file).exists()))
				return false;
			
        	String line;
        	FileReader fr = new FileReader(file);
        	BufferedReader reader = new BufferedReader(fr);
            
        	//Skip the first line
            username = reader.readLine();
            password = reader.readLine();
            
            //            while((line = reader.readLine()) != null) 
//            { 
//                if(line.length() > 0)
//                {
//                    numberOfDevices++;
//                    devices.add(line);
//                    System.out.println(line);
//                }
//            }
        }
        catch(IOException e1) 
        {
            System.out.println("Exception: " + e1);
        } 
		return true;
	}

}
