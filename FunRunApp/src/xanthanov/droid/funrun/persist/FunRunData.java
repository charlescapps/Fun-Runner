package xanthanov.droid.funrun.persist; 

import java.util.List; 
import java.util.ArrayList;
import java.io.Serializable;  
import xanthanov.droid.gplace.*; 

public class FunRunData implements Serializable {

	private List<GoogleDirections> myRuns; 
	//To add: settings? 

	public FunRunData() {
		myRuns = new ArrayList<GoogleDirections>(); 


	}

	public GoogleDirections get(int i) {return myRuns.get(i); }

	public int size() {return myRuns.size(); } 

	public void add(GoogleDirections gd) {
		myRuns.add(gd); 
	}

}
