import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

import IA.AutoPilot;
import Map.EndOfMapException;
import Map.Map;
import Map.Path;
import Map.Truck;

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
	    play_ia();
		//System.out.println(map.print());
	}
	
	private void play_ia() {
		System.out.println("Map for ia");
		System.out.println(map.print());
		AutoPilot autoPilot = new AutoPilot(map);
		
		String path_str = "";
		while(!map.getDeliveries().isEmpty()){
			LinkedList<Point> destinations = map.getDeliveries();
			Point truckPosition = map.convert0BasedTo1Based(map.getTruckPosition());
			Path path = autoPilot.getPath(truckPosition, destinations);
			path_str += play_ia_walk(truckPosition, path);
		}
		
		System.out.println("[Action] " + path_str);
		
	}
	
	private String play_ia_walk(Point truckPosition, Path path) {
		String path_str = ""; 
		for (Point point : path.getPath()) {
			String input = AutoPilot.getDirection(truckPosition, point);
			System.out.println("Direction: " + input);
			System.out.println("TruckX: " + truckPosition.getX() + "TruckY: " + truckPosition.getY());
			System.out.println("DestinationX: " + point.getX() + "DestinationY: " + truckPosition.getY());
			path_str += input + "";
			
			try {
				map.makeMove(input);
				//map.update();
			} catch (EndOfMapException e) {
				System.out.println(e.getMessage());
				
			}
			truckPosition = point;
			System.out.println(map.print());
		}
		return path_str;
	}

}
