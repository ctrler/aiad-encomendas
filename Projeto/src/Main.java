import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import Map.Map;

public class Main {
	
	//Change here to load another file
	public static final String INPUT_FILE = "maps/example1.map";

	//Initialization of the map
	private Map map;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Main main = new Main();
	}
	
	public Main() {
		System.out.println("The file will now be loaded!\n");

	    try {
			map = new Map(INPUT_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		System.out.println(map.print());
	}

}
