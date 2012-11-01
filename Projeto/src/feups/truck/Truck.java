package feups.truck;

import java.util.LinkedList;
import java.util.Set;

import feups.map.Parcel;
import feups.map.Position;

/**
 * A truck. Big or small, doesn't matter.
 * It has an id so we can reference it by its name.
 * It takes parcels as its cargo.
 * Has a position so we can now where it is.
 * 
 * @author Ricardo Teixeira
 *
 */
public class Truck {
	
	Set<Parcel> parcels;
	Position currentPosition;
	
	/** Constructor that takes the position of the truck;
	 * 
	 */
	public Truck(Position pos){
		this.currentPosition = pos;
	}
	
	public boolean addParcel(Parcel p){
		
		return parcels.add(p);
		
	}

}
