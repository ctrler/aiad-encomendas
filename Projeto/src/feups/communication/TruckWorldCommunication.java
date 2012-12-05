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
	/** Distancia percorrida */
	double km;

	/**
	 * @param args
	 */
	
	public TruckWorldCommunication (Point currentPosition, double km){
		this.currentPosition = currentPosition;
		this.km = km;
	}
	
	
	public String print() {
		return ("CurrentPosition: "+this.getCurrentPosition()+" - km: "+this.getKM());
	}
	
	private double getKM() {
		return this.km;
	}


	public Point getCurrentPosition(){
		return this.currentPosition;
	}

}