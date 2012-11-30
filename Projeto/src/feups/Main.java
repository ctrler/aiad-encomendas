package feups;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

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
		
		
	
		//TESTING RETURNS
		for (String key : world.getTrucks().keySet()) {
		   System.out.println("------------------------------------------------");
		   System.out.println("[TruckInfo]");
		   System.out.println("TruckName: " + key);
		   System.out.println("Position: " + world.getTrucks().get(key).getCurrentPosition().toString());
		   
		   //Testing Return Path
		   AutoPilot autoPilot = new AutoPilot(world.getMap());
		   Point truckPosition = world.getTrucks().get(key).getCurrentPosition();
		   
		   //Print parcels information
		   Iterator<Parcel> iter = world.getTrucks().get(key).getParcels().iterator();
		   while (iter.hasNext()) {
			   Parcel parc = iter.next();
			   System.out.println("\n\t[ParcelInfo]");
			   System.out.println("\tName: " + parc.getNome());
			   System.out.println("\tOrigin: " + parc.getPosition().toString());
			   System.out.println("\tDestination: " + parc.getDestination().getPosition().toString());
			   
			   System.out.println("\n\t\t[PATH: Origin - Destination]");
			   
			   Point pickupPoint = parc.getPosition();
			   Point deliveryPoint = parc.getDestination().getPosition();
			   Path path = autoPilot.getPath(truckPosition, pickupPoint);
			   Path path2 = autoPilot.getPath(pickupPoint, deliveryPoint);
			   System.out.println("\t\t[/PATH]");
			   System.out.println("\t[/ParcelInfo]");
			   
			   String path_str = ""; 
			   
			   //Make the Trucks Move from one position to a destination
			   for (Point point : path.getPath()) {
				    String input = AutoPilot.getDirection(truckPosition, point);
				    System.out.println("Direction: " + input);
				    System.out.println("TruckX: " + truckPosition.getX() + "TruckY: " + truckPosition.getY());
				    System.out.println("DestinationX: " + point.getX() + "DestinationY: " + truckPosition.getY());
					path_str += input + "";
					
					try {
						world.getTrucks().get(key).makeMove(input);
						//map.update();
					} catch (EndOfMapException e) {
						System.out.println(e.getMessage());
						
					}
					truckPosition = point;
					System.out.println(world.getMap().print());
			   }
			   
			   System.out.println("Truck Final Position: " + world.getTrucks().get(key).getCurrentPosition());
			   System.out.println("Parcel Final Position: " + pickupPoint);
			   if(world.getTrucks().get(key).getCurrentPosition().equals(pickupPoint)){
					System.out.println("Parcel Picked up with sucess at: " + pickupPoint);
			   }
			   
			   //Make the Trucks Move from one position to a destination
			   for (Point point : path2.getPath()) {
				    String input = AutoPilot.getDirection(truckPosition, point);
				    System.out.println("Direction: " + input);
				    System.out.println("TruckX: " + truckPosition.getX() + "TruckY: " + truckPosition.getY());
				    System.out.println("DestinationX: " + point.getX() + "DestinationY: " + truckPosition.getY());
					path_str += input + "";
					
					try {
						world.getTrucks().get(key).makeMove(input);
						//map.update();
					} catch (EndOfMapException e) {
						System.out.println(e.getMessage());
						
					}
					truckPosition = point;
					System.out.println(world.getMap().print());
			   }
			   
			   System.out.println("Truck Final Position: " + world.getTrucks().get(key).getCurrentPosition());
			   System.out.println("Parcel Final Position: " + deliveryPoint);
			   if(world.getTrucks().get(key).getCurrentPosition().equals(deliveryPoint)){
					//if(world.getTrucks().get(key).removeParcel(parc))
						System.out.println("Parcel Delivered with sucess at: " + deliveryPoint);
			   }
			   
			   //world.getTrucks().get(key).printAllInfo();
			   
		   }
		   //LinkedList<Point> destinations = world.getTrucks().get(key).getDestinations();
		   //Path path = autoPilot.getPath(truckPosition, destinations);
		   System.out.println("[/TruckInfo]");
		   
		   
		}
		
		
		// End of Testing
		
		/*
		try {
			map = new Roads(INPUT_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		play_ia();
		System.out.println(map.print());
		*/
	}
	
	

}
