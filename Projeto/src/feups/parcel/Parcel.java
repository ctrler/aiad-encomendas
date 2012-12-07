package feups.parcel;

import java.awt.Point;

import feups.city.City;

/**
 * A parcel to be delivered.
 * 
 * @author Ricardo Teixeira
 *
 */
public class Parcel implements  java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6564708950059323455L;
	String name;
	Point position;
	City destination;
	boolean assigned;
	
	
	public Parcel(String name, Point p, City destination) {
		this.name = name;
		this.position = p;
		this.destination = destination;
		this.assigned = false;
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
	 * @param pos
	 */
	public void setCurrentPosition(Point pos) {
		this.position = pos;
	}

	@Override
	public String toString() {
		return "Parcel [" + (name != null ? "name=" + name + ", " : "")
				+ (position != null ? "position=" + position + ", " : "")
				+ (destination != null ? "destination=" + destination : "")
				+ "]";
	}

	public boolean isAssigned() {
		return assigned;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}
	
	
	

}
