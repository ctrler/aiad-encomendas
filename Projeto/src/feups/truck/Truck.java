package feups.truck;

import java.awt.Point;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import feups.map.EndOfMapException;
import feups.parcel.Parcel;

/**
 * A truck. Big or small, doesn't matter.
 * It has an id so we can reference it by its name.
 * It takes parcels as its cargo.
 * Has a position so we can now where it is.
 * 
 * @author Ricardo Teixeira & João Carvalho
 *
 */
public class Truck {
	
	Set<Parcel> parcels;
	Point currentPosition;
	
	/** Constructor that takes the position of the truck;
	 * 
	 */
	public Truck(Point pos){
		this.currentPosition = pos;
		parcels = new HashSet<Parcel>();
	}
	
	public boolean addParcel(Parcel p){
		return parcels.add(p);
	}
	
	public boolean removeParcel(Parcel p){
		return parcels.remove(p);
	}
	
	/**
	 * Transfer a certain Parcel from a truck to another
	 * @param truckDestination
	 * @param p
	 */
	public void transferParcelToTruck(Truck truckDestination, Parcel p){
		this.removeParcel(p);			//Removes Parcel from actual Truck
		truckDestination.addParcel(p);	//Adds Parcel to the Destination Truck
	}

	public Set<Parcel> getParcels() {
		return parcels;
	}
	
	/**
	 * Returns a Parcel from a given Parcel name
	 * @param name
	 * @return
	 */
	public Parcel getParcelByName(String name){
		Iterator<Parcel> iter = this.getParcels().iterator();
	    while (iter.hasNext()) {
	    	Parcel parc = iter.next();
	    	if(parc.getNome() == name)
	    		return parc;
	    }
	    return null;
	}

	public void setParcels(Set<Parcel> parcels) {
		this.parcels = parcels;
	}

	public Point getCurrentPosition() {
		return currentPosition;
	}

	/**
	 * Sets the current position (x, y) for the Truck
	 * @param destination
	 */
	public void setCurrentPosition(Point currentPosition) {
		this.currentPosition = currentPosition;
	}

	/**
	 * Returns every parcels in the Truck in a LinkedList<Point>
	 * Used in AutoPilot
	 * @return
	 */
	public LinkedList<Point> getDestinations() {
		LinkedList<Point> listOfDestinations = new LinkedList<Point>();
		Iterator<Parcel> iter = this.getParcels().iterator();
	    while (iter.hasNext()) {
	    	Parcel iterTemp = iter.next();
	    	/*if(this.currentPosition!=iterTemp.getPosition()){ //If different, adds to destination list
	    		listOfDestinations.add(iterTemp.getPosition());
	    	}*/
	    	listOfDestinations.add(iterTemp.getPosition());
	    }
		return listOfDestinations;
	}
	
	/**
	 * Moves the Truck to one Specific direction: Left, Right, Up or Down
	 * @param direction
	 * @return
	 * @throws EndOfMapException
	 */
	public boolean makeMove(String direction) throws EndOfMapException{
		
		Point truckPosition = this.getCurrentPosition();
		Point destination = null;
		
		switch (direction.toLowerCase()){
			case "u":
				destination =  new Point(truckPosition.x, truckPosition.y+1);
				break;
			case "l":
				destination =  new Point(truckPosition.x-1, truckPosition.y);
				break;
			case "d":
				destination =  new Point(truckPosition.x, truckPosition.y-1);
				break;
			case "r":
				destination =  new Point(truckPosition.x+1, truckPosition.y);
				break;
			case "w":
				return true;
			case "a":
				//throw new EndOfMapException("You aborted the city-finding activity. Score: " + truck.getFinalScore());
			default:
				return false;
		}
		
		this.setCurrentPosition(destination);
		
		//Moves every parcel inside the truck with it
		this.makeParcelsInsideMove(destination);
		
		return true;
	}

	/**
	 * Moves the Parcels inside the truck with it
	 * @param destination
	 */
	private void makeParcelsInsideMove(Point destination) {
		Iterator<Parcel> iter = this.getParcels().iterator();
	    while (iter.hasNext()) {
		    Parcel parc = iter.next();
		    parc.setCurrentPosition(destination);
	    }
	}
	
	/**
	 * Prints in screen a representation of the Truck and its Parcels
	 */
	public void printAllInfo(){
		System.out.println("------------------------------------------------");
	    System.out.println("[TruckInfo]");
	    //System.out.println("TruckName: " + this.);
	    System.out.println("Position: " + this.getCurrentPosition().toString());
	    
	    //Print parcels information
	    Iterator<Parcel> iter = this.getParcels().iterator();
	    while (iter.hasNext()) {
	    	Parcel parc = iter.next();
	    	System.out.println("\n\t[ParcelInfo]");
	    	System.out.println("\tName: " + parc.getNome());
	    	System.out.println("\tOrigin: " + parc.getPosition().toString());
	    	System.out.println("\tDestination: " + parc.getDestination().getPosition().toString());
	    }	
	}

	
}
