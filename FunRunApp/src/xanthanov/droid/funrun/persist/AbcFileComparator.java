package xanthanov.droid.funrun.persist; 

import java.util.Comparator; 
import java.io.File; 

public class AbcFileComparator implements Comparator<File> {
	
	@Override 
	public int compare(File f1, File f2) {
		return f1.getName().compareTo(f2.getName()); 
	}

	@Override
	public boolean equals(Object o) {
		if (this==o) {
			return true; 
		}

		if (AbcFileComparator.class.isInstance(o)) {
			return true; 
		}

		return false; 
	}

}
