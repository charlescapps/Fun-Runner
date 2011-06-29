package xanthanov.droid.funrun.persist; 

import xanthanov.droid.funrun.*; 
import xanthanov.droid.gplace.*; 

import java.util.List; 
import java.util.ArrayList; 
import java.io.Serializable; 
import java.io.FileInputStream; 
import java.io.ObjectOutputStream; 
import java.io.ObjectInputStream; 

public class RunData implements Serializable{
	private List<GoogleDirections> myRuns; 

	public RunData(String filename) {
		ObjectInputStream is = new ObjectInputStream(new FileInputStream(filename));
		
	}

}
