package feups.parcel;

import feups.city.City;
import feups.map.Position;

/**
 * A parcel to be delivered.
 * 
 * @author Ricardo Teixeira
 *
 */
public class Parcel {
	String name;
	Position position;
	City destination;
	
	
	public Parcel(String name, Position position, City destination) {
		this.name = name;
		this.position = position;
		this.destination = destination;
	}
	
	

}
