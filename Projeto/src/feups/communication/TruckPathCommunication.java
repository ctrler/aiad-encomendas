package feups.communication;

import java.awt.Point;
import java.util.Set;

import feups.map.Path;
import feups.parcel.Parcel;

public class TruckPathCommunication implements java.io.Serializable {
	
	Path currentRoute;
	Parcel cargo;
	
	public TruckPathCommunication(Path currentRoute,Parcel cargo){
		this.currentRoute = currentRoute;
		this.cargo = cargo;
	}
	
	public String toString() {
		return ("CurrentRoute: "+this.getCurrentRoute() + " Current Parcels" + this.cargo.toString());
	}

	public Path getCurrentRoute() {
		return this.currentRoute;
	}
	
	public Parcel getParcel(){
		return this.cargo;
	}
	
}
