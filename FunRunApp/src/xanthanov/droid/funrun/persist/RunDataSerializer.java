package xanthanov.droid.funrun.persist; 

import xanthanov.droid.funrun.*; 
import xanthanov.droid.gplace.*; 

import java.util.List; 
import java.util.ArrayList; 
import java.io.Serializable; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.io.ObjectOutputStream; 
import java.io.ObjectInputStream; 

public class RunDataSerializer { 

	//Returns a FunRunData from the given filename, or null if it fails
	public static FunRunData getFunRunData(String filename) {
		FunRunData data = null; 
		ObjectInputStream ois = null; 
		
		try {
			ois = new ObjectInputStream(new FileInputStream(filename));
			data = (FunRunData) ois.readObject(); 
			ois.close(); 
		}
		catch (Exception e) {
			e.printStackTrace(); 
			return null; 
		}
		return data; 
	}


	//Return true/false if the write was success/failure
	public static boolean writeFunRunData(String filename, FunRunData data) {

		ObjectOutputStream oos = null; 
		try {
			oos = new ObjectOutputStream(new FileOutputStream(filename));
			oos.writeObject(data);
			oos.close();
		} 
		catch (Exception e) {
			e.printStackTrace(); 
			return false; 
		}
		return true; 
	}

}
