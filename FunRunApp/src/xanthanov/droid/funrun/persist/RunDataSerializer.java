package xanthanov.droid.funrun.persist; 

import xanthanov.droid.funrun.*; 
import xanthanov.droid.gplace.*; 

import java.util.Date; 
import java.util.List; 
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.io.File;   
import java.io.Serializable; 
import java.io.FileInputStream; 
import java.io.FileOutputStream; 
import java.io.ObjectOutputStream; 
import java.io.ObjectInputStream; 
import java.io.FilenameFilter; 

import java.io.IOException; 

public class RunDataSerializer { 
	
	private static File dataDir = null; 
	private static SimpleDateFormat directoryFormat = new SimpleDateFormat("MM_dd_yyyy_HH_mm_ss"); 
	private static final String baseRunFile = "base_run.ser";
	private static FilenameFilter legFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.contains("leg_"); 	
		}
	};

	public static void setDataDir(File dataDir) {
		RunDataSerializer.dataDir = dataDir; 
	}

	public static String dateToDirString(Date d) {
		return directoryFormat.format(d); 
	}

	public static boolean createRunDir(GoogleDirections gd) {
		
		if (gd.size() > 0) {
			System.err.println("Must create new run directory from empty directions"); 
			return false; 
		}

		String dirName = dateToDirString(gd.getDate()); 
		File runDir = new File(dataDir.getPath() + File.separatorChar + dirName); 

		if (runDir.exists()) {
			System.err.println("Run directory already exists: " + dirName); 
			return false; 
		}
		
		if ( runDir.mkdir() == false) {
			return false;
		}

		String baseFileName = runDir.getPath() + File.separatorChar + baseRunFile; 		

		ObjectOutputStream oos = null; 

		try {
			oos = new ObjectOutputStream(new FileOutputStream(baseFileName));
			oos.writeObject(gd); //Write empty Google Directions
			oos.close();
		} 
		catch (Exception e) {
			e.printStackTrace(); 
			return false; 
		}
		 
		return true; 
	}	

	public static boolean writeLegToFile(GoogleDirections parentDirections, GoogleLeg leg, int index) {
		String dirName = dateToDirString(parentDirections.getDate()); 
		File runDir = new File(dataDir.getPath() + File.separatorChar + dirName); 
		String legFileName = runDir.getPath() + File.separatorChar + "leg_" + index;  

		if (!runDir.exists()) {
			System.err.println("Attempt to write a leg when run directory didn't exist: " + runDir.getPath());
			return false; 
		}

		ObjectOutputStream oos = null; 

		try {
			oos = new ObjectOutputStream(new FileOutputStream(legFileName));
			oos.writeObject(leg); //Write empty Google Directions
			oos.close();
		} 
		catch (Exception e) {
			e.printStackTrace(); 
			return false; 
		}
		return true;
	}

	public static boolean deleteEmptyRun(GoogleDirections directions) {
		String dirName = dateToDirString(directions.getDate()); 
		File runDir = new File(dataDir.getPath() + File.separatorChar + dirName); 
		
		if (!runDir.exists()) {
			System.err.println("Attempt to delete non-existent run: " + runDir); 
			return false; 
		}

		return runDir.delete(); 	
	}

	//Returns a FunRunData by reading in all the subdirectories and building the GoogleDirections objects for each dir
	public static FunRunData getFunRunData() throws IOException, SecurityException, ClassNotFoundException {

		FunRunData data = new FunRunData(); 
		ObjectInputStream ois = null; 
		File[] runDirs = dataDir.listFiles(); 		

		for (int i = 0; i < runDirs.length; i++) {
			if (!runDirs[i].exists() || !runDirs[i].isDirectory()) {
				System.err.println("Found a non-directory in the data directory: " + runDirs[i].getPath()); 
				continue; 
			}
			System.out.println("Getting run in dir: " + runDirs[i]); 

			String baseFileName = runDirs[i].getPath() + File.separatorChar + baseRunFile; 
			ois = new ObjectInputStream(new FileInputStream(baseFileName));
			GoogleDirections baseDirections = (GoogleDirections) ois.readObject(); 
			ois.close(); 

			File[] legs = runDirs[i].listFiles(legFilter); 			

			if (legs.length <= 0 ) { //If we somehow got an empty run stored on the SD card, delete it. 
				File baseFile = new File(baseFileName); 
				boolean deleteSuccess = baseFile.delete(); 
				deleteSuccess = deleteSuccess && runDirs[i].delete(); 
				System.err.println(deleteSuccess ? "Successfully deleted empty folder" : "Failed to delete empty folder: " + runDirs[i].getPath() );				
				continue; 
			}

			java.util.Arrays.sort(legs, new AbcFileComparator()); 

			for (int j = 0; j < legs.length; j++) {
				System.out.println("Leg Filename #" + j + ": " + legs[i].getPath()); 
				GoogleLeg legToAdd;
				try {
					ois = new ObjectInputStream(new FileInputStream(legs[j].getPath()));
					legToAdd = (GoogleLeg) ois.readObject(); 
				}
				catch (Exception e) { //Just skip the file if an error occurs reading it in. Tolerance = good + more robust
					System.err.println("Failed to read GoogleLeg from file: " + legs[i]); 
					e.printStackTrace(); 
					continue; 
				}
				finally {
					if (ois != null) {
						ois.close(); 
					}
				}

				baseDirections.add(legToAdd); 
						
			}

			data.add(baseDirections); 
			
		}

		return data; 
	}


}

