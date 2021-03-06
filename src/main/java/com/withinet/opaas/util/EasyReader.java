package com.withinet.opaas.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.withinet.opaas.controller.system.Validation;

/**
 * The sole task of this class is to read the paragraphs from disk
 * 
 * @author Folarin Omotoriogun
 *
 */
public class EasyReader {
	
	public static List<String> readFile(String filename) throws IOException {
			// try to read from the specified file and store paragraphs (lines of text 
			// with new-line at end) in list and convert list to array for return
			FileReader fr = new FileReader(filename);
			BufferedReader bfr = new BufferedReader(fr);
			ArrayList<String> content = new ArrayList<String>();
			String paragraph = null;
			while((paragraph = bfr.readLine())!= null){
				content.add(paragraph);
			}
			return content;
	}
	
	public static StringBuffer getString(String filename) {
		// try to read from the specified file and store paragraphs (lines of text 
		// with new-line at end) in list and convert list to array for return
		StringBuffer buffer = new StringBuffer ();
		FileReader fr = null;
		try {
			fr = new FileReader(filename);
			BufferedReader bfr = new BufferedReader(fr);
			
			String paragraph = null;
			while((paragraph = bfr.readLine())!= null){
				buffer.insert(0, paragraph);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return buffer;
}
	public static Map.Entry <Integer, StringBuffer> getString(String filename, int startLine, int stopLine) {
		// try to read from the specified file and store paragraphs (lines of text 
		// with new-line at end) in list and convert list to array for return
		StringBuffer buffer = new StringBuffer ();
		FileReader fr = null;
		int count = 0;
		try {
			Validation.assertNotNull(filename);
			if (!new File (filename).exists())
				return null;
			fr = new FileReader(filename);
			BufferedReader bfr = new BufferedReader(fr);
			String paragraph = null;
			
			while((paragraph = bfr.readLine())!= null){
				if (count >= startLine && count < stopLine)
					buffer.append(paragraph);
				else if (count == stopLine)
					break;
				count++;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (fr != null)
					fr.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new AbstractMap.SimpleEntry<Integer,StringBuffer>(count, buffer);
	}
	
}