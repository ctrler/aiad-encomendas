package feups.communication;

import java.awt.Point;
import java.io.Serializable;

import feups.map.Path;
import feups.parcel.Parcel;

public class TruckPathAnswer implements Serializable{
	
	private static final long serialVersionUID = 6081223276040673178L;

	Path pathMine;
	Path pathTheirs;
	Point meeting;
	
	/** custo do total das paths, para se poder ordenar
	 */
	double cost;
	
	Parcel parcelTheirs;
	
	/** Modo de funcionamento no ponto de encontro
	 * 1 troca a parcel de mine para theirs 
	 * 		(ou seja o agente que recebe fica com as duas parcels)
	 * 2 troca theirs para mine
	 * 		(ou seja o agente que recebe fica sem parcel)
	 */
	int mode;
	
	

	public TruckPathAnswer(Path pathMine, Path pathTheirs, Point meeting,
			double cost, int mode) {
		super();
		this.pathMine = pathMine;
		this.pathTheirs = pathTheirs;
		this.meeting = meeting;
		this.cost = cost;
		this.mode = mode;
	}

	public Path getPathMine() {
		return pathMine;
	}

	public void setPathMine(Path pathMine) {
		this.pathMine = pathMine;
	}

	public Path getPathTheirs() {
		return pathTheirs;
	}

	public void setPathTheirs(Path pathTheirs) {
		this.pathTheirs = pathTheirs;
	}

	public Point getMeeting() {
		return meeting;
	}

	public void setMeeting(Point meeting) {
		this.meeting = meeting;
	}

	public double getCost() {
		return cost;
	}

	public void setCost(double cost) {
		this.cost = cost;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public Parcel getParcelTheirs() {
		return parcelTheirs;
	}

	public void setParcelTheirs(Parcel parcelTheirs) {
		this.parcelTheirs = parcelTheirs;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	} 
	
	

}
