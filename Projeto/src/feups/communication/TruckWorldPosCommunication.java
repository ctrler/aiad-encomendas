/**
 * 
 */
package feups.communication;

import java.awt.Point;

/**
 * @author Joca
 *
 */
public class TruckWorldPosCommunication implements java.io.Serializable {
	
	Point currentPosition;

	/**
	 * @param args
	 */
	
	public TruckWorldPosCommunication (Point currentPosition){
		this.currentPosition = currentPosition;
	}
	
	
	public String toString() {
		return ("CurrentPosition: "+this.getCurrentPosition());
	}
	

	public Point getCurrentPosition(){
		return this.currentPosition;
	}

}