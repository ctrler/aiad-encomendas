package feups;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;

import feups.communication.TruckPathCommunication;
import feups.ia.AutoPilot;
import feups.map.EndOfMapException;
import feups.map.Roads;
import feups.map.Path;
import feups.parcel.Parcel;


public class Main {
	
	//Change here to load another file
	public static final String INPUT_FILE = "maps/example1.map";

	//Initialization of the map
	private static Roads map;
	
	//Initialization of the world
	private static World world;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		world = new World();
		
		System.out.println("The file will now be loaded!\n");
		
		Parser parser = new Parser(world);
		if (parser.getDetails())
			System.out.println("Parsing OK");
		else
			System.out.println("Parsing FAIL");
		
		
		AutoPilot autoPilot = new AutoPilot(world.getMap());
		
		Path path1 = autoPilot.getPath(new Point(4,3), new Point(11,11));
		Parcel parcel1;
		
		Path path2 = autoPilot.getPath(new Point(15,3), new Point(7,11));
		Parcel parcel2;
		
		System.out.println(path1.toString());
		System.out.println(world.getMap().printRoute(path1));
		System.out.println(path2.toString());
		System.out.println(world.getMap().printRoute(path2));
		// Determinar o ponto de encontro em path1 e path2
		
				
	}
	
	
	

}
