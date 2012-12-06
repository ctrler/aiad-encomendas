/**
 * 
 */
package feups.communication;

import java.awt.Point;

/**
 * @author Joca
 *
 */
public class TruckWorldCommunication implements java.io.Serializable {
	
	Point currentPosition;

	/**
	 * @param args
	 */
	
	public TruckWorldCommunication (Point currentPosition){
		this.currentPosition = currentPosition;
	}
	
	
	public String print() {
		return ("CurrentPosition: "+this.getCurrentPosition());
	}
	

	public Point getCurrentPosition(){
		return this.currentPosition;
	}

}