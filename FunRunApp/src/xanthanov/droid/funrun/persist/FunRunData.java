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

	public void trim() {
		for (int i =0; i < myRuns.size(); i++) {
			for (int j = 0; j < myRuns.get(i).size(); j++) {
				if (myRuns.get(i).get(j).size() <= 0 ) { //If a leg is empty
					myRuns.get(i).getLegs().remove(j); 
				}
			}

			System.err.println("Trimming Directions # " + i + ", size = " + myRuns.get(i).size()); 
			if (myRuns.get(i).size() <= 0) {
				myRuns.remove(i); 
			}

		}
	}

}
