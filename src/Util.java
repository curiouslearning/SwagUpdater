/*
 * The MIT License (MIT)

Copyright (c) 2014 The Dalai Lama Center

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

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	private static String MAC_OS = "MAC";
	private static String WIN_OS = "WINDOWS";
	private List<String> listOfFilesWrittenTo = new LinkedList<String>();
	
	private static final String PATTERN = 
	        "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
	        "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
	private String returnOS(){

	    String currentOs = System.getProperty("os.name").toUpperCase();
	    if( currentOs.contains(MAC_OS)){
	        currentOs = MAC_OS;
	    }
	    else if( currentOs.contains(WIN_OS) ){
	        currentOs = WIN_OS;
	    }
	    else{
	        currentOs = "Linux";
	    }
	    return currentOs;
	}
	
	public Boolean isWindows()
	{
		if(returnOS().equals(WIN_OS))
			return true;
		else 
			return false;
	}
	
	public boolean writeToFile(String fileName, String dataToWrite)
	{
		try (FileWriter fr = new FileWriter(new File(fileName)))
		{
			BufferedWriter writer = new BufferedWriter(fr);
			
			writer.write(dataToWrite);
			
			writer.close();
			listOfFilesWrittenTo.add(fileName);
			return true;
		}
		catch(IOException e)
		{
			System.out.println("File Exception: " + e);
			return false;
		}
	}
	
	public void removeAllWrittenFiles()
	{
		for(String file : listOfFilesWrittenTo)
		{
			if(!new File(file).delete())
				System.out.println("Unable to delete file: " + file);
			
			//For hidden files
			new File("." + file).delete();
		}
		listOfFilesWrittenTo.clear();	
	}
	
	public static boolean validateIp(final String ip){          

	      Pattern pattern = Pattern.compile(PATTERN);
	      Matcher matcher = pattern.matcher(ip);
	      return matcher.matches();             
	}
	
}
