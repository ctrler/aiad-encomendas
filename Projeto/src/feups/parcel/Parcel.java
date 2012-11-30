package feups.parcel;

import java.awt.Point;

import feups.city.City;

/**
 * A parcel to be delivered.
 * 
 * @author Ricardo Teixeira
 *
 */
public class Parcel {
	String name;
	Point position;
	City destination;
	
	
	public Parcel(String name, Point p, City destination) {
		this.name = name;
		this.position = p;
		this.destination = destination;
	}
	
	public String getNome(){
		return this.name;
	}
	
	public Point getPosition(){
		return this.position;
	}
	
	public City getDestination(){
		return destination;
	}

	/**
	 * Sets the current position (x, y) for the Parcel
	 * @param destination
	 */
	public void setCurrentPosition(Point destination) {
		this.position = destination;
	}
	
	

}
